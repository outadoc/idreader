package fr.outadoc.eidas.securemessaging

import fr.outadoc.eidas.crypto.CryptoEngine
import fr.outadoc.eidas.nfc.CApdu
import fr.outadoc.eidas.nfc.Iso7816
import fr.outadoc.eidas.nfc.NfcSessionManager
import fr.outadoc.eidas.nfc.NfcTag
import fr.outadoc.eidas.nfc.RApdu
import fr.outadoc.eidas.nfc.tlvList
import fr.outadoc.eidas.pace.PaceSessionKeys

@OptIn(ExperimentalUnsignedTypes::class)
class SecureSessionManager(
    private val nfcSessionManager: NfcSessionManager,
    private val paceSessionKeys: PaceSessionKeys,
    private val cryptoEngine: CryptoEngine,
) : NfcSessionManager {
    var sendSequenceCounter: UInt = 0u

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
        val protectedLe: UByte? = command.le

        // TODO pad + encrypt data
        val protectedData: UByteArray? = null

        // TODO MAC of APDU
        val checksum: UByteArray = ubyteArrayOf()

        return CApdu(
            cla = command.cla,
            ins = command.ins,
            p1 = command.p1,
            p2 = command.p2,
            data =
                tlvList {
                    if (protectedData != null) {
                        tlv(
                            Iso7816.Tags.PaddingContentIndicatorFollowedByCryptogram,
                            protectedData,
                        )
                    }

                    if (protectedLe != null) {
                        tlv(
                            Iso7816.Tags.ProtectedLe,
                            protectedLe,
                        )
                    }

                    tlv(
                        Iso7816.Tags.CryptographicChecksum,
                        checksum,
                    )
                },
            le = 0x00u,
        )
    }

    private fun decryptRApdu(response: RApdu): RApdu {
        TODO()
    }
}
