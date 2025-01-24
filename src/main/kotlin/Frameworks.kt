package trotech

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.*
import com.mongodb.kotlin.client.coroutine.MongoClient
import dev.inmo.krontab.builder.*
import io.github.damir.denis.tudor.ktor.server.rabbitmq.RabbitMQ
import io.github.damir.denis.tudor.ktor.server.rabbitmq.dsl.*
import io.github.damir.denis.tudor.ktor.server.rabbitmq.rabbitMQ
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

fun Application.configureFrameworks() {
    install(RabbitMQ) {
        uri = "amqp://<user>:<password>@<address>:<port>"
        defaultConnectionName = "default-connection"
        dispatcherThreadPollSize = 2
        tlsEnabled = false
    }

    rabbitmq {
        queueBind {
            queue = "dlq"
            exchange = "dlx"
            routingKey = "dlq-dlx"
            exchangeDeclare {
                exchange = "dlx"
                type = "direct"
            }
            queueDeclare {
                queue = "dlq"
                durable = true
            }
        }
    }

    rabbitmq {
        queueBind {
            queue = "test-queue"
            exchange = "test-exchange"
            routingKey = "test-routing-key"
            exchangeDeclare {
                exchange = "test-exchange"
                type = "direct"
            }
            queueDeclare {
                queue = "test-queue"
                arguments = mapOf(
                    "x-dead-letter-exchange" to "dlx",
                    "x-dead-letter-routing-key" to "dlq-dlx"
                )
            }
        }.onSuccess { response ->
            log.info("Successfully bind queue: $response")
        }.onFailure { error ->
            log.error("Failed to bind queue: $error")
        }
    }

    routing {
        get("/rabbitmq") {
            rabbitmq {
                basicPublish {
                    exchange = "test-exchange"
                    routingKey = "test-routing-key"
                    message { "Hello Ktor!" }
                }
            }
            call.respondText("Hello RabbitMQ!")
        }


        rabbitmq {
            basicConsume {
                autoAck = true
                queue = "test-queue"
                dispatcher = Dispatchers.rabbitMQ
                deliverCallback<String> { tag, message ->
                    log.debug("Received message: $message")
                }
            }
        }
    }
}
