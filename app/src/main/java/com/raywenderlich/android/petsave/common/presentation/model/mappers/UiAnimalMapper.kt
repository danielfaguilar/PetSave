package com.raywenderlich.android.petsave.common.presentation.model.mappers

import com.raywenderlich.android.petsave.common.domain.model.animal.Animal
import com.raywenderlich.android.petsave.common.presentation.model.UIAnimal
import javax.inject.Inject

class UiAnimalMapper @Inject constructor(): UiMapper<Animal, UIAnimal> {
    override fun mapToView(input: Animal) =
        UIAnimal(
            input.id.value,
            input.name,
            input.media.getFirstSmallestAvailablePhoto()
        )
}