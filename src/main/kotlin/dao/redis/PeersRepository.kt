package trotech.dao.redis

import redis.clients.jedis.Jedis
import java.util.*
import kotlinx.coroutines.*

class PeersRepository {
    var jedis : Jedis = Jedis("localhost", 6379)

    val cleanInactivePeersTask = Runnable {
        val currentTime = System.currentTimeMillis()
        if (jedis != null) {
            val peers = jedis.keys("peer:*:last_active")

            peers.forEach { peerKey ->
                val lastActiveTime = jedis.get(peerKey)?.toLong() ?: 0L
                if (currentTime - lastActiveTime > 120000) {
                    val peerId = peerKey.split(":")[1]
                    println("Удаление пира $peerId из-за отсутствия активности")
                    jedis.del(peerKey)
                }
            }
        }
    }

    fun addPeerId(peerID : UUID) {
        jedis?.set("peer:${peerID.toString()}:last_active", System.currentTimeMillis().toString())
    }

    fun getActivePeers(): List<String> {
        val activePeers = jedis.keys("peer:*:last_active")

        if (activePeers != null) {
            return activePeers.map { peer -> peer.split(":")[1] }
        }

        return listOf()
    }
}