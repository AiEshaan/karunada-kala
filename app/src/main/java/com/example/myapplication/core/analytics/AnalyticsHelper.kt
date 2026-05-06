package com.example.myapplication.core.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(context: Context) {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        firebaseAnalytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }

    fun logArtViewed(artName: String, category: String) {
        val bundle = Bundle().apply {
            putString("art_name", artName)
            putString("category", category)
        }
        firebaseAnalytics.logEvent("art_viewed", bundle)
    }

    fun logArtLiked(artName: String) {
        val bundle = Bundle().apply {
            putString("art_name", artName)
        }
        firebaseAnalytics.logEvent("art_liked", bundle)
    }

    fun logEventRegistered(eventTitle: String) {
        val bundle = Bundle().apply {
            putString("event_title", eventTitle)
        }
        firebaseAnalytics.logEvent("event_registered", bundle)
    }

    fun logWorkshopEnrolled(workshopTitle: String) {
        val bundle = Bundle().apply {
            putString("workshop_title", workshopTitle)
        }
        firebaseAnalytics.logEvent("workshop_enrolled", bundle)
    }

    fun logAiChatSent(messageLength: Int) {
        val bundle = Bundle().apply {
            putInt("message_length", messageLength)
        }
        firebaseAnalytics.logEvent("ai_chat_sent", bundle)
    }

    fun logMentorshipRequested(artistName: String) {
        val bundle = Bundle().apply {
            putString("artist_name", artistName)
        }
        firebaseAnalytics.logEvent("mentorship_requested", bundle)
    }
}
