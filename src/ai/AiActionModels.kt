package com.me2.app.ai

enum class AppActionType {
    LOG_FOOD,
    LOG_SLEEP,
    LOG_WATER,
    COMPLETE_MISSION,
    GENERAL_ADVICE,
    UNKNOWN
}

data class ParsedAppAction(
    val actionType: AppActionType,
    val foodName: String? = null,
    val calories: Int? = null,
    val proteinG: Float? = null,
    val sleepHours: Float? = null,
    val waterMl: Int? = null,
    val missionTitle: String? = null,
    val aiVoiceReply: String
)
