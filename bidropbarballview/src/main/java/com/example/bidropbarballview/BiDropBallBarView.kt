package com.example.bidropbarballview

import android.view.View
import android.view.MotionEvent
import android.graphics.Paint
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Color
import android.app.Activity
import android.content.Context

val colors : Array<Int> = arrayOf(
    "#F44336",
    "#2196F3",
    "#8BC34A",
    "#3F51B5",
    "#009688"
).map {
    Color.parseColor(it)
}.toTypedArray()
val backColor : Int = Color.parseColor("#BDBDBD")
val barHFactor : Float = 11.2f
val ballRFactor : Float = 5.9f
val delay : Long = 20
val parts : Int = 4
val scGap : Float = 0.02f / parts

fun Int.inverse() : Float = 1f / this
fun Float.maxScale(i : Int, n : Int) : Float = Math.max(0f, this - i * n.inverse())
fun Float.divideScale(i : Int, n : Int) : Float = Math.min(n.inverse(), maxScale(i, n)) * n
fun Float.sinify() : Float = Math.sin(this * Math.PI).toFloat()

fun Canvas.drawBiDropBallBar(scale : Float, w : Float, h : Float, paint : Paint) {
    val sf : Float = scale.sinify()
    val sf1 : Float = sf.divideScale(0, parts)
    val sf2 : Float = sf.divideScale(1, parts)
    val sf3 : Float = sf.divideScale(2, parts)
    val sf4 : Float = sf.divideScale(3, parts)
    val ballR : Float = Math.min(w, h) / ballRFactor
    val barH : Float = h / barHFactor
    save()
    translate(w / 2, h / 2)
    for (j in 0..1) {
        save()
        scale(1f - 2 * j, 1f)
        translate(-w / 2 + (w / 2 - ballR) * sf4, -h / 2 + ballR + (h - 2 * ballR) * sf3)
        drawCircle(0f, 0f, ballR * sf2, paint)
        restore()
    }
    save()
    translate(0f, h / 2 - barH)
    drawRect(
        RectF(
            -(w / 2 - ballR) * sf1,
                0f,
            (w / 2 - ballR) * sf1,
                barH
        ),
        paint
    )
    restore()
    restore()
}

fun Canvas.drawDBBNode(i : Int, scale : Float, paint : Paint) {
    val w : Float = width.toFloat()
    val h : Float = height.toFloat()
    paint.color = colors[i]
    drawBiDropBallBar(scale, w, h, paint)
}
