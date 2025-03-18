package dto

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class ResponceMatrix(
    val level: Int,
    val images: ArrayList<ResponceImage>,
)