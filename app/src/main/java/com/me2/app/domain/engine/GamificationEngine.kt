package com.me2.app.domain.engine

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor
import kotlin.math.pow

data class LevelProgress(
    val currentLevel: Int,
    val currentLevelXp: Long,
    val xpForNextLevel: Long,
    val progressFraction: Float
)

class GamificationEngine {

    // লেভেল অনুযায়ী প্রয়োজনীয় XP হিসাব: 100 * (level ^ 1.5)
    fun calculateRequiredXpForLevel(level: Int): Long {
        if (level <= 1) return 100L
        return floor(100.0 * (level.toDouble().pow(1.5))).toLong()
    }

    // মোট অর্জিত XP থেকে বর্তমান লেভেল ও প্রগ্রেস বের করা
    fun calculateProgression(totalXp: Long): LevelProgress {
        var level = 1
        var accumulatedXp = 0L

        while (true) {
            val neededForThisLevel = calculateRequiredXpForLevel(level)
            if (accumulatedXp + neededForThisLevel > totalXp) {
                val currentLevelXp = totalXp - accumulatedXp
                val fraction = (currentLevelXp.toFloat() / neededForThisLevel.toFloat()).coerceIn(0f, 1f)
                return LevelProgress(
                    currentLevel = level,
                    currentLevelXp = currentLevelXp,
                    xpForNextLevel = neededForThisLevel,
                    progressFraction = fraction
                )
            }
            accumulatedXp += neededForThisLevel
            level++
        }
    }

    // স্ট্রিক এবং রিকভারি মোড যাচাই
    fun evaluateStreak(
        lastActiveDate: LocalDate,
        today: LocalDate,
        completedCoreMissionsCount: Int,
        currentStreak: Int
    ): Pair<Int, Boolean> {
        val daysBetween = ChronoUnit.DAYS.between(lastActiveDate, today)

        return when {
            daysBetween == 0L -> {
                // একই দিনে থাকলে স্ট্রিক আগেরটাই থাকবে
                Pair(currentStreak, false)
            }
            daysBetween == 1L -> {
                // পরের দিন: ন্যূনতম ৩টি কোর মিশন শেষ করলে স্ট্রিক বাড়বে
                if (completedCoreMissionsCount >= 3) {
                    Pair(currentStreak + 1, false)
                } else {
                    // টার্গেট পূরণ না হলে রিকভারি মোড অন হবে, স্ট্রিক কাটবে না
                    Pair(currentStreak, true)
                }
            }
            daysBetween == 2L -> {
                // ১ দিন গ্যাপ: সরাসরি স্ট্রিক শূন্য না করে রিকভারি মোড অন
                Pair(currentStreak, true)
            }
            else -> {
                // ২ দিনের বেশি মিস হলে স্ট্রিক নতুন করে শুরু হবে
                Pair(1, false)
            }
        }
    }
}
