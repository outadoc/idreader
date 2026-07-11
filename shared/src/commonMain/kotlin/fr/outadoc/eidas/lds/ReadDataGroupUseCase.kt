package fr.outadoc.eidas.lds

import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.utils.toByteArrayBe

@OptIn(ExperimentalUnsignedTypes::class)
class ReadDataGroupUseCase(
    private val readWholeFile: ReadWholeFileUseCase,
) {
    suspend operator fun invoke(
        nfcSession: NfcSession,
        dataGroup: Icao9303.DataGroup,
    ): Result<UByteArray> =
        readWholeFile(
            nfcSession = nfcSession,
            fileId = dataGroup.fid.toByteArrayBe(byteCount = 2),
            fileLabel = dataGroup.toString(),
        )
}
