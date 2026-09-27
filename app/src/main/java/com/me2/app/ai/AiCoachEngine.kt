package com.me2.app.ai

import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import org.json.JSONArray
import org.json.JSONObject

class AiCoachEngine(
    private val apiKey: String? = System.getenv("GEMINI_API_KEY")
) {
    fun askGemini(prompt: String): String? {
        val key = apiKey ?: return null
        if (key.isBlank()) return null
        return try {
            val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent?key=$key"
            val conn = (URL(endpoint).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                connectTimeout = 8000
                readTimeout = 8000
                doOutput = true
            }
            val payload = JSONObject().apply {
                put("contents", JSONArray().apply {
                    put(JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", prompt)
                            })
                        })
                    })
                })
            }
            conn.outputStream.use { it.write(payload.toString().toByteArray()) }
            if (conn.responseCode == 200) {
                val res = BufferedReader(InputStreamReader(conn.inputStream)).use { it.readText() }
                val root = JSONObject(res)
                root.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
            } else null
        } catch (e: Exception) {
            null
        }
    }
}
