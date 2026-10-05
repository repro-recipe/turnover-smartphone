package com.example.turnover

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.turnover.ui.screens.EveningReflectionScreen
import com.example.turnover.ui.screens.FailureArchiveScreen
import com.example.turnover.ui.screens.HomeScreen
import com.example.turnover.ui.theme.DadsBgCanvas
import com.example.turnover.ui.theme.TurnOverTheme
import com.example.turnover.ui.viewmodel.AppScreen
import com.example.turnover.ui.viewmodel.TurnOverViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: TurnOverViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TurnOverTheme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = DadsBgCanvas
                ) {
                    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
                    val reflectionStep by viewModel.reflectionStep.collectAsStateWithLifecycle()

                    // BackHandler for secondary screens
                    BackHandler(enabled = currentScreen != AppScreen.HOME) {
                        when (currentScreen) {
                            AppScreen.ARCHIVE -> viewModel.navigateTo(AppScreen.HOME)
                            AppScreen.REFLECTION -> {
                                if (reflectionStep > 1) {
                                    viewModel.setReflectionStep(reflectionStep - 1)
                                } else {
                                    viewModel.navigateTo(AppScreen.HOME)
                                }
                            }
                            AppScreen.HOME -> { /* Do nothing / exit */ }
                        }
                    }

                    when (currentScreen) {
                        AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                        AppScreen.ARCHIVE -> FailureArchiveScreen(viewModel = viewModel)
                        AppScreen.REFLECTION -> EveningReflectionScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
