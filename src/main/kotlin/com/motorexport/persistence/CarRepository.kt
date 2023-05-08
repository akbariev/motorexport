package com.motorexport.persistence

import com.motorexport.controller.dto.CarListRequest
import com.motorexport.persistence.entity.CarEntity
import java.util.UUID
import org.springframework.data.domain.PageRequest
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CarRepository : CoroutineCrudRepository<CarEntity, UUID> {

    @Query("""
        SELECT * FROM car c  WHERE 
        c.engine_group = 'DIESEL' 
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
        """
    )
    /* AND
        c.gear_type  LIKE '%:#{#car.gearType}%' AND
        c.transmission LIKE '%:#{#car.transmission}%' AND
        c.body_type_group LIKE '%:#{#car.bodyTypeGroup}%' AND
        c.in_stock LIKE '%:#{#car.inStock}%' AND
        c.country LIKE '%:#{#car.country}%' AND
        c.year >= :#{#car.yearFrom} AND
        c.year <= :#{#car.yearTo} AND
        c.price >= :#{#car.priceFrom} AND
        c.price <= :#{#car.priceTo} AND
        c.mileage >= :#{#car.mileageFrom} AND
        c.mileage <= :#{#car.mileageTo} AND
        c.displacement >= :#{#car.displacementFrom} AND
        c.displacement <= :#{#car.displacementTo}
        */
    suspend fun findAllByFilter(@Param("car") carRequest: CarListRequest): List<CarEntity?>?

}