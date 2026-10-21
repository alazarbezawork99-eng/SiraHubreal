package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.ui.MainScreen
import com.example.ui.SiraViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: SiraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appSettings by viewModel.appSettings.collectAsState()
            val systemInDark = isSystemInDarkTheme()
            val isDark = when (appSettings.themeMode.uppercase()) {
                "LIGHT" -> false
                "DARK" -> true
                "SYSTEM" -> systemInDark
                else -> appSettings.darkThemeEnabled
            }

            MyApplicationTheme(darkTheme = isDark) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
