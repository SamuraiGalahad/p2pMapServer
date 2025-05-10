package trotech.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import trotech.dao.database.peers.findMatchingPeer
import trotech.dao.database.peers.getMatrixSize
import trotech.dao.redis.PeersRepository
import trotech.dto.entities.TrackerAnnouncePeerInfo
import trotech.service.usecase.algorithm.chooseBestPeersByLoad
import trotech.service.usecase.metrics.CounterService
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

val activeSessions = ConcurrentHashMap<String, ArrayList<String>>()

@Serializable
data class Response(
    val host: String,
    val port: String,
    val key: String
)

fun ApplicationCall.getRealIp(): String {
    val forwardedFor = request.headers["X-Forwarded-For"]
    if (forwardedFor != null) {
        return forwardedFor.split(",").first().trim()
    }
    return request.origin.remoteHost
}

@Serializable
data class TrackerAskReply(
    val host: String,
    val port: Int,
    val key: String,
    val tiles: ArrayList<TrackerAskReplyTile>
)

@Serializable
data class TrackerAskReplyTile(
    val tileMatrix: String,
    val tileColsAndRows: List<List<Int>>,
    val format: String
)

fun generateMatrixIndices(n: Int, m: Int): List<List<Int>> {
    return (0 until n).flatMap { row ->
        (0 until m).map { col ->
            listOf(col, row)
        }
    }
}

fun splitMatrixByRows(matrix: List<List<Int>>, k: Int): List<List<List<Int>>> {
    val n = matrix.size
    val chunkSize = (n + k - 1) / k  // округляем вверх
    return matrix.chunked(chunkSize)
}

fun Route.peersRoutes() {
    val counter by application.inject<CounterService>()

    val repository by application.inject<PeersRepository>()

    var algorithm = 0

    val peersRepository = PeersRepository()
    route("/peers") {
        post("/changealg") {
            if (algorithm == 0) {
                algorithm = 1
            } else if (algorithm == 1) {
                algorithm = 0
            }
        }
        get("/ask") {
            counter.addPeersAsk()

            val peerid = call.request.queryParameters["peerid"]

            if (peerid == null) {
                call.respond(HttpStatusCode.BadRequest, "Missing Peer Identification")
                return@get
            }

            val layerId = call.request.queryParameters["layer"]
            val tileMatrixSetId = call.request.queryParameters["tms"]

            if (layerId == null || tileMatrixSetId == null) {
                call.respondText("Missing layer or tms parameter", status = HttpStatusCode.BadRequest)
                return@get
            }

            /**
             * Пиры, у которых потенциально есть эти данные
             */
            val matchingPeerIds = findMatchingPeer(layerId, tileMatrixSetId, peerid)

            val matricesSizes = getMatrixSize(tileMatrixSetId)

            val peerInfos = matchingPeerIds.mapNotNull { repository.getFullPeerInfo(it) }

            val selectedPeers : List<TrackerAnnouncePeerInfo> = if (algorithm == 0) {
                chooseBestPeersByLoad(peerInfos, maxLoad = 3, count = 5)
            } else {
                chooseBestPeersByLoad(peerInfos, maxLoad = 3, count = 1)
            }

            val result = arrayListOf<TrackerAskReply>()

            val peersTileMatrix = Array(selectedPeers.size) {arrayListOf<TrackerAskReplyTile>()}

            for (size in matricesSizes) {
                val indexes = generateMatrixIndices(size.second.second, size.second.first)
                val splitByRows = splitMatrixByRows(indexes, selectedPeers.size)
                for (i in selectedPeers.indices) {
                    peersTileMatrix[i].add(TrackerAskReplyTile(size.first, splitByRows[i], "image/png"))
                }
            }

            /**
             * Нагрузка на пиров
             */


            for (i in selectedPeers.indices) {
                val sessionKey = UUID.randomUUID().toString()
                val localPeerId = selectedPeers[i].peerId
                if (activeSessions.containsKey(localPeerId)) {
                    activeSessions[localPeerId]?.add(sessionKey)
                } else {
                    activeSessions[localPeerId] = arrayListOf<String>()
                    activeSessions[localPeerId]?.add(sessionKey)
                }
                result.add(
                    TrackerAskReply(
                        "192.168.1.24",
                        9999,
                        sessionKey,
                        peersTileMatrix[i]
                    ))
            }

            if (result.isNotEmpty()) {
                call.respond(result)
            } else {
                call.respondText("No matching peer found", status = HttpStatusCode.NotFound)
            }
        }

        get("/check") {
            counter.addPeersCheck()

            val peerid = call.request.queryParameters["peerid"]

            if (peerid == null || peerid.isEmpty()) {
                call.respond(HttpStatusCode.NoContent, "Session key not found or already processed.")
                return@get
            }

            val clientIp = call.getRealIp()

            peersRepository.addPeerId(peerid)

            if (activeSessions.containsKey(peerid)) {
                val sessionKeys = activeSessions[peerid] ?: listOf<String>()

                call.respond(sessionKeys.map { Response("192.168.1.24",
                    "9999", it) }.toList())

                activeSessions[peerid]?.clear()
            } else {
                call.respond(HttpStatusCode.NoContent, "Session key not found or already processed.")
            }
        }
    }
    route("/speedtest") {
        get("/download") {
            call.respondText("http://192.168.1.24:8087/backend/garbage.php")
        }
        get("/upload") {
            call.respondText("http://192.168.1.24:8087/backend/empty.php")
        }
    }
}