package com.motorexport.service

import com.motorexport.controller.dto.CarListRequest
import com.motorexport.controller.dto.CreateCarRequest
import com.motorexport.controller.dto.UpdateCarRequest
import com.motorexport.persistence.CarImagesRepository
import com.motorexport.persistence.CarRepository
import com.motorexport.persistence.entity.CarEntity
import com.motorexport.persistence.entity.CarImageEntity
import com.motorexport.persistence.entity.CarModel
import com.motorexport.persistence.entity.CarsResponse
import java.math.BigDecimal
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.attribute.PosixFilePermission
import java.util.UUID
import kotlin.math.ceil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.awaitFirstOrNull
import kotlinx.coroutines.withContext
import org.springframework.http.codec.multipart.FilePart
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux

private const val MAX_ALLOWED_IMAGES_SIZE = 5

@Service
class CarService(
    val carRepository: CarRepository,
    val carImagesRepository: CarImagesRepository
) {
    companion object {
        const val IMAGE_FOLDER_PATH = "images/" //creates folder on src level
    }

    @Transactional(readOnly = true)
    suspend fun getCar(id: String): CarModel? {
        return carRepository.findById(UUID.fromString(id))?.let {
            it.id ?: error("not car id present")
            val carImages = carImagesRepository.findAllByCarId(it.id)
            val images = mutableListOf<String>()
            carImages.forEach { image -> images.add(image.path) }
            CarModel(
                id = it.id,
                engineGroup = it.engineGroup,
                gearType = it.gearType,
                transmission = it.transmission,
                bodyTypeGroup = it.bodyTypeGroup,
                inStock = it.inStock,
                year = it.year,
                price = it.price,
                mileage = it.mileage,
                displacement = it.displacement,
                country = it.country,
                imagePaths = images,
                make = it.make,
                model = it.model,
                createdAt = it.createdAt,
                updatedAt = it.updatedAt,
            )
        }
    }

    @Transactional(readOnly = true)
    suspend fun getCars(carRequest: CarListRequest): CarsResponse {
        val cars = carRepository.findAllByFilter(carRequest)
        val carsSize = carRepository.countAllByFilter(carRequest)
        val totalPages = if (carsSize > 0) ceil(carsSize / carRequest.size.toDouble()).toLong() else 0
        return CarsResponse(cars, totalPages)
    }

    @Transactional
    suspend fun createCar(request: CreateCarRequest, images: Flux<FilePart>): UUID {
        if (request.secretKey != "export2023") error("Incorrect secret")
        val entity = CarEntity(
            engineGroup = request.engineGroup,
            gearType = request.gearType,
            transmission = request.transmission,
            bodyTypeGroup = request.bodyTypeGroup,
            inStock = request.inStock,
            year = request.year,
            price = BigDecimal(request.price),
            mileage = request.mileage,
            displacement = request.displacement,
            country = request.country,
            make = request.make,
            model = request.model,
        )
        val carId = carRepository.save(entity).id ?: error("no car id present")

        val carImagesItems = mutableListOf<CarImageEntity>()
        // Collect the files and save them to the directory
        images.map { filePart ->
            val imageId = UUID.randomUUID()
            val carImagesFolder = IMAGE_FOLDER_PATH + carId
            val imagePath = "$carImagesFolder/$imageId.jpg"
            val file = Paths.get(imagePath)

            // Write the file to disk
            GlobalScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    Files.createDirectories(Paths.get(carImagesFolder))
                    filePart.transferTo(file).awaitFirstOrNull()
                    // Set file permissions to allow reading
                    Files.setPosixFilePermissions(file, setOf(PosixFilePermission.OTHERS_READ))
                }
            }
            carImagesItems.add(CarImageEntity(id = imageId, path = imagePath, carId = carId))
        }.collectList().awaitFirstOrNull()

        carImagesRepository.saveAll(carImagesItems).collect()
        return carId
    }


    @Transactional
    suspend fun updateCar(request: UpdateCarRequest, images: Flux<FilePart>?): UUID {
        if (request.secretKey != "export2023") error("Incorrect secret")
        val carEntity = carRepository.findById(request.carId) ?: error("no car present by id ${request.carId}")
        val updatedCarEntity = carEntity.copy(
            engineGroup = request.engineGroup ?: carEntity.engineGroup,
            gearType = request.gearType ?: carEntity.gearType,
            transmission = request.transmission ?: carEntity.transmission,
            bodyTypeGroup = request.bodyTypeGroup ?: carEntity.bodyTypeGroup,
            inStock = request.inStock ?: carEntity.inStock,
            year = request.year ?: carEntity.year,
            price = request.price?.toBigDecimal() ?: carEntity.price,
            mileage = request.mileage ?: carEntity.mileage,
            displacement = request.displacement ?: carEntity.displacement,
            country = request.country ?: carEntity.country,
            make = request.make ?: carEntity.make,
            model = request.model ?: carEntity.model,
        )

        val carId = carRepository.save(updatedCarEntity).id ?: error("no car id present")

        val carImageIdsByEntities = carImagesRepository.findAllByCarId(request.carId).associateBy { it.path }

        val finalCarImagePathsToBeDeleted = mutableListOf<String>()
        request.carImagePathsToBeDeleted.forEach {
            if (carImageIdsByEntities.contains(it)) {
                finalCarImagePathsToBeDeleted.add(it)
            }
        }
        finalCarImagePathsToBeDeleted.takeIf { it.size > 0 }
            ?.let { carImagesRepository.deleteAllByPathIn(finalCarImagePathsToBeDeleted) }
        val allowedMaxCarImagesSize =
            MAX_ALLOWED_IMAGES_SIZE - (carImageIdsByEntities.size - finalCarImagePathsToBeDeleted.size)

        // и сверяем с новым числом картинок, берем то количество картинок, чтобы в сумме не привышало 5 картинок.

        // сохраняем новые картинки.
        val carImagesItems = mutableListOf<CarImageEntity>()
        // Collect the files and save them to the directory
        images?.take(allowedMaxCarImagesSize.toLong())?.map { filePart ->
            val imageId = UUID.randomUUID()
            val imagePath = "$IMAGE_FOLDER_PATH/$imageId.jpeg"
            val file = Paths.get(imagePath)

            // Write the file to disk
            GlobalScope.launch(Dispatchers.IO) {
                withContext(Dispatchers.IO) {
                    filePart.transferTo(file).awaitFirstOrNull()
                }
            }
            carImagesItems.add(CarImageEntity(id = imageId, path = imagePath, carId = carId))
        }?.collectList()?.awaitFirstOrNull()?.also {
            carImagesRepository.saveAll(carImagesItems).collect()
        }
        return carId
    }

    suspend fun deleteCar(id: String) {
        carRepository.deleteById(UUID.fromString(id))
    }
}
