package com.motorexport.persistence

import com.motorexport.controller.dto.CarListRequest
import com.motorexport.persistence.entity.CarEntity
import com.motorexport.persistence.entity.CarResponseModel
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CarRepository : CoroutineCrudRepository<CarEntity, UUID> {

    @Query("""
        SELECT c.*, string_agg(ci.path, ',') as image_paths FROM car c LEFT JOIN car_image ci ON c.id = ci.car_id WHERE 
             (:#{#car.engineGroup} IS NULL OR c.engine_group = :#{#car.engineGroup})
        AND (:#{#car.gearType} IS NULL OR c.gear_type = :#{#car.gearType})
        AND (:#{#car.transmission} IS NULL OR c.transmission = :#{#car.transmission})
        AND (:#{#car.bodyTypeGroup} IS NULL OR c.body_type_group = :#{#car.bodyTypeGroup})
        AND (:#{#car.inStock} IS NULL OR c.in_stock = :#{#car.inStock})
        AND (:#{#car.country} IS NULL OR c.country = :#{#car.country})
        AND c.year >= :#{#car.yearFrom}
        AND c.year <= :#{#car.yearTo}
        AND c.price >= :#{#car.priceFrom}
        AND c.price <= :#{#car.priceTo}
        AND c.mileage >= :#{#car.mileageFrom}
        AND c.mileage <= :#{#car.mileageTo}
        AND c.displacement >= :#{#car.displacementFrom}
        AND c.displacement <= :#{#car.displacementTo}
        GROUP BY c.id;
        """
    )
    suspend fun findAllByFilter(@Param("car") carRequest: CarListRequest, page: PageRequest, by: Sort): List<CarResponseModel>

}