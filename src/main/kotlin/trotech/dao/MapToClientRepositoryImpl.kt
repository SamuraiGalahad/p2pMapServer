package trotech.dao

import trotech.dto.MapMeta
import trotech.dto.MapToClient
import org.hibernate.SessionFactory
import java.util.*

//todo: функция рандомизации
//class MapToClientRepositoryImpl(private val sessionFactory: SessionFactory) : MapToClientRepository {
//    override fun getClientsByMapId(mapId: UUID): List<UUID> {
////        sessionFactory.openуSession().use { session ->
////            return session.get(MapToClient::class.java, mapId)
////        }
//    }
//}