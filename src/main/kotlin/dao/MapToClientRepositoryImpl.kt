package dao

import dto.MapMeta
import dto.MapToClient
import org.hibernate.SessionFactory
import java.util.*

//todo: функция рандомизации
//class MapToClientRepositoryImpl(private val sessionFactory: SessionFactory) : MapToClientRepository {
//    override fun getClientsByMapId(mapId: UUID): List<UUID> {
////        sessionFactory.openSession().use { session ->
////            return session.get(MapToClient::class.java, mapId)
////        }
//    }
//}