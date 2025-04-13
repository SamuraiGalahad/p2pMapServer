package trotech.dto

import kotlinx.serialization.Serializable
import trotech.trotech.UUIDSerializer
import java.util.*

@Serializable
data class AnnounceInfo(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val layers: List<Layer>
)
