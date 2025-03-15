package dao

import dto.MapMeta

interface MapMetaRepository {
    fun getAll(): List<MapMeta>

    fun getById(id: Int): MapMeta?
}