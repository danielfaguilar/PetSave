package com.raywenderlich.android.petsave.search.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.raywenderlich.android.petsave.common.domain.model.NetworkException
import com.raywenderlich.android.petsave.common.domain.model.NetworkUnavailableException
import com.raywenderlich.android.petsave.common.domain.model.NoMoreAnimalsException
import com.raywenderlich.android.petsave.common.domain.model.animal.Animal
import com.raywenderlich.android.petsave.common.domain.model.pagination.Pagination
import com.raywenderlich.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.common.utils.createExceptionHandler
import com.raywenderlich.android.petsave.search.domain.model.SearchParameters
import com.raywenderlich.android.petsave.search.domain.model.SearchResults
import com.raywenderlich.android.petsave.search.domain.usecases.GetSearchFilters
import com.raywenderlich.android.petsave.search.domain.usecases.SearchAnimals
import com.raywenderlich.android.petsave.search.domain.usecases.SearchAnimalsRemotely
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.CancellationException
import javax.inject.Inject

@HiltViewModel
class SearchFragmentViewModel @Inject constructor(
    private val searchAnimals: SearchAnimals,
    private val getFilterValues: GetSearchFilters,
    private val searchAnimalsRemotely: SearchAnimalsRemotely,
    private val uiAnimalMapper: UiAnimalMapper,
    private val dispatchersProvider: DispatchersProvider,
    private val compositeDisposable: CompositeDisposable
): ViewModel() {

    private val _state = MutableLiveData<SearchViewState>()
    val state: LiveData<SearchViewState> get() = _state

    private val querySubject = BehaviorSubject.create<String>()
    private val ageSubject = BehaviorSubject.createDefault("")
    private val typeSubject = BehaviorSubject.createDefault("")

    private var remoteSearchJob: Job = Job()
    var currentPage = 0

    init {
        _state.value = SearchViewState()
    }

    fun onEvent(event: SearchEvent) {
        when(event) {
            is SearchEvent.PrepareForSearch -> prepareForSearch()
            else -> onSearchParametersUpdate(event)
        }
    }

    private fun onSearchParametersUpdate(event: SearchEvent) {
        remoteSearchJob.cancel(
            CancellationException("New search parameters incoming")
        )
        when(event) {
            is SearchEvent.QueryInput -> updateQuery(event.input)
            is SearchEvent.AgeValueSelected -> updateAgeValue(event.age)
            is SearchEvent.TypeValueSelected -> updateTypeValue(event.type)
        }
    }

    private fun updateQuery(query: String) {
        resetPagination()

        querySubject.onNext(query)

        if(query.isEmpty())
            setNoSearchQueryState()
        else
            setSearchingState()
    }

    private fun setNoSearchQueryState() {
        _state.value = state.value!!.updateToNoSearchQuery()
    }

    private fun setSearchingState() {
        _state.value = state.value!!.updateToSearching()
    }

    private fun resetPagination() {
        currentPage = 0
    }

    private fun updateAgeValue(age: String) {
        ageSubject.onNext(age)
    }

    private fun updateTypeValue(type: String) {
        typeSubject.onNext(type)
    }

    private fun prepareForSearch() {
        loadFilterValues()
        setupSearchSubscription()
    }

    private fun setupSearchSubscription() {
        searchAnimals(querySubject, ageSubject, typeSubject)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { onSearchResult(it) },
                { onFailure(it) }
            ).addTo(compositeDisposable)
    }

    private fun onSearchResult(searchResults: SearchResults) {
        val (animals, searchParameters) = searchResults

        if(animals.isEmpty()) {
            onEmptyCacheResults(searchParameters)
        } else
            onAnimalList(animals)
    }

    private fun onEmptyCacheResults(searchParameters: SearchParameters) {
        _state.value = state.value!!.updateToSearchingRemotely()
        searchRemotely(searchParameters)
    }

    private fun searchRemotely(searchParameters: SearchParameters) {
        val exceptionHandler = createExceptionHandler("Failed to search remotely.")

        remoteSearchJob = viewModelScope.launch(exceptionHandler) {
            val pagination = withContext(dispatchersProvider.io()) {
                searchAnimalsRemotely(++currentPage, searchParameters)
            }

            onPaginationInfoObtained(pagination)
        }

        remoteSearchJob.invokeOnCompletion { /*it?.printStackTrace()*/ }
    }

    private fun onPaginationInfoObtained(pagination: Pagination) {
        currentPage = pagination.currentPage
    }

    private fun onAnimalList(animals: List<Animal>) {
        _state.value = state.value!!.updateToHasSearchResults(
            animals.map { uiAnimalMapper.mapToView(it) }
        )
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

    private fun onFailure(throwable: Throwable) {
        _state.value = if (throwable is NoMoreAnimalsException) {
            state.value!!.updateToNoResultsAvailable()
        } else {
            state.value!!.updateToHasFailure(throwable)
        }
    }
}