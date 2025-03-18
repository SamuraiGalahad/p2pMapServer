package dto

import kotlinx.serialization.Serializable

@Serializable
data class Image(
    val col: Int,
    val row: Int,
    val type: String
)
