package dto

import dto.entities.Peer
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ResponceImage(
    val col: Int,
    val row: Int,
    val connectionCode: String,
    val peer: Peer
)