package trotech.dao.database.metadata

import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import trotech.dao.database.LayerTileMatrixSetLinks
import trotech.dao.database.Layers
import trotech.dao.database.TileMatrices
import trotech.dao.database.TileMatrixSets
import trotech.dto.Layer
import trotech.dto.TileMatrix
import trotech.dto.TileMatrixSet

fun loadAllLayers(): List<Layer> {
    return transaction {
        val layersList = Layers.selectAll().map { row ->
            val id = row[Layers.id]
            val title = row[Layers.title]
            val format = row[Layers.format]
            val style = row[Layers.style]

            val tileMatrixSetLinks = LayerTileMatrixSetLinks
                .select { LayerTileMatrixSetLinks.layerId eq id }
                .map { it[LayerTileMatrixSetLinks.tileMatrixSetId] }

            Layer(
                id = id,
                title = title,
                tileMatrixSetLinks = tileMatrixSetLinks,
                format = format,
                style = style
            )
        }
        layersList
    }
}

fun loadAllTileMatrixSets(): List<TileMatrixSet> {
    return transaction {
        val tileMatrixSetList = TileMatrixSets.selectAll().map { row ->
            val id = row[TileMatrixSets.id]
            val supportedCRS = row[TileMatrixSets.supportedCRS]

            val tileMatrices = TileMatrices
                .select { TileMatrices.tileMatrixSetId eq id }
                .map { matrixRow ->
                    TileMatrix(
                        id = matrixRow[TileMatrices.id],
                        scaleDenominator = matrixRow[TileMatrices.scaleDenominator],
                        topLeftCornerX = matrixRow[TileMatrices.topLeftCornerX],
                        topLeftCornerY = matrixRow[TileMatrices.topLeftCornerY],
                        tileWidth = matrixRow[TileMatrices.tileWidth],
                        tileHeight = matrixRow[TileMatrices.tileHeight],
                        matrixWidth = matrixRow[TileMatrices.matrixWidth],
                        matrixHeight = matrixRow[TileMatrices.matrixHeight]
                    )
                }

            TileMatrixSet(
                id = id,
                supportedCRS = supportedCRS,
                tileMatrices = tileMatrices
            )
        }
        tileMatrixSetList
    }
}