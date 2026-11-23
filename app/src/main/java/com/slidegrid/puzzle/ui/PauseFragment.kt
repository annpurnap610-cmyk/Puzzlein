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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.slidegrid.puzzle.R
import com.slidegrid.puzzle.databinding.FragmentPauseBinding
import com.slidegrid.puzzle.models.GameScreen
import com.slidegrid.puzzle.utils.TimeFormatter
import kotlinx.coroutines.launch

class PauseFragment : Fragment() {

    private var _binding: FragmentPauseBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPauseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnResume.setOnClickListener {
            viewModel.resumeGame()
        }

        binding.btnRestartRound.setOnClickListener {
            val state = viewModel.uiState.value
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_restart_title)
                .setMessage(R.string.confirm_restart_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_restart) { _, _ ->
                    viewModel.startNewGame(state.activeMode)
                }
                .show()
        }

        binding.btnChangeDifficulty.setOnClickListener {
            viewModel.openDifficultyFromGame()
        }

        binding.btnSettings.setOnClickListener {
            viewModel.navigateTo(GameScreen.SETTINGS)
        }

        binding.btnMainMenu.setOnClickListener {
            viewModel.returnToMainMenu()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val mode = state.activeMode
                    binding.textPauseStats.text = "${getString(mode.titleRes)} · ${state.moveCount} moves · ${
                        TimeFormatter.formatSeconds(state.elapsedSeconds)
                    }"
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
