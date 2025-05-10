package trotech.module

import trotech.service.usecase.dataload.MessageWMTSFormatter
import org.koin.dsl.module
import trotech.dao.redis.PeersRepository
import trotech.service.usecase.algorithm.Graph
import trotech.service.usecase.metrics.CounterService
import kotlin.math.sin

val appModule = module {
    single {
        MessageWMTSFormatter(
            title = "Trotech WMTS",
            abstract = "Trotech WMTS Service",
            serviceProvicderName = "Trotech",
            serviceProviderUrl = "url",
            serviceType = "OCS WMTS",
            serviceVersion = "1.0.0"
        )
    }
    single {
        CounterService()
    }
    single {
        PeersRepository()
    }
    single {
        Graph()
    }
}