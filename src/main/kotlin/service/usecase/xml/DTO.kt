package trotech.service.usecase.xml

import kotlinx.serialization.Serializable

@Serializable
data class BoundingBox(
    val lowerCorner: String,
    val upperCorner: String
)

@Serializable
data class Layer(
    val title: String,
    val identifier: String,
    val boundingBox: BoundingBox,
    val formats: List<String>,
    val tileMatrixSets: List<String>
)