package dto

import kotlinx.serialization.Serializable

@Serializable
data class DataToLoadInfo(
    val connectionKey: String,
    val tileMatrix: Matrix
)
