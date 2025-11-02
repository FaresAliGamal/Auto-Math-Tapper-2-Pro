package com.math.autotapper2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.core.content.ContextCompat

class ScreenCaptureHelper(private val activity: Activity) {

    private var mediaProjection: MediaProjection? = null
    private var imageReader: ImageReader? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var serviceStarted = false

    fun requestCaptureIntent(): Intent {
        // ابدأ الـFGS قبل طلب الإذن (مطلوب في Android 14)
        startFgService()
        val mpm = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        return mpm.createScreenCaptureIntent()
    }

    fun onActivityResult(resultCode: Int, data: Intent?) {
        val mpm = activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mpm.getMediaProjection(resultCode, data!!)
        setupVirtualDisplay()
    }

    private fun setupVirtualDisplay() {
        val wm = activity.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        wm.defaultDisplay.getRealMetrics(metrics)
        val w = metrics.widthPixels
        val h = metrics.heightPixels
        val density = metrics.densityDpi

        imageReader?.close()
        imageReader = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)

        virtualDisplay?.release()
        virtualDisplay = mediaProjection?.createVirtualDisplay(
            "AMT2",
            w, h, density,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            imageReader?.surface, null, null
        )
    }

    fun capture(callback: (Bitmap?) -> Unit) {
        val img = imageReader?.acquireLatestImage() ?: run { callback(null); return }
        val plane = img.planes[0]
        val w = img.width; val h = img.height; val rowStride = plane.rowStride; val pixelStride = plane.pixelStride
        val buffer = plane.buffer
        val tmp = Bitmap.createBitmap(rowStride / pixelStride, h, Bitmap.Config.ARGB_8888)
        tmp.copyPixelsFromBuffer(buffer)
        img.close()
        val bmp = Bitmap.createBitmap(tmp, 0, 0, w, h)
        callback(bmp)
    }

    fun stop() {
        virtualDisplay?.release(); virtualDisplay = null
        imageReader?.close(); imageReader = null
        mediaProjection?.stop(); mediaProjection = null
        stopFgService()
    }

    private fun startFgService() {
        if (!serviceStarted) {
            ContextCompat.startForegroundService(activity, Intent(activity, MediaProjectionFgService::class.java))
            serviceStarted = true
        }
    }

    private fun stopFgService() {
        if (serviceStarted) {
            activity.stopService(Intent(activity, MediaProjectionFgService::class.java))
            serviceStarted = false
        }
    }
}
