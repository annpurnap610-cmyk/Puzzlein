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
import com.slidegrid.puzzle.databinding.FragmentStatsBinding
import com.slidegrid.puzzle.models.DifficultyMode
import com.slidegrid.puzzle.models.DifficultyRecord
import com.slidegrid.puzzle.utils.TimeFormatter
import kotlinx.coroutines.launch

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            viewModel.handleBackNavigation()
        }

        binding.btnResetStats.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.confirm_reset_title)
                .setMessage(R.string.confirm_reset_message)
                .setNegativeButton(R.string.action_cancel, null)
                .setPositiveButton(R.string.action_confirm) { _, _ ->
                    viewModel.resetAllRecords()
                }
                .show()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stats.collect { stats ->
                    binding.textGamesPlayed.text = stats.totalGamesPlayed.toString()
                    binding.textGamesCompleted.text = stats.totalGamesCompleted.toString()
                    binding.textWinRate.text = "${stats.completionRatePercent()}%"
                    binding.textTotalMoves.text = stats.totalMoves.toString()

                    binding.textRecordRelaxed.text = formatRecord(stats.records[DifficultyMode.RELAXED])
                    binding.textRecordClassic.text = formatRecord(stats.records[DifficultyMode.CLASSIC])
                    binding.textRecordMaster.text = formatRecord(stats.records[DifficultyMode.MASTER])
                }
            }
        }
    }

    private fun formatRecord(record: DifficultyRecord?): String {
        return if (record?.bestMoves != null && record.bestTimeSeconds != null) {
            "Best Moves: ${record.bestMoves}  ·  Best Time: ${
                TimeFormatter.formatSeconds(record.bestTimeSeconds)
            }"
        } else {
            getString(R.string.no_record_yet)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
