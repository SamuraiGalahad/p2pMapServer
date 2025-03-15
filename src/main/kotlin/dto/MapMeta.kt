package dto

import java.util.UUID
import javax.persistence.*

@Entity
@Table(name = "map_meta")
class MapMeta (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    var id: Int = 0,

    @Column(name = "name")
    val name: String,

    @Column(name = "mapId")
    val mapId: UUID
)