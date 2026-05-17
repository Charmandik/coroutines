package ru.bikbulatov.coroutines.presentation.vacancies

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.bikbulatov.coroutines.domain.Vacancy
import ru.bikbulatov.coroutines.presentation.theme.CoroutinesTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VacancyListScreen(
    vacancies: List<Vacancy>,
    isSearching: Boolean = false,
    lastProcessedQuery: String = "",
    onVacancyClick: (Vacancy) -> Unit = {},
    onSearchQueryChange: (String) -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Вакансии") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { query ->
                        searchQuery = query
                        onSearchQueryChange(query)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text("Поиск вакансий...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Поиск"
                        )
                    },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    },
                    singleLine = true
                )
                
                // Индикатор последнего обработанного запроса - для демонстрации проблемы
                AnimatedVisibility(
                    visible = lastProcessedQuery.isNotEmpty(),
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Text(
                        text = "🔍 Последний запрос: '$lastProcessedQuery'",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    ) { paddingValues ->
        if (vacancies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Вакансий не найдено",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (isSearching) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "(поиск ещё выполняется...)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vacancies, key = { it.id }) { vacancy ->
                    VacancyCard(
                        vacancy = vacancy,
                        onClick = { onVacancyClick(vacancy) }
                    )
                }
            }
        }
    }
}

@Composable
private fun VacancyCard(
    vacancy: Vacancy,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = vacancy.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = vacancy.company,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )

            vacancy.location?.let { location ->
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 $location",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            vacancy.salary?.let { salary ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = salary,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            vacancy.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VacancyListScreenPreview() {
    val sampleVacancies = listOf(
        Vacancy(
            id = "1",
            title = "Android Developer",
            company = "Tech Company",
            salary = "от 200 000 ₽",
            location = "Москва",
            description = "Разработка мобильных приложений на Kotlin. Опыт работы от 3 лет."
        ),
        Vacancy(
            id = "2",
            title = "Senior Kotlin Developer",
            company = "Startup Inc",
            salary = "от 300 000 ₽",
            location = "Санкт-Петербург",
            description = "Разработка бэкенда на Kotlin. Микросервисная архитектура."
        ),
        Vacancy(
            id = "3",
            title = "Junior Android Developer",
            company = "Mobile Studio",
            location = "Екатеринбург",
            description = "Начинающий разработчик для работы над Android приложениями."
        ),
        Vacancy(
            id = "4",
            title = "Lead Mobile Developer",
            company = "Big Tech Corp",
            salary = "от 450 000 ₽",
            location = "Удаленно",
            description = "Руководство командой мобильной разработки. Архитектура и код-ревью."
        )
    )

    CoroutinesTheme {
        VacancyListScreen(
            vacancies = sampleVacancies,
            isSearching = false,
            lastProcessedQuery = "and"
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun VacancyListScreenEmptyPreview() {
    CoroutinesTheme {
        VacancyListScreen(
            vacancies = emptyList(),
            isSearching = true,
            lastProcessedQuery = "xyz"
        )
    }
}
