package trotech.dto

import java.util.*
import javax.persistence.*


@Entity
@Table(name = "clients")
class ClientMeta (
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int = 0,

    @Column(name = "uuid")
    var uuid: UUID = UUID.randomUUID(),

    @Column(name = "ip")
    val address: String? = null,

    @Column(name = "port")
    val port: Int
)