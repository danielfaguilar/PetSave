package com.raywenderlich.android.petsave.search.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.raywenderlich.android.petsave.RxImmediateSchedulerRule
import com.raywenderlich.android.petsave.TestCoroutineRule
import com.raywenderlich.android.petsave.common.presentation.Event
import com.raywenderlich.android.petsave.common.presentation.model.mappers.UiAnimalMapper
import com.raywenderlich.android.petsave.common.utils.DispatchersProvider
import com.raywenderlich.android.petsave.search.domain.usecases.GetSearchFilters
import com.raywenderlich.android.petsave.search.domain.usecases.SearchAnimals
import com.raywenderlich.android.petsave.search.domain.usecases.SearchAnimalsRemotely
import com.google.common.truth.Truth.assertThat
import com.raywenderlich.android.petsave.common.data.FakeRepository
import io.reactivex.disposables.CompositeDisposable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SearchFragmentViewModelTest {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val testCoroutineRule = TestCoroutineRule()

    @get:Rule
    val rxImmediateSchedulerRule = RxImmediateSchedulerRule()

    private lateinit var viewModel: SearchFragmentViewModel
    private lateinit var repository: FakeRepository
    private lateinit var getSearchFilters: GetSearchFilters
    private val uiAnimalsMapper = UiAnimalMapper()

    private lateinit var compositeDisposable: CompositeDisposable

    @Before
    fun setup() {
        val dispatchersProvider = object : DispatchersProvider {
            override fun io() = Dispatchers.Main
        }

        compositeDisposable = CompositeDisposable()

        repository = FakeRepository()
        getSearchFilters = GetSearchFilters(repository)

        viewModel = SearchFragmentViewModel(
            SearchAnimals(repository),
            getSearchFilters,
            SearchAnimalsRemotely(repository),
            uiAnimalsMapper,
            dispatchersProvider,
            compositeDisposable
        )
    }

    @After
    fun teardown() {
        compositeDisposable.clear()
    }

    @Test
    fun `SearchFragmentViewModel remote search with success`() =
        testCoroutineRule.runBlockingTest {
            // Given
            val (name, age, type) = repository.remotelySearchableAnimal
            val (ages, types) = getSearchFilters()

            val expectedRemoteAnimals = repository.remoteAnimals.map {
                uiAnimalsMapper.mapToView(it)
            }

            viewModel.state.observeForever {  }

            val expectedViewState = SearchViewState(
                noSearchQuery = false,
                searchResults = expectedRemoteAnimals,
                ageFilterValues = Event(ages),
                typeFilterValues = Event(types),
                searchingRemotely = false,
                noRemoteResults = false
            )
            // When
            viewModel.onEvent(SearchEvent.PrepareForSearch)
            viewModel.onEvent(SearchEvent.TypeValueSelected(type))
            viewModel.onEvent(SearchEvent.AgeValueSelected(age))
            viewModel.onEvent(SearchEvent.QueryInput(name))

            // Then
            val viewState = viewModel.state.value!!

            assertThat(viewState).isEqualTo(expectedViewState)
        }

    @Test
    fun `SearchFragmentViewModel cached search with success`() =
        testCoroutineRule.runBlockingTest {
            // Given
            val (name, age, type) = repository.locallySearchableAnimal
            val (ages, types) = getSearchFilters()

            val expectedCachedAnimals = repository.localAnimals.map {
                uiAnimalsMapper.mapToView(it)
            }

            viewModel.state.observeForever {  }

            val expectedState = SearchViewState(
                noSearchQuery = false,
                searchResults = expectedCachedAnimals,
                ageFilterValues = Event(ages),
                typeFilterValues = Event(types),
                searchingRemotely = false,
                noRemoteResults = false,
            )

            // When
            viewModel.onEvent(SearchEvent.PrepareForSearch)
            viewModel.onEvent(SearchEvent.QueryInput(name))
            viewModel.onEvent(SearchEvent.AgeValueSelected(age))
            viewModel.onEvent(SearchEvent.TypeValueSelected(type))

            // Then
            val viewState = viewModel.state.value!!

            assertThat(viewState).isEqualTo(expectedState)
        }
}