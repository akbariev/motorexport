package com.motorexport.controller.dto

data class CarListRequest(
    val engineGroup: EngineGroup? = null,
    val gearType: GearType? = null,
    val transmission: Transmission? = null,
    val bodyTypeGroup: BodyTypeGroup? = null,
    val inStock: InStock? = null,
    val country: String? = null,
    val yearFrom: Int,
    val yearTo: Int,
    val priceFrom: Long,
    val priceTo: Long,
    val mileageFrom: Long,
    val mileageTo: Long,
    val displacementFrom: Long,
    val displacementTo: Long,
    val page: Int,
    val size: Int,
)