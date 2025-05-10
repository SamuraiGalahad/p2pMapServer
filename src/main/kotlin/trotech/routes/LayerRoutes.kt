package trotech.routes

import trotech.service.usecase.dataload.MessageWMTSFormatter
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import org.slf4j.LoggerFactory
import trotech.dao.database.peers.saveLayerForUser
import trotech.dao.database.peers.saveTileMatrixSetForUser
import trotech.dao.database.peers.saveUserIfNotExists
import trotech.dao.redis.PeersRepository
import trotech.dto.Layer
import trotech.dto.TileMatrixSet
import trotech.dto.entities.parsePeerInfo
import trotech.service.usecase.dataload.parseLayer
import trotech.service.usecase.dataload.parseTileMatrixSet
import trotech.service.usecase.metrics.CounterService
import java.io.File

fun Route.layerRoutes() {
    val counter by application.inject<CounterService>()

    val repository by application.inject<PeersRepository>()

    route("/announce") {

        post("/layer") {
            val xml = call.receive<String>()
            val userId = call.request.queryParameters["peerid"]
            counter.addAnnounceLayer()
            if (xml.isEmpty() || userId == null) {
                call.respond(HttpStatusCode.BadRequest, "Illegal Arguments")
                return@post
            }

            val layer : Layer

            try {
                layer = parseLayer(xml)
            } catch (_ : Exception) {
                call.respond(HttpStatusCode.BadRequest, "Wrong xml")
                return@post
            }
            try {
                saveUserIfNotExists(userId)
                saveLayerForUser(userId, layer)
            } catch (er : Exception) {
                call.respond(HttpStatusCode.RequestTimeout, "Cant save")
                throw er
            }
            println("Получил Layer от пользователя $userId:$userId")

            val formatter by application.inject<MessageWMTSFormatter>()
            val file = File("wmts.xml")
            file.writeText(formatter.getCapabilitiesGen())

            call.respond(HttpStatusCode.OK)
        }

        post("/tms") {
            val xml = call.receive<String>()
            val userId = call.request.queryParameters["peerid"]
            counter.addAnnounceTms()
            if (xml.isEmpty() || userId == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val tileMatrixSet: TileMatrixSet

            try {
                tileMatrixSet = parseTileMatrixSet(xml)
            } catch (er : Exception) {
                call.respond(HttpStatusCode.BadRequest, "Wrong xml")
                throw er
            }

            try {
                saveUserIfNotExists(userId)
                saveTileMatrixSetForUser(userId, tileMatrixSet)
            } catch (er : Exception) {
                call.respond(HttpStatusCode.RequestTimeout, "Cant save")
                throw er
            }

            val formatter by application.inject<MessageWMTSFormatter>()
            val file = File("wmts.xml")
            file.writeText(formatter.getCapabilitiesGen())


            println("Получил TileMatrixSet от пользователя $userId: $userId")
            call.respond(HttpStatusCode.OK)
        }

        post("/peer") {
            val json = call.receive<String>()
            val peerInfo = parsePeerInfo(json)
            val logger = LoggerFactory.getLogger("AnnouncePeer")
            repository.saveMetaInfo(peerInfo)
            logger.info("New info:\n${peerInfo}")
        }
    }

    route("/layers"){
        get("/GetCapabilities") {
            val formatter by application.inject<MessageWMTSFormatter>()

            counter.addGetCapabilities()

            try {
                call.respondText(formatter.getCapabilitiesGen(), ContentType.Application.Xml)
            } catch (er : Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            call.respond(HttpStatusCode.OK)
        }
    }
}