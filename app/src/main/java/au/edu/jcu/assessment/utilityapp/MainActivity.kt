package au.edu.jcu.assessment.utilityapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import au.edu.jcu.assessment.utilityapp.ui.WeatherUiState
import au.edu.jcu.assessment.utilityapp.ui.WeatherViewModel
import au.edu.jcu.assessment.utilityapp.ui.theme.UtilityAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UtilityAppTheme {
                UtilityAppScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UtilityAppScaffold() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val viewModel: WeatherViewModel = viewModel()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (selectedTab == 0) "QuickWeather Utility" else "App Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.WbSunny, contentDescription = "Weather") },
                    label = { Text("Utility") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (selectedTab == 0) {
                UtilityScreen(viewModel)
            } else {
                SettingsScreen(viewModel)
            }
        }
    }
}

@Composable
fun UtilityScreen(viewModel: WeatherViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val isCelsius by viewModel.isCelsius.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = "Live Conditions: $selectedCity", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                when (val state = uiState) {
                    is WeatherUiState.Loading -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Fetching live data...")
                    }
                    is WeatherUiState.Success -> {
                        val tempC = state.data.current.temperature
                        val displayTemp = if (isCelsius) tempC else (tempC * 9/5) + 32
                        val unitLabel = if (isCelsius) "°C" else "°F"

                        Text(
                            text = "%.1f%s".format(displayTemp, unitLabel),
                            style = MaterialTheme.colorScheme.displayLarge.let { it } ?: MaterialTheme.typography.displayLarge
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Wind Speed: ${state.data.current.windspeed} km/h", style = MaterialTheme.typography.bodyLarge)
                    }
                    is WeatherUiState.Error -> {
                        Text(text = "Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = { viewModel.loadWeather() }) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Refresh At-A-Glance Data")
        }
    }
}

@Composable
fun SettingsScreen(viewModel: WeatherViewModel) {
    val isCelsius by viewModel.isCelsius.collectAsState()
    val selectedCity by viewModel.selectedCity.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("Utility Preferences", style = MaterialTheme.typography.titleLarge)

        // Location selection
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Select Location", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { viewModel.setLocation("Brisbane") },
                        enabled = selectedCity != "Brisbane"
                    ) {
                        Text("Brisbane")
                    }
                    Button(
                        onClick = { viewModel.setLocation("Townsville") },
                        enabled = selectedCity != "Townsville"
                    ) {
                        Text("Townsville")
                    }
                }
            }
        }

        // Temperature unit toggle
        Card(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Temperature Unit", style = MaterialTheme.typography.titleMedium)
                    Text("Switch between Celsius and Fahrenheit", style = MaterialTheme.typography.bodySmall)
                }
                Switch(
                    checked = isCelsius,
                    onCheckedChange = { viewModel.setUnit(it) }
                )
            }
        }
    }
}
