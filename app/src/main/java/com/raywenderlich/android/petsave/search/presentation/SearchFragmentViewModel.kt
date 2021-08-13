package com.raywenderlich.android.petsave.search.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.petsave.common.domain.model.NetworkException
import com.raywenderlich.android.petsave.common.domain.model.NetworkUnavailableException
import com.raywenderlich.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.common.utils.createExceptionHandler
import com.raywenderlich.android.petsave.search.domain.usecases.GetSearchFilters
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val getFilterValues: GetSearchFilters,
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

    private val _state = MutableLiveData<SearchViewState>()
    val state: LiveData<SearchViewState> get() = _state

    init {
        _state.value = SearchViewState()
    }

    fun onEvent(event: SearchEvent) {
        when(event) {
            is SearchEvent.PrepareForSearch -> prepareForSearch()
        }
    }

    private fun prepareForSearch() {
        loadFilterValues()
    }

    private fun loadFilterValues() {
        val exceptionHandler = createExceptionHandler("Error while getting filter values")

        viewModelScope.launch(exceptionHandler) {
            val (ages, types) = withContext(dispatchersProvider.io()) {
                getFilterValues()
            }
            updateStateWithFilterValues(ages, types)
        }
    }

    private fun updateStateWithFilterValues(ages: List<String>, types: List<String>) {
        _state.value = state.value!!.updateToReadyToSearch(
            ages, types
        )
    }

    private fun createExceptionHandler(message: String): CoroutineExceptionHandler {
        return viewModelScope
            .createExceptionHandler(message) {
                onFailure(it)
            }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }

    private fun onFailure(failure: Throwable) {
        when( failure ) {
            is NetworkException,
            is NetworkUnavailableException -> {
                _state.value = state.value!!.updateToHasFailure(
                    failure
                )
            }
        }
    }
}