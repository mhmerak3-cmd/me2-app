package com.me2.app

import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.me2.app.ai.AiActionDispatcher
import com.me2.app.ai.AiCoachEngine
import com.me2.app.data.local.Me2Vault
import com.me2.app.data.models.MealType
import com.me2.app.data.models.RankTier
import com.me2.app.data.models.UserProfile
import com.me2.app.data.repository.Me2LocalRepository
import com.me2.app.domain.engine.GamificationEngine
import java.time.LocalDate

val MeDarkBg = Color(0xFF080F1A)
val MeDarkCard = Color(0xFF121826)
val MeCardBorder = Color(0xFF1E293B)
val MeNeonGreen = Color(0xFF00E676)
val MeGold = Color(0xFFFBBF24)
val MeCyan = Color(0xFF00E5FF)
val MeRed = Color(0xFFEF4444)
val MeMuted = Color(0xFF94A3B8)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vault = Me2Vault(applicationContext)
        val repository = Me2LocalRepository(GamificationEngine()).apply { bindVault(vault) }
        val aiEngine = AiCoachEngine()
        val assistant = AiActionDispatcher(repository, aiEngine, vault)

        setContent {
            var onboarded by remember { mutableStateOf(vault.isOnboarded()) }
            if (!onboarded) {
                OnboardingScreen(vault) { onboarded = true }
            } else {
                MainRpgScreen(vault, assistant)
            }
        }
    }
}

