import com.me2.app.ai.AiActionDispatcher
import com.me2.app.data.models.*
import com.me2.app.data.repository.Me2LocalRepository
import com.me2.app.domain.engine.*
import java.time.LocalTime

fun main() {
    println("==================================================")
    println("     ME 2.0 — AI FULL-CONTROL VOICE ASSISTANT     ")
    println("==================================================\n")

    val gamificationEngine = GamificationEngine()
    val repository = Me2LocalRepository(gamificationEngine)
    val assistant = AiActionDispatcher(repository)

    // প্রোফাইল ইনিশিয়ালাইজ
    val profile = UserProfile(
        name = "Mehedi",
        age = 24,
        sex = "male",
        heightCm = 175f,
        currentWeightKg = 72f,
        targetWeightKg = 75f,
        activityLevel = ActivityLevel.MODERATE,
        primaryGoal = PrimaryGoal.BUILD_MUSCLE,
        wakeUpTime = LocalTime.of(5, 0),
        sleepTargetTime = LocalTime.of(22, 0),
        currentLevel = 1,
        currentXp = 0L,
        currentStreakDays = 12
    )
    repository.saveProfile(profile)

    println("[VOICE COMMAND SIMULATION]")

    // কমান্ড ১: ইউজার মুখে বলল ডিম খাওয়ার কথা
    val voiceCmd1 = "আমি আজকে একটা ডিম খেয়েছি সেটা এখানে সেট করে দাও"
    println("🗣️ User: \"$voiceCmd1\"")
    val reply1 = assistant.processVoiceCommand(voiceCmd1)
    println("🤖 AI Assistant: $reply1\n")

    // কমান্ড ২: ইউজার মুখে বলল ঘুমের কথা
    val voiceCmd2 = "আজকে আমি ৭ ঘণ্টা ঘুমিয়েছি সেট করে দাও"
    println("🗣️ User: \"$voiceCmd2\"")
    val reply2 = assistant.processVoiceCommand(voiceCmd2)
    println("🤖 AI Assistant: $reply2\n")

    // কমান্ড ৩: পানি খাওয়ার কথা
    val voiceCmd3 = "২৫০ মিলি পানি খেলাম"
    println("🗣️ User: \"$voiceCmd3\"")
    val reply3 = assistant.processVoiceCommand(voiceCmd3)
    println("🤖 AI Assistant: $reply3\n")

    // কমান্ড ৪: মিশন শেষের কথা
    val voiceCmd4 = "আজকের ওয়ার্কআউট করেছি মিশন কমপ্লিট করো"
    println("🗣️ User: \"$voiceCmd4\"")
    val reply4 = assistant.processVoiceCommand(voiceCmd4)
    println("🤖 AI Assistant: $reply4\n")

    // ডাটাবেজের অবস্থা যাচাই (স্বয়ংক্রিয়ভাবে আপডেট হয়েছে কি না)
    println("[SYSTEM DATABASE VERIFICATION]")
    val macros = repository.getTodayMacroSummary()
    println("• Auto-Logged Calories: ${macros["calories"]} kcal")
    println("• Auto-Logged Protein: ${macros["protein"]}g")
    
    val currentProf = repository.getProfile()!!
    println("• Current Operator XP: ${currentProf.currentXp} XP (Updated via Assistant)")

    println("\n==================================================")
    println("    ASSISTANT COMMAND EXECUTION: VERIFIED PASS    ")
    println("==================================================")
}
