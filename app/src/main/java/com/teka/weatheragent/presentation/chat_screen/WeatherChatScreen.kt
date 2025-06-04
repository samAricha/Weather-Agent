package com.teka.weatheragent.presentation.chat_screen

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

data class WeatherCondition(
    val icon: ImageVector,
    val description: String,
    val color: Color = Color.White
)

object WeatherIconUtils {

    fun detectWeatherCondition(message: String): WeatherCondition? {
        val lowerMessage = message.lowercase()

        return when {
            // Sunny conditions
            lowerMessage.contains("sunny") ||
                    lowerMessage.contains("clear") ||
                    lowerMessage.contains("bright") ||
                    lowerMessage.contains("sunshine") ->
                WeatherCondition(Icons.Default.WbSunny, "Sunny", Color(0xFFFFD700))

            // Cloudy conditions
            lowerMessage.contains("cloudy") ||
                    lowerMessage.contains("overcast") ||
                    lowerMessage.contains("clouds") ->
                WeatherCondition(Icons.Default.Cloud, "Cloudy", Color(0xFF9CA3AF))

            // Rainy conditions
            lowerMessage.contains("rain") ||
                    lowerMessage.contains("drizzle") ||
                    lowerMessage.contains("shower") ||
                    lowerMessage.contains("wet") ->
                WeatherCondition(Icons.Default.Grain, "Rainy", Color(0xFF60A5FA))

            // Snowy conditions
            lowerMessage.contains("snow") ||
                    lowerMessage.contains("blizzard") ||
                    lowerMessage.contains("flurries") ->
                WeatherCondition(Icons.Default.AcUnit, "Snowy", Color(0xFFE5E7EB))

            // Windy conditions
            lowerMessage.contains("windy") ||
                    lowerMessage.contains("breezy") ||
                    lowerMessage.contains("gusty") ->
                WeatherCondition(Icons.Default.Air, "Windy", Color(0xFF10B981))

            // Stormy conditions
            lowerMessage.contains("storm") ||
                    lowerMessage.contains("thunder") ||
                    lowerMessage.contains("lightning") ->
                WeatherCondition(Icons.Default.Thunderstorm, "Stormy", Color(0xFF7C3AED))

            // Foggy conditions
            lowerMessage.contains("fog") ||
                    lowerMessage.contains("mist") ||
                    lowerMessage.contains("hazy") ->
                WeatherCondition(Icons.Default.CloudQueue, "Foggy", Color(0xFF6B7280))

            // Hot conditions
            lowerMessage.contains("hot") ||
                    lowerMessage.contains("scorching") ||
                    lowerMessage.contains("blazing") ->
                WeatherCondition(Icons.Default.Whatshot, "Hot", Color(0xFFEF4444))

            // Cold conditions
            lowerMessage.contains("cold") ||
                    lowerMessage.contains("freezing") ||
                    lowerMessage.contains("chilly") ||
                    lowerMessage.contains("frost") ->
                WeatherCondition(Icons.Default.Thermostat, "Cold", Color(0xFF3B82F6))

            // Partly cloudy
            lowerMessage.contains("partly cloudy") ||
                    lowerMessage.contains("partially cloudy") ||
                    lowerMessage.contains("mixed") ->
                WeatherCondition(Icons.Default.FilterDrama, "Partly Cloudy", Color(0xFFF59E0B))

            else -> null
        }
    }

    fun hasTemperature(message: String): Boolean {
        return message.contains("°") ||
                message.contains("degrees") ||
                message.contains("temperature")
    }

    fun extractTemperatures(message: String): List<String> {
        val temperatureRegex = """(\d+(?:\.\d+)?)\s*°[CF]?""".toRegex()
        return temperatureRegex.findAll(message).map { it.value }.toList()
    }
}

