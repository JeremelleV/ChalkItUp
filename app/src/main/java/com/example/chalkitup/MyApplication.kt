// Google was not launching due to not having emoji compatibility, causing a crash indicated from logcat

package com.example.chalkitup

import android.app.Application
import androidx.emoji2.text.EmojiCompat
import androidx.emoji2.bundled.BundledEmojiCompatConfig

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize EmojiCompat with bundled emoji support
        val config = BundledEmojiCompatConfig(this)
        EmojiCompat.init(config)
    }
}
