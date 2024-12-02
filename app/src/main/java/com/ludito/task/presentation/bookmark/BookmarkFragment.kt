package com.ludito.task.presentation.bookmark

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ludito.task.databinding.FragmentBookmarkBinding
import com.ludito.task.presentation.bookmark.adapter.BookmarkRecyclerAdapter
import com.ludito.task.presentation.bookmark.model.BookmarkUiState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class BookmarkFragment : Fragment() {
    private var _binding: FragmentBookmarkBinding? = null
    private val binding get() = _binding!!

    private val vm: BookmarkViewModel by viewModels()
    private lateinit var adapter: BookmarkRecyclerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookmarkBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initAdapter()
        observeData()
    }


    private fun initAdapter() {
        adapter = BookmarkRecyclerAdapter() {
            navigateToMap(it.location?.latitude ?: 0.0, it.location?.longitude ?: 0.0)
        }

        binding.rvAddresses.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAddresses.adapter = adapter
    }

    private fun observeData() {
        lifecycleScope.launch {
            vm.uiState.flowWithLifecycle(lifecycle, Lifecycle.State.CREATED).collect {
                handleData(uiState = it)
            }
        }
    }

    private fun navigateToMap(latitude: Double, longitude: Double) {
        val action =
            BookmarkFragmentDirections.actionBookmarkToMap(latitude.toFloat(), longitude.toFloat())
        findNavController().navigate(action)
    }

    private fun handleData(uiState: BookmarkUiState) {
        if (uiState.list.isNotEmpty()) {
            adapter.setList(uiState.list)
            binding.txtEmptyAddress.visibility = View.GONE
            binding.rvAddresses.visibility = View.VISIBLE
        } else {
            binding.txtEmptyAddress.visibility = View.VISIBLE
            binding.rvAddresses.visibility = View.GONE
        }
    }
}