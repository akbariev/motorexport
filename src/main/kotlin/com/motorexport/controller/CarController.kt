package com.motorexport.controller

import com.motorexport.controller.dto.CarListRequest
import com.motorexport.controller.dto.CreateCarRequest
import com.motorexport.controller.dto.UpdateCarRequest
import com.motorexport.persistence.entity.CarModel
import com.motorexport.persistence.entity.CarsResponse
import com.motorexport.service.CarService
import java.util.UUID
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.codec.multipart.FilePart
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("/api/v1/car")
@CrossOrigin(origins = ["*"])
class CarController(
    val carService: CarService,
) {
    @GetMapping("/")
    suspend fun getCar(@RequestParam("id") @NotBlank id: String): ResponseEntity<CarModel?> {
        return ResponseEntity.ok(carService.getCar(id))
    }

    @GetMapping("/list")
    suspend fun getCars(@Valid @ModelAttribute request: CarListRequest): ResponseEntity<CarsResponse> {
        return ResponseEntity.ok(carService.getCars(request))
    }

    @PostMapping(
        "/create",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun createCar(
        @ModelAttribute @Valid request: CreateCarRequest,
        // todo необходимо переделать на flow. но приходит ошибка
        //  org.springframework.web.server.UnsupportedMediaTypeStatusException: 415 UNSUPPORTED_MEDIA_TYPE "Content type 'image/png' not supported for bodyType=org.springframework.http.codec.multipart.FilePart", message is = 415 UNSUPPORTED_MEDIA_TYPE "Content type 'image/png' not supported for bodyType=org.springframework.http.codec.multipart.FilePart"
        //  По хорошему узнать больше за реактивное программирование + корутины
        @RequestPart("files") files: Flux<FilePart>,
    ): ResponseEntity<UUID> {
        return ResponseEntity.ok(carService.createCar(request, files))
    }

    @PostMapping(
        "/update",
        consumes = [MediaType.MULTIPART_FORM_DATA_VALUE],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    suspend fun updateCar(
        @ModelAttribute @Valid request: UpdateCarRequest,
        @RequestPart(value = "files", required = false) files: Flux<FilePart>?,
    ): ResponseEntity<UUID> {
        return ResponseEntity.ok(carService.updateCar(request, files))
    }

    @PostMapping("/delete")
    suspend fun deleteCar(@RequestParam("id") @NotBlank id: String) {
        ResponseEntity.ok(carService.deleteCar(id))
    }
}