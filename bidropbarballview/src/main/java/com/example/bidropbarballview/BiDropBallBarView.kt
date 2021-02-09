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
val ballRFactor : Float = 15.3f
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
        drawCircle(ballR, 0f, ballR * sf2, paint)
        restore()
    }
    save()
    translate(0f, h / 2 - barH)
    drawRect(
        RectF(
            -(w / 2 -  2 * ballR) * (sf1 - sf4),
                0f,
            (w / 2 - 2 * ballR) * (sf1 - sf4),
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

class BiDropBallBarView(ctx : Context) : View(ctx) {

    private val renderer : Renderer = Renderer(this)

    override fun onDraw(canvas : Canvas) {
        renderer.render(canvas)
    }

    override fun onTouchEvent(event : MotionEvent) : Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                renderer.handleTap()
            }
        }
        return true
    }

    data class State(var scale : Float = 0f, var dir : Float = 0f, var prevScale : Float = 0f) {

        fun update(cb : (Float) -> Unit) {
            scale += scGap * dir
            if (Math.abs(scale - prevScale) > 1) {
                scale = prevScale + dir
                dir = 0f
                prevScale = scale
                cb(prevScale)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            if (dir == 0f) {
                dir = 1f - 2 * prevScale
                cb()
            }
        }
    }

    data class Animator(var view : View, var animated : Boolean = false) {

        fun animate(cb : () -> Unit) {
            if (animated) {
                cb()
                try {
                    Thread.sleep(delay)
                    view.invalidate()
                } catch(ex : Exception) {

                }
            }
        }

        fun start() {
            if (!animated) {
                animated = true
                view.postInvalidate()
            }
        }

        fun stop() {
            if (animated) {
                animated = false
            }
        }
    }

    data class BDBBNode(var i : Int, val state : State = State()) {

        private var next : BDBBNode? = null
        private var prev : BDBBNode? = null

        init {
            addNeighbor()
        }

        fun addNeighbor() {
            if (i < colors.size - 1) {
                next = BDBBNode(i + 1)
                next?.prev = this
            }
        }

        fun draw(canvas : Canvas, paint : Paint) {
            canvas.drawDBBNode(i, state.scale, paint)
        }

        fun update(cb : (Float) -> Unit) {
            state.update(cb)
        }

        fun startUdpating(cb : () -> Unit) {
            state.startUpdating(cb)
        }

        fun getNext(dir : Int, cb : () -> Unit) : BDBBNode {
            var curr : BDBBNode? = prev
            if (dir == 1) {
                curr = next
            }
            if (curr != null) {
                return curr
            }
            cb()
            return this
        }
    }

    data class BiDropBallBar(var i : Int) {

        private var curr : BDBBNode = BDBBNode(0)
        private var dir : Int = 1

        fun draw(canvas : Canvas, paint : Paint) {
            curr.draw(canvas, paint)
        }

        fun update(cb : (Float) -> Unit) {
            curr.update {
                curr = curr.getNext(dir) {
                    dir *= -1
                }
                cb(it)
            }
        }

        fun startUpdating(cb : () -> Unit) {
            curr.startUdpating(cb)
        }
    }

    data class Renderer(var view : BiDropBallBarView) {

        private val animator : Animator = Animator(view)
        private val bdbb : BiDropBallBar = BiDropBallBar(0)
        private val paint : Paint = Paint(Paint.ANTI_ALIAS_FLAG)

        fun render(canvas : Canvas) {
            canvas.drawColor(backColor)
            bdbb.draw(canvas, paint)
            animator.animate {
                bdbb.update {
                    animator.stop()
                }
            }
        }

        fun handleTap() {
            bdbb.startUpdating {
                animator.start()
            }
        }
    }

    companion object {

        fun create(activity : Activity) : BiDropBallBarView {
            val view : BiDropBallBarView = BiDropBallBarView(activity)
            activity.setContentView(view)
            return view
        }
    }
}