package trotech.dto

import trotech.dto.entities.Peer
import kotlinx.serialization.Serializable

@Serializable
data class Torrent(
    val name: String,
    val peers: MutableList<Peer> = mutableListOf()
)
