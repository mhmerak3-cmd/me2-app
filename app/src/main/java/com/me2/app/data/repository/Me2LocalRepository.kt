package com.me2.app.data.repository

import com.me2.app.data.models.*
import com.me2.app.domain.engine.GamificationEngine
import java.time.LocalDate
import java.time.LocalTime

class Me2LocalRepository(
    private val gamificationEngine: GamificationEngine
) {
    // লোকাল ইন-মেমোরি ডাটা স্টোর (অফলাইন ফার্স্ট)
    private var userProfile: UserProfile? = null
    private val shifts = mutableListOf<ShiftRecord>()
    private val foodEntries = mutableListOf<FoodEntry>()
    private val waterEntries = mutableMapOf<LocalDate, WaterRecord>()
    private val sleepEntries = mutableListOf<SleepRecord>()
    private val xpTransactions = mutableListOf<XPTransaction>()

    private var transactionIdCounter = 1L
    private var foodIdCounter = 1L

    // ইউজার প্রোফাইল সেভ ও রিড
    fun saveProfile(profile: UserProfile) {
        this.userProfile = profile
    }

    fun getProfile(): UserProfile? = userProfile

    // শিফট ম্যানেজমেন্ট
    fun addShift(shift: ShiftRecord) {
        shifts.add(shift)
    }

    fun getShiftForDay(dayOfWeek: Int): ShiftRecord? {
        return shifts.firstOrNull { it.dayOfWeek == dayOfWeek }
    }

    // খাবার ও পুষ্টি যোগ করা
    fun logFood(
        rawInput: String,
        itemName: String,
        portion: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float
    ): FoodEntry {
        val entry = FoodEntry(
            id = foodIdCounter++,
            date = LocalDate.now(),
            timestamp = LocalTime.now(),
            rawInput = rawInput,
            itemName = itemName,
            portionDescription = portion,
            calories = calories,
            proteinGrams = protein,
            carbsGrams = carbs,
            fatGrams = fat,
            isAiEstimated = true
        )
        foodEntries.add(entry)
        return entry
    }

    // আজকের মোট পুষ্টির হিসাব
    fun getTodayMacroSummary(date: LocalDate = LocalDate.now()): Map<String, Number> {
        val todayEntries = foodEntries.filter { it.date == date }
        val totalKcal = todayEntries.sumOf { it.calories }
        val totalProtein = todayEntries.sumOf { it.proteinGrams.toDouble() }.toFloat()
        val totalCarbs = todayEntries.sumOf { it.carbsGrams.toDouble() }.toFloat()
        val totalFat = todayEntries.sumOf { it.fatGrams.toDouble() }.toFloat()

        return mapOf(
            "calories" to totalKcal,
            "protein" to totalProtein,
            "carbs" to totalCarbs,
            "fat" to totalFat
        )
    }

    // পানি যোগ করা (যেমন +250ml বা +500ml)
    fun addWater(amountMl: Int, date: LocalDate = LocalDate.now()): WaterRecord {
        val record = waterEntries.getOrPut(date) { WaterRecord(date = date) }
        record.consumedMl += amountMl
        return record
    }

    // XP প্রদান ও স্বয়ংক্রিয় লেভেল আপডেট
    fun awardXp(amount: Int, reason: String, category: com.me2.app.domain.engine.MissionCategory): XPTransaction {
        val tx = XPTransaction(
            id = transactionIdCounter++,
            date = LocalDate.now(),
            timestamp = LocalTime.now(),
            xpAmount = amount,
            reason = reason,
            category = category
        )
        xpTransactions.add(tx)

        // ইউজারের লেভেল ও মোট এক্সপি আপডেট
        userProfile?.let { profile ->
            profile.currentXp += amount
            val progress = gamificationEngine.calculateProgression(profile.currentXp)
            profile.currentLevel = progress.currentLevel
        }

        return tx
    }
}
