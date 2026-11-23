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
import com.slidegrid.puzzle.databinding.FragmentSettingsBinding
import com.slidegrid.puzzle.models.ThemePreference
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: GameViewModel by activityViewModels()
    private var suppressListeners = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnBack.setOnClickListener {
            viewModel.handleBackNavigation()
        }

        binding.switchSound.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressListeners) {
                viewModel.setSoundEnabled(isChecked)
            }
        }

        binding.switchHaptics.setOnCheckedChangeListener { _, isChecked ->
            if (!suppressListeners) {
                viewModel.setHapticsEnabled(isChecked)
            }
        }

        binding.radioGroupTheme.setOnCheckedChangeListener { _, checkedId ->
            if (suppressListeners) return@setOnCheckedChangeListener
            val pref = when (checkedId) {
                R.id.radioThemeLight -> ThemePreference.LIGHT
                R.id.radioThemeSystem -> ThemePreference.SYSTEM
                else -> ThemePreference.DARK
            }
            viewModel.setThemePreference(pref)
        }

        binding.btnClearData.setOnClickListener {
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
                    suppressListeners = true
                    binding.switchSound.isChecked = stats.soundEnabled
                    binding.switchHaptics.isChecked = stats.hapticsEnabled
                    val themeRadioId = when (stats.themePreference) {
                        ThemePreference.DARK -> R.id.radioThemeDark
                        ThemePreference.LIGHT -> R.id.radioThemeLight
                        ThemePreference.SYSTEM -> R.id.radioThemeSystem
                    }
                    if (binding.radioGroupTheme.checkedRadioButtonId != themeRadioId) {
                        binding.radioGroupTheme.check(themeRadioId)
                    }
                    suppressListeners = false
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
