package service.usecase.math

import com.google.common.hash.HashCode
import dao.*
import dto.AlgorithmParams
import dto.ClientMeta
import java.util.UUID

/**
 *
 */
class StandartAlgorithm : Algorithm {
    private var nPeers : Int = 40

    private val hibernateUtils = HibernateUtils()

    private var clientMetaRepository : ClientMetaRepository = ClientMetaRepositoryImpl(hibernateUtils.sessionFactory)

    //private var mapToClientRepository : MapToClientRepository = MapToClientRepositoryImpl(hibernateUtils.sessionFactory)

    override fun getPeers(uuid: UUID): List<ClientMeta> {
        //val clientsUUID = mapToClientRepository.getClientsByMapId(uuid, nPeers)
        //todo: realise
        return ArrayList<ClientMeta>()
    }

    override fun setAlgorithmParams(algorithmParams: AlgorithmParams) {
        //todo: realise
        return
    }

    fun setNPeers(nPeers: Int) {
        if (this.nPeers < 0) {
            throw Exception("Peers answer must be greater than 0.")
        // todo: заменить на значения из пропертей
        } else if (this.nPeers > 100) {}
        this.nPeers = nPeers
    }

    fun setRepository(clientMetaRepository : ClientMetaRepository) {
        this.clientMetaRepository = clientMetaRepository
    }
}