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
import com.slidegrid.puzzle.R
import com.slidegrid.puzzle.databinding.FragmentVictoryBinding
import com.slidegrid.puzzle.models.GameScreen
import com.slidegrid.puzzle.utils.TimeFormatter
import kotlinx.coroutines.launch

class VictoryFragment : Fragment() {

    private var _binding: FragmentVictoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVictoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPlayAgain.setOnClickListener {
            viewModel.startNewGame(viewModel.uiState.value.activeMode)
        }

        binding.btnChangeDifficulty.setOnClickListener {
            viewModel.openDifficultyFromGame()
        }

        binding.btnViewStats.setOnClickListener {
            viewModel.navigateTo(GameScreen.STATS)
        }

        binding.btnMainMenu.setOnClickListener {
            viewModel.returnToMainMenu()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.textVictoryKicker.text =
                        getString(R.string.victory_kicker, state.activeMode.gridSize)
                    binding.textVictoryMoves.text = state.moveCount.toString()
                    binding.textVictoryTime.text = TimeFormatter.formatSeconds(state.elapsedSeconds)

                    binding.textBestMovesTag.visibility =
                        if (state.isNewBestMoves) View.VISIBLE else View.GONE
                    binding.textBestTimeTag.visibility =
                        if (state.isNewBestTime) View.VISIBLE else View.GONE

                    when {
                        state.isNewBestMoves && state.isNewBestTime -> {
                            binding.textNewRecordBanner.visibility = View.VISIBLE
                            binding.textNewRecordBanner.setText(R.string.victory_record_both)
                        }
                        state.isNewBestMoves -> {
                            binding.textNewRecordBanner.visibility = View.VISIBLE
                            binding.textNewRecordBanner.setText(R.string.victory_record_moves)
                        }
                        state.isNewBestTime -> {
                            binding.textNewRecordBanner.visibility = View.VISIBLE
                            binding.textNewRecordBanner.setText(R.string.victory_record_time)
                        }
                        else -> {
                            binding.textNewRecordBanner.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
