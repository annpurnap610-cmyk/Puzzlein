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
import com.slidegrid.puzzle.databinding.FragmentMainMenuBinding
import com.slidegrid.puzzle.models.DifficultyMode
import com.slidegrid.puzzle.models.GameScreen
import kotlinx.coroutines.launch

class MainMenuFragment : Fragment() {

    private var _binding: FragmentMainMenuBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSelectDifficulty.setOnClickListener {
            viewModel.navigateTo(GameScreen.DIFFICULTY)
        }
        binding.btnQuickPlay.setOnClickListener {
            viewModel.startNewGame(DifficultyMode.CLASSIC)
        }
        binding.btnStats.setOnClickListener {
            viewModel.navigateTo(GameScreen.STATS)
        }
        binding.btnSettings.setOnClickListener {
            viewModel.navigateTo(GameScreen.SETTINGS)
        }
        binding.btnAbout.setOnClickListener {
            viewModel.navigateTo(GameScreen.ABOUT)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stats.collect { stats ->
                    binding.textMenuSummary.text =
                        "Played: ${stats.totalGamesPlayed} · Solved: ${stats.totalGamesCompleted} · Win Rate: ${stats.completionRatePercent()}%"
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
