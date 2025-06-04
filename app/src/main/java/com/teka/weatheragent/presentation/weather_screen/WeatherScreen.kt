package com.teka.weatheragent.presentation.weather_screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.sin

// Enhanced weather condition mapping similar to chat screen
object WeatherDesignUtils {
    fun getWeatherIcon(temperature: Double): ImageVector {
        return when {
            temperature >= 30 -> Icons.Default.WbSunny
            temperature >= 25 -> Icons.Default.FilterDrama
            temperature >= 20 -> Icons.Default.Cloud
            temperature >= 15 -> Icons.Default.Air
            temperature >= 10 -> Icons.Default.Thermostat
            else -> Icons.Default.AcUnit
        }
    }

    fun getWeatherColor(temperature: Double): Color {
        return when {
            temperature >= 30 -> Color(0xFFFFD700) // Gold for hot
            temperature >= 25 -> Color(0xFFF59E0B) // Orange for warm
            temperature >= 20 -> Color(0xFF9CA3AF) // Gray for mild
            temperature >= 15 -> Color(0xFF10B981) // Green for cool
            temperature >= 10 -> Color(0xFF3B82F6) // Blue for cold
            else -> Color(0xFFE5E7EB) // Light gray for very cold
        }
    }

    fun getGradientColors(temperature: Double): List<Color> {
        return when {
            temperature >= 30 -> listOf(
                Color(0xFFFF6B35), // Hot orange
                Color(0xFFFF8E53), // Warm orange
                Color(0xFFFFB347)  // Light orange
            )
            temperature >= 20 -> listOf(
                Color(0xFF1E3A8A), // Deep blue
                Color(0xFF3B82F6), // Blue
                Color(0xFF60A5FA)  // Light blue
            )
            temperature >= 10 -> listOf(
                Color(0xFF1E40AF), // Dark blue
                Color(0xFF2563EB), // Blue
                Color(0xFF3B82F6)  // Medium blue
            )
            else -> listOf(
                Color(0xFF1E1B4B), // Very dark blue
                Color(0xFF312E81), // Dark purple-blue
                Color(0xFF3730A3)  // Purple-blue
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel = viewModel(),
    onNavigateToChat: () -> Unit = {}
) {
    val uiState: WeatherUiState by viewModel.uiState.collectAsState()

    // Get current temperature for gradient selection
    val currentTemp = (uiState as? WeatherUiState.Success)?.currentTemperature ?: 20.0
    val gradientColors = WeatherDesignUtils.getGradientColors(currentTemp)

    LaunchedEffect(Unit) {
        viewModel.fetchWeatherData()
    }

    Scaffold(
        floatingActionButton = {
            EnhancedChatFAB(
                onClick = onNavigateToChat,
                isWeatherLoaded = uiState is WeatherUiState.Success
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(colors = gradientColors)
                )
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(30.dp))

            // Enhanced Header with Animation
            EnhancedWeatherHeader(
                uiState = uiState,
                onRefresh = { viewModel.fetchWeatherData() }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Temperature Display
            MainTemperatureCard(uiState = uiState)

            Spacer(modifier = Modifier.height(16.dp))

            // Content based on state
            when (val currentState = uiState) {
                is WeatherUiState.Success -> {
                    WeatherSuccessContent(successState = currentState)
                }
                is WeatherUiState.Loading -> {
                    LoadingContent()
                }
                is WeatherUiState.Error -> {
                    ErrorContent(onRetry = { viewModel.fetchWeatherData() })
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EnhancedWeatherHeader(
    uiState: WeatherUiState,
    onRefresh: () -> Unit
) {
    // Animated header icon similar to chat screen
    var headerIcon by remember { mutableStateOf(Icons.Default.Cloud) }
    val isLoading = uiState is WeatherUiState.Loading

    LaunchedEffect(isLoading) {
        if (isLoading) {
            val icons = listOf(
                Icons.Default.Cloud,
                Icons.Default.WbSunny,
                Icons.Default.FilterDrama,
                Icons.Default.Air,
                Icons.Default.Thermostat
            )
            var index = 0
            while (isLoading) {
                headerIcon = icons[index % icons.size]
                index++
                kotlinx.coroutines.delay(1000)
            }
        } else {
            val temp = (uiState as? WeatherUiState.Success)?.currentTemperature ?: 20.0
            headerIcon = WeatherDesignUtils.getWeatherIcon(temp)
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Animated weather icon
        AnimatedContent(
            targetState = headerIcon,
            transitionSpec = {
                (scaleIn() + fadeIn()) with (scaleOut() + fadeOut())
            },
            label = "header_icon"
        ) { icon ->
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = "Location",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Nairobi, Kenya",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = when (uiState) {
                    is WeatherUiState.Loading -> "Fetching weather data..."
                    is WeatherUiState.Success -> "Weather updated"
                    is WeatherUiState.Error -> "Failed to load weather"
                },
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp
            )
        }

        // Refresh button with animation
        FloatingActionButton(
            onClick = onRefresh,
            modifier = Modifier.size(48.dp),
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White
        ) {
            val rotation by animateFloatAsState(
                targetValue = if (isLoading) 360f else 0f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "refresh_rotation"
            )

            Icon(
                Icons.Default.Refresh,
                contentDescription = "Refresh",
                modifier = Modifier.scale(if (isLoading) rotation / 360f + 0.8f else 1f)
            )
        }
    }
}

@Composable
private fun MainTemperatureCard(uiState: WeatherUiState) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(64.dp),
                        color = Color.White,
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading temperature...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp
                    )
                }
                is WeatherUiState.Success -> {
                    // Large temperature with animation
                    var visible by remember { mutableStateOf(false) }
                    LaunchedEffect(uiState.currentTemperature) {
                        visible = true
                    }

                    AnimatedVisibility(
                        visible = visible,
                        enter = scaleIn() + fadeIn()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Weather icon
                            Icon(
                                imageVector = WeatherDesignUtils.getWeatherIcon(uiState.currentTemperature),
                                contentDescription = null,
                                tint = WeatherDesignUtils.getWeatherColor(uiState.currentTemperature),
                                modifier = Modifier.size(48.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "${uiState.currentTemperature.toInt()}°C",
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Light,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Text(
                                text = "Current Temperature",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Unable to load temperature",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeatherSuccessContent(successState: WeatherUiState.Success) {
    // Hourly Forecast with enhanced design
    EnhancedHourlyForecast(successState.hourlyData)

    Spacer(modifier = Modifier.height(16.dp))

    // Temperature insights with modern cards
    EnhancedTemperatureInsights(successState.hourlyData)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EnhancedHourlyForecast(hourlyData: List<HourlyWeatherData>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "24-Hour Forecast",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(hourlyData.take(24)) { hourData ->
                    EnhancedHourlyCard(hourData)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun EnhancedHourlyCard(hourData: HourlyWeatherData) {
    val weatherColor = WeatherDesignUtils.getWeatherColor(hourData.temperature)
    val weatherIcon = WeatherDesignUtils.getWeatherIcon(hourData.temperature)

    Card(
        modifier = Modifier
            .width(80.dp)
            .animateContentSize(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.15f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hourData.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Icon(
                imageVector = weatherIcon,
                contentDescription = null,
                tint = weatherColor,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${hourData.temperature.toInt()}°",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EnhancedTemperatureInsights(hourlyData: List<HourlyWeatherData>) {
    val temps = hourlyData.map { it.temperature }
    val maxTemp = temps.maxOrNull() ?: 0.0
    val minTemp = temps.minOrNull() ?: 0.0
    val avgTemp = temps.average()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Temperature Insights",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EnhancedStatCard(
                    label = "Maximum",
                    value = "${maxTemp.toInt()}°C",
                    icon = Icons.Default.KeyboardArrowUp,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                EnhancedStatCard(
                    label = "Minimum",
                    value = "${minTemp.toInt()}°C",
                    icon = Icons.Default.KeyboardArrowDown,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                EnhancedStatCard(
                    label = "Average",
                    value = "${avgTemp.toInt()}°C",
                    icon = Icons.Default.Remove,
                    color = Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun EnhancedStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = label,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        repeat(3) { index ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White.copy(alpha = 0.7f),
                        strokeWidth = 2.dp
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorContent(onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CloudOff,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Unable to load weather data",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Check your connection and try again",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Try Again")
            }
        }
    }
}


@Composable
private fun EnhancedChatFAB(
    onClick: () -> Unit,
    isWeatherLoaded: Boolean
) {
    // Pulsing animation when weather is loaded to encourage interaction
    val infiniteTransition = rememberInfiniteTransition(label = "fab_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isWeatherLoaded) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Rotation animation for the AI icon
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ai_rotation"
    )

    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier
            .size(64.dp)
            .scale(pulseScale),
        containerColor = Color(0xFF8B5CF6),
        contentColor = Color.White,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )

            Icon(
                imageVector = Icons.Default.Psychology,
                contentDescription = "Ask AI about weather",
                modifier = Modifier
                    .size(28.dp)
                    .graphicsLayer { rotationZ = rotation * 0.1f },
                tint = Color.White
            )
        }
    }
}


@Composable
private fun SimpleChatFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Chat,
            contentDescription = "Chat with AI",
            tint = Color.White
        )
    }
}

@Composable
private fun ExtendedChatFAB(
    onClick: () -> Unit,
    isWeatherLoaded: Boolean
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        text = {
            Text(
                text = if (isWeatherLoaded) "Ask AI" else "Chat",
                color = Color.White
            )
        },
        icon = {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = null,
                tint = Color.White
            )
        },
        containerColor = MaterialTheme.colorScheme.primary
    )
}