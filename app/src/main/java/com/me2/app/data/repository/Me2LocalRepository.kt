package com.me2.app.data.repository

import com.me2.app.data.local.Me2Vault
import com.me2.app.data.models.*
import com.me2.app.domain.engine.GamificationEngine
import java.time.LocalDate
import java.time.LocalTime

class Me2LocalRepository(
    private val gamificationEngine: GamificationEngine
) {
    private var vault: Me2Vault? = null
    private var userProfile: UserProfile? = null
    private val foodEntries = mutableListOf<FoodEntry>()
    private val waterEntries = mutableMapOf<LocalDate, WaterRecord>()
    private var foodIdCounter = 1L

    fun bindVault(v: Me2Vault) {
        this.vault = v
    }

    fun saveProfile(profile: UserProfile) {
        this.userProfile = profile
    }

    fun getProfile(): UserProfile? {
        val v = vault
        return if (v != null) {
            UserProfile(
                name = "Mehedi",
                currentXp = v.getXp(),
                currentLevel = v.getLevel()
            )
        } else {
            userProfile
        }
    }

    fun logFood(
        rawInput: String,
        itemName: String,
        portion: String,
        calories: Int,
        protein: Float,
        carbs: Float,
        fat: Float
    ): FoodEntry {
        vault?.addMeal(calories, protein)
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

    fun getTodayMacroSummary(date: LocalDate = LocalDate.now()): Map<String, Number> {
        val v = vault
        return if (v != null) {
            mapOf(
                "calories" to v.getCalories(),
                "protein" to v.getProtein(),
                "carbs" to 0f,
                "fat" to 0f
            )
        } else {
            val todayEntries = foodEntries.filter { it.date == date }
            mapOf(
                "calories" to todayEntries.sumOf { it.calories },
                "protein" to todayEntries.sumOf { it.proteinGrams.toDouble() }.toFloat(),
                "carbs" to 0f,
                "fat" to 0f
            )
        }
    }

    fun addWater(amountMl: Int, date: LocalDate = LocalDate.now()): WaterRecord {
        val v = vault
        val total = v?.addWater(amountMl) ?: amountMl
        val record = WaterRecord(date = date, consumedMl = total)
        waterEntries[date] = record
        return record
    }

    fun awardXp(amount: Int, reason: String, category: com.me2.app.domain.engine.MissionCategory): XPTransaction {
        vault?.let { it.setXp(it.getXp() + amount) }
        return XPTransaction(
            id = System.currentTimeMillis(),
            date = LocalDate.now(),
            timestamp = LocalTime.now(),
            xpAmount = amount,
            reason = reason,
            category = category
        )
    }
}
