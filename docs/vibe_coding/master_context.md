# Karunada Kala - Vibe Coding Master Context

This document contains the "Master Context" and "Prompts" required to build or iterate on **Karunada Kala** from scratch using AI Studio (Gemini 1.5 Pro).

---

## 🎨 Doc 1: The "Vibe" & Design Language (Aesthetic PRD)
**Project Name:** Karunada Kala (The Art of Karunada)  
**Core Theme:** "Swiss Minimalist meets Ancient Sandalwood."  

**Visual Identity:**
*   **Background:** Antique Parchment (#FCF9F2) and Aged Walnut (#1A1612).
*   **Primary Colors:** Forest Green (#1B3A2D) and Terracotta (#C4662F).
*   **Typography:** Serif for titles (Heritage feel), Sans-Serif for body (Modern utility).
*   **Texture:** Subtle grain overlays and paper-unrolling animations.
**Atmosphere:** Immersive, respectful, educational, and premium.

---

## 🛠 Doc 2: Technical Stack & Architecture
**Platform:** Android (Jetpack Compose)  
**Language:** Kotlin  
**Architecture:** MVVM with Clean Architecture principles.  
**Back-end:** Firebase (Firestore, Auth, Storage, Analytics, FCM).  
**AI Engine:** Google Gemini API (for cultural narratives and image analysis).  
**Mapping:** Google Maps Compose SDK with custom JSON styling.  
**Media:** Media3 ExoPlayer for high-fidelity artisan voices.  
**Local Cache:** Jetpack DataStore + Gson (Cache-first strategy).

---

## 📊 Doc 3: Firestore Schema (Data Model)
```json
{
  "arts": {
    "name": "String", "category": "String", "description": "String", 
    "imageUrl": "String", "audioUrl": "String", "videoUrl": "String", "viewCount": "Int"
  },
  "artists": {
    "name": "String", "bio": "String", "phone": "String", "photoUrl": "String",
    "worksCount": "Int", "studentsCount": "Int", "galleryUrls": "List<String>"
  },
  "events": {
    "title": "String", "date": "String", "location": "String", 
    "lat": "Double", "lng": "Double", "imageUrl": "String"
  }
}
```

---

## 🚀 The "Master Prompts" for AI Studio

### Prompt 1: The Foundation (Infrastructure)
"Act as a Senior Android Engineer and Lead Designer. I want to build a heritage app called 'Karunada Kala'. Use the provided Design Language and Tech Stack. Start by creating the `MainActivity`, the `NavRoutes` for a 5-tab system (Explore, Map, Events, Chronicles, Journey), and a `Theme.kt` that supports both Light (Antique Paper) and Dark (Heritage Night) modes. Include the `GrainOverlay` composable to be used globally for texture."

### Prompt 2: The "Explore" Hero UI
"Build the Explore Screen. I want a parallax hero header that fades out as the user scrolls. Below it, add a horizontal 'Art of the Day' spotlight card with a shimmering badge. Then, implement a staggered grid of 'ArtCards' that feature high-quality images with gradient overlays. Ensure the screen supports Pull-to-Refresh and loads data from a Repository using a cache-first strategy with DataStore."

### Prompt 3: The "Magic" Detail Screen (3D Flip & AI)
"Create a Detail Screen for a specific art form. The main feature is a 'Legend Card' that performs a 3D flip animation when tapped. When flipped, it should call the Gemini API to generate a 'Personalized Legend' based on the art name. Below the card, include an Artisan Voice audio player (ExoPlayer) and a 'Legacy Tree' visualization. Add a 'Related Arts' horizontal scroll at the bottom."

### Prompt 4: The Custom Map Experience
"Implement the Map Screen using Google Maps Compose. I don't want the default look. Apply a custom 'Parchment style' JSON (sepia tones, forest green labels). Create a 'Happening Now' filter chip that only shows events occurring within the next 7 days. Add a BottomSheet that pops up when a marker is tapped, showing the artist's photo, a WhatsApp contact button, and a distance indicator."

### Prompt 5: Gamification & Journey
"Build the 'My Journey' screen. Implement an XP progress bar and a system of 5 unlockable badges (Explorer, Patron, Apprentice, Chronicler, Guardian). The badges should be grayed out if locked and have a golden glow if unlocked. Include a 'Timeline' view of the user's cultural engagements (registrations and enrollments) with a vertical line connecting the items."
