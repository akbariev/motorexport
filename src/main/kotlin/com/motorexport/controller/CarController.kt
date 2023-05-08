package com.motorexport.controller

import com.motorexport.controller.dto.CarListRequest
import com.motorexport.controller.dto.CreateCarRequest
import com.motorexport.persistence.entity.CarEntity
import com.motorexport.service.CarService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/car")
class CarController(
    val carService: CarService,
) {
    @GetMapping("/")
    suspend fun getCar(@RequestParam("id") id: String): ResponseEntity<CarEntity> {
        return ResponseEntity.ok(carService.getCar(id))
    }

    @GetMapping("/list")
    suspend fun getCars(request: CarListRequest): ResponseEntity<List<CarEntity?>> {
        return ResponseEntity.ok(carService.getCars(request))
    }

    @PostMapping("/create")
    suspend fun createCar(request: CreateCarRequest) {
        carService.createCar(request)
    }

    @PostMapping("/delete")
    suspend fun deleteCar(@RequestParam("id") id: String) {
        ResponseEntity.ok(carService.deleteCar(id))
    }
}