package com.example.myapplication.data

import android.util.Log
import com.example.myapplication.data.model.ArtForm
import com.example.myapplication.data.model.Artist
import com.example.myapplication.data.model.Event
import com.example.myapplication.data.model.Workshop
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

/**
 * Utility object to seed Firestore with initial cultural data for Karunada Kala.
 * This ensures the "Directory of Pride" has content for presentation.
 */
object SeedData {
    private val db get() = FirebaseFirestore.getInstance()
    private const val TAG = "SeedData"

    suspend fun seedAll() {
        Log.d(TAG, "Starting data seeding...")
        try {
            seedArtForms()
            seedArtists()
            seedEvents()
            seedWorkshops()
            Log.d(TAG, "Seeding completed successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Seeding failed", e)
        }
    }

    private suspend fun seedArtForms() {
        val arts = listOf(
            ArtForm(
                name = "Yakshagana",
                description = "A magnificent traditional theatre form of Karnataka that blends dance, music, dialogue, and stunning costumes. It brings ancient epics to life with vibrant energy and rhythmic perfection, serving as the soul of coastal Karnataka's cultural expression.",
                category = "Performing Arts",
                imageUrl = "https://images.unsplash.com/photo-1628155930542-3c7a64e2c833",
                artistName = "Keshava Hegde",
                artistId = "keshava_hegde",
                viewCount = 1250,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
            ),
            ArtForm(
                name = "Kinnala Toys",
                description = "The royal heritage of Koppal, Kinnala toys are exquisite handcrafted wooden wonders known for their 'Lajja' work. These toys, often depicting deities and rural life, are made using special paste and gold foil, representing a 400-year-old legacy of the Vijayanagara Empire.",
                category = "Craft",
                imageUrl = "https://images.pexels.com/photos/161154/doll-toy-handmade-traditional-161154.jpeg",
                artistName = "Radha Krishna",
                artistId = "radha_krishna",
                viewCount = 1420,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-6.mp3"
            ),
            ArtForm(
                name = "Dollu Kunitha",
                description = "A rhythmic power-dance performed with large drums, rooted in the devotion to Lord Beeralingeshwara.",
                category = "Dance",
                imageUrl = "https://images.unsplash.com/photo-1590053413903-89945d35a8f4",
                artistName = "Narasimhaiah",
                artistId = "narasimhaiah",
                viewCount = 980,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-2.mp3",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"
            ),
            ArtForm(
                name = "Togalu Gombeyaata",
                description = "Ancient shadow puppetry of Karnataka, where leather puppets tell tales from the epics.",
                category = "Puppetry",
                imageUrl = "https://images.unsplash.com/photo-1516280440614-37939bbacd81",
                artistName = "Belagal Veeranna",
                artistId = "belagal_veeranna",
                viewCount = 750,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-3.mp3",
                videoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"
            ),
            ArtForm(
                name = "Channapatna Toys",
                description = "Lacquered wooden toys from the 'Town of Toys', world-renowned for their safe, organic craft.",
                category = "Craft",
                imageUrl = "https://images.unsplash.com/photo-1531317506698-472bc0df6124",
                artistName = "Ibrahim Sait",
                artistId = "ibrahim_sait",
                viewCount = 2100,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-4.mp3"
            ),
            ArtForm(
                name = "Mysore Paintings",
                description = "Classical paintings known for their elegance, muted colors, and intricate gold leaf work.",
                category = "Painting",
                imageUrl = "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5",
                artistName = "B.P. Bayiri",
                artistId = "bp_bayiri",
                viewCount = 1540,
                audioUrl = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-5.mp3"
            ),
            ArtForm(
                name = "Bidriware",
                description = "A stunning metal handicraft from Bidar, involving silver inlay on a blackened zinc-copper alloy.",
                category = "Handicraft",
                imageUrl = "https://images.unsplash.com/photo-1605721911519-3dfeb3be25e7",
                artistName = "Shah Rasheed Ahmed",
                artistId = "shah_rasheed",
                viewCount = 1100
            ),
            ArtForm(
                name = "Lambani Embroidery",
                description = "Vibrant, geometric embroidery with mirror work by the nomadic Lambani community.",
                category = "Textile",
                imageUrl = "https://images.unsplash.com/photo-1606103920295-9a091573f160",
                artistName = "Shanti Bai",
                artistId = "shanti_bai",
                viewCount = 890
            ),
            ArtForm(
                name = "Kambala",
                description = "The legendary buffalo race of coastal Karnataka, celebrating the bond between farmer and soil.",
                category = "Tradition",
                imageUrl = "https://images.unsplash.com/photo-1624008915317-cb3ad69b16ad",
                artistName = "Srinivasa Gowda",
                artistId = "srinivasa_gowda",
                viewCount = 3200
            ),
            ArtForm(
                name = "Ilkal Sarees",
                description = "Traditional handloom sarees from Bagalkot, recognized by their distinct red 'Topi Teni' pallu.",
                category = "Textile",
                imageUrl = "https://images.unsplash.com/photo-1610030469668-93510029013d",
                artistName = "Mallappa",
                artistId = "mallappa",
                viewCount = 1800
            ),
            ArtForm(
                name = "Udupi Cuisine",
                description = "A world-famous culinary legacy originating from the Krishna Temple of Udupi.",
                category = "Culinary",
                imageUrl = "https://images.unsplash.com/photo-1589302168068-964664d93dc0",
                artistName = "Krishna Rao",
                artistId = "krishna_rao",
                viewCount = 4500
            ),
        )

        val batch = db.batch()
        arts.forEach { art ->
            val docRef = db.collection("arts").document()
            batch.set(docRef, art.copy(id = docRef.id))
        }
        batch.commit().await()
    }

