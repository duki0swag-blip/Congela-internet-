package com.ejemplo.vpntoggle

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.View

class BotonFlotante(context: Context) : View(context) {

    private val paintCirculo = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.RED
    }

    private val paintBorde = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.WHITE
    }

    private val paintTexto = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    var activo: Boolean = false
        set(value) {
            field = value
            paintCirculo.color = if (value) Color.rgb(0, 180, 0) else Color.rgb(200, 0, 0)
            paintTexto.text = if (value) "ON" else "OFF"
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        val r = minOf(width, height) / 2f
        val cx = width / 2f
        val cy = height / 2f

        canvas.drawCircle(cx, cy, r - 3f, paintCirculo)
        canvas.drawCircle(cx, cy, r - 3f, paintBorde)

        paintTexto.textSize = r * 0.7f
        val baseline = cy - (paintTexto.descent() + paintTexto.ascent()) / 2f
        canvas.drawText(paintTexto.text, cx, baseline, paintTexto)
    }
}
