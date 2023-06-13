package com.motorexport.controller.dto

import com.fasterxml.jackson.annotation.JsonInclude
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Positive
import javax.validation.constraints.Size

@JsonInclude(JsonInclude.Include.NON_NULL)
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
    @Size(min = 2, message = "{validation.name.size.too_short}")
    @Size(max = 3, message = "{validation.name.size.too_long}")
    val country: String,
    @Deprecated("create manager table")
    val managerPhoneNumber: String,
    val secretKey: String?,
    //todo добавить hp
)


