package com.example.myapplication.data.repository

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.example.myapplication.BuildConfig
import com.example.myapplication.data.model.ArtRecommendation

class GeminiRepository {

    init {
        Log.d("GEMINI_INIT", "API Key length: ${BuildConfig.GEMINI_API_KEY.length}")
    }

    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateText(prompt: String): String {
        return try {
            val response = model.generateContent(prompt)
            response.text ?: "I'm sorry, I couldn't generate a response. Please try asking about Karnataka's culture!"
        } catch (e: Exception) {
            e.printStackTrace()
            "Namaskara! I'm experiencing a small technical issue. Please check your internet and try again."
        }
    }

    suspend fun generateRecommendations(
        viewedArts: List<String>,
        allArts: List<String>
    ): List<ArtRecommendation> {
        if (viewedArts.isEmpty()) return emptyList()
        
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
            
            regex.findAll(response).map {
                ArtRecommendation(
                    name = it.groupValues[1],
                    reason = it.groupValues[2]
                )
            }.toList()

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun askKala(userMessage: String): String {
        // Mock responses for known suggestions to bypass API issues during development
        when {
            userMessage.contains("Yakshagana", ignoreCase = true) -> return "Yakshagana is a traditional theater form from Karnataka that combines dance, music, dialogue, costume, make-up, and stage techniques with a unique style and form. It is traditionally performed in the coastal districts and Malenadu regions of Karnataka."
            userMessage.contains("Mysore Dasara", ignoreCase = true) -> return "Mysore Dasara is the state festival of Karnataka. It is a 10-day festival, culminating with Vijayadashami. The festival features a grand procession of decorated elephants, with the lead elephant carrying the Golden Howdah containing the idol of Goddess Chamundeshwari."
            userMessage.contains("Hampi", ignoreCase = true) -> return "Hampi is a UNESCO World Heritage site featuring the ruins of the Vijayanagara Empire. Famous temples include the Virupaksha Temple, Vitthala Temple (famous for its stone chariot), and the Hazara Rama Temple."
            userMessage.contains("food", ignoreCase = true) -> return "Karnataka's cuisine is diverse. Must-try dishes include Bisi Bele Bath, Davanagere Benne Dosa, Mysore Pak, Akki Roti, and the coastal specialty Neer Dosa with Gassi."
        }

        return try {
            val prompt = """
            You are Kala, a friendly and knowledgeable cultural guide specialized in the heritage of Karnataka, India.
            
            Your personality:
            - Engaging, respectful, and passionate about Karnataka's art, traditions, and history.
            - You use occasional Kannada greetings like 'Namaskara'.
            
            Scope:
            - Answer ONLY questions related to Karnataka's art forms (Yakshagana, Dollu Kunitha, etc.), festivals, artists, history, and cultural traditions.
            - If a user asks something completely unrelated to Karnataka's culture, politely inform them that you are specialized in Karnataka's heritage and suggest they ask a cultural question.
            
            Keep your answers concise, informative, and formatted for easy reading.
            
            User question:
            $userMessage
            """

            val response = model.generateContent(prompt)
            response.text ?: "I'm having trouble finding an answer right now. Please try asking another cultural question!"

        } catch (e: Exception) {
            e.printStackTrace()
            "Namaskara! I'm having a small technical issue with my cultural database (Gemini API 404). Please try one of the suggested topics or check back soon!"
        }
    }

    suspend fun generateArtDescription(
        artName: String,
        category: String
    ): String {
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
            response.text ?: "No description available"

        } catch (e: Exception) {
            e.printStackTrace()
            "Error generating description"
        }
    }
}
