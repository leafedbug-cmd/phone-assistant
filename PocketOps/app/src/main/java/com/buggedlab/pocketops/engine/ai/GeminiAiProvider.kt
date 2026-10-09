package com.buggedlab.pocketops.engine.ai

import android.util.Log
import com.buggedlab.pocketops.core.model.AiIntent
import com.buggedlab.pocketops.core.model.AiPlan
import com.buggedlab.pocketops.core.model.DeviceCapabilities
import com.buggedlab.pocketops.core.model.PlanStatus
import com.buggedlab.pocketops.core.model.ProposedAction
import com.buggedlab.pocketops.core.model.RiskLevel
import com.buggedlab.pocketops.data.db.NotificationEntity
import java.io.IOException
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class GeminiAiProvider(
    private val apiKeyProvider: () -> String?,
    private val modelName: String = "gemini-2.0-flash"
) : AiProvider {

    private val offlineFallback = OfflineRuleAiProvider()
    private val json = Json { ignoreUnknownKeys = true }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiAiProvider"
    }

    override suspend fun parseUserRequest(userRequest: String, capabilities: DeviceCapabilities): AiPlan = withContext(Dispatchers.IO) {
        val apiKey = apiKeyProvider()
        if (apiKey.isNullOrBlank()) {
            return@withContext offlineFallback.parseUserRequest(userRequest, capabilities)
        }

        // Sanitize user prompt to prevent prompt injection overriding authorization rules
        val sanitizedPrompt = userRequest.replace("\"", "\\\"").take(500)

        val systemPrompt = """
            You are PocketOps AI, an on-device personal phone administrator for Android (Samsung One UI).
            Target device: ${capabilities.manufacturer} ${capabilities.model}, Android OS: ${capabilities.osVersion}.
            Your task is to analyze the user request and respond strictly in valid JSON format:
            {
              "intent": "ORGANIZE_NOTIFICATIONS" | "FIND_NOISY_APPS" | "CREATE_ROUTINE" | "OPEN_SETTINGS" | "GENERATE_DIGEST" | "SAMSUNG_ROUTINE_HELP" | "GENERAL_QUERY",
              "explanation": "Clear, concise user-facing rationale",
              "actions": [
                {
                  "title": "Short title",
                  "description": "Action details",
                  "riskLevel": "LOW" | "MEDIUM" | "HIGH",
                  "targetCapability": "NAVIGATION_INTENT" | "NOTIFICATION_FILTER" | "AUTOMATION_ENGINE" | "DIGEST_GENERATOR",
                  "parameters": { "key": "value" }
                }
              ]
            }
            Do not include markdown tags (```json). Output raw JSON only.
        """.trimIndent()

        val systemPromptEscaped = kotlinx.serialization.json.JsonPrimitive(systemPrompt).toString()
        val sanitizedPromptEscaped = kotlinx.serialization.json.JsonPrimitive(sanitizedPrompt).toString()

        val requestBodyJson = """
            {
              "systemInstruction": {
                "parts": [{ "text": $systemPromptEscaped }]
              },
              "contents": [{
                "parts": [{ "text": $sanitizedPromptEscaped }]
              }],
              "generationConfig": {
                "temperature": 0.2,
                "responseMimeType": "application/json"
              }
            }
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestBodyJson.toRequestBody("application/json".toMediaType()))
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Gemini API call failed with code ${response.code}. Falling back to offline engine.")
                    return@withContext offlineFallback.parseUserRequest(userRequest, capabilities)
                }

                val responseBody = response.body?.string() ?: ""
                val rootJson = json.parseToJsonElement(responseBody).jsonObject
                val candidates = rootJson["candidates"]?.jsonArray
                val firstCandidate = candidates?.firstOrNull()?.jsonObject
                val parts = firstCandidate?.get("content")?.jsonObject?.get("parts")?.jsonArray
                val rawText = parts?.firstOrNull()?.jsonObject?.get("text")?.jsonPrimitive?.content ?: ""

                parseGeminiJsonResponse(rawText, userRequest)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error invoking Gemini API: ${e.message}. Using offline fallback.")
            offlineFallback.parseUserRequest(userRequest, capabilities)
        }
    }

    private suspend fun parseGeminiJsonResponse(rawJson: String, userRequest: String): AiPlan {
        return try {
            val root = json.parseToJsonElement(rawJson).jsonObject
            val intentStr = root["intent"]?.jsonPrimitive?.content ?: "GENERAL_QUERY"
            val explanation = root["explanation"]?.jsonPrimitive?.content ?: ""
            val actionsArray = root["actions"]?.jsonArray ?: emptyList()

            val actions = actionsArray.mapIndexed { index, elem ->
                val obj = elem.jsonObject
                val title = obj["title"]?.jsonPrimitive?.content ?: "Action $index"
                val desc = obj["description"]?.jsonPrimitive?.content ?: ""
                val riskStr = obj["riskLevel"]?.jsonPrimitive?.content ?: "MEDIUM"
                val targetCap = obj["targetCapability"]?.jsonPrimitive?.content ?: "GENERAL"

                val params = mutableMapOf<String, String>()
                obj["parameters"]?.jsonObject?.forEach { (k, v) ->
                    params[k] = v.jsonPrimitive.content
                }

                val riskLevel = runCatching { RiskLevel.valueOf(riskStr) }.getOrDefault(RiskLevel.MEDIUM)

                ProposedAction(
                    id = "gemini_action_${System.currentTimeMillis()}_$index",
                    title = title,
                    description = desc,
                    riskLevel = riskLevel,
                    targetCapability = targetCap,
                    parameters = params
                )
            }

            val intent = runCatching { AiIntent.valueOf(intentStr) }.getOrDefault(AiIntent.GENERAL_QUERY)
            val requiresApproval = actions.any { it.riskLevel != RiskLevel.LOW }

            AiPlan(
                id = UUID.randomUUID().toString(),
                userPrompt = userRequest,
                intent = intent,
                explanation = explanation,
                actions = actions,
                requiresUserApproval = requiresApproval,
                status = if (requiresApproval) PlanStatus.PENDING_APPROVAL else PlanStatus.APPROVED
            )
        } catch (_: Exception) {
            offlineFallback.parseUserRequest(userRequest, DeviceCapabilities("", "", "", 0, false, null, false, com.buggedlab.pocketops.core.model.DevicePermissions(false, false, false, false, false, false)))
        }
    }

    override suspend fun summarizeDigest(notifications: List<NotificationEntity>): String {
        return offlineFallback.summarizeDigest(notifications)
    }
}
