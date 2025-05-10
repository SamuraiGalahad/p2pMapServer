package trotech.dao.redis

import redis.clients.jedis.Jedis
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import trotech.dto.entities.TrackerAnnouncePeerInfo

class PeersRepository {
    var jedis : Jedis = Jedis("localhost", 6379)
    val json = Json { encodeDefaults = true }

    val cleanInactivePeersTask = Runnable {
        val currentTime = System.currentTimeMillis()
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

    fun addPeerId(peerID : String) {
        try {
            jedis.set("peer:${peerID}:last_active", System.currentTimeMillis().toString())
        } catch (er : Exception) {
            jedis.close()
            jedis  = Jedis("localhost", 6379)
        }
    }

    fun getActivePeers(): List<String> {
        try {
            val activePeers = jedis.keys("peer:*:last_active")

            if (activePeers != null) {
                return activePeers.map { peer -> peer.split(":")[1] }
            }

            return listOf()
        } catch (_ : Exception) {
            jedis.close()
            jedis  = Jedis("localhost", 6379)
        }
        return listOf()
    }

    fun saveMetaInfo(info: TrackerAnnouncePeerInfo) {
        try {
            val fullInfoKey = "peerinfo:${info.peerId}"
            jedis.set(fullInfoKey, Json.encodeToString(TrackerAnnouncePeerInfo.serializer(), info))
        } catch (_ : Exception) {
            jedis.close()
            jedis  = Jedis("localhost", 6379)
        }
    }

    fun getFullPeerInfo(peerId: String): TrackerAnnouncePeerInfo? {
        try {
            val jsonData = jedis.get("peerinfo:$peerId") ?: return null
            return json.decodeFromString(jsonData)
        } catch (_ : Exception) {
            jedis.close()
            jedis  = Jedis("localhost", 6379)
        }
        return null
    }

    fun getAverageDownloadSpeed() : Long {
        try {
            val infos = getAllPeerInfos()

            if (infos.isEmpty()) {
                return 0
            }
            return (infos.map { it.internetDownloadSpeed }.sum().div(infos.size))
        } catch (ex : Exception) {
            jedis.close()
            jedis  = Jedis("localhost", 6379)
        }
        return 0
    }

    fun getAverageUploadSpeed() : Long {
        try {
            val infos = getAllPeerInfos()

            if (infos.isEmpty()) {
                return 0
            }
            return (infos.map { it.internetUploadSpeed }.sum().div(infos.size))
        } catch (ex : Exception) {
            jedis.close()
            jedis  = Jedis("localhost", 6379)
        }
        return 0
    }


    fun getAllPeerInfos(): List<TrackerAnnouncePeerInfo> {
        try {
            return getActivePeers().mapNotNull { peerId ->
                getFullPeerInfo(peerId)
            }
        } catch (ex : Exception) {
            jedis.close()
            jedis  = Jedis("localhost", 6379)
        }
        return  listOf()
    }
}