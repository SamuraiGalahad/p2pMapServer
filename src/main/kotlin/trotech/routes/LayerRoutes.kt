package trotech.routes


import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import trotech.dao.LayerRepository
import trotech.service.usecase.xml.Layer

fun Route.layerRoutes() {
    route("/layers") {
        get {
            call.respond(LayerRepository.getLayers())
        }

        post {
            val layer = call.receive<Layer>()
            LayerRepository.insertLayer(layer)
            call.respond(mapOf("status" to "Layer added"))
        }
    }
}