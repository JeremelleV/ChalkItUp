package com.example.chalkitup.ui.offline

import android.app.Application
import com.google.firebase.database.FirebaseDatabase
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.bundled.BundledEmojiCompatConfig
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings

class OfflineFirebase: Application() {
        override fun onCreate(){
            super.onCreate()
            FirebaseDatabase.getInstance().setPersistenceEnabled(true)

            FirebaseFirestore.getInstance().firestoreSettings = FirebaseFirestoreSettings.Builder()
                .build()

            // EmojiCompat initialization, Without emoji edition i got incompatibility crashes, this fixed that issue
            val config = BundledEmojiCompatConfig(this)
            EmojiCompat.init(config)

        }
}