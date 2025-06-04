package com.teka.weatheragent.presentation.chat_screen

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
private fun MessageBubble(message: ChatMessage) {
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
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Color.White,
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
                
                // Temperature highlight (if message contains temperature)
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
            containerColor = Color.White.copy(alpha = 0.2f)
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
                    tint = Color.White,
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
    // Extracting temperature values from the message
    val temperatureRegex = """(\d+(?:\.\d+)?)\s*°[CF]?""".toRegex()
    val temperatures = temperatureRegex.findAll(message).map { it.value }.toList()
    
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
                    tint = Color.White,
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
private fun TypingIndicator() {
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
            // Animated weather icon while thinking
            var currentIcon by remember { mutableStateOf(Icons.Default.Cloud) }
            
            LaunchedEffect(Unit) {
                val icons = listOf(
                    Icons.Default.Cloud,
                    Icons.Default.WbSunny,
                    Icons.Default.Grain,
                    Icons.Default.Air
                )
                var index = 0
                while (true) {
                    kotlinx.coroutines.delay(1000)
                    currentIcon = icons[index % icons.size]
                    index++
                }
            }
            
            AnimatedContent(
                targetState = currentIcon,
                transitionSpec = { fadeIn() with fadeOut() },
                label = "weather_icon_transition"
            ) { icon ->
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
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
                        animationSpec = androidx.compose.animation.core.tween(500),
                        label = "dot_animation"
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