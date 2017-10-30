package org.walleth.functions

import android.graphics.Canvas
import android.graphics.Paint

import android.graphics.PaintFlagsDrawFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.DrawableWrapper
import android.widget.ImageView
import net.glxn.qrgen.android.QRCode


class AliasingDrawableWrapper(wrapped: Drawable) : DrawableWrapper(wrapped) {

    override fun draw(canvas: Canvas) {
        val oldDrawFilter = canvas.drawFilter
        canvas.drawFilter = DRAW_FILTER
        super.draw(canvas)
        canvas.drawFilter = oldDrawFilter
    }

    companion object {
        private val DRAW_FILTER = PaintFlagsDrawFilter(Paint.FILTER_BITMAP_FLAG, 0)
    }
}

fun ImageView.setQRCode(content: String)
        = setImageDrawable(AliasingDrawableWrapper(BitmapDrawable(resources, QRCode.from(content).bitmap())))
