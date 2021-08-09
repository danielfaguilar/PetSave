package com.raywenderlich.android.petsave.common.data

import com.raywenderlich.android.petsave.common.data.api.PetFinderApi
import com.raywenderlich.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.raywenderlich.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.raywenderlich.android.petsave.common.data.cache.Cache
import com.raywenderlich.android.petsave.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import com.raywenderlich.android.petsave.common.domain.model.animal.Animal
import com.raywenderlich.android.petsave.common.domain.model.animal.details.AnimalWithDetails
import com.raywenderlich.android.petsave.common.domain.model.pagination.PaginatedAnimals
import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import io.reactivex.Flowable

const val postcode = "07097"
const val maxDistance = 100f

class PetFinderAnimalRepository(
    private val api: PetFinderApi,
    private val cache: Cache,
    private val apiPaginationMapper: ApiPaginationMapper,
    private val apiAnimalMapper: ApiAnimalMapper
): AnimalRepository {
    override fun getAnimals(): Flowable<List<Animal>> {
        return cache.getNearbyAnimals().map { animalList ->
            animalList.map {
                it.animal.toAnimalDomain(
                    it.photos,
                    it.videos,
                    it.tags
                )
            }
        }
    }

    override suspend fun requestMoreAnimals(pageToLoad: Int, numberOfItems: Int): PaginatedAnimals {
        val (apiAnimals, apiPagination) = api.getNearbyAnimals(
            pageToLoad,
            numberOfItems,
            postcode,
            maxDistance
        )

        return PaginatedAnimals(
            apiAnimals?.map {
               apiAnimalMapper.mapToDomain(it)
            }.orEmpty(),
            apiPaginationMapper.mapToDomain(apiPagination)
        )
    }

    override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
        cache.storeNearbyAnimals(
            animals.map {
                CachedAnimalAggregate.fromDomain(it)
            }
        )
    }

}