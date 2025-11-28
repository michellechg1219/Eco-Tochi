package com.example.ecotochi.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.ecotochi.data.db.dao.HistoricalDao
import com.example.ecotochi.data.db.entity.HistoricalReading
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class HistoryRow(
    val id: Long,
    val temperatureText: String,
    val wetText: String,
    val phText: String,
    val dateText: String
)

class HistoryViewModel(
    private val dao: HistoricalDao
) : ViewModel() {

    val rows: StateFlow<List<HistoryRow>> =
        dao.getAll()
            .map { list ->
                list.map {
                    HistoryRow(
                        id = it.id,
                        temperatureText = String.format("%.1f °C", it.temperature),
                        wetText = String.format("%.1f %%", it.wet),
                        phText = String.format("%.2f", it.ph),
                        dateText = it.date
                    )
                }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Helpers de demo
    suspend fun seedOne() {
        val now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        dao.insert(
            HistoricalReading(
                temperature = 24.3,
                wet = 55.0,
                ph = 6.8,
                date = now
            )
        )
    }

    suspend fun clearAll() = dao.clear()

    companion object {
        fun factory(dao: HistoricalDao): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return HistoryViewModel(dao) as T
                }
            }
    }
}
