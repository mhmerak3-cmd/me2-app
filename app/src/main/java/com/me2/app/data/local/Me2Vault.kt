package com.me2.app.data.local

import android.content.Context
import android.content.SharedPreferences
import java.time.LocalDate

class Me2Vault(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("me2_persistent_vault", Context.MODE_PRIVATE)

    fun getXp(): Long = prefs.getLong("user_xp", 0L)
    fun setXp(xp: Long) = prefs.edit().putLong("user_xp", xp).apply()

    fun getLevel(): Int {
        val xp = getXp()
        return (xp / 100).toInt() + 1
    }

    fun getStreak(): Int = prefs.getInt("user_streak", 12)

    private fun checkDayReset() {
        val today = LocalDate.now().toString()
        val lastDate = prefs.getString("last_active_date", "")
        if (lastDate != today) {
            prefs.edit()
                .putString("last_active_date", today)
                .putInt("daily_water_ml", 0)
                .putInt("daily_calories", 0)
                .putFloat("daily_protein", 0f)
                .putStringSet("completed_missions", emptySet())
                .apply()
        }
    }

    fun getWater(): Int {
        checkDayReset()
        return prefs.getInt("daily_water_ml", 0)
    }

    fun addWater(ml: Int): Int {
        checkDayReset()
        val newWater = getWater() + ml
        val newXp = getXp() + 10
        prefs.edit()
            .putInt("daily_water_ml", newWater)
            .putLong("user_xp", newXp)
            .apply()
        return newWater
    }

    fun getCalories(): Int {
        checkDayReset()
        return prefs.getInt("daily_calories", 0)
    }

    fun getProtein(): Float {
        checkDayReset()
        return prefs.getFloat("daily_protein", 0f)
    }

    fun addMeal(cal: Int, protein: Float): Long {
        checkDayReset()
        val newCal = getCalories() + cal
        val newProt = getProtein() + protein
        val newXp = getXp() + 25
        prefs.edit()
            .putInt("daily_calories", newCal)
            .putFloat("daily_protein", newProt)
            .putLong("user_xp", newXp)
            .apply()
        return newXp
    }

    fun getCompletedMissions(): Set<String> {
        checkDayReset()
        return prefs.getStringSet("completed_missions", emptySet()) ?: emptySet()
    }

    fun toggleMission(missionId: String, xpReward: Int): Boolean {
        checkDayReset()
        val set = getCompletedMissions().toMutableSet()
        val isNowCompleted = if (set.contains(missionId)) {
            set.remove(missionId)
            val currentXp = getXp()
            prefs.edit().putLong("user_xp", (currentXp - xpReward).coerceAtLeast(0L)).apply()
            false
        } else {
            set.add(missionId)
            prefs.edit().putLong("user_xp", getXp() + xpReward).apply()
            true
        }
        prefs.edit().putStringSet("completed_missions", set).apply()
        return isNowCompleted
    }
}
