package fr.outadoc.eidas.lds

import fr.outadoc.eidas.lds.model.ComData
import fr.outadoc.eidas.nfc.Icao9303
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.tlv.firstWithTag
import fr.outadoc.eidas.tlv.parseTlv
import fr.outadoc.eidas.utils.flatMap

@OptIn(ExperimentalUnsignedTypes::class)
class ReadComFileUseCase(
    private val readWholeFile: ReadWholeFileUseCase,
) {
    suspend operator fun invoke(nfcSession: NfcSession): Result<ComData> =
        readWholeFile(
            nfcSession = nfcSession,
            fileId = Icao9303.File.COM.fileId,
            fileLabel = "EF.COM",
        ).flatMap { comBytes -> comBytes.parseTlv() }
            .flatMap { rootNode -> rootNode.first().children() }
            .flatMap { children -> children.firstWithTag(Icao9303.Tags.TagList) }
            .map { dgListNode ->
                ComData(
                    dataGroups =
                        dgListNode.value.mapNotNull { tag ->
                            Icao9303.DataGroup.fromTag(tag.toUInt())
                        },
                )
            }
}
