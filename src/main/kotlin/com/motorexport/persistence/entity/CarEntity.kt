package com.motorexport.persistence.entity

import com.motorexport.controller.dto.BodyTypeGroup
import com.motorexport.controller.dto.EngineGroup
import com.motorexport.controller.dto.GearType
import com.motorexport.controller.dto.InStock
import com.motorexport.controller.dto.Transmission
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table

@Table("car")
data class CarEntity(
    @Id
    val id: UUID? = null,
    val engineGroup: EngineGroup,
    val gearType: GearType,
    val transmission: Transmission,
    val bodyTypeGroup: BodyTypeGroup,
    val inStock: InStock,
    val year: Long,
    val price: BigDecimal,
    val mileage: Long,
    val displacement: Int,
    val country: String,
    val make: String,
    val model: String,
    @CreatedDate
    val createdAt: Instant = Instant.now(),
    @LastModifiedDate
    val updatedAt: Instant = Instant.now(),
)