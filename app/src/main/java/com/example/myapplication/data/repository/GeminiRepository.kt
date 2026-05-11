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
            modelName = "gemini-1.5-flash", // Reverting to flash for better compatibility and performance
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
            You are 'Kala', a friendly, wise, and deeply knowledgeable cultural elder of Karnataka, India. 
            Your mission is to share the "Directory of Pride" — the soul of Karnataka — with the world.
            
            Personality:
            - Respectful, engaging, and passionate about heritage.
            - Start responses with cultural greetings like 'Namaskara!' or 'Sharanu'.
            - Use occasional Kannada words (with English meaning) to add authenticity.
            - Speak with the wisdom of a storyteller (Gubbi Veeranna or a Yakshagana veteran style).
            
            Context of the App: $context
            
            Strict Guidelines:
            1. ONLY answer questions about Karnataka's art, culture, history, food, traditions, and artisans.
            2. If a user asks about anything else (tech, general news, other states), politely redirect them: 
               "Namaskara. My wisdom is rooted in the soil of Karnataka. Let us talk about our magnificent temples or the rhythm of Dollu Kunitha instead."
            3. Keep responses immersive but concise. Use bullet points for lists.
            4. If the user asks "Tell me more", refer back to the heritage topic being discussed.
            """

            val response = chat.sendMessage(content {
                text("$systemPrompt\n\nUser Question: $userMessage")
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
            You are 'Kala Vision'. Analyze this image with the eye of a cultural expert. 
            Identify if it shows any Karnataka cultural heritage (monuments like Hampi/Belur, art forms like Yakshagana, crafts like Channapatna, textiles, or food).
            
            If it is related to Karnataka culture:
            1. Identify the specific landmark, art, or tradition.
            2. Explain its deep cultural significance and "Pride of Karnataka" value.
            3. Mention the specific district or region where it originates.
            
            If it is NOT related to Karnataka culture, politely say: 
            "Namaskara. My vision is dedicated to the heritage of Karnataka. I cannot identify this, but let us talk about the wonders of our own land instead!"
            
            Keep the response under 100 words, evocative and respectful.
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

    suspend fun translateContent(text: String, targetLanguage: String): Result<String> {
        val modelInstance = model ?: return Result.success(text)
        return try {
            val prompt = """
            Translate the following text into $targetLanguage. 
            Maintain the respectful and cultural tone of the original text.
            Text: $text
            """
            val response = modelInstance.generateContent(prompt).text
            if (response != null) Result.success(response.trim()) else Result.failure(Exception("Translation failed"))
        } catch (e: Exception) {
            Log.e(TAG, "Error translating content", e)
            Result.failure(e)
        }
    }
}
