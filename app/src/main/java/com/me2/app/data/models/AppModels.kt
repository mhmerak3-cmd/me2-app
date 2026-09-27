package com.me2.app.data.models

enum class RankTier(val title: String, val minLevel: Int, val maxLevel: Int, val badge: String) {
    BRONZE("Bronze", 1, 10, "🥉"),
    SILVER("Silver", 11, 25, "🥈"),
    GOLD("Gold", 26, 45, "🥇"),
    PLATINUM("Platinum", 46, 70, "💎"),
    DIAMOND("Diamond", 71, 99, "💠"),
    MASTER("Master", 100, 999, "👑")
}

enum class MealType(val bangla: String) {
    BREAKFAST("সকালের নাস্তা"),
    LUNCH("দুপুরের খাবার"),
    DINNER("রাতের খাবার"),
    SNACKS("বিকেলের নাশতা / স্ন্যাক্স")
}

data class UserProfile(
    val name: String = "Mehedi",
    val age: Int = 24,
    val heightCm: Float = 175f,
    val weightKg: Float = 68f,
    val primaryGoal: String = "স্বাস্থ্য ও পেশিবহুল বডি বৃদ্ধি",
    val targetWaterMl: Int = 2800,
    val targetCalories: Int = 2350,
    val targetProteinG: Float = 110f,
    val currentXp: Long = 0L,
    val currentLevel: Int = 1,
    val tokens: Int = 0,
    val streakDays: Int = 0,
    val isOnboarded: Boolean = false
)

data class MissionModel(
    val id: String,
    val title: String,
    val xpReward: Int,
    val category: String,
    val isAiCreated: Boolean = false
)
