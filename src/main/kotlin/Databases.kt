//package trotech
//
//import com.auth0.jwt.JWT
//import com.auth0.jwt.algorithms.Algorithm
//import com.fasterxml.jackson.databind.*
//import com.mongodb.kotlin.client.coroutine.MongoClient
//import dev.inmo.krontab.builder.*
////import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
////import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
////import io.github.damir.denis.tudor.ktor.server.rabbitmq.rabbitMQ
//import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
//import io.github.flaxoos.ktor.server.plugins.kafka.MessageTimestampType
//import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
//import io.github.flaxoos.ktor.server.plugins.kafka.admin
//import io.github.flaxoos.ktor.server.plugins.kafka.common
//import io.github.flaxoos.ktor.server.plugins.kafka.consumer
//import io.github.flaxoos.ktor.server.plugins.kafka.consumerConfig
//import io.github.flaxoos.ktor.server.plugins.kafka.consumerRecordHandler
//import io.github.flaxoos.ktor.server.plugins.kafka.producer
//import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
//import io.github.flaxoos.ktor.server.plugins.kafka.topic
//import io.github.flaxoos.ktor.server.plugins.ratelimiter.*
//import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.*
//import io.github.flaxoos.ktor.server.plugins.taskscheduling.*
//import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.database.*
//import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.redis.*
//import io.ktor.client.*
//import io.ktor.client.engine.apache.*
//import io.ktor.http.*
//import io.ktor.network.selector.*
//import io.ktor.network.sockets.*
//import io.ktor.network.tls.*
//import io.ktor.serialization.jackson.*
//import io.ktor.serialization.kotlinx.json.*
//import io.ktor.server.application.*
//import io.ktor.server.auth.*
//import io.ktor.server.auth.jwt.*
//import io.ktor.server.html.*
//import io.ktor.server.plugins.calllogging.*
//import io.ktor.server.plugins.contentnegotiation.*
//import io.ktor.server.plugins.openapi.*
//import io.ktor.server.plugins.swagger.*
//import io.ktor.server.request.*
//import io.ktor.server.response.*
//import io.ktor.server.routing.*
//import io.ktor.server.sessions.*
//import io.ktor.server.sse.*
//import io.ktor.server.websocket.*
//import io.ktor.sse.*
//import io.ktor.utils.io.*
//import io.ktor.utils.io.core.*
//import io.ktor.websocket.*
//import java.io.InputStream
//import java.sql.Connection
//import java.sql.DriverManager
//import java.time.Duration
//import java.util.*
//import kotlin.time.Duration.Companion.seconds
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.launch
//import kotlinx.coroutines.runBlocking
//import kotlinx.html.*
//import org.jetbrains.exposed.sql.SchemaUtils
//import org.jetbrains.exposed.sql.transactions.transaction
//import org.slf4j.event.*
//
//fun Application.configureDatabases() {
//    val dbConnection: Connection = connectToPostgres(embedded = true)
//    val cityService = CityService(dbConnection)
//
//    routing {
//
//        // Create city
//        post("/cities") {
//            val city = call.receive<City>()
//            val id = cityService.create(city)
//            call.respond(HttpStatusCode.Created, id)
//        }
//
//        // Read city
//        get("/cities/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            try {
//                val city = cityService.read(id)
//                call.respond(HttpStatusCode.OK, city)
//            } catch (e: Exception) {
//                call.respond(HttpStatusCode.NotFound)
//            }
//        }
//
//        // Update city
//        put("/cities/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            val user = call.receive<City>()
//            cityService.update(id, user)
//            call.respond(HttpStatusCode.OK)
//        }
//
//        // Delete city
//        delete("/cities/{id}") {
//            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")
//            cityService.delete(id)
//            call.respond(HttpStatusCode.OK)
//        }
//    }
//    install(Kafka) {
//        schemaRegistryUrl = "my.schemaRegistryUrl"
//        val myTopic = TopicName.named("my-topic")
//        topic(myTopic) {
//            partitions = 1
//            replicas = 1
//            configs {
//                messageTimestampType = MessageTimestampType.CreateTime
//            }
//        }
//        common { // <-- Define common properties
//            bootstrapServers = listOf("my-kafka")
//            retries = 1
//            clientId = "my-client-id"
//        }
//        admin { } // <-- Creates an admin client
//        producer { // <-- Creates a producer
//            clientId = "my-client-id"
//        }
//        consumer { // <-- Creates a consumer
//            groupId = "my-group-id"
//            clientId = "my-client-id-override" //<-- Override common properties
//        }
//        consumerConfig {
//            consumerRecordHandler(myTopic) { record ->
//                // Do something with record
//            }
//        }
//        registerSchemas {
//            using { // <-- optionally provide a client, by default CIO is used
//                HttpClient()
//            }
//            // MyRecord::class at myTopic // <-- Will register schema upon startup
//        }
//    }
//}
//
///**
// * Makes a connection to a Postgres database.
// *
// * In order to connect to your running Postgres process,
// * please specify the following parameters in your configuration file:
// * - postgres.url -- Url of your running database process.
// * - postgres.user -- Username for database connection
// * - postgres.password -- Password for database connection
// *
// * If you don't have a database process running yet, you may need to [download]((https://www.postgresql.org/download/))
// * and install Postgres and follow the instructions [here](https://postgresapp.com/).
// * Then, you would be able to edit your url,  which is usually "jdbc:postgresql://host:port/database", as well as
// * user and password values.
// *
// *
// * @param embedded -- if [true] defaults to an embedded database for tests that runs locally in the same process.
// * In this case you don't have to provide any parameters in configuration file, and you don't have to run a process.
// *
// * @return [Connection] that represent connection to the database. Please, don't forget to close this connection when
// * your application shuts down by calling [Connection.close]
// * */
//fun Application.connectToPostgres(embedded: Boolean): Connection {
//    Class.forName("org.postgresql.Driver")
//    if (embedded) {
//        log.info("Using embedded H2 database for testing; replace this flag to use postgres")
//        return DriverManager.getConnection("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "root", "")
//    } else {
//        val url = environment.config.property("postgres.url").getString()
//        log.info("Connecting to postgres database at $url")
//        val user = environment.config.property("postgres.user").getString()
//        val password = environment.config.property("postgres.password").getString()
//
//        return DriverManager.getConnection(url, user, password)
//    }
//}
