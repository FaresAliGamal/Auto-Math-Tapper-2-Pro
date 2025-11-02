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
    }

    override fun onDestroy() {
        super.onDestroy()
        capture.detach()
        Log.d("LiveAnalyzeService", "Service stopped")
    }

    fun analyzeFrame(bitmap: Bitmap?) {
        if (bitmap == null) return
        // placeholder لتحليل الصورة
        val text = matcher.findText(bitmap) ?: "no text"
        val expression = math.extractExpression(text)
        math.postExpression(expression)
    }
}
