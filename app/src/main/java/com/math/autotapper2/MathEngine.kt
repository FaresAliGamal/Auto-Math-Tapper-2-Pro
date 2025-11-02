package com.math.autotapper2

import android.util.Log

object MathEngine {
    fun extractExpression(text: String): String {
        // Placeholder: استخرج أي تعبير بسيط من النص
        return text.trim()
    }
    fun postExpression(expression: String) {
        // Placeholder: إرسال/عرض الناتج
        Log.d("MathEngine", "postExpression: $expression")
    }
}