@Composable
fun WeatherChatScreen(
    viewModel: WeatherChatViewModel = viewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(uiState.messages.size - 1)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1E3A8A), // Deep blue
                        Color(0xFF3B82F6), // Blue
                        Color(0xFF60A5FA)  // Light blue
                    )
                )
            )
    ) {
        // Header
        EnhancedHeader(
            isLoading = uiState.isLoading,
            onNavigateBack = onNavigateBack // Pass the callback
        )
        // Messages
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.messages.isEmpty()) {
                item {
                    WelcomeMessage()
                }
            }

            items(uiState.messages) { message ->
                AnimatedVisibility(
                    visible = true,
                    enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn()
                ) {
                    EnhancedMessageBubble(message = message)
                }
            }

            if (uiState.isLoading) {
                item {
                    EnhancedTypingIndicator()
                }
            }
        }

        // Input area
        MessageInput(
            message = uiState.currentMessage,
            onMessageChange = viewModel::updateCurrentMessage,
            onSendMessage = {
                viewModel.sendMessage()
                keyboardController?.hide()
            },
            isLoading = uiState.isLoading
        )
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EnhancedHeader(
    isLoading: Boolean,
    onNavigateBack: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .statusBarsPadding(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to weather",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Animated header icon
            var headerIcon by remember { mutableStateOf(Icons.Default.Cloud) }

            LaunchedEffect(isLoading) {
                if (isLoading) {
                    val icons = listOf(
                        Icons.Default.Cloud,
                        Icons.Default.WbSunny,
                        Icons.Default.Grain,
                        Icons.Default.Air
                    )
                    var index = 0
                    while (isLoading) {
                        headerIcon = icons[index % icons.size]
                        index++
                        kotlinx.coroutines.delay(1000)
                    }
                } else {
                    headerIcon = Icons.Default.Cloud
                }
            }

            AnimatedContent(
                targetState = headerIcon,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "header_icon_transition"
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Weather Agent",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (isLoading) "Analyzing weather..." else "Ready to help",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun WelcomeMessage() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Animated welcome icon
            var welcomeIcon by remember { mutableStateOf(Icons.Default.Cloud) }

            LaunchedEffect(Unit) {
                val icons = listOf(
                    Icons.Default.Cloud,
                    Icons.Default.WbSunny,
                    Icons.Default.Grain,
                    Icons.Default.AcUnit
                )
                var index = 0
                while (true) {
                    welcomeIcon = icons[index % icons.size]
                    index++
                    kotlinx.coroutines.delay(2000)
                }
            }

            AnimatedContent(
                targetState = welcomeIcon,
                transitionSpec = {
                    slideInVertically { -it } + fadeIn() with
                            slideOutVertically { it } + fadeOut()
                },
                label = "welcome_icon_transition"
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hi! I'm your Weather Agent",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Ask me about the weather anywhere in the world! I can provide forecasts, current conditions, and even send you detailed reports.",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EnhancedMessageBubble(message: ChatMessage) {
    val weatherCondition = if (!message.isFromUser) {
        remember(message.content) { WeatherIconUtils.detectWeatherCondition(message.content) }
    } else null

    val hasTemperature = if (!message.isFromUser) {
        remember(message.content) { WeatherIconUtils.hasTemperature(message.content) }
    } else false

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isFromUser) {
            Arrangement.End
        } else {
            Arrangement.Start
        }
    ) {
        if (!message.isFromUser) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = weatherCondition?.icon ?: Icons.Default.Cloud,
                    contentDescription = null,
                    tint = weatherCondition?.color ?: Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (message.isFromUser) {
                    Color.White
                } else {
                    Color.White.copy(alpha = 0.15f)
                }
            ),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (message.isFromUser) 16.dp else 4.dp,
                bottomEnd = if (message.isFromUser) 4.dp else 16.dp
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Weather header with animation
                if (weatherCondition != null && !message.isFromUser) {
                    WeatherHeader(weatherCondition = weatherCondition)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Text(
                    text = message.content,
                    color = if (message.isFromUser) {
                        Color.Black
                    } else {
                        Color.White
                    },
                    fontSize = 14.sp
                )

                // Temperature highlight
                if (hasTemperature && !message.isFromUser) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TemperatureHighlight(message.content)
                }
            }
        }

        if (message.isFromUser) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "You",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun WeatherHeader(weatherCondition: WeatherCondition) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = weatherCondition.color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            var visible by remember { mutableStateOf(false) }

            LaunchedEffect(weatherCondition) {
                visible = true
            }

            AnimatedVisibility(
                visible = visible,
                enter = scaleIn() + fadeIn()
            ) {
                Icon(
                    imageVector = weatherCondition.icon,
                    contentDescription = weatherCondition.description,
                    tint = weatherCondition.color,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = weatherCondition.description,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun TemperatureHighlight(message: String) {
    val temperatures = WeatherIconUtils.extractTemperatures(message)

    if (temperatures.isNotEmpty()) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF3B82F6).copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Thermostat,
                    contentDescription = "Temperature",
                    tint = Color(0xFF60A5FA),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = temperatures.joinToString(" • "),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun EnhancedTypingIndicator() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            // Enhanced animated weather icon while thinking
            var currentIcon by remember { mutableStateOf(Icons.Default.Cloud) }
            var currentColor by remember { mutableStateOf(Color.White) }

            LaunchedEffect(Unit) {
                val iconColorPairs = listOf(
                    Icons.Default.Cloud to Color(0xFF9CA3AF),
                    Icons.Default.WbSunny to Color(0xFFFFD700),
                    Icons.Default.Grain to Color(0xFF60A5FA),
                    Icons.Default.Air to Color(0xFF10B981),
                    Icons.Default.AcUnit to Color(0xFFE5E7EB)
                )
                var index = 0
                while (true) {
                    kotlinx.coroutines.delay(800)
                    val (icon, color) = iconColorPairs[index % iconColorPairs.size]
                    currentIcon = icon
                    currentColor = color
                    index++
                }
            }

            AnimatedContent(
                targetState = currentIcon to currentColor,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                            fadeOut(animationSpec = tween(300))
                },
                label = "weather_icon_transition"
            ) { (icon, color) ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val alpha by animateFloatAsState(
                        targetValue = if ((System.currentTimeMillis() / 500) % 3 == index.toLong()) 1f else 0.3f,
                        animationSpec = tween(500),
                        label = "dot_animation_$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = alpha))
                    )
                    if (index < 2) Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun MessageInput(
    message: String,
    onMessageChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    isLoading: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding(),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = message,
                onValueChange = onMessageChange,
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "Ask about weather conditions...",
                        color = Color.White.copy(alpha = 0.6f)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = Color.White.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendMessage() }),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = onSendMessage,
                modifier = Modifier.size(48.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF1E3A8A),
                // enabled = message.isNotBlank() && !isLoading
            ) {
                AnimatedContent(
                    targetState = isLoading,
                    transitionSpec = { fadeIn() with fadeOut() },
                    label = "send_button_animation"
                ) { loading ->
                    if (loading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF1E3A8A),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send message"
                        )
                    }
                }
            }
        }
    }
}