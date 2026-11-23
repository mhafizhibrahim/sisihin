package com.sisihin.app.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

object ImageStorageManager {
    private const val MAX_WIDTH = 1280
    private const val QUALITY = 85

    suspend fun saveFromUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val input = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeStream(input, null, opts)
            input.close()
            val scale = if (opts.outWidth > MAX_WIDTH) opts.outWidth / MAX_WIDTH else 1
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = scale.coerceAtLeast(1) }
            val input2 = context.contentResolver.openInputStream(uri) ?: return@withContext null
            val bitmap = BitmapFactory.decodeStream(input2, null, decodeOpts)
            input2.close()
            if (bitmap == null) return@withContext null
            val scaled = if (bitmap.width > MAX_WIDTH) {
                val ratio = MAX_WIDTH.toFloat() / bitmap.width
                Bitmap.createScaledBitmap(bitmap, MAX_WIDTH, (bitmap.height * ratio).toInt(), true)
            } else bitmap
            val dir = File(context.filesDir, "goal_images").apply { mkdirs() }
            val file = File(dir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(file).use { out ->
                scaled.compress(Bitmap.CompressFormat.JPEG, QUALITY, out)
            }
            if (scaled !== bitmap) scaled.recycle()
            bitmap.recycle()
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }

    fun delete(path: String?) {
        if (path.isNullOrBlank()) return
        try { File(path).delete() } catch (_: Exception) { }
    }
}