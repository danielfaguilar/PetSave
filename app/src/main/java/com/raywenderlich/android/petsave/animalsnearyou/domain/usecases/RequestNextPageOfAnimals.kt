package com.raywenderlich.android.petsave.animalsnearyou.domain.usecases

import com.raywenderlich.android.petsave.common.domain.model.NoMoreAnimalsException
import com.raywenderlich.android.petsave.common.domain.model.pagination.Pagination
import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class RequestNextPageOfAnimals @Inject constructor(
    private val repository: AnimalRepository
) {
    suspend operator fun invoke(
        pageToLoad: Int,
        pageSize: Int = Pagination.DEFAULT_PAGE_SIZE
    ): Pagination{
        val (animals, pagination) = repository.requestMoreAnimals(pageToLoad, pageSize)

        if( animals.isEmpty() )
            throw NoMoreAnimalsException("No more animals nearby :c")

        // Store the animals
        repository.storeAnimals(animals)

        // Return the pagination
        return pagination
    }
}