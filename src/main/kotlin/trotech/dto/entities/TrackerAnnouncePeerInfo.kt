package trotech.dto.entities

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
class TrackerAnnouncePeerInfo(
    @SerialName("peerid")
    val peerId: String,
    val connectedPeers: List<String>,
    val internetDownloadSpeed: Long,
    val internetUploadSpeed: Long,
) {
    override fun toString(): String {
        return "TrackerAnnouncePeerInfo(peerId='$peerId', connectedPeers=$connectedPeers, internetDownloadSpeed=$internetDownloadSpeed, internetUploadSpeed=$internetUploadSpeed)"
    }
}

fun parsePeerInfo(jsonString: String): TrackerAnnouncePeerInfo {
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromString(jsonString)
}
