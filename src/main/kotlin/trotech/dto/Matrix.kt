package trotech.dto

import kotlinx.serialization.Serializable

@Serializable
data class Matrix(
    val level: Int,
    val images: List<Image>
)
