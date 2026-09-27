package com.me2.app
import com.me2.app.ai.AiActionDispatcher
import com.me2.app.ai.AiCoachEngine
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me2.app.data.models.UserProfile
import com.me2.app.data.repository.Me2LocalRepository
import com.me2.app.domain.engine.GamificationEngine
import com.me2.app.domain.engine.ActivityLevel
import com.me2.app.domain.engine.PrimaryGoal
import java.time.LocalTime
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val gamificationEngine = GamificationEngine()
    private val repository = Me2LocalRepository(gamificationEngine)
    private val assistant = AiActionDispatcher(repository)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // বেস প্রোফাইল ইনিশিয়ালাইজ
        repository.saveProfile(
            UserProfile(
                name = "Mehedi",
                age = 24,
                sex = "male",
                heightCm = 175f,
                currentWeightKg = 72f,
                targetWeightKg = 75f,
                activityLevel = ActivityLevel.MODERATE,
                primaryGoal = PrimaryGoal.BUILD_MUSCLE,
                wakeUpTime = LocalTime.of(5, 0),
                sleepTargetTime = LocalTime.of(22, 0)
            )
        )

        setContent {
            Me2AppRoot(repository, assistant)
        }
    }
}

@Composable
fun Me2AppRoot(repository: Me2LocalRepository, assistant: AiActionDispatcher) {
    var aiStatusText by remember { mutableStateOf("মাইক্রোফোন চাপুন এবং কথা বলুন...") }
    var xpState by remember { mutableStateOf(repository.getProfile()?.currentXp ?: 0L) }
    var levelState by remember { mutableStateOf(repository.getProfile()?.currentLevel ?: 1) }
    var macroState by remember { mutableStateOf(repository.getTodayMacroSummary()) }

    // ভয়েস ইনপুট লঞ্চার
    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val reply = assistant.executeVoiceDirective(spoken)
                aiStatusText = "🗣️ \"$spoken\"\n\n🤖 $reply"
                // স্টেট রিফ্রেশ
                xpState = repository.getProfile()?.currentXp ?: 0L
                levelState = repository.getProfile()?.currentLevel ?: 1
                macroState = repository.getTodayMacroSummary()
            }
        }
    }

    MaterialTheme(colorScheme = darkColorScheme()) {
        Scaffold(
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "আপনার নির্দেশ বলুন...")
                        }
                        speechLauncher.launch(intent)
                    },
                    containerColor = Color(0xFFFFB800),
                    contentColor = Color.Black,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Mic, contentDescription = "Voice Directive", modifier = Modifier.size(32.dp))
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF0D0F12))
                    .padding(padding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // HUD Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("OPERATOR MEHEDI", color = Color(0xFF8E95A5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("LEVEL 0$levelState", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    }
                    Text("$xpState XP", color = Color(0xFFFFB800), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                // AI Response Status Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFFB800).copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF15181E))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("⚡ AI ASSISTANT", color = Color(0xFFFFB800), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(aiStatusText, color = Color.White, fontSize = 14.sp)
                    }
                }

                // Daily Metrics Matrix
                Text("DAILY MATRIX", color = Color(0xFF8E95A5), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricBox("CALORIES", "${macroState["calories"]} kcal", Modifier.weight(1f))
                    MetricBox("PROTEIN", "${macroState["protein"]} g", Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun MetricBox(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFF15181E),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(label, color = Color(0xFF8E95A5), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
        }
    }
}
