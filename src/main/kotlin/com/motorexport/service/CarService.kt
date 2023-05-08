package com.motorexport.service

import com.motorexport.controller.dto.CarListRequest
import com.motorexport.controller.dto.CreateCarRequest
import com.motorexport.persistence.CarRepository
import com.motorexport.persistence.entity.CarEntity
import java.math.BigDecimal
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class CarService(
    val carRepository: CarRepository,
) {

    suspend fun getCar(id: String): CarEntity? {
        return carRepository.findById(UUID.fromString(id))
    }

    suspend fun getCars(carRequest: CarListRequest): List<CarEntity?>? {
        val page = PageRequest.of(carRequest.page, carRequest.size)
        return carRepository.findAllByFilter(carRequest)
    }

    suspend fun createCar(request: CreateCarRequest) {
        val entity = CarEntity(
            engineGroup = request.engineGroup,
            gearType = request.gearType,
            transmission = request.transmission,
            bodyTypeGroup = request.bodyTypeGroup,
            inStock = request.inStock,
            year = request.year,
            price = BigDecimal(request.price),
            mileage = request.mileage,
            displacement = request.displacement,
            country = request.country,

            )
        carRepository.save(entity)
    }

    suspend fun deleteCar(id: String) {
        carRepository.deleteById(UUID.fromString(id))
    }
}
