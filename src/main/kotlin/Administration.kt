package trotech

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.*
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.inmo.krontab.builder.*
//import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
//import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
//import io.github.damir.denis.tudor.ktor.server.rabbitmq.rabbitMQ
import io.github.flaxoos.ktor.server.plugins.kafka.Kafka
import io.github.flaxoos.ktor.server.plugins.kafka.MessageTimestampType
import io.github.flaxoos.ktor.server.plugins.kafka.TopicName
import io.github.flaxoos.ktor.server.plugins.kafka.admin
import io.github.flaxoos.ktor.server.plugins.kafka.common
import io.github.flaxoos.ktor.server.plugins.kafka.consumer
import io.github.flaxoos.ktor.server.plugins.kafka.consumerConfig
import io.github.flaxoos.ktor.server.plugins.kafka.consumerRecordHandler
import io.github.flaxoos.ktor.server.plugins.kafka.producer
import io.github.flaxoos.ktor.server.plugins.kafka.registerSchemas
import io.github.flaxoos.ktor.server.plugins.kafka.topic
import io.github.flaxoos.ktor.server.plugins.ratelimiter.*
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.*
import io.github.flaxoos.ktor.server.plugins.taskscheduling.*
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.database.*
import io.github.flaxoos.ktor.server.plugins.taskscheduling.managers.lock.redis.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.http.*
import io.ktor.network.selector.*
import io.ktor.network.sockets.*
import io.ktor.network.tls.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.html.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.openapi.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.sse.*
import io.ktor.server.websocket.*
import io.ktor.sse.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.*
import io.ktor.websocket.*
import java.io.InputStream
import java.sql.Connection
import java.sql.DriverManager
import java.time.Duration
import java.util.*
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.*

fun Application.configureAdministration() {
    routing {
        route("/") {
            install(RateLimiting) {
                rateLimiter {
                    type = TokenBucket::class
                    capacity = 100
                    rate = 10.seconds
                }
            }
        }
    }
    install(TaskScheduling) {
        // Choose task manager config based on your chosen task manager dependencies
        redis { // <-- given no name, this will be the default manager
            connectionPoolInitialSize = 1
            host = "host"
            port = 6379
            username = "my_username"
            password = "my_password"
            connectionAcquisitionTimeoutMs = 1_000
            lockExpirationMs = 60_000
        }
        jdbc("my jdbc manager") { // <-- given a name, a manager can be explicitly selected for a task
            database = org.jetbrains.exposed.sql.Database.connect(
                url = "jdbc:postgresql://host:port",
                driver = "org.postgresql.Driver",
                user = "my_username",
                password = "my_password"
            ).also {
                transaction { SchemaUtils.create(DefaultTaskLockTable) }
            }
        }
        mongoDb("my mongodb manager") {
            databaseName = "test"
            client = MongoClient.create("mongodb://host:port")
        }

        task { // if no taskManagerName is provided, the task would be assigned to the default manager
            name = "My task"
            task = { taskExecutionTime ->
                log.info("My task is running: $taskExecutionTime")
            }
            kronSchedule = {
                hours {
                    from(0).every(12)
                }
                minutes {
                    from(10).every(30)
                }
            }
            concurrency = 2
        }

        task(taskManagerName = "my jdbc manager") {
            name = "My Jdbc task"
            // rest of task config
        }
    }
}
