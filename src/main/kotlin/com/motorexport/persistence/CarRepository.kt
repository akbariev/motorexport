package com.motorexport.persistence

import com.motorexport.controller.dto.CarListRequest
import com.motorexport.persistence.entity.CarEntity
import com.motorexport.persistence.entity.CarModel
import java.util.UUID
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CarRepository : CoroutineCrudRepository<CarEntity, UUID> {

    @Query(
        """
        SELECT c.*, string_agg(ci.path, ',') as image_paths FROM car c LEFT JOIN car_image ci ON c.id = ci.car_id WHERE 
             (:#{#car.engineGroup} IS NULL OR c.engine_group = :#{#car.engineGroup})
        AND (:#{#car.gearType} IS NULL OR c.gear_type = :#{#car.gearType})
        AND (:#{#car.transmission} IS NULL OR c.transmission = :#{#car.transmission})
        AND (:#{#car.bodyTypeGroup} IS NULL OR c.body_type_group = :#{#car.bodyTypeGroup})
        AND (:#{#car.inStock} IS NULL OR c.in_stock = :#{#car.inStock})
        AND (:#{#car.country} IS NULL OR c.country = :#{#car.country})
        AND (:#{#car.yearFrom} IS NULL OR c.year >= :#{#car.yearFrom})
        AND (:#{#car.yearTo} IS NULL OR c.year <= :#{#car.yearTo})
        AND (:#{#car.priceFrom} IS NULL OR c.price >= :#{#car.priceFrom})
        AND (:#{#car.priceTo} IS NULL OR c.price <= :#{#car.priceTo})
        AND (:#{#car.mileageFrom} IS NULL OR c.mileage >= :#{#car.mileageFrom})
        AND (:#{#car.mileageTo} IS NULL OR c.mileage <= :#{#car.mileageTo})
        AND (:#{#car.displacementFrom} IS NULL OR c.displacement >= :#{#car.displacementFrom})
        AND (:#{#car.displacementTo} IS NULL OR c.displacement <= :#{#car.displacementTo})
        GROUP BY c.id
        OFFSET :#{#car.page}
        LIMIT :#{#car.size}
        """
    )
    /*ORDER BY c.updated_at desc, c.created_at desc*/
    suspend fun findAllByFilter(@Param("car") carRequest: CarListRequest): List<CarModel>

    @Query(
        """
        SELECT COUNT(*) FROM car c WHERE 
             (:#{#car.engineGroup} IS NULL OR c.engine_group = :#{#car.engineGroup})
        AND (:#{#car.gearType} IS NULL OR c.gear_type = :#{#car.gearType})
        AND (:#{#car.transmission} IS NULL OR c.transmission = :#{#car.transmission})
        AND (:#{#car.bodyTypeGroup} IS NULL OR c.body_type_group = :#{#car.bodyTypeGroup})
        AND (:#{#car.inStock} IS NULL OR c.in_stock = :#{#car.inStock})
        AND (:#{#car.country} IS NULL OR c.country = :#{#car.country})
        AND (:#{#car.yearFrom} IS NULL OR c.year >= :#{#car.yearFrom})
        AND (:#{#car.yearTo} IS NULL OR c.year <= :#{#car.yearTo})
        AND (:#{#car.priceFrom} IS NULL OR c.price >= :#{#car.priceFrom})
        AND (:#{#car.priceTo} IS NULL OR c.price <= :#{#car.priceTo})
        AND (:#{#car.mileageFrom} IS NULL OR c.mileage >= :#{#car.mileageFrom})
        AND (:#{#car.mileageTo} IS NULL OR c.mileage <= :#{#car.mileageTo})
        AND (:#{#car.make} IS NULL OR c.make = :#{#car.make})
        AND (:#{car.model} IS NULL OR c.model = :#{#car.model})
        AND (:#{#car.displacementFrom} IS NULL OR c.displacement >= :#{#car.displacementFrom})
        AND (:#{#car.displacementTo} IS NULL OR c.displacement <= :#{#car.displacementTo})
        """
    )
    suspend fun countAllByFilter(@Param("car") carRequest: CarListRequest): Long

}