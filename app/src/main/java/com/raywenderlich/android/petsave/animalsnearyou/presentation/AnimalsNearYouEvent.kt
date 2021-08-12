package com.raywenderlich.android.petsave.animalsnearyou.presentation

sealed class AnimalsNearYouEvent {
    object RequestInitialAnimalsList: AnimalsNearYouEvent()
    object RequestMoreAnimals: AnimalsNearYouEvent()
}