package com.motorexport.persistence.entity

import java.util.UUID
import org.springframework.data.relational.core.mapping.Table

@Table("car_image")
data class CarImageEntity(
    val id: UUID,
    val path: String,
    val carId: UUID,
)