package com.slidegrid.puzzle.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.slidegrid.puzzle.R
import com.slidegrid.puzzle.databinding.ItemDifficultyCardBinding
import com.slidegrid.puzzle.models.DifficultyMode
import com.slidegrid.puzzle.models.DifficultyRecord
import com.slidegrid.puzzle.utils.TimeFormatter

class DifficultyAdapter(
    private val onSelect: (DifficultyMode) -> Unit
) : RecyclerView.Adapter<DifficultyAdapter.ViewHolder>() {

    private var modes: List<DifficultyMode> = DifficultyMode.entries
    private var selectedMode: DifficultyMode = DifficultyMode.CLASSIC
    private var records: Map<DifficultyMode, DifficultyRecord> = emptyMap()

    fun submitState(
        selected: DifficultyMode,
        modeRecords: Map<DifficultyMode, DifficultyRecord>
    ) {
        selectedMode = selected
        records = modeRecords
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDifficultyCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(modes[position])
    }

    override fun getItemCount(): Int = modes.size

    inner class ViewHolder(
        private val binding: ItemDifficultyCardBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(mode: DifficultyMode) {
            val ctx = binding.root.context
            val isSelected = mode == selectedMode
            val record = records[mode]

            binding.root.setBackgroundResource(
                if (isSelected) R.drawable.bg_card_selected else R.drawable.bg_card_default
            )
            binding.root.contentDescription =
                "${ctx.getString(mode.titleRes)}, ${ctx.getString(mode.labelRes)}"

            binding.textModeLabel.text = ctx.getString(mode.labelRes)
            binding.textModeLabel.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    if (isSelected) R.color.accent_amber else R.color.ink_muted
                )
            )
            binding.textModeTitle.text = ctx.getString(mode.titleRes)
            binding.textTileCount.text = ctx.getString(mode.tilesRes)
            binding.textDescription.text = ctx.getString(mode.descriptionRes)

            binding.miniGridPreview.configure(mode.gridSize, isSelected)

            if (record?.bestMoves != null && record.bestTimeSeconds != null) {
                binding.textBestRecord.text = "${record.bestMoves} moves · ${
                    TimeFormatter.formatSeconds(record.bestTimeSeconds)
                }"
                binding.textBestRecord.setTextColor(ContextCompat.getColor(ctx, R.color.ink_secondary))
            } else {
                binding.textBestRecord.text = ctx.getString(R.string.no_record_yet)
                binding.textBestRecord.setTextColor(ContextCompat.getColor(ctx, R.color.ink_muted))
            }

            binding.textSelectBadge.text = ctx.getString(
                if (isSelected) R.string.selected_label else R.string.choose_label
            )
            binding.textSelectBadge.setTextColor(
                ContextCompat.getColor(
                    ctx,
                    if (isSelected) R.color.accent_amber else R.color.ink_muted
                )
            )

            binding.root.setOnClickListener {
                onSelect(mode)
            }
        }
    }
}
