package com.teka.weatheragent.presentation.weather_screen

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Clean weather utilities with consistent purple theme
object CleanWeatherUtils {
    // Consistent purple gradient theme
    val PrimaryGradient = listOf(Color(0xFF667eea), Color(0xFF764ba2))
    val SurfaceWhite = Color.White
    val TextPrimary = Color(0xFF1F2937)
    val TextSecondary = Color(0xFF6B7280)
    val BackgroundLight = Color(0xFFF8FAFC)
    val AccentPurple = Color(0xFF667eea)

    fun getWeatherIcon(temperature: Double): ImageVector {
        return when {
            temperature >= 30 -> Icons.Default.WbSunny
            temperature >= 25 -> Icons.Default.WbSunny
            temperature >= 20 -> Icons.Default.FilterDrama
            temperature >= 15 -> Icons.Default.Cloud
            temperature >= 10 -> Icons.Default.Air
            else -> Icons.Default.AcUnit
        }
    }

    fun getWeatherCondition(temperature: Double): String {
        return when {
            temperature >= 30 -> "Hot"
            temperature >= 25 -> "Warm"
            temperature >= 20 -> "Pleasant"
            temperature >= 15 -> "Cool"
            temperature >= 10 -> "Cold"
            else -> "Very Cold"
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
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(Unit) {
        viewModel.fetchWeatherData()
    }

    Scaffold(
        containerColor = CleanWeatherUtils.BackgroundLight,
        floatingActionButton = {
            CleanChatFAB(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onNavigateToChat()
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
        ) {
            // Clean gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = CleanWeatherUtils.PrimaryGradient + Color.Transparent
                        )
                    )
                    .padding(horizontal = 20.dp, vertical = 32.dp)
            ) {
                CleanHeader(
                    onRefresh = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        viewModel.fetchWeatherData()
                    },
                    currentTemp = (uiState as? WeatherUiState.Success)?.currentTemperature ?: 22.0
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                // Main temperature display
                CleanTemperatureDisplay(uiState = uiState)

                Spacer(modifier = Modifier.height(20.dp))

                // Content
                when (val currentState = uiState) {
                    is WeatherUiState.Success -> {
                        CleanSuccessContent(successState = currentState)
                    }
                    is WeatherUiState.Loading -> {
                        CleanLoadingContent()
                    }
                    is WeatherUiState.Error -> {
                        CleanErrorContent(
                            onRetry = { viewModel.fetchWeatherData() }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Composable
private fun CleanHeader(
    onRefresh: () -> Unit,
    currentTemp: Double
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Nairobi",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Kenya • ${CleanWeatherUtils.getWeatherCondition(currentTemp)}",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Simple refresh button
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(color = Color.White)
                ) { onRefresh() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Refresh",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun CleanTemperatureDisplay(uiState: WeatherUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CleanWeatherUtils.SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (uiState) {
                is WeatherUiState.Loading -> {
                    CircularProgressIndicator(
                        color = CleanWeatherUtils.AccentPurple,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading...",
                        color = CleanWeatherUtils.TextSecondary,
                        fontSize = 16.sp
                    )
                }
                is WeatherUiState.Success -> {
                    // Clean temperature entry animation
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn(animationSpec = tween(600))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Simple weather icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(CleanWeatherUtils.AccentPurple),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = CleanWeatherUtils.getWeatherIcon(uiState.currentTemperature),
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "${uiState.currentTemperature.toInt()}°",
                                fontSize = 72.sp,
                                fontWeight = FontWeight.Light,
                                color = CleanWeatherUtils.TextPrimary,
                                letterSpacing = (-2).sp
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Current Temperature",
                                fontSize = 16.sp,
                                color = CleanWeatherUtils.TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                is WeatherUiState.Error -> {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = CleanWeatherUtils.AccentPurple,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Unable to load",
                        color = CleanWeatherUtils.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CleanSuccessContent(successState: WeatherUiState.Success) {
    // Hourly forecast
    CleanHourlyForecast(successState.hourlyData)

    Spacer(modifier = Modifier.height(20.dp))

    // Temperature stats
    CleanTemperatureStats(successState.hourlyData)
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CleanHourlyForecast(hourlyData: List<HourlyWeatherData>) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = CleanWeatherUtils.AccentPurple,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "24-Hour Forecast",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = CleanWeatherUtils.TextPrimary
            )
        }

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            items(hourlyData.take(24)) { hourData ->
                CleanHourlyItem(hourData)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CleanHourlyItem(hourData: HourlyWeatherData) {
    val haptic = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "item_scale"
    )

    Card(
        modifier = Modifier
            .width(64.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                isPressed = !isPressed
            },
        colors = CardDefaults.cardColors(
            containerColor = CleanWeatherUtils.SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = hourData.time.format(DateTimeFormatter.ofPattern("HH:mm")),
                fontSize = 12.sp,
                color = CleanWeatherUtils.TextSecondary,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(CleanWeatherUtils.AccentPurple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = CleanWeatherUtils.getWeatherIcon(hourData.temperature),
                    contentDescription = null,
                    tint = CleanWeatherUtils.AccentPurple,
                    modifier = Modifier.size(14.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${hourData.temperature.toInt()}°",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = CleanWeatherUtils.TextPrimary
            )
        }
    }
}

@Composable
private fun CleanTemperatureStats(hourlyData: List<HourlyWeatherData>) {
    val temps = hourlyData.map { it.temperature }
    val maxTemp = temps.maxOrNull() ?: 0.0
    val minTemp = temps.minOrNull() ?: 0.0
    val avgTemp = temps.average()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = CleanWeatherUtils.SurfaceWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = null,
                    tint = CleanWeatherUtils.AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Temperature Range",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CleanWeatherUtils.TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CleanStatCard(
                    label = "High",
                    value = "${maxTemp.toInt()}°",
                    icon = Icons.Default.KeyboardArrowUp,
                    color = Color(0xFFEF4444),
                    modifier = Modifier.weight(1f)
                )
                CleanStatCard(
                    label = "Low",
                    value = "${minTemp.toInt()}°",
                    icon = Icons.Default.KeyboardArrowDown,
                    color = Color(0xFF3B82F6),
                    modifier = Modifier.weight(1f)
                )
                CleanStatCard(
                    label = "Average",
                    value = "${avgTemp.toInt()}°",
                    icon = Icons.Default.Remove,
                    color = CleanWeatherUtils.AccentPurple,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CleanStatCard(
    label: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(80.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = CleanWeatherUtils.TextPrimary
            )

            Text(
                text = label,
                fontSize = 12.sp,
                color = CleanWeatherUtils.TextSecondary
            )
        }
    }
}

@Composable
private fun CleanLoadingContent() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            color = CleanWeatherUtils.AccentPurple,
            strokeWidth = 3.dp,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Loading forecast data...",
            color = CleanWeatherUtils.TextSecondary,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun CleanErrorContent(onRetry: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.CloudOff,
            contentDescription = null,
            tint = CleanWeatherUtils.AccentPurple,
            modifier = Modifier.size(40.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Unable to load weather",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = CleanWeatherUtils.TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Check your connection and try again",
            fontSize = 14.sp,
            color = CleanWeatherUtils.TextSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onRetry()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = CleanWeatherUtils.AccentPurple
            ),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier.height(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Try Again",
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CleanChatFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        modifier = Modifier.size(56.dp),
        shape = CircleShape,
        containerColor = Color.Transparent,
        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.radialGradient(CleanWeatherUtils.PrimaryGradient),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.SmartToy,
                contentDescription = "Chat with AI",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}