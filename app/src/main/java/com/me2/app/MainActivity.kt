package com.me2.app

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me2.app.ai.AiActionDispatcher
import com.me2.app.ai.AiCoachEngine
import com.me2.app.data.local.Me2Vault
import com.me2.app.data.repository.Me2LocalRepository
import com.me2.app.domain.engine.GamificationEngine

// Theme Palette (ME 2.0 Cyber Dark)
val MeBgDark = Color(0xFF080F1A)
val MeCardDark = Color(0xFF121826)
val MeCardStroke = Color(0xFF1E293B)
val MeNeonGreen = Color(0xFF00E676)
val MeAmberGold = Color(0xFFFBBF24)
val MeCyanAccent = Color(0xFF00E5FF)
val MeTextWhite = Color(0xFFF8FAFC)
val MeTextMuted = Color(0xFF94A3B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val vault = Me2Vault(applicationContext)
        val gamificationEngine = GamificationEngine()
        val repository = Me2LocalRepository(gamificationEngine).apply {
            bindVault(vault)
        }
        val aiEngine = AiCoachEngine()
        val assistant = AiActionDispatcher(repository, aiEngine)

        setContent {
            Me2AppScreen(vault = vault, repository = repository, assistant = assistant)
        }
    }
}

data class MissionItem(val id: String, val title: String, val xp: Int, val icon: ImageVector)

