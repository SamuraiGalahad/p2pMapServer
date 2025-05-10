package trotech.service.usecase.algorithm

import trotech.dto.entities.TrackerAnnouncePeerInfo

fun calculatePeerLoad(peerInfos: List<TrackerAnnouncePeerInfo>): Map<String, Int> {
    val loadMap = mutableMapOf<String, Int>()

    for (info in peerInfos) {
        for (connected in info.connectedPeers) {
            loadMap[connected] = loadMap.getOrDefault(connected, 0) + 1
        }
    }

    return loadMap
}

fun chooseBestPeersByLoad(
    peerInfos: List<TrackerAnnouncePeerInfo>,
    maxLoad: Int,
    count: Int
): List<TrackerAnnouncePeerInfo> {
    val loadMap = calculatePeerLoad(peerInfos)

    val underloaded = peerInfos.filter {
        (loadMap[it.peerId] ?: 0) < maxLoad
    }

    return if (underloaded.isNotEmpty()) {
        underloaded.sortedByDescending {
            0.6 * it.internetUploadSpeed + 0.4 * it.internetDownloadSpeed
        }.take(count)
    } else {
        // Если все перегружены — берём наименее загруженных
        peerInfos.sortedWith(
            compareBy(
                { loadMap[it.peerId] ?: Int.MAX_VALUE },
                { -(0.6 * it.internetUploadSpeed + 0.4 * it.internetDownloadSpeed) }
            )
        ).take(count)
    }
}