package trotech.dto

import kotlinx.serialization.Serializable

@Serializable
data class Layer(
    val name: String,
    val type: String,
    val matrix: List<Matrix>
)