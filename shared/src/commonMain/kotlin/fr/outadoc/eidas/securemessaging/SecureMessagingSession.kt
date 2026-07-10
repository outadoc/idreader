package fr.outadoc.eidas.securemessaging

import fr.outadoc.eidas.crypto.Algorithm
import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.d
import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.RApdu
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.pace.model.PaceCredentials
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap
import fr.outadoc.eidas.utils.toPrettyHex

private val TAG = "SecureSessionManager"

@OptIn(ExperimentalUnsignedTypes::class)
class SecureMessagingSession(
    private val paceCredentials: PaceCredentials,
    private val nfcSession: NfcSession,
    private val cryptoEngine: CryptoEngine,
    private val logger: Logger,
) : NfcSession {
    private val algorithm: Algorithm
        get() = paceCredentials.algorithm

    // 128-bit Send Sequence Counter (big-endian), starts at 0 after PACE
    private var ssc = UByteArray(16)

    override suspend fun transceive(command: CApdu): Result<RApdu> {
        logger.d(TAG, "SEND >> ${command.serialize().toPrettyHex()}")

        return nfcSession
            .transceive(
                command = secureCApdu(command),
            ).flatMap { response ->
                decryptRApdu(response)
            }.onSuccess { clearResponse ->
                logger.d(TAG, "RECV << ${clearResponse.raw.toPrettyHex()}")
            }
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
                        key = paceCredentials.kEnc,
                        iv = UByteArray(16),
                        data = ssc,
                    )

                val ciphertext: UByteArray =
                    cryptoEngine
                        .encryptSymmetric(
                            algorithm = algorithm,
                            key = paceCredentials.kEnc,
                            iv = iv,
                            data = padded,
                        )

                tlvList {
                    tlv(
                        Icao9303.Tags.PaddingContentIndicatorFollowedByCryptogram,
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
                    tlv(Icao9303.Tags.ProtectedLe, command.le)
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
                    key = paceCredentials.kMac,
                    data = macInput,
                ).copyOfRange(0, 8)

        val do8eBytes =
            tlvList {
                tlv(Icao9303.Tags.CryptographicChecksum, mac)
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

    private fun decryptRApdu(response: RApdu): Result<RApdu> {
        incrementSsc()

        val tlvs =
            response
                .getData()
                .flatMap { body -> body.parseTlv() }
                .getOrElse { return Result.failure(it) }

        val do87Value: UByteArray? =
            tlvs
                .firstWithTag(Icao9303.Tags.PaddingContentIndicatorFollowedByCryptogram)
                .getOrNull()
                ?.value

        val do99Value: UByteArray =
            tlvs
                .firstWithTag(Icao9303.Tags.ProcessingStatus)
                .getOrElse { return Result.failure(it) }
                .value

        val do8eValue: UByteArray =
            tlvs
                .firstWithTag(Icao9303.Tags.CryptographicChecksum)
                .getOrElse { return Result.failure(it) }
                .value

        // Reconstruct TLV wire bytes for MAC verification
        val do87TlvBytes: UByteArray =
            if (do87Value != null) {
                tlvList {
                    tlv(
                        Icao9303.Tags.PaddingContentIndicatorFollowedByCryptogram,
                        do87Value,
                    )
                }
            } else {
                ubyteArrayOf()
            }

        val do99TlvBytes: UByteArray =
            tlvList {
                tlv(Icao9303.Tags.ProcessingStatus, do99Value)
            }

        // Verify MAC: CMAC(K_mac, iso_pad(SSC || [DO'87' TLV] || DO'99' TLV))[0..7]
        val macInput: UByteArray = isoPad(ssc + do87TlvBytes + do99TlvBytes)

        val expectedMac: UByteArray =
            cryptoEngine
                .computeCmac(
                    algorithm = algorithm,
                    key = paceCredentials.kMac,
                    data = macInput,
                ).copyOfRange(0, 8)

        if (!expectedMac.contentEquals(do8eValue)) {
            return Result.failure(IllegalStateException("SM response MAC verification failed"))
        }

        // Decrypt data if present
        val decryptedData: UByteArray =
            if (do87Value != null) {
                if (do87Value[0] != 0x01u.toUByte()) {
                    return Result.failure(IllegalStateException("Expected padding-content indicator 0x01"))
                }

                val encrypted = do87Value.copyOfRange(1, do87Value.size)

                val iv: UByteArray =
                    cryptoEngine.encryptSymmetric(
                        algorithm,
                        paceCredentials.kEnc,
                        UByteArray(16),
                        ssc,
                    )

                val padded: UByteArray =
                    cryptoEngine.decryptSymmetricWithIv(
                        algorithm = algorithm,
                        key = paceCredentials.kEnc,
                        iv = iv,
                        data = encrypted,
                    )
                removeIsoPad(padded).getOrElse { return Result.failure(it) }
            } else {
                ubyteArrayOf()
            }

        val sw1 = do99Value[0]
        val sw2 = do99Value[1]

        return Result.success(RApdu.parse(decryptedData + ubyteArrayOf(sw1, sw2)))
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

    private fun removeIsoPad(data: UByteArray): Result<UByteArray> {
        for (i in data.indices.reversed()) {
            if (data[i] == 0x80u.toUByte()) {
                return Result.success(data.copyOfRange(0, i))
            }
        }

        return Result.failure(IllegalStateException("ISO 7816-4 padding marker 0x80 not found"))
    }
}
