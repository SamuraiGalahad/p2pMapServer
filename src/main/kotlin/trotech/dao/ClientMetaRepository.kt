package trotech.dao

import trotech.dto.ClientMeta

interface ClientMetaRepository {
    fun getAllClients(): List<ClientMeta>

    fun getClientById(id: Int): ClientMeta?
}