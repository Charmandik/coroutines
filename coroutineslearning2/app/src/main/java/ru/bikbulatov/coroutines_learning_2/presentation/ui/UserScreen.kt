package ru.bikbulatov.coroutines_learning_2.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.bikbulatov.coroutines_learning_2.presentation.UserViewModel
import ru.bikbulatov.coroutines_learning_2.presentation.UiState

@Composable
fun UserScreen(
    viewModel: UserViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Exception Handling in Coroutines",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { viewModel.loadUserDataScenario1() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scenario 1: try/catch around launch")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.loadUserDataScenario2() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scenario 2: try/catch in nested launch")
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.loadUserDataScenario3() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Scenario 3: async without proper await")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.loadUserDataCorrect() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Correct Implementation (Demo)")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { viewModel.reset() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset")
            }

            Spacer(modifier = Modifier.height(24.dp))

            when (val state = uiState) {
                is UiState.Idle -> {
                    Text(
                        text = "Press a button to test exception handling",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                is UiState.Loading -> {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Loading...")
                }
                is UiState.Success -> {
                    Text(
                        text = "Success!",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("User: ${state.user.name}")
                    Text("Posts: ${state.posts.size}")
                    Text("Theme: ${state.settings.theme}")
                }
                is UiState.Error -> {
                    Text(
                        text = "Error",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = state.scenario,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
