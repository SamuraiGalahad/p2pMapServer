package dto

import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

@Entity
@Table(name = "mapToClient")
class MapToClient (
    @Column(name = "mapUUID")
    val mapUUID: UUID,
    @Column(name = "clientUUID")
    val clientUUID: UUID,
)