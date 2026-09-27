package com.me2.app.ai

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration

data class AiNutritionEstimate(
    val itemName: String,
    val portion: String,
    val calories: Int,
    val protein: Float,
    val carbs: Float,
    val fat: Float,
    val isEstimated: Boolean = true,
    val source: String = "LOCAL_RULE_ENGINE"
)

data class DailyReviewSummary(
    val statusHeadline: String,
    val performanceAnalysis: String,
    val tomorrowFocus: String,
    val source: String
)

class AiCoachEngine(
    private val apiKey: String? = System.getenv("GEMINI_API_KEY")
) {
    private val httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(6))
        .build()

    // ১. ন্যাচারাল ফুড ইনপুট ইন্টারপ্রিটেশন (অনলাইন Gemini + অফলাইন ফলব্যাক)
    fun parseFoodEntry(rawInput: String): AiNutritionEstimate {
        val cleanInput = rawInput.lowercase()

        // ইন্টারনেট বা API Key না থাকলে অফলাইন রুল-বেসড লোকাল পার্সার কাজ করবে
        if (apiKey.isNullOrBlank()) {
            return parseFoodLocally(cleanInput)
        }

        return try {
            val prompt = """
            Extract estimated nutrition for: "$rawInput".
            Return ONLY raw format: ITEM_NAME|PORTION|CALORIES|PROTEIN|CARBS|FAT
            Example: 2 Eggs|2 boiled eggs|150|12|1|10
            """.trimIndent()

            val response = callGeminiApi(prompt)
            if (response != null && response.contains("|")) {
                val parts = response.split("|").map { it.trim() }
                AiNutritionEstimate(
                    itemName = parts.getOrNull(0) ?: rawInput,
                    portion = parts.getOrNull(1) ?: "1 serving",
                    calories = parts.getOrNull(2)?.toIntOrNull() ?: 250,
                    protein = parts.getOrNull(3)?.toFloatOrNull() ?: 10f,
                    carbs = parts.getOrNull(4)?.toFloatOrNull() ?: 30f,
                    fat = parts.getOrNull(5)?.toFloatOrNull() ?: 8f,
                    source = "GEMINI_AI"
                )
            } else {
                parseFoodLocally(cleanInput)
            }
        } catch (e: Exception) {
            parseFoodLocally(cleanInput)
        }
    }

    // লোকাল ফলব্যাক ফুড পার্সার (সম্পূর্ণ অফলাইন)
    private fun parseFoodLocally(input: String): AiNutritionEstimate {
        return when {
            input.contains("ডিম") || input.contains("egg") -> {
                AiNutritionEstimate("ডিম / Eggs", "২টি ডিম", 140, 12f, 1f, 10f)
            }
            input.contains("রুটি") || input.contains("roti") -> {
                AiNutritionEstimate("রুটি / Roti", "২টি রুটি", 200, 6f, 40f, 2f)
            }
            input.contains("ভাত") || input.contains("rice") -> {
                AiNutritionEstimate("ভাত / Rice", "১ প্লেট", 260, 5f, 58f, 1f)
            }
            input.contains("মাছ") || input.contains("fish") -> {
                AiNutritionEstimate("মাছ / Fish Curry", "১ টুকরা", 180, 22f, 2f, 8f)
            }
            input.contains("চিকেন") || input.contains("chicken") -> {
                AiNutritionEstimate("চিকেন / Chicken", "১৫০ গ্রাম", 250, 31f, 0f, 12f)
            }
            input.contains("কলা") || input.contains("banana") -> {
                AiNutritionEstimate("কলা / Banana", "১টি মাঝারি", 105, 1.3f, 27f, 0.3f)
            }
            else -> {
                AiNutritionEstimate(input, "১ সার্ভিং (আনুমানিক)", 300, 10f, 40f, 8f)
            }
        }
    }

    // ২. দৈনিক রিভিউ তৈরি (অনলাইন Gemini + অফলাইন ডাটা এনালিসিস)
    fun generateDailyReview(
        completedMissions: Int,
        totalMissions: Int,
        consumedKcal: Int,
        targetKcal: Int,
        proteinG: Float,
        targetProteinG: Int,
        shiftName: String
    ): DailyReviewSummary {
        val completionRate = if (totalMissions > 0) (completedMissions * 100) / totalMissions else 0

        // অফলাইন লোকাল রিভিউ অ্যালগরিদম
        val localHeadline = if (completionRate >= 75) "চমৎকার ধারাবাহিকতা!" else "রিকভারি প্রয়োজন"
        val localAnalysis = "আজ $shiftName শিফটের মাঝে $totalMissions টির মধ্যে $completedMissions টি মিশন সম্পন্ন হয়েছে। প্রোটিন লক্ষ্যমাত্রা ${(proteinG / targetProteinG * 100).toInt()}% পূরণ হয়েছে।"
        val localFocus = if (completionRate >= 75) "আগামীকাল সময়মতো ঘুম থেকে উঠা এবং হাইজিন রুটিন বজায় রাখুন।" else "আগামীকাল প্রথমে ৩টি কোর মিশন শেষ করার দিকে নজর দিন।"

        if (apiKey.isNullOrBlank()) {
            return DailyReviewSummary(localHeadline, localAnalysis, localFocus, source = "LOCAL_OFFLINE_ENGINE")
        }

        return try {
            val prompt = """
            Create a 2-sentence tactical performance review:
            - Shift: $shiftName
            - Missions: $completedMissions/$totalMissions completed
            - Caloric Target: $consumedKcal/$targetKcal kcal
            - Protein: ${proteinG.toInt()}/${targetProteinG}g
            Give constructive feedback without extreme diet recommendations.
            """.trimIndent()

            val aiText = callGeminiApi(prompt)
            if (!aiText.isNullOrBlank()) {
                DailyReviewSummary(
                    statusHeadline = "অপারেটর ডেইলি এনালাইসিস",
                    performanceAnalysis = aiText.trim(),
                    tomorrowFocus = localFocus,
                    source = "GEMINI_AI"
                )
            } else {
                DailyReviewSummary(localHeadline, localAnalysis, localFocus, source = "LOCAL_OFFLINE_ENGINE")
            }
        } catch (e: Exception) {
            DailyReviewSummary(localHeadline, localAnalysis, localFocus, source = "LOCAL_OFFLINE_ENGINE")
        }
    }

    // নিরাপদ HTTP রিকোয়েস্ট (Gemini REST API)
    private fun callGeminiApi(prompt: String): String? {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
        val escapedPrompt = prompt.replace("\"", "\\\"").replace("\n", " ")
        val requestJson = """
        {
            "contents": [{
                "parts": [{"text": "$escapedPrompt"}]
            }]
        }
        """.trimIndent()

        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(requestJson))
            .timeout(Duration.ofSeconds(6))
            .build()

        val response = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() == 200) {
            val body = response.body()
            // সিম্পল টেক্সট এক্সট্রাক্টর
            val textToken = "\"text\": \""
            val startIndex = body.indexOf(textToken)
            if (startIndex != -1) {
                val sub = body.substring(startIndex + textToken.length)
                val endIndex = sub.indexOf("\"")
                if (endIndex != -1) {
                    return sub.substring(0, endIndex).replace("\\n", "\n")
                }
            }
        }
        return null
    }
}
