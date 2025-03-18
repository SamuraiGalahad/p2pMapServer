package dto

import kotlinx.serialization.Serializable

@Serializable
data class PeerInfo(
    val connectionCodes: List<String>,
    val udpPort: Int = 5000
)
