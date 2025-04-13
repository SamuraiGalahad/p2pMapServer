package trotech.dto

import trotech.dto.entities.Peer
import kotlinx.serialization.Serializable

@Serializable
data class MapInfo(val name: String,
                   val peers: MutableList<Peer>,
                   val matrix: MutableMap<Int, MutableSet<String>> = mutableMapOf<Int, MutableSet<String>>()) {
    fun addImageToMatrix(image : Image, level : Int) {
        if (matrix.containsKey(level)) {
            matrix[level]?.add("${image.col}:${image.row}")
        } else {
            matrix[level] = mutableSetOf<String>()
            matrix[level]?.add("${image.col}:${image.row}")
        }
    }
}
