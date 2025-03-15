package service.usecase.math

import dto.AlgorithmParams
import dto.ClientMeta
import java.util.UUID

interface Algorithm {
    fun getPeers(uuid: UUID) : List<ClientMeta>

    fun setAlgorithmParams(algorithmParams: AlgorithmParams)
}