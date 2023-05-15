package com.motorexport.persistence

import com.motorexport.persistence.entity.CarImageEntity
import java.util.UUID
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface CarImagesRepository : CoroutineCrudRepository<CarImageEntity, UUID> {

    suspend fun findAllByCarId(carId: UUID): List<CarImageEntity>
    suspend fun deleteAllByPathIn(path: List<String>): Long
}