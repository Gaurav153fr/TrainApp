package com.example.apktest.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.apktest.data.train.LiveTrainRepository
import com.example.apktest.data.train.LiveTrainResponse
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId

data class LiveStatusUiState(
    val liveData: LiveTrainResponse? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class LiveStatusViewModel : ViewModel() {
    private val repo = LiveTrainRepository()
    private val _uiState = MutableStateFlow(LiveStatusUiState())
    val uiState: StateFlow<LiveStatusUiState> = _uiState.asStateFlow()

    fun loadStatus(trainNo: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repo.fetchLiveTrain(trainNo)
                if (response.success && response.data != null) {
                    _uiState.update { it.copy(isLoading = false, liveData = response.data) }
                } else {
                    _uiState.update { it.copy(isLoading = false, error = "Failed to fetch live status") }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message ?: "Error") }
            }
        }
    }
}

private fun formatTime(epochSecs: Long?): String {
    if (epochSecs == null || epochSecs == 0L) return "---"
    val dt = Instant.ofEpochSecond(epochSecs).atZone(ZoneId.of("Asia/Kolkata"))
    return String.format("%02d:%02d", dt.hour, dt.minute)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveStatusActivityScreen(trainNo: String, viewModel: LiveStatusViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(trainNo) {
        viewModel.loadStatus(trainNo)
    }

    val timelineColor = MaterialTheme.colorScheme.primary

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Live Status: $trainNo") },
                navigationIcon = {
                    IconButton(onClick = { /* Handle back */ }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding).background(MaterialTheme.colorScheme.background)) {
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp)
                )
            } else if (state.liveData != null) {
                val data = state.liveData!!
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(data.route) { index, stop ->
                        val isCurrent = data.currentPosition?.stationCode == stop.stationCode
                        val isEven = index % 2 == 0
                        val bgColor = if (isEven) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        val textColor = MaterialTheme.colorScheme.onSurface
                        
                        Box(modifier = Modifier.fillMaxWidth().background(bgColor)) {
                            Row(
                                modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Left Arrival Column
                                Column(
                                    modifier = Modifier.width(60.dp).padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = formatTime(stop.scheduledArrivalTime),
                                        fontSize = 14.sp,
                                        color = textColor
                                    )
                                    val arrActual = stop.actualArrivalTime
                                    val arrSched = stop.scheduledArrivalTime
                                    val arrColor = if (arrActual != null && arrSched != null && (arrActual - arrSched) > 300) 
                                        MaterialTheme.colorScheme.error else Color(0xFF388E3C)
                                    Text(
                                        text = formatTime(arrActual),
                                        fontSize = 13.sp,
                                        color = arrColor
                                    )
                                }

                                // Timeline Center
                                Box(
                                    modifier = Modifier.width(40.dp).fillMaxHeight(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Vertical line
                                    Box(
                                        modifier = Modifier
                                            .width(4.dp)
                                            .fillMaxHeight()
                                            .background(timelineColor)
                                    )
                                    
                                    // Current Train Icon or dot
                                    if (isCurrent) {
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(timelineColor)
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Train,
                                                contentDescription = "Train Location",
                                                tint = MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(bgColor)
                                                .border(2.dp, timelineColor, CircleShape)
                                        )
                                    }
                                }

                                // Station Name and Tooltip
                                Column(
                                    modifier = Modifier.weight(1f).padding(vertical = 12.dp, horizontal = 8.dp)
                                ) {
                                    Text(
                                        text = stop.station_name ?: stop.stationCode,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = textColor
                                    )
                                    Text(
                                        text = "${(index * 15)} km",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.tertiary, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = "Departed ${stop.station_name ?: stop.stationCode}",
                                                color = MaterialTheme.colorScheme.onTertiary,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }

                                // Right Departure Column
                                Column(
                                    modifier = Modifier.width(60.dp).padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = formatTime(stop.scheduledDepartureTime),
                                        fontSize = 14.sp,
                                        color = textColor
                                    )
                                    val depActual = stop.actualDepartureTime
                                    val depSched = stop.scheduledDepartureTime
                                    val depColor = if (depActual != null && depSched != null && (depActual - depSched) > 300) 
                                        MaterialTheme.colorScheme.error else Color(0xFF388E3C)
                                    Text(
                                        text = formatTime(depActual),
                                        fontSize = 13.sp,
                                        color = depColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}