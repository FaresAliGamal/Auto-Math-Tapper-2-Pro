
package com.math.autotapper2
import android.content.Context
import android.graphics.Rect
import androidx.preference.PreferenceManager

object ROIStore {
    private const val L="roi_l"; private const val T="roi_t"; private const val R="roi_r"; private const val B="roi_b"
    fun save(ctx: Context, rect: Rect) {
        PreferenceManager.getDefaultSharedPreferences(ctx).edit()
            .putInt(L, rect.left).putInt(T, rect.top).putInt(R, rect.right).putInt(B, rect.bottom).apply()
    }
    fun load(ctx: Context): Rect {
        val p = PreferenceManager.getDefaultSharedPreferences(ctx)
        return if (p.contains(L) && p.contains(T) && p.contains(R) && p.contains(B))
            Rect(p.getInt(L,100), p.getInt(T,200), p.getInt(R,800), p.getInt(B,500))
        else Rect(100,200,800,500)
    }
}
