package com.raywenderlich.android.petsave.search.domain.usecases

import com.raywenderlich.android.petsave.common.domain.model.animal.details.Age
import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import com.raywenderlich.android.petsave.search.domain.model.SearchFilters
import java.util.*
import javax.inject.Inject

class GetSearchFilters @Inject constructor(
    private val repository: AnimalRepository
) {
    companion object {
        const val NO_FILTER_SELECTED = "Any"
    }

    suspend operator fun invoke(): SearchFilters {
        val unknownAge = Age.UNKNOWN.name

        val types = listOf(NO_FILTER_SELECTED) + repository.getAnimalTypes()

        val ages = repository.getAnimalAges()
            .map { age ->
                if(age.name == unknownAge)
                    NO_FILTER_SELECTED
                else
                    age.name.lowercase(Locale.ROOT).replaceFirstChar { age.name.first().uppercase() }
            }

        return SearchFilters(ages, types)
    }
}