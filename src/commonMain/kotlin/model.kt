import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OptimizeResponse(
    @SerialName("status")
    val status: String?,
    @SerialName("id")
    val id: String?,
    @SerialName("url")
    val url: String?,
) {
    val success: Boolean
        get() = status == "success"

    val optimizeId: String?
        get() = id?.takeIf { success }
}

///////////////////////////////////////////////////////////

@Serializable
data class StatusResponse(
    @SerialName("status")
    val status: String?,
    @SerialName("input")
    val input: Content?,
    @SerialName("optimized")
    val optimized: Content?,
) {
    val success: Boolean
        get() = status == "ready"

    val downloadUrl: String?
        get() = optimized?.url?.takeIf { success }

    @Serializable
    data class Content(
        @SerialName("contentLength")
        val contentLength: Long,

        @SerialName("url")
        val url: String?,
    )
}

data class Optimize(
    val source: String,
    val dest: String,
    val sourceSize: Long,
    val destSize: Long,
) {
    val savedSize: Long
        get() = sourceSize - destSize
    val savedPercent: Int
        get() = savedSize.toDouble().div(sourceSize).times(100).toInt()

}
