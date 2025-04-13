package trotech.dto.entities

import jakarta.persistence.*
import kotlinx.serialization.Serializable
import trotech.trotech.UUIDSerializer
import java.util.*

@Table(name = "peers")
@Entity
@Serializable
data class Peer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    var id: Long? = null,
    @Serializable(with = UUIDSerializer::class)
    @Column(name = "uuid", nullable = false)
    val uuid: UUID,
    @Column(name = "ip", nullable = false)
    val ip: String,
    @Column(name = "port", nullable = false)
    val port: Int
)
