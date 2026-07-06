package fr.outadoc.eidas.securemessaging

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSessionManager
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.RApdu
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.pace.PaceSession
import io.github.rafaelrabeloit.bertlv.TLVList

@OptIn(ExperimentalUnsignedTypes::class)
class SecureSessionManager(
    private val paceSession: PaceSession,
    private val nfcSessionManager: NfcSessionManager,
    private val cryptoEngine: CryptoEngine,
) : NfcSessionManager {
    private val algorithm: Algorithm
        get() = paceSession.algorithm

    // 128-bit Send Sequence Counter (big-endian), starts at 0 after PACE
    private var ssc = UByteArray(16)

    override suspend fun transceive(
        tag: NfcTag,
        command: CApdu,
    ): Result<RApdu> =
        nfcSessionManager
            .transceive(
                tag = tag,
                command = secureCApdu(command),
            ).map { response ->
                decryptRApdu(response)
            }

    private fun secureCApdu(command: CApdu): CApdu {
        incrementSsc()

        // CLA with bit-5 set for SM, interindustry class (bit-8 = 0)
        val secureCla = ((command.cla.toInt() and 0x0F) or 0x0C).toUByte()

        // DO'87': encrypt data if present
        val do87Bytes: UByteArray =
            if (command.data != null) {
                val padded: UByteArray = isoPad(command.data)

                // IV = E(K_enc, SSC): single-block AES-CBC with zero IV equals AES-ECB of the block
                val iv: UByteArray =
                    cryptoEngine.encryptSymmetric(
                        algorithm = algorithm,
                        key = paceSession.kEnc,
                        iv = UByteArray(16),
                        data = ssc,
                    )

                val ciphertext: UByteArray =
                    cryptoEngine
                        .encryptSymmetric(
                            algorithm = algorithm,
                            key = paceSession.kEnc,
                            iv = iv,
                            data = padded,
                        )

                tlvList {
                    tlv(
                        Iso7816.Tags.PaddingContentIndicatorFollowedByCryptogram,
                        ubyteArrayOf(0x01u) + ciphertext,
                    )
                }
            } else {
                ubyteArrayOf()
            }

        // DO'97': protected Le if present
        val do97Bytes: UByteArray =
            if (command.le != null) {
                tlvList {
                    tlv(Iso7816.Tags.ProtectedLe, command.le)
                }
            } else {
                ubyteArrayOf()
            }

        // MAC input: SSC || ISO-padded header || DO'87' || DO'97'
        val header: UByteArray =
            ubyteArrayOf(
                secureCla,
                command.ins,
                command.p1,
                command.p2,
            )

        val paddedHeader: UByteArray = isoPad(header)
        val macInput: UByteArray = isoPad(ssc + paddedHeader + do87Bytes + do97Bytes)

        val mac: UByteArray =
            cryptoEngine
                .computeCmac(
                    algorithm = algorithm,
                    key = paceSession.kMac,
                    data = macInput,
                ).copyOfRange(0, 8)

        val do8eBytes =
            tlvList {
                tlv(Iso7816.Tags.CryptographicChecksum, mac)
            }

        return CApdu(
            cla = secureCla,
            ins = command.ins,
            p1 = command.p1,
            p2 = command.p2,
            data = do87Bytes + do97Bytes + do8eBytes,
            le = 0x00u,
        )
    }

    private fun decryptRApdu(response: RApdu): RApdu {
        incrementSsc()

        val body: UByteArray =
            response
                .getData()
                .getOrElse {
                    throw IllegalStateException("SM response had non-success status")
                }

        val tlvs = TLVList.fromTlvListBuffer(body.toByteArray())

        val do87Value: ByteArray? =
            tlvs
                .find(Iso7816.Tags.PaddingContentIndicatorFollowedByCryptogram.toInt())
                ?.value as? ByteArray

        val do99Value: ByteArray =
            (tlvs.find(Iso7816.Tags.ProcessingStatus.toInt())?.value as? ByteArray)
                ?: throw IllegalStateException("SM response missing DO'99' processing status")

        val do8eValue: ByteArray =
            (tlvs.find(Iso7816.Tags.CryptographicChecksum.toInt())?.value as? ByteArray)
                ?: throw IllegalStateException("SM response missing DO'8E' checksum")

        // Reconstruct TLV wire bytes for MAC verification
        val do87TlvBytes: UByteArray =
            if (do87Value != null) {
                tlvList {
                    tlv(
                        Iso7816.Tags.PaddingContentIndicatorFollowedByCryptogram,
                        do87Value,
                    )
                }
            } else {
                ubyteArrayOf()
            }

        val do99TlvBytes: UByteArray =
            tlvList {
                tlv(Iso7816.Tags.ProcessingStatus, do99Value)
            }

        // Verify MAC: CMAC(K_mac, iso_pad(SSC || [DO'87' TLV] || DO'99' TLV))[0..7]
        val macInput: UByteArray = isoPad(ssc + do87TlvBytes + do99TlvBytes)

        val expectedMac: UByteArray =
            cryptoEngine
                .computeCmac(
                    algorithm = algorithm,
                    key = paceSession.kMac,
                    data = macInput,
                ).copyOfRange(0, 8)

        check(expectedMac.toByteArray().contentEquals(do8eValue)) {
            "SM response MAC verification failed"
        }

        // Decrypt data if present
        val decryptedData: UByteArray =
            if (do87Value != null) {
                check(do87Value[0] == 0x01.toByte()) {
                    "Expected padding-content indicator 0x01"
                }

                val encrypted =
                    do87Value
                        .drop(1)
                        .toByteArray()
                        .toUByteArray()

                val iv: UByteArray =
                    cryptoEngine.encryptSymmetric(
                        algorithm,
                        paceSession.kEnc,
                        UByteArray(16),
                        ssc,
                    )

                val padded: UByteArray =
                    cryptoEngine.decryptSymmetricWithIv(
                        algorithm = algorithm,
                        key = paceSession.kEnc,
                        iv = iv,
                        data = encrypted,
                    )
                removeIsoPad(padded)
            } else {
                ubyteArrayOf()
            }

        val sw1 = do99Value[0].toUByte()
        val sw2 = do99Value[1].toUByte()

        return RApdu.parse(decryptedData + ubyteArrayOf(sw1, sw2))
    }

    private fun incrementSsc() {
        var carry = 1
        for (i in ssc.indices.reversed()) {
            val sum = ssc[i].toInt() + carry
            ssc[i] = (sum and 0xFF).toUByte()
            carry = sum ushr 8
            if (carry == 0) break
        }
    }

    private fun isoPad(
        data: UByteArray,
        blockSize: Int = 16,
    ): UByteArray {
        val padded = data + ubyteArrayOf(0x80u)
        val remaining = blockSize - (padded.size % blockSize)
        return if (remaining == blockSize) {
            padded
        } else {
            padded + UByteArray(remaining)
        }
    }

    private fun removeIsoPad(data: UByteArray): UByteArray {
        for (i in data.indices.reversed()) {
            if (data[i] == 0x80u.toUByte()) {
                return data.copyOfRange(0, i)
            }
        }

        throw IllegalStateException("ISO 7816-4 padding marker 0x80 not found")
    }
}
