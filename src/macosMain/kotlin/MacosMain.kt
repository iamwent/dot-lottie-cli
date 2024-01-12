import io.ktor.client.*
import io.ktor.client.engine.darwin.*

actual fun getHttpClient(): HttpClient {
    return HttpClient(Darwin)
}

actual fun getSystem(): System = MacosSystem()
