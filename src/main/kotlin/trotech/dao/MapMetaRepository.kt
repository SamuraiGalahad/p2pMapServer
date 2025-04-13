package trotech.dao

import trotech.dto.MapMeta

interface MapMetaRepository {
    fun getAll(): List<MapMeta>

    fun getById(id: Int): MapMeta?
}