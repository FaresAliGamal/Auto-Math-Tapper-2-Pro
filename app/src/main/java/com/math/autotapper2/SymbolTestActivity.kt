
package com.math.autotapper2

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SymbolTestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_symbol_test)
        val container = findViewById<LinearLayout>(R.id.container)

        for (s in TemplateStore.listSymbols()) {
            val uri = TemplateStore.loadSymbol(this, s)
            val title = TextView(this).apply { text = "Symbol: $s" }
            container.addView(title)

            if (uri != null) {
                contentResolver.openInputStream(uri)?.use { ins ->
                    val bmp = BitmapFactory.decodeStream(ins)
                    val iv = ImageView(this)
                    iv.setImageBitmap(bmp)
                    iv.adjustViewBounds = true
                    iv.maxWidth = 400
                    container.addView(iv)
                }
            } else {
                val miss = TextView(this).apply { text = "Not uploaded yet." }
                container.addView(miss)
            }
        }
    }
}
