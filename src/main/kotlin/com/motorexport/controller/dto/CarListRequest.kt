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
    @Min(value = 1890)
    val country: String? = null,
    @Min(value = 1890)
    val yearFrom: Long? = null,
    @Min(value = 1890)
    val yearTo: Int? = null,
    @Positive
    val priceFrom: Long? = null,
    @Positive
    val priceTo: Long? = null,
    @PositiveOrZero
    val mileageFrom: Long? = null,
    @Max(99999999)
    val mileageTo: Long? = null,
    val displacementFrom: Long? = null,
    val displacementTo: Long? = null,
    @PositiveOrZero
    val page: Int,
    @Positive
    val size: Int,
)