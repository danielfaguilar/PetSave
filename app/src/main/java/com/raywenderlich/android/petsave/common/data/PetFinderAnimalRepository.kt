package com.raywenderlich.android.petsave.common.data

import com.raywenderlich.android.petsave.common.data.api.PetFinderApi
import com.raywenderlich.android.petsave.common.data.api.model.mappers.ApiAnimalMapper
import com.raywenderlich.android.petsave.common.data.api.model.mappers.ApiPaginationMapper
import com.raywenderlich.android.petsave.common.data.cache.Cache
import com.raywenderlich.android.petsave.common.data.cache.model.cachedanimal.CachedAnimalAggregate
import com.raywenderlich.android.petsave.common.data.cache.model.cachedorganization.CachedOrganization
import com.raywenderlich.android.petsave.common.domain.model.NetworkException
import com.raywenderlich.android.petsave.common.domain.model.animal.Animal
import com.raywenderlich.android.petsave.common.domain.model.animal.details.AnimalWithDetails
import com.raywenderlich.android.petsave.common.domain.model.pagination.PaginatedAnimals
import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import io.reactivex.Flowable
import retrofit2.HttpException
import javax.inject.Inject

const val postcode = "07097"
const val maxDistance = 100

class PetFinderAnimalRepository @Inject constructor(
    private val api: PetFinderApi,
    private val cache: Cache,
    private val apiAnimalMapper: ApiAnimalMapper,
    private val apiPaginationMapper: ApiPaginationMapper
): AnimalRepository {
    override fun getAnimals(): Flowable<List<Animal>> {
        return cache.getNearbyAnimals().distinctUntilChanged()
            .map { animalList ->
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
        try{
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
        } catch (exception: HttpException) {
            throw NetworkException(exception.message ?: "Code ${exception.code()}")
        }
    }

    override suspend fun storeAnimals(animals: List<AnimalWithDetails>) {
        // Organizations have a 1-to-many relation with animals, so we need to insert them first in
        // order for Room not to complain about foreign keys being invalid (since we have the
        // organizationId as a foreign key in the animals table)
        val organizations = animals.map { CachedOrganization.fromDomain(it.details.organization) }

        cache.storeOrganizations(organizations)
        cache.storeNearbyAnimals(animals.map { CachedAnimalAggregate.fromDomain(it) })
    }

}