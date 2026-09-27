package com.me2.app.data.models

import com.me2.app.domain.engine.ActivityLevel
import com.me2.app.domain.engine.PrimaryGoal
import com.me2.app.domain.engine.MissionCategory
import com.me2.app.domain.engine.MissionDifficulty
import com.me2.app.domain.engine.ShiftType
import java.time.LocalDate
import java.time.LocalTime

// ১. ইউজার প্রোফাইল মডেল
data class UserProfile(
    val id: Int = 1,
    val name: String,
    val age: Int,
    val sex: String,
    val heightCm: Float,
    val currentWeightKg: Float,
    val targetWeightKg: Float?,
    val activityLevel: ActivityLevel,
    val primaryGoal: PrimaryGoal,
    val wakeUpTime: LocalTime,
    val sleepTargetTime: LocalTime,
    var currentLevel: Int = 1,
    var currentXp: Long = 0L,
    var currentStreakDays: Int = 0,
    var isRecoveryMode: Boolean = false
)

// ২. কাজের শিফট মডেল
data class ShiftRecord(
    val shiftId: Long,
    val shiftName: String,
    val shiftType: ShiftType,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val dayOfWeek: Int // ১ = সোমবার ... ৭ = রবিবার
)

// ৩. খাদ্য ও পুষ্টি লগ মডেল (AI এস্টিমেটেড ফ্ল্যাগ সহ)
data class FoodEntry(
    val id: Long,
    val date: LocalDate,
    val timestamp: LocalTime,
    val rawInput: String,
    val itemName: String,
    val portionDescription: String,
    val calories: Int,
    val proteinGrams: Float,
    val carbsGrams: Float,
    val fatGrams: Float,
    val isAiEstimated: Boolean = true
)

// ৪. পানি ও হাইড্রেশন রেকর্ড
data class WaterRecord(
    val date: LocalDate,
    val targetMl: Int = 2500,
    var consumedMl: Int = 0
)

// ৫. স্লিপ লগ মডেল
data class SleepRecord(
    val date: LocalDate,
    val bedTime: LocalTime,
    val wakeTime: LocalTime,
    val durationMinutes: Int,
    val targetMinutes: Int = 480
)

// ৬. XP ট্রানজ্যাকশন হিস্টোরি
data class XPTransaction(
    val id: Long,
    val date: LocalDate,
    val timestamp: LocalTime,
    val xpAmount: Int,
    val reason: String,
    val category: MissionCategory
)
