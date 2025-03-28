package trotech

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dao.database.DatabaseFactory
import dto.*
import dto.entities.Peer
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.html.*
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*
import kotlinx.html.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.collections.ArrayList
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import service.usecase.stability.NetworkPeerService
import trotech.dao.redis.PeersRepository
import trotech.service.usecase.xml.MessageWMTSFormatter

object UUIDSerializer : KSerializer<UUID> {
    override val descriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): UUID {
        return UUID.fromString(decoder.decodeString())
    }

    override fun serialize(encoder: Encoder, value: UUID) {
        encoder.encodeString(value.toString())
    }
}

val dictForMap = mutableMapOf<String, MutableSet<Peer>>()

//todo: сделать очистку по времени
val peers = mutableMapOf<UUID, AnnounceInfo>()

val mapsInfo = mutableMapOf<String,MapInfo>()

val networkBalancer = NetworkPeerService()

val mapsPeersImagesMatrix = mutableMapOf<String, MutableMap<String, MutableSet<Peer>>>()

val secret = "my_secret_key"

val tokens = ArrayList<String>()

val redisRepository = PeersRepository()

@Serializable
data class UserSession(val token: String)

fun main() {


    val server = embeddedServer(Netty, port = 8000) {
        environment.monitor.subscribe(ApplicationStopping) {
            println("Stopping")
        }
        launch(Dispatchers.IO) {
            while (isActive) {
                delay(20_000)
                redisRepository.cleanInactivePeersTask.run()
            }
        }

        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }


        val shutdown = ShutDownUrl("") { 1 }

        install(ShutDownUrl.ApplicationCallPlugin) {
            shutDownUrl = "/shutdown"
            exitCodeSupplier = { 0 }
        }
        install(Authentication) {
            jwt("auth-jwt") {
                realm = "ktor-server"
                verifier(JWT.require(Algorithm.HMAC256(secret)).withIssuer("ktor-app").build())
            }
        }
        install(Sessions) {
            cookie<UserSession>("SESSION") {
                cookie.httpOnly = true
                cookie.maxAgeInSeconds = 3600
            }
        }

        DatabaseFactory.init()

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

                redisRepository.addPeerId(uuid)

                // если не передают идентификаторы пира: возвращаем ошибку
                if (uuid == null || port == null) {
                    call.respondText(HttpStatusCode.BadRequest.toString())
                    return@post
                }


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

            get("/maps") {
                var line = ""

                for (name in mapsInfo.keys) {
                    line += "${name}\n";
                }

                call.respondText(line)
            }

            // получить активных пиров и их колличество
            get("/active_peers") {
                var line = "Active peers\n"
                val activePeers = redisRepository.getActivePeers()

                for (peer in activePeers) {
                    line += "${peer}\n";
                }

                line += "Всего:${activePeers.size}\n"

                call.respondText(line)
            }

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
                    val token = JWT.create().withIssuer("ktor-app").withClaim("username", username).sign(Algorithm.HMAC256(secret))
                    call.sessions.set(UserSession(token))
                    tokens.add(token)
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
                val session = call.sessions.get<UserSession>()
                if (tokens.contains(session?.token)) {
                    return@get
                }
                call.respondHtml(HttpStatusCode.OK) {
                    head {
                        title("P2P Dashboard")
                    }
                    body {
                        h1 { +"Статистика P2P сети" }
                        table {
                            tr {
                                th { +"Идентификатор" }
                                th { +"Статус" }
                                th { +"Количество слоёв" }
                            }

                            peers.keys.forEach {
                                key ->
                                tr {
                                    th {
                                        +peers[key]?.uuid.toString()
                                    }
                                    th {
                                        +"Активный"
                                    }
                                    th {
                                        +peers[key]?.layers?.size.toString()
                                    }
                                }
                            }
                        }
                        a(href = "/doAction?action=logout") { +"Выйти\n" }
                        a(href = "/doAction?action=shutdown") { +"Выключить сервер\n" }
                    }
                }
            }

            get("/doAction") {
                val action = call.request.queryParameters["action"]
                when (action) {
                    "logout" -> {
                        val session = call.sessions.get<UserSession>()
                        if (session != null) {
                            tokens.remove(session.token)
                        }
                        call.respondText("Вы вышли!", ContentType.Text.Plain)
                    }
                    "shutdown" -> {
                        call.respondText("Выключение сервера", ContentType.Text.Plain)
                        shutdown.doShutdown(call)
                    }
                    else -> call.respond(HttpStatusCode.BadRequest, "Неизвестное действие")
                }
            }

            get("/delete_peer") {
                val peerUUID = UUID.fromString(call.parameters["uuid"])
            }

            get("/peer_info") {
                val peerUUID = UUID.fromString(call.parameters["uuid"])

                call.respond(PeerInfo(arrayListOf("1awdNAWadwWAD")))
            }

            get("/peers") {
                val fileName = call.parameters["fileName"] ?: return@get call.respond(HttpStatusCode.BadRequest, "Missing file hash")
                val result = networkBalancer.selectPeersByHash(fileName)

                val m = mapsPeersImagesMatrix

                if (result == null) {
                    call.respondText(HttpStatusCode.BadRequest.toString())
                    return@get
                }
                call.respond(result)
            }

            post("/layer") {
                val xml = call.receiveText()
                val layer = MessageWMTSFormatter().parseLayer(xml)
                val capabilitiesXml = MessageWMTSFormatter().generateCapabilitiesXml(layer)
                call.respondText(capabilitiesXml, ContentType.Text.Xml)
            }
        }

    }.start(wait = true)
}
