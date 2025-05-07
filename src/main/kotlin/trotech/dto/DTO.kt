package trotech.dto

data class Layer(
    val id: String,
    val title: String,
    val tileMatrixSetLinks: List<String>,
    val format: String = "image/png",
    val style: String = "default"
)

data class TileMatrixSet(
    val id: String,
    val supportedCRS: String,
    val tileMatrices: List<TileMatrix>
)

data class TileMatrix(
    val id: String,
    val scaleDenominator: Double,
    val topLeftCornerX: Double,
    val topLeftCornerY: Double,
    val tileWidth: Int,
    val tileHeight: Int,
    val matrixWidth: Int,
    val matrixHeight: Int
)