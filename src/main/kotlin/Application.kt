package trotech

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dto.*
import dto.entities.Peer
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.*
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlinx.html.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
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


fun main() {
    embeddedServer(Netty, port = 8000) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
        routing {
            /**
             * Сервер получает информацию о новом участнике в сети
             */
            post("/announce") {
                val parsedMapInfo = call.receive<AnnounceInfo>()

                // собираем параметры для определения пира
                val uuid = parsedMapInfo.uuid
                val ip = call.request.local.remoteHost
                val port = call.request.local.remotePort

                val peer = Peer(0, uuid, ip, port)

                // если не передают идентификаторы пира: возвращаем ошибку
                if (uuid == null || port == null) {
                    call.respondText(HttpStatusCode.BadRequest.toString())
                    return@post
                }

                // берем карты от пира

                // если идентификатор пира передаваемый не совпадает с ид в конструкции: возвращаем ошибку
                if (uuid != parsedMapInfo.uuid) {
                    call.respondText(HttpStatusCode.BadRequest.toString())
                    return@post
                }

                // берем список карт
                val layers = parsedMapInfo.layers

                // соотносим идентификатор карт к пирам
                for (layer in layers) {
                    if (dictForMap.containsKey(layer.name)) {
                        dictForMap[layer.name]?.add(peer)
                    } else {
                        dictForMap[layer.name] = mutableSetOf(peer)
                    }


                    if (mapsInfo.containsKey(layer.name)) {
                        for (matrix in layer.matrix) {
                            for (image in matrix.images) {
                                mapsInfo[layer.name]?.addImageToMatrix(image, matrix.level)
                            }
                        }
                    } else {
                        mapsInfo[layer.name] = MapInfo(layer.name, arrayListOf())
                        for (matrix in layer.matrix) {
                            for (image in matrix.images) {
                                mapsInfo[layer.name]?.addImageToMatrix(image, matrix.level)
                            }
                        }
                    }

                    var name = layer.name
                    var matrix = layer.matrix

                    for (mat in matrix) {
                        for (image in mat.images) {
                            val key = "${mat.level}:${image.col}:${image.row}"
                            if (mapsPeersImagesMatrix.containsKey(name)) {
                                if (mapsPeersImagesMatrix[name]?.containsKey(key) == true) {
                                    mapsPeersImagesMatrix[name]?.get(key)?.add(peer)
                                } else {
                                    mapsPeersImagesMatrix[name]?.set(key, mutableSetOf(peer))
                                }
                            } else {
                                mapsPeersImagesMatrix[name] = mutableMapOf(Pair(key, mutableSetOf(peer)))
                            }
                        }
                    }
                }



                peers[parsedMapInfo.uuid] = parsedMapInfo

                call.respond(HttpStatusCode.OK, "Peer announced successfully")
            }

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
                val result = selectPeersByHash(fileName)

                val m = mapsPeersImagesMatrix

                if (result == null) {
                    call.respondText(HttpStatusCode.BadRequest.toString())
                    return@get
                }
                call.respond(result)
            }
        }
    }.start(wait = true)
}

val mapper = jacksonObjectMapper()

val dictForMap = mutableMapOf<String, MutableSet<Peer>>()

//todo: сделать очистку по времени
val peers = mutableMapOf<UUID, AnnounceInfo>()

val mapsInfo = mutableMapOf<String,MapInfo>()

val mapsPeersImagesMatrix = mutableMapOf<String, MutableMap<String, MutableSet<Peer>>>()


fun selectPeersByHash(fileHash: String): DownloadDistributionMap? {
    // получили пиров, которые владеют картой
    val peersId = dictForMap[fileHash]?.toList() ?: ArrayList<Peer>()

    if (peersId.isEmpty()) {
        return null
    }

    val mapInfo = mapsInfo[fileHash] ?: return null

    val result = DownloadDistributionMap(mapInfo.name, arrayListOf())

    val connectionCodes = mutableMapOf<UUID, String>()

    for (key in mapInfo.matrix.keys) {
        val rm = ResponceMatrix(key, arrayListOf())
        for (image in mapInfo.matrix[key]!!) {
            val localImageKey = "${key}:${image}"

            println(image)

            val peer = mapsPeersImagesMatrix.get(fileHash)?.get(localImageKey)?.toList()?.random()

            var connectionCode = UUID.randomUUID().toString()

            if (connectionCodes.containsKey(peer?.uuid)) {
                connectionCode = connectionCodes.get(peer?.uuid).toString()
            } else {
                if (peer != null) {
                    connectionCodes.put(peer.uuid, connectionCode)
                }
            }


            val responceImage =
                peer?.let {
                    ResponceImage(image.split(":")[0].toInt(), image.split(":")[1].toInt(), connectionCode,
                        it
                    )
                }
            if (responceImage != null) {
                rm.images.add(responceImage)
            }
        }
        result.loadMatrix.add(rm)
    }

    return result
}
