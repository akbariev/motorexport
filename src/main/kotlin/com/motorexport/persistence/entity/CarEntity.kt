package com.motorexport.persistence.entity

import com.motorexport.controller.dto.BodyTypeGroup
import com.motorexport.controller.dto.EngineGroup
import com.motorexport.controller.dto.GearType
import com.motorexport.controller.dto.InStock
import com.motorexport.controller.dto.Transmission
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID
import org.springframework.data.annotation.Id
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
    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
)

/*
enum class EngineGroup() {
    DIESEL,
    GASOLINE,
    TURBO,
    ATMO,
    LPG,
}

enum class GearType() {
    ALL_WHEEL_DRIVE,
    FORWARD_CONTROL
}

enum class Transmission() {
    AUTOMATIC,
    ROBOT,
    VARIATOR,
    MECHANICAL,
}

enum class BodyTypeGroup() {
    SEDAN,
    WAGON,
    CABRIO,
}

enum class InStock() {
    IN_STOCK,
    ON_ORDER,
    IN_TRANSIT,
    SOLD
}*/
