package com.math.autotapper2

import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.util.Log

class LiveAnalyzeService : Service() {

    private val matcher = TemplateMatcher
    private val math = MathEngine
    private val capture = ScreenCaptureHelper()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        Log.d("LiveAnalyzeService", "Service started")
        // لو عندك MediaProjection لاحقاً، نقدر نمرّره هنا:
        capture.attach(null)
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.detach()
        Log.d("LiveAnalyzeService", "Service stopped")
    }

    // تُستدعى عندما يكون عندك إطار شاشة/صورة
    fun analyzeFrame(bitmap: Bitmap?) {
        if (bitmap == null) return
        val text = matcher.findText(bitmap) // من TemplateMatcher (موجود)
        val expression = math.extractExpression(text)
        math.postExpression(expression)
    }
}
