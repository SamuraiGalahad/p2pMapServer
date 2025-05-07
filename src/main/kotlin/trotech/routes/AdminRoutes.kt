package trotech.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import trotech.dao.redis.PeersRepository
import trotech.service.usecase.administration.reloadDockerContainers
import trotech.service.usecase.metrics.CounterService


fun Route.adminRoute() {
    val repository = PeersRepository()
    val counter by application.inject<CounterService>()

    route("/auth") {
        post("/login") {

        }
    }

    route("/status") {
        get("/ping") {
            call.respond(HttpStatusCode.OK)
        }
    }

    route("/charts") {
        get("/counters") {
            call.respond(counter.getAndZero())
        }
        get("/activePeers") {
            call.respond(repository.getActivePeers())
        }
    }

    route("/server") {
        post("/stop") {
            print("stop")
            application.environment.monitor.raise(ApplicationStopPreparing, application.environment)
            call.respond(HttpStatusCode.OK)
        }
        post("/reload") {
            reloadDockerContainers()
            call.respondText("Docker containers restarted!")
        }
        post("/algorithm") {

        }
    }
}