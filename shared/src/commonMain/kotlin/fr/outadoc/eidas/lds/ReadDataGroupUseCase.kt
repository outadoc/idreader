package fr.outadoc.eidas.lds

import fr.outadoc.eidas.logging.Logger
import fr.outadoc.eidas.logging.i
import fr.outadoc.eidas.nfc.NfcSession
import fr.outadoc.eidas.nfc.commands.CommandFactory
import fr.outadoc.eidas.utils.flatMap
import kotlinx.io.Buffer
import kotlinx.io.readByteArray

private val TAG = "ReadDataGroupUseCase"

@OptIn(ExperimentalUnsignedTypes::class)
class ReadDataGroupUseCase(
    private val logger: Logger,
    private val commandFactory: CommandFactory,
) {
    suspend operator fun invoke(
        nfcSession: NfcSession,
        dataGroupNumber: UByte,
    ): Result<UByteArray> {
        logger.i(TAG, "SELECT FILE DG$dataGroupNumber")

        nfcSession
            .transceive(
                commandFactory.selectFile(
                    ubyteArrayOf(FID_RANGE_START, dataGroupNumber),
                ),
            ).flatMap { it.getData() }
            .getOrElse { return Result.failure(it) }

        logger.i(TAG, "READ BINARY DG$dataGroupNumber offset=0")

        val firstChunk: UByteArray =
            nfcSession
                .transceive(commandFactory.readBinary(offset = 0))
                .flatMap { it.getData() }
                .getOrElse { return Result.failure(it) }

        val totalLength: Int =
            runCatching { parseTotalLength(firstChunk) }
                .getOrElse { return Result.failure(it) }

        val buffer = Buffer()
        buffer.write(firstChunk.toByteArray())

        while (buffer.size.toInt() < totalLength) {
            val offset = buffer.size.toInt()
            val remaining = totalLength - offset

            logger.i(TAG, "READ BINARY DG$dataGroupNumber offset=$offset remaining=$remaining")

            val chunk: UByteArray =
                nfcSession
                    .transceive(
                        commandFactory.readBinary(
                            offset = offset,
                            length = minOf(remaining, MAX_READ),
                        ),
                    ).flatMap { it.getData() }
                    .getOrElse { return Result.failure(it) }

            buffer.write(chunk.toByteArray())
        }

        return Result.success(
            buffer.readByteArray().toUByteArray(),
        )
    }

    // Parses the outer BER-TLV tag + length to return the total encoded byte count.
    private fun parseTotalLength(data: UByteArray): Int {
        var pos = 1 // skip single-byte outer tag
        val lengthByte = data[pos++].toInt() and 0xFF
        val contentLength =
            when {
                lengthByte <= 0x7F -> {
                    lengthByte
                }

                lengthByte == 0x81 -> {
                    data[pos++].toInt() and 0xFF
                }

                lengthByte == 0x82 -> {
                    ((data[pos++].toInt() and 0xFF) shl 8) or (data[pos++].toInt() and 0xFF)
                }

                else -> {
                    error("Unsupported BER-TLV length: 0x${lengthByte.toString(16)}")
                }
            }
        return pos + contentLength
    }

    private companion object {
        const val FID_RANGE_START: UByte = 0x01u
        const val MAX_READ = 256
    }
}
