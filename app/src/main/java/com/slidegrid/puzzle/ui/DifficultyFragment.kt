package com.slidegrid.puzzle.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.slidegrid.puzzle.R
import com.slidegrid.puzzle.adapters.DifficultyAdapter
import com.slidegrid.puzzle.databinding.FragmentDifficultyBinding
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class DifficultyFragment : Fragment() {

    private var _binding: FragmentDifficultyBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()
    private lateinit var adapter: DifficultyAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDifficultyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DifficultyAdapter { mode ->
            viewModel.selectDifficulty(mode)
        }

        binding.recyclerDifficulties.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerDifficulties.adapter = adapter

        binding.btnBack.setOnClickListener {
            viewModel.handleBackNavigation()
        }

        binding.btnStartPuzzle.setOnClickListener {
            viewModel.startNewGame(viewModel.uiState.value.selectedMode)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.uiState, viewModel.stats) { uiState, stats ->
                    Pair(uiState.selectedMode, stats.records)
                }.collect { (selectedMode, records) ->
                    adapter.submitState(selectedMode, records)
                    binding.btnStartPuzzle.text = getString(
                        R.string.start_puzzle_format,
                        selectedMode.gridSize
                    )
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
