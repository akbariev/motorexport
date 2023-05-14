package com.motorexport.persistence.entity

data class CarsResponse(
    val carModels: List<CarModel>,
    val totalPages: Long,
)