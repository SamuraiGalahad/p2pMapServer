package trotech.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.inject
import trotech.dao.redis.PeersRepository
import trotech.dto.GraphResponse
import trotech.service.usecase.administration.reloadDockerContainers
import trotech.service.usecase.algorithm.Graph
import trotech.service.usecase.metrics.CounterService


fun Route.adminRoute() {
    val repository = PeersRepository()
    val counter by application.inject<CounterService>()
    val graph by application.inject<Graph>()

    route("/avt") {
        post("/login") {
            call.respondText("NTNv7j0TuYARvmNMmWXo6fKvM4o6nv/aUi9ryX38ZH+L1bkrnD1ObOQ8JAUmHCBq7Iy7otZcyAagBLHVKvvYaIpmMuxmARQ97jUVG16Jkpkp1wXOPsrF9zwew6TpczyHkHgX5EuLg2MeBuiT/qJACs1J0apruOOJCg/gOtkjB4c=")
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
        get("/graphMetrics") {
            call.respond(graph.getMetrics())
        }
        get("/active") {
            call.respond(PeersRepository().getActivePeers())
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