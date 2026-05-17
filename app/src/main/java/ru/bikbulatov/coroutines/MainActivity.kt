package ru.bikbulatov.coroutines

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import ru.bikbulatov.coroutines.ui.VacancyListScreen
import ru.bikbulatov.coroutines.ui.VacancyListViewModel
import ru.bikbulatov.coroutines.ui.VacancyListUiState
import ru.bikbulatov.coroutines.ui.theme.CoroutinesTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CoroutinesTheme {
                val viewModel: VacancyListViewModel = hiltViewModel()

                val vacancies by viewModel.vacancies.collectAsState()
                val uiState by viewModel.uiState.collectAsState()
                val isSearching by viewModel.isSearching.collectAsState()
                val lastProcessedQuery by viewModel.lastProcessedQuery.collectAsState()

                when (uiState) {
                    is VacancyListUiState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is VacancyListUiState.Error -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Text(
                                text = "Ошибка: ${(uiState as VacancyListUiState.Error).message}",
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    is VacancyListUiState.Success -> {
                        VacancyListScreen(
                            vacancies = vacancies,
                            isSearching = isSearching,
                            lastProcessedQuery = lastProcessedQuery,
                            onVacancyClick = { vacancy ->
                                viewModel.onVacancyClick(vacancy)
                            },
                            onSearchQueryChange = { query ->
                                viewModel.onSearchQueryChange(query)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    CoroutinesTheme {
        VacancyListScreen(
            vacancies = emptyList(),
            onSearchQueryChange = {}
        )
    }
}