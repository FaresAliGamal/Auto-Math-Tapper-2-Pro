package com.math.autotapper2

import android.app.*
import android.content.*
import android.graphics.Bitmap
import android.graphics.Rect
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import kotlin.concurrent.thread

/**
 * LiveAnalyzeService:
 * - تستقبل (resultCode, data) من MediaProjection عبر Intent
 * - تلتقط الشاشة دوريًا داخل ROI
 * - تتعرف على التعبير وتحسبه
 * - تبحث عن نتيجة الحل كنص داخل نفس الـROI
 * - لو خدمة الوصول مفعّلة، تضغط على مكان النتيجة
 */
class LiveAnalyzeService : Service() {

    companion object {
        const val CH_ID = "amt2_live"
        const val NOTI_ID = 2001
        const val EXTRA_RESULT_CODE = "resultCode"
        const val EXTRA_DATA = "data"
        const val EXTRA_PERIOD_MS = "periodMs"
    }

    private var running = false
    private var periodMs = 600L
    private var projection: MediaProjection? = null
    private lateinit var screen: ScreenCaptureHelper
    private val matcher = TemplateMatcher
    private val math = MathEngine

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        ensureChannel()
        startForeground(
            NOTI_ID,
            NotificationCompat.Builder(this, CH_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle("Auto Math Tapper 2")
                .setContentText("Live analyzing screen…")
                .setOngoing(true)
                .build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) return START_NOT_STICKY
        periodMs = intent.getLongExtra(EXTRA_PERIOD_MS, 600L).coerceAtLeast(150L)
        val resultCode = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
        val data = intent.getParcelableExtra<Intent>(EXTRA_DATA)
        if (data == null || resultCode != Activity.RESULT_OK) {
            stopSelf()
            return START_NOT_STICKY
        }
        val mpm = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        projection = mpm.getMediaProjection(resultCode, data)
        screen = ScreenCaptureHelper(this).apply { attach(projection!!) }

        if (!running) {
            running = true
            thread(name = "amt2-live") {
                loop()
            }
        }
        return START_STICKY
    }

    private fun loop() {
        while (running) {
            try {
                val roi = ROIStore.get(this)
                val bmp: Bitmap = screen.capture(roi) ?: continue
                val expr: String = matcher.extractExpression(bmp) ?: ""
                if (expr.isNotBlank()) {
                    val value = math.evaluate(expr)
                    MainActivity.postExpression(this, "$expr = $value")
                    // ابحث عن قيمة الحل داخل نفس الصورة واضغط
                    val best: Rect? = matcher.findText(bmp, value.toString())
                    if (best != null && AutoTapService.isEnabled(this)) {
                        // حول إحداثيات ROI المحلية إلى إحداثيات الشاشة
                        val gx = roi.left + best.exactCenterX().toInt()
                        val gy = roi.top + best.exactCenterY().toInt()
                        AutoTapService.performTap(this, gx, gy)
                    }
                }
            } catch (_: Throwable) { /* تجاهل الإطار التالف */ }
            SystemClock.sleep(periodMs)
        }
        stopSelf()
    }

    override fun onDestroy() {
        running = false
        screen.detach()
        projection?.stop()
        super.onDestroy()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (nm.getNotificationChannel(CH_ID) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(CH_ID, "Live Analyze", NotificationManager.IMPORTANCE_LOW)
                )
            }
        }
    }
}
