package trotech

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.ktor.http.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.*
import io.ktor.server.netty.Netty
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import kotlinx.serialization.Serializable
import java.util.*
import kotlinx.html.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonObject
import java.security.MessageDigest
import kotlin.collections.ArrayList

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

@Serializable
data class Peer(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val ip: String,
    val port: Int
)

@Serializable
data class Torrent(
    val name: String,
    val peers: MutableList<Peer> = mutableListOf()
)

@Serializable
data class MapInfo(val name: String, val peers: MutableList<Peer>)

@Serializable
data class Image(
    val n: Int,
    val m: Int,
    val type: String
)

@Serializable
data class Matrix(
    val level: Int,
    val images: List<Image>
)

@Serializable
data class Layer(
    val name: String,
    val type: String,
    val matrix: Matrix
)

@Serializable
data class AnnounceInfo(
    @Serializable(with = UUIDSerializer::class)
    val uuid: UUID,
    val layers: List<Layer>
)

@Serializable
data class PeerInfo(
    val connectionCodes: List<String>,
    val udpPort: Int = 5000
)

val mapper = jacksonObjectMapper()

val dictForMap = mutableMapOf<String, MutableSet<Peer>>()

fun main() {

    val mapsInfo = mutableMapOf<UUID,MapInfo>()

    embeddedServer(Netty, port = 8000) {
        routing {
            /**
             * Сервер получает информацию о новом участнике в сети
             */
            post("/announce") {
                val params = call.receiveParameters()

                val uuid = UUID.fromString(params["uuid"])
                val mapInfo = params["info"]
                val ip = call.request.local.remoteHost
                val port = call.request.local.remotePort


                if (uuid == null || port == null) {
                    call.respondText(HttpStatusCode.BadRequest.toString())
                    return@post
                }

                val parsedMapInfo = mapper.readValue(mapInfo, AnnounceInfo::class.java)

                if (uuid != parsedMapInfo.uuid) {
                    call.respondText(HttpStatusCode.BadRequest.toString())
                    return@post
                }

                val layers = parsedMapInfo.layers

                for (layer in layers) {
                    if (dictForMap.containsKey(layer.name)) {
                        dictForMap[layer.name]?.add(Peer(uuid, ip, port))
                    }
                }

                call.respond(HttpStatusCode.OK, "Peer announced successfully")
            }

//            get("/peers") {
//                val infoHash = call.request.queryParameters["infoHash"]
//
//                if (infoHash == null) {
//                    call.respond(HttpStatusCode.BadRequest, "Missing infoHash")
//                    return@get
//                }
//
//                val torrent = torrents[infoHash]
//
//                if (torrent == null) {
//                    call.respondText("Torrent not found")
//                    return@get
//                }
//                call.respondText(torrent.peers?.get(0).toString())
//            }

            get("/maps/") {
            }

            //todo: поменять на ручку maps
            get("/home") {

                val keys = mapsInfo.iterator()

                val string = StringBuilder()

                string.append("Track Server\n")

                for (data in keys) {
                    val key = data.key
                    val value = data.value
                    string.append("$key=${value.name}\n")
                }

                call.respondText(string.toString())
            }

            get("/") {
                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title("Авторизация")
                    }
                    body {
                        h1 { +"Вход в систему" }
                        form(action = "/login", method = FormMethod.post) {
                            p {
                                label { +"Логин:" }
                                textInput(name = "username")
                            }
                            p {
                                label { +"Пароль:" }
                                passwordInput(name = "password")
                            }
                            p {
                                submitInput { value = "Войти" }
                            }
                        }
                    }
                }
            }

            post("/login") {
                val params = call.receiveParameters()
                val username = params["username"]
                val password = params["password"]

                if (username == "admin" && password == "password") {
                    call.respondRedirect("/dashboard")
                } else {
                    call.respondHtml(HttpStatusCode.Unauthorized) {
                        body {
                            h1 { +"Ошибка авторизации" }
                            p { +"Неверный логин или пароль." }
                            a(href = "/") { +"Попробовать снова" }
                        }
                    }
                }
            }

            get("/dashboard") {
                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title("P2P Dashboard")
                    }
                    body {
                        h1 { +"Статистика P2P сети" }
                        table {
                            tr {
                                th { +"Узел" }
                                th { +"IP-адрес" }
                                th { +"Статус" }
                                th { +"Количество соединений" }
                            }
                            tr {
                                td { +"Node-1" }
                                td { +"192.168.1.10" }
                                td { +"Активен" }
                                td { +"12" }
                            }
                            tr {
                                td { +"Node-2" }
                                td { +"192.168.1.11" }
                                td { +"Не в сети" }
                                td { +"0" }
                            }
                        }
                        a(href = "/") { +"Выйти" }
                    }
                }
            }

            get("/peer_info") {
                val peerUUID = UUID.fromString(call.parameters["uuid"])

                call.respond(PeerInfo(arrayListOf("1awdNAWadwWAD")))
            }

            get("/peers") {
                val fileName = call.parameters["fileName"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing file hash")
                val peers = selectPeersByHash(fileName)
                call.respond(peers)
            }
        }
    }.start(wait = true)
}

fun selectPeersByHash(fileHash: String): List<Peer> {
    return dictForMap[fileHash]?.toList() ?: ArrayList<Peer>()
}
