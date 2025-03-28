package trotech.dao

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import trotech.dto.entities.LayerEntity
import trotech.service.usecase.xml.Layer

object LayerRepository {
    suspend fun insertLayer(layer: Layer) {
        newSuspendedTransaction {
            LayerEntity.new {
                title = layer.title
                identifier = layer.identifier
                lowerCorner = layer.boundingBox.lowerCorner
                upperCorner = layer.boundingBox.upperCorner
                formats = layer.formats.joinToString(",")
                tileMatrixSets = layer.tileMatrixSets.joinToString(",")
            }
        }
    }

    suspend fun getLayers(): List<Layer> = newSuspendedTransaction {
        LayerEntity.all().map { it.toLayer() }
    }
}