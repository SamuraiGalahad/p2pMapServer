package trotech.dto

import kotlinx.serialization.Serializable

@Serializable
data class GraphResponse(
    val connectedComponentsParam : Int,
    val clusteringCoefficient : Double,
    val algebraicConnectivity : Double,
    val assortativityCoefficient : Double,
    val downloadSpeed: Long,
    val uploadSpeed: Long
)