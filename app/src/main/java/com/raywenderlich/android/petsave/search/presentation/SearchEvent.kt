package com.raywenderlich.android.petsave.search.presentation

sealed class SearchEvent {
    object PrepareForSearch: SearchEvent()
    data class QueryInput(val input: String): SearchEvent()
    data class AgeValueSelected(val age: String): SearchEvent()
    data class TypeValueSelected(val type: String): SearchEvent()
}