package com.motorexport.controller.dto

data class CreateCarRequest(
    val engineGroup: EngineGroup,
    val gearType: GearType,
    val transmission: Transmission,
    val bodyTypeGroup: BodyTypeGroup,
    val inStock: InStock,
    val year: Long,
    val price: Long,
    val mileage: Long,
    val displacement: Int,
    val country: String,
)


