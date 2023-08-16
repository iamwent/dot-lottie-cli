import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer

class KottieConverter(
    private val fileSystem: FileSystem,
    private val client: HttpClient
) {
    suspend fun convert(source: Path, dest: Path): Result<Path> {
        val uploadResult = upload(source)
        val dataFile = uploadResult.getOrNull()?.payload?.dataFile
        if (dataFile.isNullOrEmpty()) {
            return Result.failure(uploadResult.exceptionOrNull()!!)
        }

        val convertResult = convert(dataFile)
        val convertedFileUrl = convertResult.getOrNull()
        if (convertedFileUrl.isNullOrEmpty()) {
            return Result.failure(convertResult.exceptionOrNull()!!)
        }

        download(convertedFileUrl, dest)
        return Result.success(dest)
    }

    private suspend fun upload(path: Path): Result<UploadResponse> {
        val payload = fileSystem.source(path).buffer().readUtf8()
        val response = client.post(URL_UPLOAD) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(UploadRequest(payload)))
        }
        return if (response.status.isSuccess()) {
            Result.success(Json.decodeFromString<UploadResponse>(response.bodyAsText()))
        } else {
            Result.failure(IllegalArgumentException(response.status.description))
        }
    }

    private suspend fun convert(fileUrl: String): Result<String> {
        val response = client.post(URL_CONVERT) {
            contentType(ContentType.Application.Json)
            setBody(Json.encodeToString(ConvertRequest(url = fileUrl)))
        }
        return if (response.status.isSuccess()) {
            Result.success(Json.decodeFromString<ConvertResponse>(response.bodyAsText()))
        } else {
            Result.failure(IllegalArgumentException(response.status.description))
        }.map {
            "${URL_DOWNLOAD}/${it.file}"
        }
    }

    private suspend fun download(url: String, dest: Path) {
        val sink = fileSystem.sink(dest, false).buffer()

        client.prepareGet(url).execute { httpResponse ->
            val channel = httpResponse.bodyAsChannel()
            while (!channel.isClosedForRead) {
                val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE)
                while (!packet.isEmpty) {
                    val bytes = packet.readBytes()
                    sink.write(bytes)
                }
                sink.emit()
            }
        }
    }

    companion object {
        private const val URL_UPLOAD = "https://api.lottiefiles.com/v2/temp-file-upload"
        private const val URL_CONVERT = "https://api.dotlottie.io/todotlottie"
        private const val URL_DOWNLOAD = "https://lottie-editor-api-temp.s3.amazonaws.com"
        private const val DEFAULT_BUFFER_SIZE = 1024L
    }
}
