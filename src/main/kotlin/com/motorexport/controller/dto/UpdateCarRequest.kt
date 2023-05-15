package com.motorexport.controller.dto

import java.util.UUID
import javax.validation.constraints.Max
import javax.validation.constraints.Positive

data class UpdateCarRequest(
    val carId: UUID,
    val make: String? = null,
    val model: String? = null,
    val engineGroup: EngineGroup? = null,
    val gearType: GearType? = null,
    val transmission: Transmission? = null,
    val bodyTypeGroup: BodyTypeGroup? = null,
    val inStock: InStock? = null,
    @field:Positive
    val year: Long? = null,
    @field:Positive
    val price: Long? = null,
    @field:Positive
    val mileage: Long? = null,
    @field:Positive
    val displacement: Int? = null,
    @field:Max(3)
    val country: String? = null,
    val secretKey: String,
    val carImagePathsToBeDeleted: List<String> = listOf(),
)


