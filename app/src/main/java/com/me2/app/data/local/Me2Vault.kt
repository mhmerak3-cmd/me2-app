package com.me2.app.data.local

import android.content.Context
import android.content.SharedPreferences
import com.me2.app.data.models.MealType
import com.me2.app.data.models.MissionModel
import com.me2.app.data.models.RankTier
import com.me2.app.data.models.UserProfile
import java.time.LocalDate

class Me2Vault(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("me2_rpg_master_vault", Context.MODE_PRIVATE)

    fun isOnboarded(): Boolean = prefs.getBoolean("is_onboarded", false)

    fun saveProfile(age: Int, heightCm: Float, weightKg: Float, goal: String) {
        val water = ((weightKg * 35).toInt()).coerceAtLeast(2000)
        val cal = ((weightKg * 33).toInt()).coerceAtLeast(2000)
        val protein = (weightKg * 1.6f)
        prefs.edit()
            .putBoolean("is_onboarded", true)
            .putInt("age", age)
            .putFloat("height", heightCm)
            .putFloat("weight", weightKg)
            .putString("goal", goal)
            .putInt("target_water", water)
            .putInt("target_cal", cal)
            .putFloat("target_protein", protein)
            .apply()
    }

    fun getProfile(): UserProfile {
        return UserProfile(
            age = prefs.getInt("age", 24),
            heightCm = prefs.getFloat("height", 175f),
            weightKg = prefs.getFloat("weight", 68f),
            primaryGoal = prefs.getString("goal", "স্বাস্থ্য বৃদ্ধি") ?: "স্বাস্থ্য বৃদ্ধি",
            targetWaterMl = prefs.getInt("target_water", 2800),
            targetCalories = prefs.getInt("target_cal", 2350),
            targetProteinG = prefs.getFloat("target_protein", 110f),
            currentXp = prefs.getLong("current_xp", 0L),
            currentLevel = getLevel(),
            tokens = prefs.getInt("tokens", 0),
            streakDays = prefs.getInt("streak_days", 0),
            isOnboarded = prefs.getBoolean("is_onboarded", false)
        )
    }

    fun getLevel(): Int {
        val xp = prefs.getLong("current_xp", 0L)
        return (xp / 100).toInt() + 1
    }

    fun getRankTier(): RankTier {
        val lvl = getLevel()
        return RankTier.values().firstOrNull { lvl in it.minLevel..it.maxLevel } ?: RankTier.MASTER
    }

    fun addXp(amount: Int) {
        val newXp = prefs.getLong("current_xp", 0L) + amount
        prefs.edit().putLong("current_xp", newXp).apply()
    }

    fun addToken(amount: Int = 1) {
        val cur = prefs.getInt("tokens", 0) + amount
        prefs.edit().putInt("tokens", cur).apply()
    }

    fun getTodayDate(): String = LocalDate.now().toString()

    fun getWater(date: String = getTodayDate()): Int = prefs.getInt("water_$date", 0)

    fun addWater(ml: Int, date: String = getTodayDate()): Int {
        val current = getWater(date) + ml
        prefs.edit().putInt("water_$date", current).apply()
        addXp(10)
        return current
    }

    fun addDetailedMeal(date: String = getTodayDate(), type: MealType, items: String, cal: Int, protein: Float) {
        val entry = "${type.name}:::${items}:::${cal}:::${protein}:::${System.currentTimeMillis()}"
        val existing = prefs.getStringSet("meals_$date", emptySet())?.toMutableSet() ?: mutableSetOf()
        existing.add(entry)
        prefs.edit().putStringSet("meals_$date", existing).apply()
        addXp(25)
    }

    fun getDetailedMeals(date: String = getTodayDate()): List<Map<String, String>> {
        val raw = prefs.getStringSet("meals_$date", emptySet()) ?: emptySet()
        return raw.mapNotNull {
            val p = it.split(":::")
            if (p.size >= 4) {
                mapOf("type" to p[0], "items" to p[1], "cal" to p[2], "protein" to p[3])
            } else null
        }
    }

    fun addCustomMission(title: String, xp: Int = 25, cat: String = "কাস্টম") {
        val id = "custom_" + System.currentTimeMillis()
        val entry = "$id:::$title:::$xp:::$cat:::true"
        val missions = prefs.getStringSet("all_missions", defaultMissions())?.toMutableSet() ?: mutableSetOf()
        missions.add(entry)
        prefs.edit().putStringSet("all_missions", missions).apply()
    }

    private fun defaultMissions(): Set<String> = setOf(
        "workout:::২০ মিনিট ওয়ার্কআউট:::30:::শরীরচর্চা:::false",
        "water:::২.৫ লিটার পানি পান:::15:::হাইড্রেশন:::false",
        "protein:::১০০ গ্রাম প্রোটিন নিশ্চিতকরণ:::20:::পুষ্টি:::false",
        "sleep:::৭-৮ ঘণ্টা গভীর ঘুম:::20:::রিকভারি:::false"
    )

    fun getAllMissions(): List<MissionModel> {
        val raw = prefs.getStringSet("all_missions", defaultMissions()) ?: defaultMissions()
        return raw.mapNotNull {
            val parts = it.split(":::")
            if (parts.size >= 4) {
                MissionModel(parts[0], parts[1], parts[2].toIntOrNull() ?: 20, parts[3], parts.getOrNull(4) == "true")
            } else null
        }
    }

    fun getMissionStatus(date: String, missionId: String): String {
        return prefs.getString("status_${date}_$missionId", "PENDING") ?: "PENDING"
    }

    fun markMissionDone(date: String, missionId: String, xpReward: Int) {
        val oldStatus = getMissionStatus(date, missionId)
        if (oldStatus != "COMPLETED") {
            prefs.edit().putString("status_${date}_$missionId", "COMPLETED").apply()
            addXp(xpReward)
            checkAndUpdateStreak(date)
        }
    }

    fun markMissionCanceled(date: String, missionId: String) {
        prefs.edit().putString("status_${date}_$missionId", "CANCELED").apply()
    }

    private fun checkAndUpdateStreak(date: String) {
        val all = getAllMissions()
        val allDone = all.all { getMissionStatus(date, it.id) == "COMPLETED" }
        if (allDone) {
            val lastStreakDate = prefs.getString("last_full_streak_date", "")
            if (lastStreakDate != date) {
                val newStreak = prefs.getInt("streak_days", 0) + 1
                prefs.edit().putString("last_full_streak_date", date).putInt("streak_days", newStreak).apply()
                if (newStreak % 7 == 0) {
                    addToken(1)
                }
            }
        }
    }
}
