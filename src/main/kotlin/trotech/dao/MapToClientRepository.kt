package trotech.dao

import java.util.UUID

interface MapToClientRepository {
    fun getClientsByMapId(mapId: UUID, nPeers : Int): List<UUID>
}