package service.usecase.stability

import dto.DownloadDistributionMap
import dto.ResponceImage
import dto.ResponceMatrix
import dto.entities.Peer
import trotech.dictForMap
import trotech.mapsInfo
import trotech.mapsPeersImagesMatrix
import java.util.*
import kotlin.collections.ArrayList

class NetworkBalancer {
    fun selectPeersByHash(fileHash: String): DownloadDistributionMap? {
        // получили пиров, которые владеют картой
        val peersId = dictForMap[fileHash]?.toList() ?: ArrayList<Peer>()

        if (peersId.isEmpty()) {
            return null
        }

        val mapInfo = mapsInfo[fileHash] ?: return null

        val result = DownloadDistributionMap(mapInfo.name, arrayListOf())

        val connectionCodes = mutableMapOf<UUID, String>()

        for (key in mapInfo.matrix.keys) {
            val rm = ResponceMatrix(key, arrayListOf())
            for (image in mapInfo.matrix[key]!!) {
                val localImageKey = "${key}:${image}"

                println(image)

                val peer = mapsPeersImagesMatrix.get(fileHash)?.get(localImageKey)?.toList()?.random()

                var connectionCode = UUID.randomUUID().toString()

                if (connectionCodes.containsKey(peer?.uuid)) {
                    connectionCode = connectionCodes.get(peer?.uuid).toString()
                } else {
                    if (peer != null) {
                        connectionCodes.put(peer.uuid, connectionCode)
                    }
                }


                val responceImage =
                    peer?.let {
                        ResponceImage(image.split(":")[0].toInt(), image.split(":")[1].toInt(), connectionCode,
                            it
                        )
                    }
                if (responceImage != null) {
                    rm.images.add(responceImage)
                }
            }
            result.loadMatrix.add(rm)
        }

        return result
    }
}