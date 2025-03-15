package dao

import dto.ClientMeta
import org.hibernate.SessionFactory

class ClientMetaRepositoryImpl(private val sessionFactory: SessionFactory) : ClientMetaRepository {
    override fun getAllClients(): List<ClientMeta> {
        sessionFactory.openSession().use { session ->
            val hu = HibernateUtils()
            return hu.loadAllData(ClientMeta::class.java, session)
        }
    }

    override fun getClientById(id: Int): ClientMeta? {
        sessionFactory.openSession().use { session ->
            return session.get(ClientMeta::class.java, id)
        }
    }
}