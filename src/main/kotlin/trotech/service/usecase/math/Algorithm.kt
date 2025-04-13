package trotech.service.usecase.math

import trotech.dto.AlgorithmParams
import trotech.dto.ClientMeta
import java.util.UUID

interface Algorithm {
    fun getPeers(uuid: UUID) : List<ClientMeta>

    fun setAlgorithmParams(algorithmParams: AlgorithmParams)
}