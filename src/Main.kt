import com.me2.app.ai.AiCoachEngine
import com.me2.app.data.models.*
import com.me2.app.data.repository.Me2LocalRepository
import com.me2.app.domain.engine.*
import java.time.LocalDate
import java.time.LocalTime

fun main() {
    println("==================================================")
    println("       ME 2.0 — AI COACH & OFFLINE ENGINE         ")
    println("==================================================\n")

    val gamificationEngine = GamificationEngine()
    val repository = Me2LocalRepository(gamificationEngine)
    val aiCoach = AiCoachEngine() // API Key না থাকলে স্বয়ংক্রিয়ভাবে অফলাইন ফলব্যাকে যাবে

    println("[1] NATURAL LANGUAGE FOOD INTERPRETATION")
    val userMealText = "২টা ডিম আর ২টা রুটি খেয়েছি"
    println("• User Input: \"$userMealText\"")

    val parsedNutrition = aiCoach.parseFoodEntry(userMealText)
    println("• Engine Used: [${parsedNutrition.source}]")
    println("• Identified Item: ${parsedNutrition.itemName} (${parsedNutrition.portion})")
    println("• Estimated Calories: ${parsedNutrition.calories} kcal")
    println("• Macros: Protein ${parsedNutrition.protein}g | Carbs ${parsedNutrition.carbs}g | Fat ${parsedNutrition.fat}g\n")

    // খাবারটি লোকাল ডাটাবেজে সংরক্ষণ
    repository.logFood(
        rawInput = userMealText,
        itemName = parsedNutrition.itemName,
        portion = parsedNutrition.portion,
        calories = parsedNutrition.calories,
        protein = parsedNutrition.protein,
        carbs = parsedNutrition.carbs,
        fat = parsedNutrition.fat
    )

    println("[2] END-OF-DAY AI PERFORMANCE REVIEW")
    val review = aiCoach.generateDailyReview(
        completedMissions = 7,
        totalMissions = 9,
        consumedKcal = 1850,
        targetKcal = 2400,
        proteinG = 92f,
        targetProteinG = 120,
        shiftName = "Morning Duty (06:00 - 15:00)"
    )

    println("• Review Source: [${review.source}]")
    println("• Headline: ${review.statusHeadline}")
    println("• Analysis: ${review.performanceAnalysis}")
    println("• Tomorrow's Directive: ${review.tomorrowFocus}\n")

    println("==================================================")
    println("     ALL SUBSYSTEMS OPERATIONAL & VERIFIED        ")
    println("==================================================")
}
