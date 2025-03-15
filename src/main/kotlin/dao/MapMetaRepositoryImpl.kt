package dao

import dto.MapMeta
import org.hibernate.SessionFactory

class MapMetaRepositoryImpl(private val sessionFactory: SessionFactory) : MapMetaRepository {
    override fun getAll(): List<MapMeta> {
        sessionFactory.openSession().use { session ->
            val hu = HibernateUtils()
            return hu.loadAllData(MapMeta::class.java, session)
        }
    }

    override fun getById(id: Int): MapMeta? {
        sessionFactory.openSession().use { session ->
            return session.get(MapMeta::class.java, id)
        }
    }
}