package com.example.tcg_manager.util

import android.graphics.Bitmap
import android.graphics.Matrix
import kotlin.math.max

/** Returns this bitmap rotated by [degrees] (no-op for 0). */
fun Bitmap.rotated(degrees: Int): Bitmap {
    if (degrees % 360 == 0) return this
    val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

/** Scales down so the longest side is at most [maxSide] px. Keeps OCR fast without losing text. */
fun Bitmap.scaledDownTo(maxSide: Int): Bitmap {
    val longest = max(width, height)
    if (longest <= maxSide) return this
    val ratio = maxSide.toFloat() / longest
    return Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
}
