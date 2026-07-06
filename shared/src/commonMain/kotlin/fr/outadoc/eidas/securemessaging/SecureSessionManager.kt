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
    ): Result<RApdu> {
        TODO("Not yet implemented")
    }

    private fun secureCApdu(apdu: CApdu): CApdu {
        val protectedLe: UByte? = apdu.le

        // TODO pad + encrypt data
        val protectedData: UByteArray? = null

        // TODO MAC of APDU
        val checksum: UByteArray = ubyteArrayOf()

        return CApdu(
            cla = apdu.cla,
            ins = apdu.ins,
            p1 = apdu.p1,
            p2 = apdu.p2,
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
}