@Composable
fun OnboardingScreen(vault: Me2Vault, onComplete: () -> Unit) {
    var age by remember { mutableStateOf("24") }
    var height by remember { mutableStateOf("175") }
    var weight by remember { mutableStateOf("68") }
    var selectedGoal by remember { mutableStateOf("স্বাস্থ্য ও পেশিবহুল বডি বৃদ্ধি 🏋️") }

    Column(
        modifier = Modifier.fillMaxSize().background(MeDarkBg).padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("ME 2.0", color = MeNeonGreen, fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text("আপনার ব্যক্তিগত প্রোফাইল সেটআপ", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = age, onValueChange = { age = it }, label = { Text("বয়স (Age)", color = MeMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = height, onValueChange = { height = it }, label = { Text("উচ্চতা (Height in cm)", color = MeMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = weight, onValueChange = { weight = it }, label = { Text("ওজন (Weight in kg)", color = MeMuted) },
            colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text("আপনার মূল লক্ষ্য:", color = MeMuted, fontSize = 13.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(6.dp))
        listOf("স্বাস্থ্য ও পেশিবহুল বডি বৃদ্ধি 🏋️", "হাইট বৃদ্ধি ও ফ্লেক্সিবিলিটি 🧍", "ফিটনেস ও মেদ কমানো 🔥").forEach { goal ->
            Row(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                    .background(if (selectedGoal == goal) Color(0xFF1E293B) else MeDarkCard)
                    .clickable { selectedGoal = goal }.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = (selectedGoal == goal), onClick = { selectedGoal = goal })
                Text(goal, color = Color.White, fontSize = 13.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                vault.saveProfile(age.toIntOrNull() ?: 24, height.toFloatOrNull() ?: 175f, weight.toFloatOrNull() ?: 68f, selectedGoal)
                onComplete()
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MeNeonGreen),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("মিশন শুরু করুন →", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

@Composable
fun MainRpgScreen(vault: Me2Vault, assistant: AiActionDispatcher) {
    var selectedTab by remember { mutableStateOf(0) }
    var refreshTrigger by remember { mutableStateOf(0) }
    var selectedDate by remember { mutableStateOf(LocalDate.now().toString()) }

    val profile = remember(refreshTrigger) { vault.getProfile() }
    val rank = remember(refreshTrigger) { vault.getRankTier() }
    val level = remember(refreshTrigger) { vault.getLevel() }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                assistant.executeVoiceDirective(spoken)
                refreshTrigger++
            }
        }
    }

    Scaffold(
        containerColor = MeDarkBg,
        bottomBar = {
            NavigationBar(containerColor = MeDarkCard) {
                listOf(
                    Triple("হোম", Icons.Default.Home, 0),
                    Triple("ডায়েট", Icons.Default.Restaurant, 1),
                    Triple("ক্যালেন্ডার", Icons.Default.DateRange, 2),
                    Triple("মিশন", Icons.Default.CheckCircle, 3)
                ).forEach { (title, icon, index) ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = { Icon(icon, contentDescription = title) },
                        label = { Text(title, fontSize = 10.sp) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MeNeonGreen,
                            selectedTextColor = MeNeonGreen,
                            unselectedIconColor = MeMuted,
                            unselectedTextColor = MeMuted
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
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "ME 2.0 AI কে বলুন...")
                    }
                    speechLauncher.launch(intent)
                },
                containerColor = MeNeonGreen,
                contentColor = Color.Black,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Mic, contentDescription = "ভয়েস")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (selectedTab) {
                0 -> HomeRpgTab(vault, profile, rank, level) { refreshTrigger++ }
                1 -> DietTab(vault, selectedDate) { refreshTrigger++ }
                2 -> CalendarTab(vault, selectedDate, onSelectDate = { selectedDate = it }) { refreshTrigger++ }
                3 -> MissionsHubTab(vault, assistant) { refreshTrigger++ }
            }
        }
    }
}

@Composable
fun HomeRpgTab(vault: Me2Vault, profile: UserProfile, rank: RankTier, level: Int, onRefresh: () -> Unit) {
    val today = vault.getTodayDate()
    val water = vault.getWater(today)
    val missions = vault.getAllMissions()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("OPERATOR MEHEDI", color = MeMuted, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("${rank.badge} ${rank.title} (Lvl $level)", color = MeGold, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(color = MeDarkCard, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MeGold)) {
                    Text("🪙 ${profile.tokens} টোকেন", color = MeGold, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp, 4.dp))
                }
                Surface(color = MeDarkCard, shape = RoundedCornerShape(12.dp), border = androidx.compose.foundation.BorderStroke(1.dp, MeNeonGreen)) {
                    Text("🔥 ${profile.streakDays % 7}/7 দিন", color = MeNeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp, 4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        LinearProgressIndicator(
            progress = ((profile.currentXp % 100).toFloat() / 100f),
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = MeNeonGreen,
            trackColor = Color(0xFF1E293B)
        )
        Text("XP প্রগ্রেস: ${profile.currentXp % 100}/100 • মোট XP: ${profile.currentXp}", color = MeMuted, fontSize = 11.sp, modifier = Modifier.padding(top = 4.dp))

        Spacer(modifier = Modifier.height(14.dp))
        Text("কুইক এন্ট্রি (এক-ট্যাপ)", color = MeMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vault.addWater(250); onRefresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2234)), shape = RoundedCornerShape(10.dp)) {
                Text("+250ml 💧", color = MeCyan)
            }
            Button(onClick = { vault.addWater(500); onRefresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2234)), shape = RoundedCornerShape(10.dp)) {
                Text("+500ml 💧", color = MeCyan)
            }
            Button(onClick = { vault.addDetailedMeal(type = MealType.LUNCH, items = "ভাত ও ডিম/মাছ", cal = 550, protein = 28f); onRefresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2234)), shape = RoundedCornerShape(10.dp)) {
                Text("+খাবার 🥗", color = MeNeonGreen)
            }
            Button(onClick = { vault.markMissionDone(today, "workout", 30); onRefresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A2234)), shape = RoundedCornerShape(10.dp)) {
                Text("+ওয়ার্কআউট 🏋️", color = MeGold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("আজকের কোয়েস্ট ও মিশন", color = MeMuted, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(6.dp))
        missions.forEach { m ->
            val status = vault.getMissionStatus(today, m.id)
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = if (status == "COMPLETED") Color(0xFF0F261C) else MeDarkCard),
                border = androidx.compose.foundation.BorderStroke(1.dp, if (status == "COMPLETED") MeNeonGreen else MeCardBorder)
            ) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(m.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("+${m.xpReward} XP • ${m.category}", color = MeGold, fontSize = 10.sp)
                    }
                    if (status == "COMPLETED") {
                        Text("✓ সম্পন্ন", color = MeNeonGreen, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    } else if (status == "CANCELED") {
                        Text("✕ বাতিল", color = MeRed, fontSize = 12.sp)
                    } else {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { vault.markMissionDone(today, m.id, m.xpReward); onRefresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("✓ টিক", color = MeNeonGreen, fontSize = 11.sp)
                            }
                            Button(onClick = { vault.markMissionCanceled(today, m.id); onRefresh() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B161B)), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
                                Text("✕ বাতিল", color = MeRed, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun DietTab(vault: Me2Vault, date: String, onRefresh: () -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(MealType.LUNCH) }
    var foodText by remember { mutableStateOf("") }
    var calText by remember { mutableStateOf("450") }
    var proteinText by remember { mutableStateOf("25") }

    val meals = vault.getDetailedMeals(date)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("খাবারের তালিকা ($date)", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Button(onClick = { showDialog = true }, colors = ButtonDefaults.buttonColors(containerColor = MeNeonGreen)) {
                Text("+ খাবার যোগ", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))

        if (meals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("আজকে এখনো কোনো খাবার যোগ করা হয়নি।\nউপরে বাটন চেপে বা মুখে বলুন!", color = MeMuted, fontSize = 13.sp)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(meals) { meal ->
                    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MeDarkCard), border = androidx.compose.foundation.BorderStroke(1.dp, MeCardBorder)) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(meal["type"] ?: "", color = MeCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text(meal["items"] ?: "", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                            Text("ক্যালোরি: ${meal["cal"]} kcal • প্রোটিন: ${meal["protein"]}g", color = MeMuted, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            containerColor = MeDarkCard,
            title = { Text("খাবারের বিবরণ লিখুন", color = Color.White, fontSize = 15.sp) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("খাবারের সময়:", color = MeMuted, fontSize = 12.sp)
                    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MealType.values().forEach { t ->
                            FilterChip(selected = selectedType == t, onClick = { selectedType = t }, label = { Text(t.bangla, fontSize = 10.sp) })
                        }
                    }
                    OutlinedTextField(value = foodText, onValueChange = { foodText = it }, label = { Text("যেমন: ১ প্লেট ভাত, রুই মাছ, ডাল") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = calText, onValueChange = { calText = it }, label = { Text("ক্যালোরি (kcal)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = proteinText, onValueChange = { proteinText = it }, label = { Text("প্রোটিন (g)") }, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (foodText.isNotBlank()) {
                        vault.addDetailedMeal(date, selectedType, foodText, calText.toIntOrNull() ?: 400, proteinText.toFloatOrNull() ?: 20f)
                        showDialog = false
                        onRefresh()
                    }
                }, colors = ButtonDefaults.buttonColors(containerColor = MeNeonGreen)) {
                    Text("সেভ করুন", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("বাতিল", color = MeMuted) }
            }
        )
    }
}

@Composable
fun CalendarTab(vault: Me2Vault, selectedDate: String, onSelectDate: (String) -> Unit, onRefresh: () -> Unit) {
    val today = LocalDate.now()
    val pastDays = (0..6).map { today.minusDays(it.toLong()).toString() }
    val meals = vault.getDetailedMeals(selectedDate)
    val water = vault.getWater(selectedDate)

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("লাইফটাইম ক্যালেন্ডার ও হিস্ট্রি", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("যেকোনো তারিখে ট্যাপ করে অতীতের হিস্ট্রি দেখুন", color = MeMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            pastDays.forEach { dateStr ->
                val isSelected = (dateStr == selectedDate)
                Surface(
                    modifier = Modifier.clip(RoundedCornerShape(10.dp)).clickable { onSelectDate(dateStr) },
                    color = if (isSelected) Color(0xFF1E293B) else MeDarkCard,
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) MeCyan else MeCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp, 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(dateStr.substring(8), color = if (isSelected) MeCyan else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(dateStr.substring(5, 7), color = MeMuted, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("📌 $selectedDate তারিখের সংরক্ষিত ডাটা:", color = MeGold, fontSize = 13.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MeDarkCard), border = androidx.compose.foundation.BorderStroke(1.dp, MeCardBorder)) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("💧 পানি পান: $water মিলি", color = MeCyan, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("🥗 খাবারের তালিকা:", color = MeNeonGreen, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                if (meals.isEmpty()) {
                    Text("সেদিন কোনো খাবারের ডাটা এন্ট্রি করা হয়নি।", color = MeMuted, fontSize = 11.sp)
                } else {
                    meals.forEach {
                        Text("• [${it["type"]}] ${it["items"]} (${it["cal"]} kcal, ${it["protein"]}g প্রোটিন)", color = Color.White, fontSize = 12.sp)
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(70.dp))
    }
}

@Composable
fun MissionsHubTab(vault: Me2Vault, assistant: AiActionDispatcher, onRefresh: () -> Unit) {
    var customGoalText by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("🎯 এআই মিশন হাব ও লং-টার্ম গোল", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("এখানে সরাসরি লিখুন বা এআই-কে নতুন মিশন তৈরি করতে বলুন", color = MeMuted, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = customGoalText,
            onValueChange = { customGoalText = it },
            label = { Text("যেমন: আমি হাইট বাড়াতে চাই / বই পড়তে চাই") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = {
                if (customGoalText.isNotBlank()) {
                    assistant.executeVoiceDirective(customGoalText)
                    customGoalText = ""
                    onRefresh()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = MeNeonGreen),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("এআই দিয়ে মিশন তৈরি করুন ✨", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))
        Text("বর্তমান সক্রিয় সকল মিশন:", color = MeGold, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(8.dp))

        vault.getAllMissions().forEach { m ->
            Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp), colors = CardDefaults.cardColors(containerColor = MeDarkCard), border = androidx.compose.foundation.BorderStroke(1.dp, MeCardBorder)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(m.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Text("ক্যাটাগরি: ${m.category} • রিওয়ার্ড: +${m.xpReward} XP ${if (m.isAiCreated) "(এআই তৈরি)" else ""}", color = MeMuted, fontSize = 10.sp)
                }
            }
        }
        Spacer(modifier = Modifier.height(70.dp))
    }
}
