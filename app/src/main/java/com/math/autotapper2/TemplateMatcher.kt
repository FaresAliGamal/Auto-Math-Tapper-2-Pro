package com.math.autotapper2

import android.graphics.Bitmap
import android.util.Log

object TemplateMatcher {

    fun findText(bitmap: Bitmap): String {
        Log.d("TemplateMatcher", "Mock findText() called")
        return "2 + 2"
    }

    fun getTemplate(name: String): Bitmap? {
        Log.d("TemplateMatcher", "Mock getTemplate() called")
        return null
    }
}
