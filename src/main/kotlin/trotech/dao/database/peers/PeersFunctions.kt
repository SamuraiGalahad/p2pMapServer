package trotech.dao.database.peers

import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import trotech.dao.database.*
import trotech.dto.Layer
import trotech.dto.TileMatrixSet

fun saveUserIfNotExists(userId: String) {
    transaction {
        Users.insertIgnore {
            it[id] = userId
        }
    }
}

fun saveLayerForUser(userId: String, layer: Layer) {
    transaction {
        Layers.insertIgnore { table ->
            table[id] = layer.id
            table[title] = layer.title
            table[format] = layer.format
            table[style] = layer.style
        }

        UserLayers.insertIgnore { table ->
            table[this.userId] = userId
            table[layerId] = layer.id
        }

        layer.tileMatrixSetLinks.forEach { link ->
            UserTileMatrixSets.insertIgnore { table ->
                table[this.userId] = userId
                table[tileMatrixSetId] = link
            }
        }

        layer.tileMatrixSetLinks.forEach { link ->
            LayerTileMatrixSetLinks.insertIgnore {
                it[layerId] = layer.id
                it[tileMatrixSetId] = link
            }
        }
    }
}

fun saveTileMatrixSetForUser(userId: String, tileMatrixSet: TileMatrixSet) {
    transaction {
        TileMatrixSets.insertIgnore { table ->
            table[id] = tileMatrixSet.id
            table[supportedCRS] = tileMatrixSet.supportedCRS
        }

        tileMatrixSet.tileMatrices.forEach { matrix ->
            TileMatrices.insertIgnore { table ->
                table[id] = matrix.id
                table[tileMatrixSetId] = tileMatrixSet.id
                table[scaleDenominator] = matrix.scaleDenominator
                table[topLeftCornerX] = matrix.topLeftCornerX
                table[topLeftCornerY] = matrix.topLeftCornerY
                table[tileWidth] = matrix.tileWidth
                table[tileHeight] = matrix.tileHeight
                table[matrixWidth] = matrix.matrixWidth
                table[matrixHeight] = matrix.matrixHeight
            }
        }

        UserTileMatrixSets.insertIgnore { table ->
            table[this.userId] = userId
            table[tileMatrixSetId] = tileMatrixSet.id
        }
    }
}

// Проверка, есть ли у пира указанный слой
fun checkPeerLayer(peerid: String, layerId: String): Boolean {
    return transaction {
        val userLayers = UserLayers
            .select { UserLayers.userId eq peerid }
            .map { it[UserLayers.layerId] }
        userLayers.contains(layerId)
    }
}

// Проверка, есть ли у пира указанный tile matrix set для слоя
fun checkPeerTileMatrixSetForLayer(peerid: String, layerId: String, tileMatrixSetId: String?): Boolean {
    return transaction {
        val validTileMatrixSets = LayerTileMatrixSetLinks
            .select { LayerTileMatrixSetLinks.layerId eq layerId }
            .map { it[LayerTileMatrixSetLinks.tileMatrixSetId] }

        val userTileMatrixSets = UserTileMatrixSets
            .select { UserTileMatrixSets.userId eq peerid }
            .map { it[UserTileMatrixSets.tileMatrixSetId] }

        validTileMatrixSets.contains(tileMatrixSetId) && userTileMatrixSets.contains(tileMatrixSetId)
    }
}

fun findMatchingPeer(layerId: String, tileMatrixSetId: String, requestingUserId: String): String? {
    return transaction {
        val usersWithTileMatrixSet = UserTileMatrixSets
            .select { UserTileMatrixSets.tileMatrixSetId eq tileMatrixSetId }
            .map { it[UserTileMatrixSets.userId] }
            .toSet()

        if (usersWithTileMatrixSet.isEmpty()) {
            return@transaction null
        }

        val usersWithLayer = UserLayers
            .select { UserLayers.layerId eq layerId }
            .map { it[UserLayers.userId] }
            .toSet()

        val matchingUsers = usersWithTileMatrixSet.intersect(usersWithLayer)

        // Убираем requestingUserId из множества
        val filteredMatchingUsers = matchingUsers.filterNot { it == requestingUserId }

        // Возвращаем первого подходящего пользователя, если есть
        filteredMatchingUsers.firstOrNull()
    }
}