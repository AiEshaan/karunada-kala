package com.example.myapplication

import android.app.Application
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.request.CachePolicy
import coil.util.DebugLogger
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.PersistentCacheSettings
import okhttp3.OkHttpClient

class KarunadaKalaApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        
        // Initialize Google Maps SDK to prevent IBitmapDescriptorFactory not initialized crash
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) { renderer ->
            when (renderer) {
                MapsInitializer.Renderer.LATEST -> Log.d("MapsInit", "The latest version of the renderer is used.")
                MapsInitializer.Renderer.LEGACY -> Log.d("MapsInit", "The legacy version of the renderer is used.")
            }
        }
        
        // Enable Firestore Offline Persistence for a "Pass" like experience
        val settings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(PersistentCacheSettings.newBuilder().build())
            .build()
        FirebaseFirestore.getInstance().setFirestoreSettings(settings)
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient {
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request().newBuilder()
                            .header("User-Agent", "KarunadaKala/1.0 (https://example.com; contact@example.com) Coil/2.7.0")
                            .build()
                        chain.proceed(request)
                    }
                    .build()
            }
            .crossfade(600) // Slightly longer for premium feel
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Cache images even if headers say otherwise (good for art images)
            .build()
    }
}
