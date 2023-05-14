package com.motorexport.controller.dto

import javax.validation.constraints.Max
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive

data class CreateCarRequest(
    @field:NotBlank
    val make: String,
    @field:NotBlank
    val model: String,
    val engineGroup: EngineGroup,
    val gearType: GearType,
    val transmission: Transmission,
    val bodyTypeGroup: BodyTypeGroup,
    val inStock: InStock = InStock.IN_STOCK,
    @field:Positive
    val year: Long,
    @field:Positive
    val price: Long,
    @field:Positive
    val mileage: Long,
    @field:Positive
    val displacement: Int,
    @field:Positive
    @field:Max(3)
    val country: String,
    val secretKey: String?,
)


