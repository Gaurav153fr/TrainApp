package com.example.apktest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton

import androidx.compose.material3.Icon

import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp


import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.coroutines.cancellation.CancellationException
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction

@Serializable
data class JsonResponse  (
    val success: Boolean,
    val data: List<TrainData>,
    val total: Int,
    val query:String,

    )
@Serializable
data class TrainData(
    val number:String,
    val name:String,
    val fromStnCode:String,
    val toStnCode:String,

)
@Composable
fun TrainSelector(viewModel: TrainViewModel = viewModel(), onNavigateToLiveScreen: (trainNo:String) -> Unit) {
    var trainQuery by remember { mutableStateOf("") }
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MaterialTheme {
        // Use a Column here so elements flow downwards instead of overlapping
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            SearchTab(query = trainQuery, onQueryChange = {trainQuery=it}, onSearch = {viewModel.loadTrains(trainQuery)})

            Spacer(modifier = Modifier.height(8.dp))


            if (state.isLoading) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            state.error?.let {
                Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
            Text(
                text = "Results found: ${state.trains.size}",
                modifier = Modifier.padding(vertical = 8.dp)
            )
            // LazyColumn with weight(1f) to fill the remaining screen space safely
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                items(state.trains) { train ->

                    SearchCard(train, onNavigateToLiveScreen = onNavigateToLiveScreen)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


data class TrainUiState(
    val trains: List<TrainData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class TrainViewModel : ViewModel() {
    private val repository = TrainRepository()

    private val _uiState = MutableStateFlow(TrainUiState())
    val uiState: StateFlow<TrainUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun loadTrains(query: String) {
        if (query.isBlank()) return

        searchJob?.cancel() // ignore the previous search if a new one starts
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val response = repository.fetchTrains(query)
                _uiState.update {
                    it.copy(isLoading = false, trains = response?.data ?: emptyList())
                }
            } catch (e: CancellationException) {
                throw e // never swallow cancellation
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Something went wrong")
                }
            }
        }
    }
}
class TrainRepository {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun fetchTrains(query: String): JsonResponse =
        client.get("https://whereismytrain.org.in/api/search.php") {
            parameter("q", query)
            parameter("type", "train")
        }.body()
}


@Composable
fun SearchCard(trainData: TrainData,onNavigateToLiveScreen: (trainNo:String) -> Unit){

    Row(modifier = Modifier.clickable(onClick = { onNavigateToLiveScreen(trainData.number) })) {
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(MaterialTheme.shapes.small)
                .background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center
        ){
           Text(trainData.number, style = MaterialTheme.typography.headlineMedium)
        }
Spacer(modifier= Modifier.width(8.dp))
        Column() {
            Text(trainData.name, style = MaterialTheme.typography.bodyLarge)
            Row() {
                Text(trainData.fromStnCode)
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "from to")
                Text(trainData.toStnCode)
            }
        }
    }
}

@Composable
fun FromTo(onSearch: (from: String, to: String) -> Unit = { _, _ -> }) {
    var from by rememberSaveable { mutableStateOf("") }
    var to by rememberSaveable { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                TextField(
                    value = from,
                    onValueChange = { from = it },
                    label = { Text("From station") },
                    leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = searchFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                TextField(
                    value = to,
                    onValueChange = { to = it },
                    label = { Text("To station") },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.large,
                    colors = searchFieldColors(),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch(from, to) }),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.width(8.dp))

            FilledTonalIconButton(onClick = {
                val temp = from
                from = to
                to = temp
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Swap stations")
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { onSearch(from, to) },
            enabled = from.isNotBlank() && to.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Search, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Search trains")
        }
    }
}

@Composable
private fun searchFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    errorIndicatorColor = Color.Transparent
)

@Composable
fun SearchTab(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit
) {
    var selected by rememberSaveable { mutableIntStateOf(0) }
    val titles = listOf("Name/No.", "From To")

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PrimaryTabRow(selectedTabIndex = selected) {
            titles.forEachIndexed { index, title ->
                Tab(
                    selected = selected == index,
                    onClick = { selected = index },
                    text = { Text(title) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        when (selected) {
            0 -> TextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search for train number") },
                singleLine = true,
                shape = MaterialTheme.shapes.large,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch(query) }),
                trailingIcon = {
                    FilledIconButton(
                        onClick = { onSearch(query) },
                        modifier = Modifier.padding(end = 4.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = "Search train no")
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            1 -> FromTo()
        }
    }
}