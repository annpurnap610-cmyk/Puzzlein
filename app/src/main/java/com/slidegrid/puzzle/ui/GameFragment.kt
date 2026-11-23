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
import com.slidegrid.puzzle.databinding.FragmentGameBinding
import com.slidegrid.puzzle.game.PuzzleBoardView
import com.slidegrid.puzzle.models.PlayStatus
import com.slidegrid.puzzle.utils.TimeFormatter
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class GameFragment : Fragment() {

    private var _binding: FragmentGameBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()
    private var lastBoardSnapshot: List<Int> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.puzzleBoardView.setOnTileInteractionListener(object :
            PuzzleBoardView.OnTileInteractionListener {
            override fun onTileTapped(index: Int) {
                viewModel.onTileTapped(index)
            }

            override fun onAnimationStateChanged(isAnimating: Boolean) {
                viewModel.onTileAnimationChanged(isAnimating)
            }
        })

        binding.btnBack.setOnClickListener {
            viewModel.handleBackNavigation()
        }

        binding.btnSoundToggle.setOnClickListener {
            viewModel.toggleSound()
        }

        binding.btnUndo.setOnClickListener {
            viewModel.undoLastMove()
        }

        binding.btnPause.setOnClickListener {
            viewModel.pauseGame()
        }

        binding.btnRestart.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.moveCount > 0) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.confirm_restart_title)
                    .setMessage(R.string.confirm_restart_message)
                    .setNegativeButton(R.string.action_cancel, null)
                    .setPositiveButton(R.string.action_restart) { _, _ ->
                        viewModel.startNewGame(state.activeMode)
                    }
                    .show()
            } else {
                viewModel.startNewGame(state.activeMode)
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(viewModel.uiState, viewModel.stats) { uiState, stats ->
                    Pair(uiState, stats)
                }.collect { (uiState, stats) ->
                    val mode = uiState.activeMode
                    val modeLabel = "${getString(mode.titleRes)} · ${getString(mode.labelRes)}"
                    binding.textDifficultyTitle.text = modeLabel

                    val record = stats.records[mode]
                    binding.textBestSummary.text = if (record?.bestMoves != null && record.bestTimeSeconds != null) {
                        "Best ${record.bestMoves} moves · ${TimeFormatter.formatSeconds(record.bestTimeSeconds)}"
                    } else {
                        getString(R.string.no_record_yet)
                    }

                    binding.textMoves.text = uiState.moveCount.toString()
                    binding.textTimer.text = TimeFormatter.formatSeconds(uiState.elapsedSeconds)
                    binding.textPlaced.text = "${uiState.placedCount}/${mode.tileCount}"

                    binding.progressPlaced.max = mode.tileCount
                    binding.progressPlaced.progress = uiState.placedCount

                    binding.btnUndo.isEnabled = uiState.canUndo && uiState.playStatus == PlayStatus.PLAYING
                    binding.btnUndo.alpha = if (binding.btnUndo.isEnabled) 1.0f else 0.45f

                    binding.textGameplayHint.setText(
                        if (uiState.hasStartedMoving) R.string.hint_tap_slide else R.string.hint_timer_ready
                    )

                    binding.btnSoundToggle.setImageResource(
                        if (stats.soundEnabled) R.drawable.ic_volume_on else R.drawable.ic_volume_off
                    )
                    binding.btnSoundToggle.contentDescription = getString(
                        if (stats.soundEnabled) R.string.sound_on else R.string.sound_muted
                    )

                    binding.puzzleBoardView.setHapticsEnabled(stats.hapticsEnabled)
                    binding.puzzleBoardView.setInteractive(uiState.playStatus == PlayStatus.PLAYING)

                    if (uiState.board != lastBoardSnapshot) {
                        val animate = lastBoardSnapshot.isNotEmpty() &&
                            lastBoardSnapshot.size == uiState.board.size &&
                            uiState.moveCount > 0
                        lastBoardSnapshot = uiState.board
                        binding.puzzleBoardView.updateBoard(mode.gridSize, uiState.board, animate)
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        lastBoardSnapshot = emptyList()
        _binding = null
        super.onDestroyView()
    }
}
