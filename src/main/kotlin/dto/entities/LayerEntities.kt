package trotech.dto.entities

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.Entity
import org.jetbrains.exposed.dao.EntityClass
import trotech.service.usecase.xml.BoundingBox
import trotech.service.usecase.xml.Layer

object LayersTable : IntIdTable() {
    val title = varchar("title", 255)
    val identifier = varchar("identifier", 255).uniqueIndex()
    val lowerCorner = varchar("lower_corner", 255)
    val upperCorner = varchar("upper_corner", 255)
    val formats = text("formats")
    val tileMatrixSets = text("tile_matrix_sets")
}

class LayerEntity(id: EntityID<Int>) : Entity<Int>(id) {
    companion object : EntityClass<Int, LayerEntity>(LayersTable)

    var title by LayersTable.title
    var identifier by LayersTable.identifier
    var lowerCorner by LayersTable.lowerCorner
    var upperCorner by LayersTable.upperCorner
    var formats by LayersTable.formats
    var tileMatrixSets by LayersTable.tileMatrixSets

    fun toLayer(): Layer = Layer(
        title = title,
        identifier = identifier,
        boundingBox = BoundingBox(lowerCorner, upperCorner),
        formats = formats.split(","),
        tileMatrixSets = tileMatrixSets.split(",")
    )
}