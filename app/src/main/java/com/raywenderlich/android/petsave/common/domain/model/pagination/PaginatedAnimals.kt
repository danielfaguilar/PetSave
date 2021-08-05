package com.raywenderlich.android.petsave.common.domain.model.pagination

import com.raywenderlich.android.petsave.common.domain.model.animal.details.AnimalWithDetails

class PaginatedAnimals(
    val animals: List<AnimalWithDetails>,
    val pagination: Pagination
)