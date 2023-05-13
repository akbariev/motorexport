package com.motorexport.persistence.entity

import com.motorexport.controller.dto.BodyTypeGroup
import com.motorexport.controller.dto.EngineGroup
import com.motorexport.controller.dto.GearType
import com.motorexport.controller.dto.InStock
import com.motorexport.controller.dto.Transmission
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

data class CarResponseModel(
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
    val imagePaths: List<String>? = null,
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

