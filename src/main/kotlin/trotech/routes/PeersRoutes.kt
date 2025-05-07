package trotech.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import trotech.dao.database.peers.findMatchingPeer
import trotech.dao.redis.PeersRepository
import trotech.service.usecase.metrics.CounterService
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

val activeSessions = ConcurrentHashMap<String, String>()

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

fun Route.peersRoutes() {
    val counter by application.inject<CounterService>()

    val peersRepository = PeersRepository()
    route("/peers") {
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

            val matchingPeerId = findMatchingPeer(layerId, tileMatrixSetId, peerid)

            if (matchingPeerId != null) {
                val sessionKey = UUID.randomUUID().toString()
                activeSessions[matchingPeerId] = sessionKey
                call.respond(Response(host = "192.168.1.2", port = "9999", key = sessionKey))
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
                val sessionKey = activeSessions[peerid] ?: ""

                call.respond(Response(host = "192.168.1.2", port = "9999", key = sessionKey))

                activeSessions.remove(peerid)
            } else {
                call.respond(HttpStatusCode.NoContent, "Session key not found or already processed.")
            }
        }
    }
    route("/status") {
          post("/metadata") {

          }
    }
}