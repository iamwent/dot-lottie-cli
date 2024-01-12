import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.core.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.buffer

class KottieConverter(
    private val fileSystem: FileSystem,
    private val client: HttpClient,
    private val cookie: String,
) {
    suspend fun convert(source: Path, dest: Path): Result<Optimize> {
        val optimizeId = optimize(source).getOrNull()
            ?: return Result.failure(IllegalArgumentException("upload file failed"))
        val optimizeStatus = queryStatus(optimizeId).getOrNull()
        val downloadUrl = optimizeStatus?.downloadUrl
            ?: return Result.failure(IllegalArgumentException("convert failed"))
        download(downloadUrl, dest)

        val sourceSize = optimizeStatus.input?.contentLength ?: 0L
        val destSize = optimizeStatus.optimized?.contentLength ?: 0L
        return Result.success(Optimize(source.name, dest.name, sourceSize, destSize))
    }

    private suspend fun optimize(path: Path): Result<String?> {
        val file = fileSystem.source(path).buffer().readByteArray()
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = URL_OPTIMIZE,
            formData = formData {
                append("format", "dot-lottie")
                append("file", file, Headers.build {
                    append(HttpHeaders.ContentType, "application/json")
                    append(HttpHeaders.ContentDisposition, "filename=\"${path.name}\"")
                })
            },
        ) {
            header("Cookie", cookie)
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalArgumentException(response.status.description))
        }

        val result = Json.decodeFromString<OptimizeResponse?>(response.bodyAsText())
        return Result.success(result?.optimizeId)
    }

    private suspend fun queryStatus(id: String): Result<StatusResponse?> {
        var retry = 0
        while (retry < MAX_RETRY) {
            delay((retry + 1) * 1000L)
            val result = query(id).getOrNull()
            if (result?.downloadUrl.isNullOrEmpty()) {
                retry++
                continue
            }
            return Result.success(result)
        }
        return Result.failure(IllegalArgumentException("convert failed"))
    }

    private suspend fun query(id: String): Result<StatusResponse?> {
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = URL_STATUS,
            formData = formData {
                append("format", "dot-lottie")
                append("id", id)
            },
        ) {
            header("Cookie", cookie)
        }

        if (!response.status.isSuccess()) {
            return Result.failure(IllegalArgumentException(response.status.description))
        }

        val result = Json.decodeFromString<StatusResponse?>(response.bodyAsText())
        return Result.success(result)
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
        private const val MAX_RETRY = 3
        private const val URL_OPTIMIZE = "https://lottiefiles.com/api/animations/optimize"
        private const val URL_STATUS = "https://lottiefiles.com/api/animations/optimize/status"
        private const val DEFAULT_BUFFER_SIZE = 1024L
    }
}
