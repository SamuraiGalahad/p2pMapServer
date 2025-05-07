package trotech.dao.database

import org.jetbrains.exposed.sql.Table

val allTables = arrayOf(Users, Layers,
    TileMatrixSets, TileMatrices,
    UserLayers, UserTileMatrixSets,
    LayerTileMatrixSetLinks, AdminsTable)

object Users : Table() {
    val id = varchar("id", 255)

    override val primaryKey = PrimaryKey(id)
}

object Layers : Table() {
    val id = varchar("id", 255)
    val title = varchar("title", 255)
    val format = varchar("format", 100)
    val style = varchar("style", 100)

    override val primaryKey = PrimaryKey(id)
}

object TileMatrixSets : Table() {
    val id = varchar("id", 255)
    val supportedCRS = varchar("supported_crs", 255)

    override val primaryKey = PrimaryKey(id)
}

object TileMatrices : Table() {
    val id = varchar("id", 255)
    val tileMatrixSetId = varchar("tile_matrix_set_id", 255).references(TileMatrixSets.id)
    val scaleDenominator = double("scale_denominator")
    val topLeftCornerX = double("top_left_corner_x")
    val topLeftCornerY = double("top_left_corner_y")
    val tileWidth = integer("tile_width")
    val tileHeight = integer("tile_height")
    val matrixWidth = integer("matrix_width")
    val matrixHeight = integer("matrix_height")

    override val primaryKey = PrimaryKey(id, tileMatrixSetId)
}

object UserLayers : Table() {
    val userId = varchar("user_id", 255).references(Users.id)
    val layerId = varchar("layer_id", 255).references(Layers.id)
    override val primaryKey = PrimaryKey(userId, layerId)
}

object UserTileMatrixSets : Table() {
    val userId = varchar("user_id", 255).references(Users.id)
    val tileMatrixSetId = varchar("tile_matrix_set_id", 255).references(TileMatrixSets.id)
    override val primaryKey = PrimaryKey(userId, tileMatrixSetId)
}

object LayerTileMatrixSetLinks : Table() {
    val layerId = varchar("layer_id", 255).references(Layers.id)
    val tileMatrixSetId = varchar("tile_matrix_set_id", 255).references(TileMatrixSets.id)

    override val primaryKey = PrimaryKey(layerId, tileMatrixSetId)
}

object AdminsTable : Table("admins") {
    val id = integer("id").autoIncrement()
    val username = varchar("username", length = 50).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 64)

    override val primaryKey = PrimaryKey(id)
}