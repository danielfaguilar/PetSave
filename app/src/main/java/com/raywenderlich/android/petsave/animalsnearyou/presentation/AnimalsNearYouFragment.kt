package com.raywenderlich.android.petsave.animalsnearyou.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.raywenderlich.android.petsave.R
import com.raywenderlich.android.petsave.common.presentation.AnimalsAdapter
import com.raywenderlich.android.petsave.common.presentation.Event
import com.raywenderlich.android.petsave.databinding.FragmentAnimalsNearYouBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AnimalsNearYouFragment : Fragment() {

    private val binding get() = _binding!!

    private var _binding: FragmentAnimalsNearYouBinding? = null

    private val fragmentViewModel: AnimalsNearYouFragmentViewModel by viewModels()

    companion object {
        const val NUM_OF_ITEMS_PER_ROW = 2
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAnimalsNearYouBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUi()
        requestInitialAnimalsList()
    }

    private fun setupUi() {
        val adapter = createAdapter()
        setupRecyclerView(adapter)
        observerViewStateUpdates(adapter)
    }

    private fun createAdapter() = AnimalsAdapter()

    private fun setupRecyclerView(animalsAdapter: AnimalsAdapter) {
        binding.animalsRecyclerView.apply {
            adapter = animalsAdapter
            layoutManager = GridLayoutManager(requireContext(), NUM_OF_ITEMS_PER_ROW )
            setHasFixedSize(true)
            addOnScrollListener(createInfiniteScrollListener(layoutManager as GridLayoutManager))
        }
    }

    private fun createInfiniteScrollListener(
        layoutManager: GridLayoutManager
    ): RecyclerView.OnScrollListener {
        return object : InfiniteScrollListener(
            layoutManager,
            AnimalsNearYouFragmentViewModel.UI_PAGE_SIZE
        ) {
            override fun loadMoreItems() { requestMoreAnimals() }
            override fun isLoading(): Boolean = fragmentViewModel.isLoadingMoreAnimals
            override fun isLastPage(): Boolean = fragmentViewModel.isLastPage
        }
    }

    private fun requestInitialAnimalsList() {
        fragmentViewModel.onEvent(AnimalsNearYouEvent.RequestInitialAnimalsList)
    }

    private fun requestMoreAnimals() {
        fragmentViewModel.onEvent(AnimalsNearYouEvent.RequestMoreAnimals)
    }

    private fun observerViewStateUpdates(adapter: AnimalsAdapter) {
        fragmentViewModel.state.observe(viewLifecycleOwner) {
            updateScreenState(it, adapter)
        }
    }

    private fun updateScreenState(
        state: AnimalsNearYouViewState,
        adapter: AnimalsAdapter
    ) {
        binding.progressBar.isVisible = state.loading
        adapter.submitList(state.animals)
        handleNoMoreAnimalsNearby(state.noMoreAnimalsNearby)
        handleFailures(state.failure)
    }

    private fun handleNoMoreAnimalsNearby(noMoreAnimalsNearby: Boolean) {
        // Show a warning message and a prompt for the user to try a different
        // distance or postcode
    }

    private fun handleFailures(failure: Event<Throwable>?) {
        val unhandledFailure = failure?.getContentIfNotHandled() ?: return

        val fallbackMessage = getString(R.string.an_error_occurred)

        val snackBarMessage =
            if (unhandledFailure.message.isNullOrEmpty())
                fallbackMessage
            else unhandledFailure.message!!

        if (snackBarMessage.isNotEmpty())
            Snackbar.make(requireView(), snackBarMessage, Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}