package dto

import kotlinx.serialization.Serializable

@Serializable
data class DownloadDistributionMap(
    val layerName: String,
    val loadMatrix: ArrayList<ResponceMatrix>
)