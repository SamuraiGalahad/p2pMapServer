package dao

import dto.ClientMeta

interface ClientMetaRepository {
    fun getAllClients(): List<ClientMeta>

    fun getClientById(id: Int): ClientMeta?
}