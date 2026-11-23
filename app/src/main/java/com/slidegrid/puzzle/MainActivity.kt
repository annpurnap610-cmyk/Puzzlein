package com.slidegrid.puzzle

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.slidegrid.puzzle.databinding.ActivityMainBinding
import com.slidegrid.puzzle.models.GameScreen
import com.slidegrid.puzzle.models.ThemePreference
import com.slidegrid.puzzle.ui.AboutFragment
import com.slidegrid.puzzle.ui.DifficultyFragment
import com.slidegrid.puzzle.ui.GameFragment
import com.slidegrid.puzzle.ui.GameViewModel
import com.slidegrid.puzzle.ui.MainMenuFragment
import com.slidegrid.puzzle.ui.PauseFragment
import com.slidegrid.puzzle.ui.SettingsFragment
import com.slidegrid.puzzle.ui.SplashFragment
import com.slidegrid.puzzle.ui.StatsFragment
import com.slidegrid.puzzle.ui.VictoryFragment
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: GameViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!viewModel.handleBackNavigation()) {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                }
            }
        })

        observeThemePreference()
        observeNavigation()
    }

    private fun observeThemePreference() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stats
                    .map { it.themePreference }
                    .distinctUntilChanged()
                    .collect { preference ->
                        val desiredMode = when (preference) {
                            ThemePreference.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                            ThemePreference.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                            ThemePreference.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                        }
                        if (AppCompatDelegate.getDefaultNightMode() != desiredMode) {
                            AppCompatDelegate.setDefaultNightMode(desiredMode)
                        }
                    }
            }
        }
    }

    private fun observeNavigation() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState
                    .map { it.currentScreen }
                    .distinctUntilChanged()
                    .collect { screen ->
                        showScreen(screen)
                    }
            }
        }
    }

    private fun showScreen(screen: GameScreen) {
        val tag = screen.name
        val existing = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
        if (existing != null && existing.tag == tag) {
            return
        }

        val fragment: Fragment = when (screen) {
            GameScreen.SPLASH -> SplashFragment()
            GameScreen.MAIN_MENU -> MainMenuFragment()
            GameScreen.DIFFICULTY -> DifficultyFragment()
            GameScreen.GAME -> GameFragment()
            GameScreen.PAUSE -> PauseFragment()
            GameScreen.VICTORY -> VictoryFragment()
            GameScreen.STATS -> StatsFragment()
            GameScreen.SETTINGS -> SettingsFragment()
            GameScreen.ABOUT -> AboutFragment()
        }

        supportFragmentManager
            .beginTransaction()
            .setReorderingAllowed(true)
            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            .replace(R.id.fragmentContainer, fragment, tag)
            .commit()
    }

    override fun onStop() {
        super.onStop()
        if (!isChangingConfigurations) {
            viewModel.onAppBackgrounded()
        }
    }
}
