# Karunada Kala (ಕರುನಾಡ ಕಲೆ) 🎭

**Karunada Kala** is a modern Android application designed to preserve, promote, and connect users with the rich cultural heritage of Karnataka. Built with Jetpack Compose and Firebase, it offers a seamless experience for discovering traditional art forms, finding local artists, and staying updated on cultural events.

---

## 🚀 Key Features

### 🔍 Explore Art Forms
- **Dynamic Discovery**: Browse through a curated list of Karnataka's traditional arts like Yakshagana, Dollu Kunitha, and more.
- **Instant Search**: Real-time filtering to find specific art forms quickly.
- **Category Filtering**: Narrow down your search by categories like Dance, Craft, Music, and Painting using interactive chips.
- **Deep Dives**: Detailed screens for every art form with high-quality imagery and comprehensive descriptions.

### 📍 Artist Map
- **Location-Based Discovery**: An interactive Google Map showing the locations of traditional artists across the state.
- **Dynamic Markers**: Artist information is fetched live from Firestore and rendered as custom markers.
- **Connectivity**: Tap a marker to see the artist's name and their specialty.

### 📅 Cultural Events
- **Stay Updated**: A dedicated section for upcoming cultural festivals, performances, and workshops.
- **Event Details**: Clear information on dates, locations, and the types of arts featured.

### ✨ Premium Experience
- **Cultural Theme**: A custom-designed UI using a **Deep Red & Gold** palette, reflecting the royalty and tradition of Karnataka.
- **Modern Navigation**: A smooth, gesture-based navigation flow using Compose Navigation.
- **Splash Screen**: A professional entrance using the modern Android Splash API.

---

## 🛠 Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Language**: [Kotlin](https://kotlinlang.org/)
- **Backend**: [Firebase Firestore](https://firebase.google.com/docs/firestore) & [Firebase Analytics](https://firebase.google.com/docs/analytics)
- **Mapping**: [Google Maps Compose SDK](https://github.com/googlemaps/android-maps-compose)
- **Image Loading**: [Coil](https://coil-kt.github.io/coil/)
- **Architecture**: MVVM (Model-View-ViewModel) + Repository Pattern
- **Dependency Management**: Gradle Version Catalog (`libs.versions.toml`)

---

## 🏗 Project Architecture

The app follows a scalable and maintainable architecture:
- **Data Layer**: Firestore handles real-time data; Repositories abstract data source logic.
- **UI Layer**: Composable functions for a reactive UI; ViewModels manage state using `StateFlow`.
- **Navigation**: Centralized `NavRoutes` for type-safe routing.

---

## 📸 Screenshots
*(Add your screenshots here to make the README pop!)*

---

## 🏁 Getting Started

1. Clone the repository.
2. Add your `google-services.json` to the `app/` directory.
3. Add your Google Maps API Key to `AndroidManifest.xml`.
4. Build and Run!

---

Developed with ❤️ for Karnataka's Heritage.
