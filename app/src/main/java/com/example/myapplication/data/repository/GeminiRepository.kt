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

    private val model = if (BuildConfig.GEMINI_API_KEY.isNotEmpty()) {
        GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
    } else {
        null
    }

    suspend fun generateText(prompt: String): Result<String> {
        val model = model ?: return Result.failure(Exception("API Key missing"))
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
        val model = model ?: return Result.success(emptyList()) // Fallback to empty
        
        return try {
            val prompt = """
            User explored the following Karnataka art forms: ${viewedArts.joinToString()}.
            Available art forms in the app: ${allArts.joinToString()}.

            Recommend exactly 2 art forms from the available list that the user hasn't explored yet.
            Provide a short, engaging reason for each recommendation based on their interests.

            Return the response strictly as a JSON array of objects with "name" and "reason" fields.
            Example: [{"name":"Art Name", "reason":"Why they might like it"}]
            """

            val responseText = model.generateContent(prompt).text ?: ""
            
            // Simple parsing using Regex to be safe
            val regex = """\{"name"\s*:\s*"(.*?)",\s*"reason"\s*:\s*"(.*?)"\}""".toRegex()
            
            val recommendations = regex.findAll(responseText).map {
                ArtRecommendation(
                    name = it.groupValues[1],
                    reason = it.groupValues[2]
                )
            }.toList()
            
            if (recommendations.isEmpty()) {
                // Fallback recommendations if parsing fails
                Result.success(listOf(
                    ArtRecommendation("Yakshagana", "Explore the vibrant folk theatre of coastal Karnataka."),
                    ArtRecommendation("Channapatna Toys", "Discover the famous lacquered wooden toys.")
                ))
            } else {
                Result.success(recommendations)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error generating recommendations", e)
            // Safe fallback
            Result.success(listOf(
                ArtRecommendation("Yakshagana", "Explore the vibrant folk theatre of coastal Karnataka."),
                ArtRecommendation("Channapatna Toys", "Discover the famous lacquered wooden toys.")
            ))
        }
    }

    suspend fun askKala(userMessage: String, context: String = "", history: List<Content> = emptyList()): Result<String> {
        // Optimized history (Keep last 4 messages to save tokens and stay within limits)
        val optimizedHistory = if (history.size > 4) history.takeLast(4) else history
        
        // Standardized responses for common historical topics
        val lowerMessage = userMessage.lowercase()
        when {
            lowerMessage.contains("yakshagana") -> return Result.success("Yakshagana is a traditional theater form from Karnataka that combines dance, music, dialogue, costume, make-up, and stage techniques with a unique style and form.")
            lowerMessage.contains("mysore dasara") -> return Result.success("Mysore Dasara is the state festival of Karnataka, celebrated with a grand procession and the lighting of the Mysore Palace.")
            lowerMessage.contains("hampi") -> return Result.success("Hampi is a UNESCO World Heritage site featuring the stunning ruins of the Vijayanagara Empire.")
            lowerMessage.contains("food") -> return Result.success("Karnataka's cuisine is famous for Bisi Bele Bath, Mysore Pak, and Davanagere Benne Dosa.")
        }

        val modelInstance = model ?: return Result.failure(Exception("Kala is taking a break (API Key missing)."))

        return try {
            val chat = modelInstance.startChat(optimizedHistory)
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
            Result.success("Namaskara! I'm here to share the wisdom of Karnataka. Let's talk about our beautiful art forms or history.")
        }
    }

    suspend fun analyzeHeritageImage(bitmap: Bitmap): Result<String> {
        val model = model ?: return Result.success("Kala Vision is offline. This image reflects our rich heritage.")
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
            Result.success("This appears to be a beautiful part of Karnataka's vibrant culture and heritage.")
        }
    }

    suspend fun generateArtDescription(
        artName: String,
        category: String
    ): Result<String> {
        val model = model ?: return Result.success("$artName is a precious $category art form from Karnataka, carrying centuries of tradition.")
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
            Result.success("$artName is a precious $category art form from Karnataka, carrying centuries of tradition.")
        }
    }

    suspend fun generatePersonalizedLegend(artName: String): Result<String> {
        val model = model ?: return Result.success("It is said that $artName was born from the heart of our people, a gift from the heavens to preserve our stories forever.")
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
            Result.success("It is said that $artName was born from the heart of our people, a gift from the heavens to preserve our stories forever.")
        }
    }

    suspend fun suggestSearchQueries(query: String): Result<List<String>> {
        val modelInstance = model ?: return Result.success(emptyList())
        return try {
            val prompt = """
            The user is searching for Karnataka heritage and arts. 
            Based on the partial search query "$query", suggest 3 relevant cultural topics, art forms, or locations in Karnataka.
            Provide ONLY the names as a comma-separated list.
            """
            val response = modelInstance.generateContent(prompt).text ?: ""
            val suggestions = response.split(",").map { it.trim() }.filter { it.isNotEmpty() }.take(3)
            Result.success(suggestions)
        } catch (e: Exception) {
            Log.e(TAG, "Error suggesting queries", e)
            Result.failure(e)
        }
    }

    suspend fun suggestCaptionForImage(bitmap: Bitmap): Result<String> {
        val modelInstance = model ?: return Result.success("A moment of Karnataka's vibrant culture.")
        return try {
            val prompt = """
            You are a poetic cultural guide. Write a short, elegant 1-2 line caption for this image 
            rooted in Karnataka's culture and heritage. Keep it evocative, respectful, and suitable for a social media chronicle.
            Do not use hashtags.
            """
            val inputContent = content {
                image(bitmap)
                text(prompt)
            }
            val response = modelInstance.generateContent(inputContent).text
            if (response != null) Result.success(response.trim()) else Result.failure(Exception("Empty response"))
        } catch (e: Exception) {
            Log.e(TAG, "Error suggesting caption", e)
            Result.success("A timeless glimpse into Karnataka’s heritage.")
        }
    }
}
