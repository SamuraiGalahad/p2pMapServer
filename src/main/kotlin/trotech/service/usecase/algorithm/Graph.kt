package trotech.service.usecase.algorithm

import kotlinx.coroutines.*
import org.jgrapht.graph.DefaultEdge
import org.jgrapht.graph.SimpleDirectedGraph
import trotech.dao.redis.PeersRepository
import trotech.dto.GraphResponse
import trotech.service.usecase.math.assortativityCoefficient
import trotech.service.usecase.math.clusteringCoefficient
import trotech.service.usecase.math.computeAlgebraicConnectivity
import trotech.service.usecase.math.connectedComponents

fun CoroutineScope.startPeriodicGraphUpdate(
    graph: Graph,
    intervalMillis: Long = 30_000
) {
    launch {
        while (isActive) {
            graph.reloadGraph()
            delay(intervalMillis)
        }
    }
}

class Graph {
    private var graph = SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)

    private val peersRepository = PeersRepository()

    private var connectedComponentsParam = connectedComponents(graph).size

    private var clusteringCoefficient = clusteringCoefficient(graph).first

    private var algebraicConnectivity = computeAlgebraicConnectivity(graph)

    private var assortativityCoefficient = assortativityCoefficient(graph)

    private var downloadSpeed = PeersRepository().getAverageDownloadSpeed()

    private var uploadingSpeed = PeersRepository().getAverageUploadSpeed()

    suspend fun reloadGraph() {
        val peerInfos = peersRepository.getAllPeerInfos()
        graph = SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge::class.java)

        for (info in peerInfos) {
            graph.addVertex(info.peerId)
            for (connectedPeer in info.connectedPeers) {
                graph.addVertex(connectedPeer)
                graph.addEdge(info.peerId, connectedPeer)
            }
        }
        connectedComponentsParam = connectedComponents(graph).size

        clusteringCoefficient = clusteringCoefficient(graph).first

        algebraicConnectivity = computeAlgebraicConnectivity(graph)

        assortativityCoefficient = assortativityCoefficient(graph)

    }

    fun getMetrics() : GraphResponse {
        return GraphResponse(
            connectedComponentsParam,
            clusteringCoefficient,
            algebraicConnectivity,
            assortativityCoefficient,
            downloadSpeed,
            uploadingSpeed
        )
    }

}