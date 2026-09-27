package com.me2.app.data.repository

import com.me2.app.data.local.Me2Vault
import com.me2.app.data.models.UserProfile
import com.me2.app.domain.engine.GamificationEngine

class Me2LocalRepository(
    private val gamificationEngine: GamificationEngine
) {
    private var vault: Me2Vault? = null

    fun bindVault(v: Me2Vault) {
        this.vault = v
    }

    fun getProfile(): UserProfile? = vault?.getProfile()

    fun getTodayMacroSummary(): Map<String, Number> {
        val v = vault ?: return mapOf("calories" to 0, "protein" to 0f)
        val meals = v.getDetailedMeals()
        val totalCal = meals.sumOf { it["cal"]?.toIntOrNull() ?: 0 }
        val totalProt = meals.sumOf { it["protein"]?.toDoubleOrNull() ?: 0.0 }.toFloat()
        return mapOf("calories" to totalCal, "protein" to totalProt)
    }
}
