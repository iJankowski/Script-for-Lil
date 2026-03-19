package com.scriptforlil.kindroidhealthsync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scriptforlil.kindroidhealthsync.data.AppContainer
import com.scriptforlil.kindroidhealthsync.ui.MainScreen
import com.scriptforlil.kindroidhealthsync.ui.MainViewModel
import com.scriptforlil.kindroidhealthsync.ui.MainViewModelFactory
import com.scriptforlil.kindroidhealthsync.ui.theme.KindroidHealthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = AppContainer(applicationContext)

        setContent {
            KindroidHealthTheme {
                val viewModel: MainViewModel = viewModel(
                    factory = MainViewModelFactory(container)
                )
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
