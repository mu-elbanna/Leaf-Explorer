/*
 * MIT License
 *
 * Copyright (c) 2017 Yuriy Budiyev [yuriy.budiyev@yandex.ru]
 * Copyright (c) 2021 Veli Tasalı [me@velitasali.com]
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.monora.android.codescanner

import android.graphics.Rect
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.ReaderException
import com.google.zxing.Result
import org.monora.android.codescanner.Utils.decodeLuminanceSource
import org.monora.android.codescanner.Utils.getImageFrameRect
import org.monora.android.codescanner.Utils.rotateYuv

class DecodeTask(
    private val image: ByteArray,
    private val imageSize: CartesianCoordinate,
    private val previewSize: CartesianCoordinate,
    private val viewSize: CartesianCoordinate,
    private val viewFrameRect: Rect,
    private val orientation: Int,
    private val reverseHorizontal: Boolean,
) {
    @Throws(ReaderException::class)
    fun decode(reader: MultiFormatReader): Result? {
        var imageWidth = imageSize.x
        var imageHeight = imageSize.y
        val orientation = orientation
        val image = rotateYuv(image, imageWidth, imageHeight, orientation)

        if (orientation == 90 || orientation == 270) {
            val width = imageWidth
            imageWidth = imageHeight
            imageHeight = width
        }

        val frameRect = getImageFrameRect(imageWidth, imageHeight, viewFrameRect, previewSize, viewSize)
        val frameWidth: Int = frameRect.width()
        val frameHeight: Int = frameRect.height()

        return if (frameWidth < 1 || frameHeight < 1) {
            null
        } else decodeLuminanceSource(
            reader,
            PlanarYUVLuminanceSource(
                image,
                imageWidth,
                imageHeight,
                frameRect.left,
                frameRect.top,
                frameWidth,
                frameHeight,
                reverseHorizontal
            )
        )
    }
}