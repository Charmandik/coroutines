package ru.bikbulatov.coroutines.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.delay
import ru.bikbulatov.coroutines.domain.Vacancy
import ru.bikbulatov.coroutines.domain.VacancyRepository
import javax.inject.Inject
import javax.inject.Singleton



class VacancyRepositoryImpl @Inject constructor() : VacancyRepository {

    private val allVacancies = listOf(
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
        ),
        Vacancy(
            id = "5",
            title = "iOS Developer",
            company = "Apple Studio",
            salary = "от 250 000 ₽",
            location = "Москва",
            description = "Разработка приложений для iOS на Swift."
        ),
        Vacancy(
            id = "6",
            title = "Frontend Developer",
            company = "Web Studio",
            salary = "от 180 000 ₽",
            location = "Казань",
            description = "Разработка веб-интерфейсов на React."
        ),
        Vacancy(
            id = "7",
            title = "Backend Developer",
            company = "Server Solutions",
            salary = "от 220 000 ₽",
            location = "Новосибирск",
            description = "Разработка серверной части на Python/Go."
        ),
        Vacancy(
            id = "8",
            title = "Fullstack Developer",
            company = "Digital Agency",
            salary = "от 280 000 ₽",
            location = "Удаленно",
            description = "Разработка полного цикла: frontend и backend."
        )
    )

    override suspend fun loadVacancies(): Result<List<Vacancy>> {
        // Симуляция сетевого запроса с задержкой
        delay(500)
        return Result.success(allVacancies)
    }

    override suspend fun searchVacancies(query: String): Result<List<Vacancy>> {
        // Симуляция сетевого запроса поиска с БОЛЬШОЙ задержкой
        // Это демонстрирует проблему: при быстром вводе старые запросы
        // выполняются дольше и приходят позже новых
        val searchDelay = when {
            query.isEmpty() -> 100L
            query.length == 1 -> 800L  // Первый символ - долгий запрос
            query.length == 2 -> 600L  // Второй символ - средний запрос
            query.length == 3 -> 400L  // Третий символ - быстрый запрос
            else -> 300L               // Дальше - быстрые запросы
        }
        
        delay(searchDelay)
        
        val filteredVacancies = if (query.isEmpty()) {
            allVacancies
        } else {
            val lowerQuery = query.lowercase()
            allVacancies.filter { vacancy ->
                vacancy.title.lowercase().contains(lowerQuery) ||
                vacancy.company.lowercase().contains(lowerQuery) ||
                vacancy.location?.lowercase()?.contains(lowerQuery) == true ||
                vacancy.description?.lowercase()?.contains(lowerQuery) == true
            }
        }
        
        return Result.success(filteredVacancies)
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindVacancyRepository(
        repositoryImpl: VacancyRepositoryImpl
    ): VacancyRepository
}
