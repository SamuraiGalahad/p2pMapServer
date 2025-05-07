package trotech

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import trotech.dao.database.DatabaseFactory
import trotech.dto.entities.Peer
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.routing.*
import java.util.*
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlin.collections.ArrayList
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import org.koin.ktor.ext.inject
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.slf4j.LoggerFactory
import org.slf4j.event.Level
import trotech.dao.redis.PeersRepository
import trotech.module.appModule
import trotech.routes.adminRoute
import trotech.routes.authRoutes
import trotech.routes.layerRoutes
import trotech.routes.peersRoutes
import trotech.service.usecase.console.startTelnetServer
import trotech.service.usecase.dataload.MessageWMTSFormatter
import trotech.service.usecase.metrics.metricsModule

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
val mapsPeersImagesMatrix = mutableMapOf<String, MutableMap<String, MutableSet<Peer>>>()

val secret = "my_secret_key"

val tokens = ArrayList<String>()

val redisRepository = PeersRepository()

@Serializable
data class UserSession(val token: String)

lateinit var server :  EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine. Configuration>

fun main() {
    server = embeddedServer(Netty, port = 8000) {

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
        install(Koin) {
            slf4jLogger()
            modules(appModule)
        }

        metricsModule()

        install(ShutDownUrl.ApplicationCallPlugin) {
            shutDownUrl = "/shutdown"
            exitCodeSupplier = { 0 }
        }

        startTelnetServer(2323)

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

        install(CallLogging) {
            level = Level.INFO
            logger = LoggerFactory.getLogger("ktor.application")
        }

        DatabaseFactory.init()

        routing {
            authRoutes()
            adminRoute()
            layerRoutes()
            peersRoutes()
        }

    }.start(wait = true)

}
