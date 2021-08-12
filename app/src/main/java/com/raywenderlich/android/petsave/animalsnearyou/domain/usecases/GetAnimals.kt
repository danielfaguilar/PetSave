package com.raywenderlich.android.petsave.animalsnearyou.domain.usecases

import com.raywenderlich.android.petsave.common.domain.repositories.AnimalRepository
import javax.inject.Inject

class GetAnimals @Inject constructor(
    private val repository: AnimalRepository
) {
    operator fun invoke() =
        repository.getAnimals().filter { it.isNotEmpty() }
}