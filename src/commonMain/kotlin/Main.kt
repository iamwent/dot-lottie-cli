import io.ktor.client.*

expect fun getHttpClient(): HttpClient

fun main(args: Array<String>) = KottieCommand().main(args)
