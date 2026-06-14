package ru.bikbulatov.coroutines_3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import ru.bikbulatov.coroutines_3.presentation.ProfileScreen
import ru.bikbulatov.coroutines_3.presentation.ProfileViewModel
import ru.bikbulatov.coroutines_3.ui.theme.Coroutines3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val viewmodel = ProfileViewModel()
        setContent {
            Coroutines3Theme {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ProfileScreen(
                        viewmodel
                    )
                }
            }
        }
    }
}
