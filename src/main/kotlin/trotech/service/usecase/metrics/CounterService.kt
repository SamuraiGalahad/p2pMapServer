package trotech.service.usecase.metrics

import kotlinx.serialization.Serializable
import java.util.concurrent.atomic.AtomicInteger

@Serializable
data class Counters(
    val capabilities : Int,
    val ask : Int,
    val check : Int,
    val layer : Int,
    val tms : Int
)

/**
 * Счетчик для графика
 */

class CounterService {
    private val counterGetCapabilities = AtomicInteger(0)
    private val counterPeersAsk = AtomicInteger(0)
    private val counterPeersCheck = AtomicInteger(0)
    private val counterAnnounceLayer = AtomicInteger(0)
    private val counterAnnounceTms = AtomicInteger(0)

    fun addGetCapabilities() {
        counterGetCapabilities.incrementAndGet()
    }

    fun addPeersAsk() {
        counterPeersAsk.incrementAndGet()
    }

    fun addPeersCheck() {
        counterPeersCheck.incrementAndGet()
    }

    fun addAnnounceLayer() {
        counterAnnounceLayer.incrementAndGet()
    }

    fun addAnnounceTms() {
        counterAnnounceTms.incrementAndGet()
    }

    fun getAndZero() : Counters {
        return Counters(
            capabilities = counterGetCapabilities.getAndSet(0),
            ask = counterPeersAsk.getAndSet(0),
            check = counterPeersCheck.getAndSet(0),
            layer = counterAnnounceLayer.getAndSet(0),
            tms = counterAnnounceTms.getAndSet(0)
        )
    }
}