@Composable
fun Me2AppScreen(
    vault: Me2Vault,
    repository: Me2LocalRepository,
    assistant: AiActionDispatcher
) {
    var xpState by remember { mutableStateOf(vault.getXp()) }
    var levelState by remember { mutableStateOf(vault.getLevel()) }
    var waterMl by remember { mutableStateOf(vault.getWater()) }
    var calories by remember { mutableStateOf(vault.getCalories()) }
    var protein by remember { mutableStateOf(vault.getProtein()) }
    var completedMissions by remember { mutableStateOf(vault.getCompletedMissions()) }
    var aiStatusText by remember { mutableStateOf("সিস্টেম প্রস্তুত। ভয়েস কমান্ড দিন অথবা দ্রুত বাটন ব্যবহার করুন।") }
    var selectedTab by remember { mutableStateOf(0) }

    fun refreshState() {
        xpState = vault.getXp()
        levelState = vault.getLevel()
        waterMl = vault.getWater()
        calories = vault.getCalories()
        protein = vault.getProtein()
        completedMissions = vault.getCompletedMissions()
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                val reply = assistant.executeVoiceDirective(spoken)
                aiStatusText = "🗣️ "$spoken"\n\n🤖 $reply"
                refreshState()
            }
        }
    }

    val missions = listOf(
        MissionItem("workout", "২০ মিনিট ওয়ার্কআউট", 30, Icons.Filled.FitnessCenter),
        MissionItem("water", "২.৫ লিটার পানি পান", 15, Icons.Filled.LocalDrink),
        MissionItem("protein", "১০০ গ্রাম প্রোটিন পূরণ", 20, Icons.Filled.Restaurant),
        MissionItem("focus", "৪৫ মিনিট স্কিল লার্নিং", 25, Icons.Filled.Psychology)
    )

    Scaffold(
        containerColor = MeBgDark,
        bottomBar = {
            NavigationBar(
                containerColor = MeCardDark,
                contentColor = MeTextWhite
            ) {
                listOf(
                    Triple("হোম", Icons.Filled.Home, 0),
                    Triple("মিশন", Icons.Filled.CheckCircle, 1),
                    Triple("ডায়েট", Icons.Filled.Restaurant, 2),
                    Triple("কোচ", Icons.Filled.SmartToy, 3)
                ).forEach { (label, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = label) },
                        label = { Text(label, fontSize = 11.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MeNeonGreen,
                            selectedTextColor = MeNeonGreen,
                            unselectedIconColor = MeTextMuted,
                            unselectedTextColor = MeTextMuted,
                            indicatorColor = Color(0xFF1E293B)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, "bn-BD")
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "ME 2.0 অ্যাসিস্ট্যান্ট শুনছে...")
                    }
                    speechLauncher.launch(intent)
                },
                containerColor = MeNeonGreen,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Filled.Mic, contentDescription = "Voice Input", modifier = Modifier.size(28.dp))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ১. শীর্ষ হেডার
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ME 2.0 OPERATOR", color = MeTextMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Text("LEVEL %02d".format(levelState), color = MeTextWhite, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MeCardDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeAmberGold)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔥 12 STREAK", color = MeAmberGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // লেভেল প্রগ্রেস বার
            val xpInCurrentLevel = (xpState % 100).toFloat() / 100f
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MeCardDark)
                    .border(1.dp, MeCardStroke, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("XP প্রগ্রেস: ${xpState % 100}/100", color = MeTextWhite, fontSize = 12.sp)
                    Text("মোট XP: $xpState", color = MeNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { xpInCurrentLevel },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = MeNeonGreen,
                    trackColor = Color(0xFF1E293B)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ২. দ্রুত বাটন (Quick Action Bar)
            Text("কুইক অ্যাকশন (ম্যানুয়াল এন্ট্রি)", color = MeTextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        vault.addWater(250)
                        refreshState()
                        aiStatusText = "💧 ২৫০ মিলি পানি যোগ করা হয়েছে (+10 XP)"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+250ml 💧", color = MeCyanAccent)
                }

                Button(
                    onClick = {
                        vault.addWater(500)
                        refreshState()
                        aiStatusText = "💧 ৫০০ মিলি পানি যোগ করা হয়েছে (+10 XP)"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+500ml 💧", color = MeCyanAccent)
                }

                Button(
                    onClick = {
                        vault.addMeal(450, 25f)
                        refreshState()
                        aiStatusText = "🥗 সুষম খাবার যোগ করা হয়েছে: 450 kcal, 25g প্রোটিন (+25 XP)"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+খাবার 🥗", color = MeNeonGreen)
                }

                Button(
                    onClick = {
                        vault.toggleMission("workout", 30)
                        refreshState()
                        aiStatusText = "🏋️ ওয়ার্কআউট সেশন আপডেট হয়েছে!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("+ওয়ার্কআউট 🏋️", color = MeAmberGold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ৩. ডেইলি ম্যাট্রিক্স কার্ড (Hydration & Nutrition)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MeCardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeCardStroke)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("পানি (Hydration)", color = MeCyanAccent, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("${waterMl} / 2500 ml", color = MeTextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        val waterProgress = (waterMl.toFloat() / 2500f).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { waterProgress },
                            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                            color = MeCyanAccent,
                            trackColor = Color(0xFF1E293B)
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MeCardDark),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MeCardStroke)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text("পুষ্টি (Nutrition)", color = MeNeonGreen, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("$calories kcal", color = MeTextWhite, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("প্রোটিন: ${protein}g", color = MeTextMuted, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ৪. দৈনিক মিশন তালিকা (Interactive Checklist)
            Text("দৈনিক মিশন চেকলিস্ট", color = MeTextMuted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(8.dp))

            missions.forEach { mission ->
                val isDone = completedMissions.contains(mission.id)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val done = vault.toggleMission(mission.id, mission.xp)
                            refreshState()
                            aiStatusText = if (done) "অভিনন্দন! "${mission.title}" মিশন সম্পন্ন! (+${mission.xp} XP)" else ""${mission.title}" মিশন আনচেক করা হয়েছে।"
                        },
                    color = if (isDone) Color(0xFF0F291E) else MeCardDark,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isDone) MeNeonGreen else MeCardStroke
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                mission.icon,
                                contentDescription = null,
                                tint = if (isDone) MeNeonGreen else MeTextMuted,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    mission.title,
                                    color = MeTextWhite,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text("+${mission.xp} XP", color = MeAmberGold, fontSize = 11.sp)
                            }
                        }
                        Icon(
                            if (isDone) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isDone) MeNeonGreen else MeTextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ৫. এআই কোচ স্ট্যাটাস বক্স ও ডিরেক্টিভ চিপস
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MeCardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF25334D))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.SmartToy, contentDescription = null, tint = MeCyanAccent, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ME 2.0 AI কো-পাইলট", color = MeCyanAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        aiStatusText,
                        color = MeTextWhite,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("২৫০ মিলি পানি খেয়েছি", "ওয়ার্কআউট করেছি", "আজকের সামারি").forEach { prompt ->
                            SuggestionChip(
                                onClick = {
                                    val reply = assistant.executeVoiceDirective(prompt)
                                    aiStatusText = "🗣️ "$prompt"\n\n🤖 $reply"
                                    refreshState()
                                },
                                label = { Text(prompt, fontSize = 11.sp, color = MeTextWhite) },
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = Color(0xFF1E293B)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, MeCardStroke)
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(70.dp))
        }
    }
}
