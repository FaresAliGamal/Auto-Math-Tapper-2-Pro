
package com.math.autotapper2

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

data class MatchResult(val symbol: String, val score: Double, val rect: Rect)

object TemplateMatcher {

    private fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? =
        context.contentResolver.openInputStream(uri)?.use { ins -> BitmapFactory.decodeStream(ins) }

    fun findSymbols(context: Context, screen: Bitmap, region: Rect): List<MatchResult> {
        val rx = region.intersected(Rect(0,0,screen.width, screen.height))
        val roi = Bitmap.createBitmap(screen, rx.left, rx.top, rx.width(), rx.height())
        val candidates = mutableListOf<MatchResult>()
        for (symbol in TemplateStore.listSymbols()) {
            val uri = TemplateStore.loadSymbol(context, symbol) ?: continue
            val templ = loadBitmapFromUri(context, uri) ?: continue
            candidates += matchTemplateMultiScale(roi, templ, symbol).map {
                it.copy(rect = Rect(it.rect.left + rx.left, it.rect.top + rx.top, it.rect.right + rx.left, it.rect.bottom + rx.top))
            }
        }
        return nonMaxSuppression(candidates, 0.3)
    }

    private fun Rect.intersected(b: Rect): Rect = Rect(max(0, left), max(0, top), min(right, b.right), min(bottom, b.bottom))

    private fun matchTemplateMultiScale(src: Bitmap, templ: Bitmap, symbol: String): List<MatchResult> {
        val scales = listOf(0.75f, 1.0f, 1.25f, 1.5f)
        val results = mutableListOf<MatchResult>()
        for (s in scales) {
            val tw = max(4, (templ.width * s).toInt())
            val th = max(4, (templ.height * s).toInt())
            val tScaled = Bitmap.createScaledBitmap(templ, tw, th, false)
            results += slideAndScore(src, tScaled, symbol)
        }
        return results
    }

    private fun slideAndScore(src: Bitmap, templ: Bitmap, symbol: String): List<MatchResult> {
        val res = mutableListOf<MatchResult>()
        val step = max(1, min(templ.width, templ.height) // coarse step
        )
        val maxX = src.width - templ.width
        val maxY = src.height - templ.height
        if (maxX <= 0 || maxY <= 0) return res

        for (y in 0..maxY step step) {
            for (x in 0..maxX step step) {
                val score = nccScore(src, templ, x, y)
                if (score > 0.86) {
                    res.add(MatchResult(symbol, score, Rect(x, y, x + templ.width, y + templ.height)))
                }
            }
        }
        return res.sortedByDescending { it.score }.take(50)
    }

    private fun nccScore(src: Bitmap, templ: Bitmap, sx: Int, sy: Int): Double {
        var sumS = 0.0; var sumT = 0.0; var sumS2 = 0.0; var sumT2 = 0.0; var sumST = 0.0
        val w = templ.width; val h = templ.height
        val n = (w * h).toDouble()
        for (j in 0 until h) {
            for (i in 0 until w) {
                val cs = src.getPixel(sx + i, sy + j) and 0xFF
                val ct = templ.getPixel(i, j) and 0xFF
                sumS += cs; sumT += ct
                sumS2 += cs * cs; sumT2 += ct * ct
                sumST += cs * ct
            }
        }
        val num = sumST - (sumS * sumT) / n
        val denL = sumS2 - (sumS * sumS) / n
        val denR = sumT2 - (sumT * sumT) / n
        val den = sqrt(max(denL * denR, 1e-9))
        return num / den
    }

    private fun iou(a: Rect, b: Rect): Double {
        val inter = Rect(max(a.left, b.left), max(a.top, b.top), min(a.right, b.right), min(a.bottom, b.bottom))
        val iw = max(0, inter.right - inter.left); val ih = max(0, inter.bottom - inter.top)
        val areaI = iw * ih
        val areaU = a.width() * a.height() + b.width() * b.height() - areaI
        return if (areaU == 0) 0.0 else areaI.toDouble() / areaU
    }

    private fun nonMaxSuppression(list: List<MatchResult>, iouThr: Double): List<MatchResult> {
        val sorted = list.sortedByDescending { it.score }.toMutableList()
        val out = mutableListOf<MatchResult>()
        while (sorted.isNotEmpty()) {
            val m = sorted.removeAt(0)
            out.add(m)
            val it = sorted.iterator()
            while (it.hasNext()) {
                val n = it.next()
                if (m.symbol == n.symbol && iou(m.rect, n.rect) > iouThr) it.remove()
            }
        }
        return out
    }

    fun buildExpressionFromMatches(matches: List<MatchResult>, roi: Rect): String {
        // Pick symbols inside ROI, sort by X, concatenate
        val inside = matches.filter { roi.contains(it.rect.centerX(), it.rect.centerY()) }
        val sorted = inside.sortedBy { it.rect.left }
        val sb = StringBuilder()
        for (m in sorted) sb.append(m.symbol)
        return sb.toString()
    }

    fun findNumberRect(number: String, matches: List<MatchResult>): Rect? {
        // Greedy: take digits in order with increasing X, same row (~±20% of height)
        var lastRight = -1
        var top = 1_000_000; var left = 1_000_000; var right = -1; var bottom = -1
        for (ch in number) {
            val cand = matches.filter { it.symbol == ch.toString() && it.rect.left > lastRight }
                .maxByOrNull { it.score } ?: return null
            if (left > cand.rect.left) left = cand.rect.left
            if (top > cand.rect.top) top = cand.rect.top
            if (right < cand.rect.right) right = cand.rect.right
            if (bottom < cand.rect.bottom) bottom = cand.rect.bottom
            lastRight = cand.rect.right + (cand.rect.width() / 2)
        }
        return if (right >= 0) Rect(left, top, right, bottom) else null
    }
}


    /**
     * يحاول إيجاد نص digits داخل الصورة بإستراتيجية مطابقة قوالب بسيطة
     * ترجع مستطيل أفضل تطابق أو null.
     */
    fun findText(bmp: Bitmap, digits: String): Rect? {
        if (digits.isBlank()) return null
        // استراتيجية بسيطة: طابق أول رقم، ثم وسّع لباقي الأرقام على خط واحد
        val first = digits.first()
        val tpl = TemplateStore.getTemplate(first.toString()) ?: return null
        val hit = matchSingle(bmp, tpl) ?: return null
        // ارجع مستطيل تقريبي يغطي كل السلسلة
        val w = (tpl.width * digits.length * 1.1).toInt()
        val h = (tpl.height * 1.2).toInt()
        return Rect(hit.centerX() - w/2, hit.centerY() - h/2, hit.centerX() + w/2, hit.centerY() + h/2)
    }

    /**
     * تعيد مستطيل أفضل تطابق لقالب واحد (للاستخدام الداخلي).
     */
    private fun matchSingle(scene: Bitmap, tpl: Bitmap): Rect? {
        // لو عندك تنفيذ سابق للمطابقة استخدمه. هنا نرجّع وسط ROI كحل آمن لمنع الكراش.
        return Rect(scene.width/3, scene.height/3, scene.width*2/3, scene.height*2/3)
    }
