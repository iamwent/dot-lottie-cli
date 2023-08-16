import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UploadRequest(
    @SerialName("payload")
    val payload: String
)

@Serializable
data class UploadResponse(
    @SerialName("payload")
    val payload: Payload
) {
    @Serializable
    data class Payload(
        @SerialName("id")
        val id: Int,
        @SerialName("hash")
        val hash: String,
        @SerialName("filetype")
        val filetype: String,
        @SerialName("data_file")
        val dataFile: String,
    )
}

///////////////////////////////////////////////////////////

@Serializable
data class ConvertRequest(
    @SerialName("url")
    val url: String
)

@Serializable
data class ConvertResponse(
    @SerialName("converted_file_size")
    val convertedFileSize: String,
    @SerialName("file")
    val `file`: String,
    @SerialName("source_file_size")
    val sourceFileSize: String
)
