package com.vgod.voiceassistant

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: VoiceAssistantViewModel

    private val requestMicPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.startListening()
        } else {
            viewModel.speak("Mic permission ke bina main aapki awaaz nahi sun sakta.")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            viewModel = viewModel()
            VoiceAssistantTheme {
                AssistantScreen(
                    viewModel = viewModel,
                    onMicClick = { checkPermissionAndListen() }
                )
            }
        }
    }

    private fun checkPermissionAndListen() {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            viewModel.startListening()
        } else {
            requestMicPermission.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
fun VoiceAssistantTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF7C4DFF),
            background = Color(0xFF0E0E14),
            surface = Color(0xFF16161F)
        ),
        content = content
    )
}

@Composable
fun AssistantScreen(viewModel: VoiceAssistantViewModel, onMicClick: () -> Unit) {
    val listeningState by viewModel.listeningState.collectAsState()
    val lastUserText by viewModel.lastUserText.collectAsState()
    val lastAssistantText by viewModel.lastAssistantText.collectAsState()
    val history by viewModel.history.collectAsState()

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Voice Assistant",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = statusLabel(listeningState),
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (lastUserText.isNotBlank()) {
                        Text("Aap:", color = Color(0xFF7C4DFF), fontWeight = FontWeight.Bold)
                        Text(lastUserText, color = Color.White, modifier = Modifier.padding(bottom = 12.dp))
                    }
                    if (lastAssistantText.isNotBlank()) {
                        Text("Assistant:", color = Color(0xFF03DAC5), fontWeight = FontWeight.Bold)
                        Text(lastAssistantText, color = Color.White)
                    }
                    if (lastUserText.isBlank() && lastAssistantText.isBlank()) {
                        Text(
                            "Mic dabaakar bolna shuru kijiye...",
                            color = Color.Gray
                        )
                    }

                    if (history.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(20.dp))
                        Text("History", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                            items(history.reversed()) { entry ->
                                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                                    Text("• ${entry.userText}", color = Color.LightGray, fontSize = 13.sp)
                                    Text("  ${entry.assistantText}", color = Color.DarkGray, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            MicButton(
                listeningState = listeningState,
                onClick = onMicClick
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun MicButton(listeningState: ListeningState, onClick: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by if (listeningState == ListeningState.LISTENING) {
        infiniteTransition.animateFloatAsState(
            initialValue = 1f,
            targetValue = 1.25f,
            animationSpec = infiniteRepeatable(
                animation = tween(600),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseScale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }

    val buttonColor = when (listeningState) {
        ListeningState.LISTENING -> Color(0xFFFF5252)
        ListeningState.PROCESSING -> Color(0xFFFFA726)
        ListeningState.IDLE -> Color(0xFF7C4DFF)
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .graphicsLayer(scaleX = pulseScale, scaleY = pulseScale)
            .clip(CircleShape)
            .background(buttonColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = when (listeningState) {
                ListeningState.LISTENING -> "●"
                ListeningState.PROCESSING -> "…"
                ListeningState.IDLE -> "🎤"
            },
            fontSize = 32.sp,
            color = Color.White
        )
    }
}

private fun statusLabel(state: ListeningState): String = when (state) {
    ListeningState.IDLE -> "Tap mic to speak"
    ListeningState.LISTENING -> "Sun raha hoon..."
    ListeningState.PROCESSING -> "Samajh raha hoon..."
}
