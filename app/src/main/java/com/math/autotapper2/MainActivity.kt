
package com.math.autotapper2

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var tvStatus: TextView
    private var roi: Rect = Rect()
    private lateinit var capture: ScreenCaptureHelper

    private val pickRoiLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            roi = ROIStore.load(this)
            tvStatus.text = "ROI: (${roi.left},${roi.top})-(${roi.right},${roi.bottom})"
        }

    private val captureLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                capture.onActivityResult(result.resultCode, result.data)
                Toast.makeText(this, "Screen capture enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Capture permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        capture = ScreenCaptureHelper(this)

        tvStatus = findViewById(R.id.tvStatus)
        roi = ROIStore.load(this)
        tvStatus.text = "ROI: (${roi.left},${roi.top})-(${roi.right},${roi.bottom})"

        findViewById<Button>(R.id.btnUploadSymbols).setOnClickListener {
            startActivity(Intent(this, SymbolUploaderActivity::class.java))
        }
        findViewById<Button>(R.id.btnPickRoi).setOnClickListener {
            pickRoiLauncher.launch(Intent(this, PickRoiActivity::class.java))
        }
        findViewById<Button>(R.id.btnRequestCapture).setOnClickListener {
            captureLauncher.launch(capture.requestCaptureIntent())
        }
        findViewById<Button>(R.id.btnTestSymbols).setOnClickListener {
            startActivity(Intent(this, SymbolTestActivity::class.java))
        }
        findViewById<Button>(R.id.btnStartAnalyze).setOnClickListener {
            doAnalyzeAndTap()
        }
    }

    private fun doAnalyzeAndTap() {
        capture.capture { bmp: Bitmap? ->
            if (bmp == null) {
                runOnUiThread { Toast.makeText(this, "No frame yet. Tap Request Capture then try again.", Toast.LENGTH_LONG).show() }
                return@capture
            }
            val matches = TemplateMatcher.findSymbols(this, bmp, Rect(0,0,bmp.width, bmp.height))
            val expr = TemplateMatcher.buildExpressionFromMatches(matches, roi)
            val result = try { MathEngine.evaluate(expr).toInt().toString() } catch (e: Exception) { "?" }

            var status = "Expr: $expr = $result"
            var tapped = false
            if (result != "?") {
                val numberRect = TemplateMatcher.findNumberRect(result, matches.filter { !roi.contains(it.rect.centerX(), it.rect.centerY()) })
                if (numberRect != null) {
                    status += "\nTap -> $numberRect"
                    AutoTapService.instance?.tapRect(numberRect)
                    tapped = true
                } else {
                    status += "\nResult digits not located outside ROI."
                }
            }
            val s = status + if (tapped) "\nTapped." else ""
            runOnUiThread { tvStatus.text = s }
        }
    }
}
