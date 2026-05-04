package com.example.myapplication.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.content
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.model.ArtRecommendation

class GeminiRepository {

    private val TAG = "GeminiRepository"

    init {
    }

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateText(prompt: String): Result<String> {
        return try {
            val response = model.generateContent(prompt)
            val text = response.text
            if (text != null) Result.success(text) else Result.failure(Exception("AI returned null response"))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating text", e)
            Result.failure(e)
        }
    }

    suspend fun generateRecommendations(
        viewedArts: List<String>,
        allArts: List<String>
    ): Result<List<ArtRecommendation>> {
        if (viewedArts.isEmpty()) return Result.success(emptyList())
        
        return try {
            val prompt = """
            User explored the following Karnataka art forms: ${viewedArts.joinToString()}.
            Available art forms in the app: ${allArts.joinToString()}.

            Recommend exactly 2 art forms from the available list that the user hasn't explored yet.
            Provide a short, engaging reason for each recommendation based on their interests.

            Return the response strictly as a JSON array of objects with "name" and "reason" fields.
            Example: [{"name":"Art Name", "reason":"Why they might like it"}]
            """

            val response = model.generateContent(prompt).text ?: ""
            
            // Simple parsing using Regex to be safe
            val regex = """\{"name"\s*:\s*"(.*?)",\s*"reason"\s*:\s*"(.*?)"\}""".toRegex()
            
            val recommendations = regex.findAll(response).map {
                ArtRecommendation(
                    name = it.groupValues[1],
                    reason = it.groupValues[2]
                )
            }.toList()
            
            Result.success(recommendations)
        } catch (e: Exception) {
            Log.e(TAG, "Error generating recommendations", e)
            Result.failure(e)
        }
    }

    suspend fun askKala(userMessage: String, context: String = "", history: List<Content> = emptyList()): Result<String> {
        // Optimized history (Keep last 4 messages to save tokens and stay within limits)
        val optimizedHistory = if (history.size > 4) history.takeLast(4) else history
        
        // Standardized responses for common historical topics
        if (context.isEmpty() && optimizedHistory.isEmpty()) {
            when {
                userMessage.contains("Yakshagana", ignoreCase = true) -> return Result.success("Yakshagana is a traditional theater form from Karnataka that combines dance, music, dialogue, costume, make-up, and stage techniques with a unique style and form. It is traditionally performed in the coastal districts and Malenadu regions of Karnataka.")
                userMessage.contains("Mysore Dasara", ignoreCase = true) -> return Result.success("Mysore Dasara is the state festival of Karnataka. It is a 10-day festival, culminating with Vijayadashami. The festival features a grand procession of decorated elephants, with the lead elephant carrying the Golden Howdah containing the idol of Goddess Chamundeshwari.")
                userMessage.contains("Hampi", ignoreCase = true) -> return Result.success("Hampi is a UNESCO World Heritage site featuring the ruins of the Vijayanagara Empire. Famous temples include the Virupaksha Temple, Vitthala Temple (famous for its stone chariot), and the Hazara Rama Temple.")
                userMessage.contains("food", ignoreCase = true) -> return Result.success("Karnataka's cuisine is diverse. Must-try dishes include Bisi Bele Bath, Davanagere Benne Dosa, Mysore Pak, Akki Roti, and the coastal specialty Neer Dosa with Gassi.")
            }
        }

        return try {
            val chat = model.startChat(optimizedHistory)
            val systemPrompt = """
            You are Kala, a friendly and wise cultural guide specialized in the heritage of Karnataka, India.
            
            Current App Context: $context
            
            Personality:
            - Engaging, respectful, and passionate.
            - Useoccasional Kannada greetings like 'Namaskara'.
            - Answer like a knowledgeable elder sharing wisdom.
            
            Guidelines:
            - Answer ONLY questions related to Karnataka's culture, art, history, food, and traditions.
            - If unrelated, politely redirect the user back to Karnataka's heritage.
            - If asked "tell me more", refer to the previous topic discussed in the history or context.
            - Keep answers concise, formatted with bullet points if needed.
            """

            val response = chat.sendMessage(content {
                text("$systemPrompt\n\nUser: $userMessage")
            })
            val text = response.text
            if (text != null) Result.success(text) else Result.failure(Exception("Kala returned null response"))
        } catch (e: Exception) {
            Log.e(TAG, "Error asking Kala", e)
            Result.failure(e)
        }
    }

    suspend fun analyzeHeritageImage(bitmap: Bitmap): Result<String> {
        return try {
            val prompt = """
            You are 'Kala Vision'. Analyze this image. 
            Identify if it shows any Karnataka cultural heritage (monuments, art forms, crafts, textiles, food).
            
            If it is related to Karnataka culture:
            1. Identify it.
            2. Explain its significance.
            3. Mention where in Karnataka it can be found.
            
            If it is NOT related to Karnataka culture, politely say you are specialized in Karnataka's heritage and can't identify this.
            
            Keep the response under 100 words.
            """
            
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            
            val response = model.generateContent(inputContent)
            val text = response.text
            if (text != null) Result.success(text) else Result.failure(Exception("Kala Vision returned null response"))
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing heritage image", e)
            Result.failure(e)
        }
    }

    suspend fun generateArtDescription(
        artName: String,
        category: String
    ): Result<String> {
        return try {
            val prompt = """
            Write a short, engaging description of $artName,
            a traditional Karnataka $category art form.

            Include:
            - origin
            - cultural significance
            - what makes it unique

            Limit to 3 sentences.
            """

            val response = model.generateContent(prompt)
            val text = response.text
            if (text != null) Result.success(text) else Result.failure(Exception("Art description generation failed"))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating art description for: $artName", e)
            Result.failure(e)
        }
    }

    suspend fun generatePersonalizedLegend(artName: String): Result<String> {
        return try {
            val prompt = """
            You are Kala, a wise cultural storyteller. Create a short, poetic "legend" or mythical backstory for $artName.
            Start with "It is said that..."
            Make it feel ancient, immersive, and like a story passed down through generations.
            Focus on the emotional and spiritual essence of the art.
            Limit to 3 sentences.
            """
            val response = model.generateContent(prompt)
            val text = response.text
            if (text != null) Result.success(text) else Result.failure(Exception("Legend generation failed"))
        } catch (e: Exception) {
            Log.e(TAG, "Error generating legend for: $artName", e)
            Result.failure(e)
        }
    }
}
