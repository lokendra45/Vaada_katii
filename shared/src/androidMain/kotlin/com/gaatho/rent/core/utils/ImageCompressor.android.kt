package com.gaatho.rent.core.utils

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.Build
import androidx.core.graphics.scale
import androidx.exifinterface.media.ExifInterface
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

// --- Tunables -----------------------------------------------------------

/** Longest side we'll decode to initially. Plenty for mobile display/upload. */
private const val DEFAULT_MAX_DIMENSION = 2048

/** Never downscale below this on either side, no matter how tight the target. */
private const val MIN_DIMENSION_FLOOR = 320

private const val MAX_QUALITY = 92
private const val MIN_QUALITY = 20
private const val MAX_RESIZE_PASSES = 4
private const val RESIZE_FACTOR = 0.75f
private const val MAX_BINARY_SEARCH_STEPS = 6

// --- Public API -----------------------------------------------------------

actual suspend fun compressImage(
    imageBytes: ByteArray,
    fileName: String,
    compressionThreshold: Long
): ByteArray = withContext(Dispatchers.Default) {
    if (compressionThreshold <= 0 || imageBytes.size <= compressionThreshold) {
        return@withContext imageBytes
    }

    try {
        compressInternal(imageBytes, compressionThreshold)
    } catch (e: CancellationException) {
        throw e
    } catch (e: OutOfMemoryError) {
        imageBytes
    } catch (e: Exception) {
        imageBytes
    }
}

fun detectOutputMimeType(imageBytes: ByteArray): String {
    val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
    return if (options.outMimeType == "image/jpeg") "image/jpeg" else "image/webp"
}

// --- Internals -----------------------------------------------------------

private suspend fun compressInternal(
    imageBytes: ByteArray,
    compressionThreshold: Long
): ByteArray {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, boundsOptions)

    val sourceWidth = boundsOptions.outWidth
    val sourceHeight = boundsOptions.outHeight
    if (sourceWidth <= 0 || sourceHeight <= 0) {
        return imageBytes
    }

    var targetMaxDimension = DEFAULT_MAX_DIMENSION
    var decodedBitmap: Bitmap? = null

    while (decodedBitmap == null && targetMaxDimension >= MIN_DIMENSION_FLOOR) {
        decodedBitmap = try {
            decodeSampledBitmap(imageBytes, sourceWidth, sourceHeight, targetMaxDimension)
        } catch (e: OutOfMemoryError) {
            null
        }
        if (decodedBitmap == null) targetMaxDimension = (targetMaxDimension * 0.6f).toInt()
    }

    if (decodedBitmap == null) {
        return imageBytes
    }

    var workingBitmap = applyExifOrientation(decodedBitmap, imageBytes)

    try {
        val format = outputFormat(hasAlpha = workingBitmap.hasAlpha())
        var bestResult: ByteArray? = null
        var resizePass = 0

        while (true) {
            currentCoroutineContext().ensureActive()

            val (data, achievedQuality) = binarySearchQuality(
                bitmap = workingBitmap,
                format = format,
                targetBytes = compressionThreshold
            )

            if (bestResult == null || data.size < bestResult.size) {
                bestResult = data
            }

            val underThreshold = data.size <= compressionThreshold
            val atQualityFloor = achievedQuality <= MIN_QUALITY
            val canResizeFurther = resizePass < MAX_RESIZE_PASSES &&
                minOf(workingBitmap.width, workingBitmap.height) > MIN_DIMENSION_FLOOR

            if (underThreshold || !atQualityFloor || !canResizeFurther) {
                break
            }

            val newWidth = (workingBitmap.width * RESIZE_FACTOR).toInt()
                .coerceAtLeast(MIN_DIMENSION_FLOOR)
            val newHeight = (workingBitmap.height * RESIZE_FACTOR).toInt()
                .coerceAtLeast(MIN_DIMENSION_FLOOR)

            val scaled = try {
                workingBitmap.scale(newWidth, newHeight)
            } catch (e: OutOfMemoryError) {
                null
            } ?: break 

            if (scaled !== workingBitmap) {
                workingBitmap.recycle()
                workingBitmap = scaled
            }
            resizePass++
        }

        return bestResult ?: imageBytes
    } finally {
        if (!workingBitmap.isRecycled) workingBitmap.recycle()
    }
}

private suspend fun binarySearchQuality(
    bitmap: Bitmap,
    format: Bitmap.CompressFormat,
    targetBytes: Long
): Pair<ByteArray, Int> {
    var low = MIN_QUALITY
    var high = MAX_QUALITY

    val atFloor = compress(bitmap, format, low)
    if (atFloor.size > targetBytes) {
        return atFloor to low
    }

    var bestFit = atFloor
    var bestFitQuality = low
    var steps = 0

    while (low <= high && steps < MAX_BINARY_SEARCH_STEPS) {
        currentCoroutineContext().ensureActive()
        val mid = (low + high) / 2
        val attempt = compress(bitmap, format, mid)

        if (attempt.size <= targetBytes) {
            bestFit = attempt
            bestFitQuality = mid
            low = mid + 1 
        } else {
            high = mid - 1 
        }
        steps++
    }

    return bestFit to bestFitQuality
}

private fun compress(bitmap: Bitmap, format: Bitmap.CompressFormat, quality: Int): ByteArray {
    ByteArrayOutputStream(bitmap.byteCount / 8).use { stream ->
        bitmap.compress(format, quality, stream)
        return stream.toByteArray()
    }
}

private fun outputFormat(hasAlpha: Boolean): Bitmap.CompressFormat = when {
    !hasAlpha -> Bitmap.CompressFormat.JPEG
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> Bitmap.CompressFormat.WEBP_LOSSY
    else -> @Suppress("DEPRECATION") Bitmap.CompressFormat.WEBP
}

private fun decodeSampledBitmap(
    imageBytes: ByteArray,
    sourceWidth: Int,
    sourceHeight: Int,
    targetMaxDimension: Int
): Bitmap? {
    val options = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(sourceWidth, sourceHeight, targetMaxDimension)
        inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
}

private fun calculateInSampleSize(width: Int, height: Int, targetMaxDimension: Int): Int {
    var sampleSize = 1
    val longestSide = maxOf(width, height)
    while (longestSide / (sampleSize * 2) >= targetMaxDimension) {
        sampleSize *= 2
    }
    return sampleSize
}

private fun applyExifOrientation(bitmap: Bitmap, imageBytes: ByteArray): Bitmap {
    val orientation = try {
        ExifInterface(ByteArrayInputStream(imageBytes)).getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_NORMAL
        )
    } catch (e: Exception) {
        ExifInterface.ORIENTATION_NORMAL
    }

    val matrix = Matrix()
    when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
        ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
        ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
        ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
        ExifInterface.ORIENTATION_TRANSPOSE -> {
            matrix.postRotate(90f)
            matrix.postScale(-1f, 1f)
        }
        ExifInterface.ORIENTATION_TRANSVERSE -> {
            matrix.postRotate(270f)
            matrix.postScale(-1f, 1f)
        }
        else -> return bitmap 
    }

    return try {
        val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (rotated !== bitmap) bitmap.recycle()
        rotated
    } catch (e: OutOfMemoryError) {
        bitmap 
    }
}