    private suspend fun seedArtists() {
        val artists = listOf(
            Artist(name = "Keshava Hegde", artType = "Yakshagana", bio = "A legendary Yakshagana artist with over 40 years of experience in coastal theater.", phone = "9876543210", lat = 13.0, lng = 75.0, city = "Sirsi", experienceYears = 40),
            Artist(name = "Radha Krishna", artType = "Kinnala Toys", bio = "A master craftsman from Koppal specializing in the intricate 'Lajja' gold-leaf work of Kinnala.", phone = "9123488776", lat = 15.35, lng = 76.15, city = "Koppal", experienceYears = 32),
            Artist(name = "Narasimhaiah", artType = "Dollu Kunitha", bio = "Master of the Dollu drum, keeping the rhythmic heartbeat of rural Karnataka alive.", phone = "9123456789", lat = 12.5, lng = 76.8, city = "Tumkur", experienceYears = 28),
            Artist(name = "Belagal Veeranna", artType = "Togalu Gombeyaata", bio = "National award winner dedicated to preserving the ancient art of leather shadow puppetry.", phone = "9887766554", lat = 15.0, lng = 76.0, city = "Bellary", experienceYears = 35),
            Artist(name = "Ibrahim Sait", artType = "Channapatna Toys", bio = "A veteran toy-maker ensuring that the tradition of lacquered wood remains organic and safe.", phone = "9765432109", lat = 12.65, lng = 77.2, city = "Channapatna", experienceYears = 30),
            Artist(name = "Shanti Bai", artType = "Lambani Embroidery", bio = "A resilient artisan bringing the mirrors and geometric patterns of the Lambani nomads to the world.", phone = "9776655443", lat = 14.5, lng = 76.5, city = "Sandur", experienceYears = 22)
        )

        val batch = db.batch()
        artists.forEach { artist ->
            val docId = artist.name.replace(" ", "_").lowercase()
            val docRef = db.collection("artists").document(docId)
            batch.set(docRef, artist.copy(id = docId))
        }
        batch.commit().await()
    }

    private suspend fun seedEvents() {
        val events = listOf(
            Event(title = "Hampi Utsava", artType = "Cultural Festival", date = "Nov 3-5, 2024", location = "Hampi", description = "The grand festival of Hampi showcasing Karnataka culture.", lat = 15.335, lng = 76.46),
            Event(title = "Mysuru Dasara", artType = "Royal Festival", date = "Oct 3-12, 2024", location = "Mysuru", description = "The state festival of Karnataka celebrated with grand procession.", lat = 12.305, lng = 76.655),
            Event(title = "Kadalekai Parishe", artType = "Folk Festival", date = "Nov 25, 2024", location = "Bengaluru", description = "The annual groundnut fair at Bull Temple.", lat = 12.942, lng = 77.568)
        )

        val batch = db.batch()
        events.forEach { event ->
            val docRef = db.collection("events").document()
            batch.set(docRef, event.copy(id = docRef.id))
        }
        batch.commit().await()
    }

    private suspend fun seedWorkshops() {
        val workshops = listOf(
            Workshop(title = "Yakshagana Basics", artistName = "Keshava Hegde", artistId = "keshava_hegde", artType = "Yakshagana", date = "Dec 1, 2024", feeRaw = 500L, availableSlots = 20, location = "Udupi", lat = 13.34, lng = 74.74),
            Workshop(title = "Toy Making", artistName = "Ibrahim Sait", artistId = "ibrahim_sait", artType = "Craft", date = "Dec 5, 2024", feeRaw = "Free", availableSlots = 15, location = "Channapatna", lat = 12.65, lng = 77.21),
            Workshop(title = "Lambani Art", artistName = "Shanti Bai", artistId = "shanti_bai", artType = "Embroidery", date = "Dec 10, 2024", feeRaw = 300L, availableSlots = 10, location = "Hampi", lat = 15.33, lng = 76.46)
        )

        val batch = db.batch()
        workshops.forEach { workshop ->
            val docRef = db.collection("workshops").document()
            batch.set(docRef, workshop.copy(id = docRef.id))
        }
        batch.commit().await()
    }
}
