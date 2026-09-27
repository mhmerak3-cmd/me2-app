package com.me2.app.domain.engine

enum class ActivityLevel {
    SEDENTARY, LIGHT, MODERATE, VERY_ACTIVE, ATHLETIC
}

enum class PrimaryGoal {
    BUILD_MUSCLE, IMPROVE_STRENGTH, FITNESS, ENDURANCE, BODY_COMPOSITION, SLEEP_DISCIPLINE, SKIN_HEALTH, PRODUCTIVITY
}

data class CaloricTarget(
    val maintenanceCalories: Int,
    val targetCalories: Int,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val fatGrams: Int
)

class HealthCalculator {

    fun calculateMacros(
        weightKg: Float,
        heightCm: Float,
        age: Int,
        sex: String,
        activityLevel: ActivityLevel,
        goal: PrimaryGoal
    ): CaloricTarget {
        // Mifflin-St Jeor ফর্মুলা দিয়ে BMR বের করা
        val baseBmr = (10f * weightKg) + (6.25f * heightCm) - (5f * age)
        val bmr = if (sex.equals("female", ignoreCase = true)) baseBmr - 161f else baseBmr + 5f

        // অ্যাক্টিভিটি লেভেলের মাল্টিপ্লায়ার
        val multiplier = when (activityLevel) {
            ActivityLevel.SEDENTARY -> 1.2f
            ActivityLevel.LIGHT -> 1.375f
            ActivityLevel.MODERATE -> 1.55f
            ActivityLevel.VERY_ACTIVE -> 1.725f
            ActivityLevel.ATHLETIC -> 1.9f
        }

        val maintenance = (bmr * multiplier).toInt()

        // গোলের ওপর ভিত্তি করে ক্যালোরি সমন্বয় (নিরাপদ সীমার মধ্যে)
        val adjustedCalories = when (goal) {
            PrimaryGoal.BUILD_MUSCLE -> maintenance + 300
            PrimaryGoal.IMPROVE_STRENGTH -> maintenance + 200
            PrimaryGoal.BODY_COMPOSITION -> (maintenance - 350).coerceAtLeast(1500) // ১৫০০ ক্যালোরির নিচে নামবে না
            else -> maintenance
        }

        // প্রোটিন টার্গেট: প্রতি কেজিতে ১.৬ থেকে ২.০ গ্রাম
        val proteinTarget = when (goal) {
            PrimaryGoal.BUILD_MUSCLE, PrimaryGoal.IMPROVE_STRENGTH -> (weightKg * 2.0f).toInt()
            else -> (weightKg * 1.6f).toInt()
        }

        // ফ্যাট টার্গেট: মোট ক্যালোরির প্রায় ২৫% (প্রতি গ্রাম ফ্যাট = ৯ ক্যালোরি)
        val fatTarget = ((adjustedCalories * 0.25f) / 9f).toInt()

        // অবশিষ্ট ক্যালোরি কার্বোহাইড্রেটে যাবে (প্রতি গ্রাম কার্ব = ৪ ক্যালোরি)
        val remainingKcal = adjustedCalories - (proteinTarget * 4) - (fatTarget * 9)
        val carbsTarget = (remainingKcal / 4).coerceAtLeast(50)

        return CaloricTarget(
            maintenanceCalories = maintenance,
            targetCalories = adjustedCalories,
            proteinGrams = proteinTarget,
            carbsGrams = carbsTarget,
            fatGrams = fatTarget
        )
    }
}
