import io.ktor.client.*

expect fun getHttpClient(): HttpClient

expect fun getSystem(): System

fun main(args: Array<String>) = KottieCommand().main(args)
