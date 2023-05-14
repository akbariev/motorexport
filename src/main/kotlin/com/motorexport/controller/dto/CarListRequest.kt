package com.motorexport.controller.dto

import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.validation.constraints.Positive
import javax.validation.constraints.PositiveOrZero

data class CarListRequest(
    val engineGroup: EngineGroup? = null,
    val gearType: GearType? = null,
    val transmission: Transmission? = null,
    val bodyTypeGroup: BodyTypeGroup? = null,
    val inStock: InStock? = null,
    @field:Max(value = 3)
    val country: String? = null,
    @field:Min(value = 1890)
    val yearFrom: Long? = null,
    @field:Min(value = 1890)
    val yearTo: Int? = null,
    @field:Positive
    val priceFrom: Long? = null,
    @field:Positive
    val priceTo: Long? = null,
    @field:PositiveOrZero
    val mileageFrom: Long? = null,
    @field:Max(99999999)
    val mileageTo: Long? = null,
    val displacementFrom: Long? = null,
    val displacementTo: Long? = null,
    @field:PositiveOrZero
    val page: Int,
    @field:Positive
    val size: Int,
)