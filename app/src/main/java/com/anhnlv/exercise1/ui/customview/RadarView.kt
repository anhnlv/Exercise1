package com.anhnlv.exercise1.ui.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class RadarView : View {
    private var radius = 0
    private lateinit var paint: Paint
    private var isInit = false
    private var mAccuracy: Float = 0f

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    )

    private fun initRadar() {
        radius = height.coerceAtMost(width) / 2
        paint = Paint()
        isInit = true

    }

    override fun onDraw(canvas: Canvas) {
        if (!isInit) {
            initRadar()
        }
        canvas.drawColor(Color.TRANSPARENT)
        if(mAccuracy!=0f) {
            drawCircle(canvas)
        }
        drawCenterPoint(canvas)
        invalidate()
    }

    private fun drawCenterPoint(canvas: Canvas) {
        paint.style = Paint.Style.FILL
        paint.color = Color.YELLOW
        canvas.drawCircle((width / 2).toFloat(), (height / 2).toFloat(), 12f, paint)

    }

    private fun drawCircle(canvas: Canvas) {
        paint.reset()
        paint.color = Color.CYAN
        paint.strokeWidth = 5f
        paint.style = Paint.Style.STROKE
        paint.isAntiAlias = true
        canvas.drawCircle(
            radius.toFloat(),
            radius.toFloat(),
            elevationToRadius(width, mAccuracy),
            paint
        )

    }

    private fun elevationToRadius(width: Int, elevation: Float): Float {
        return (width / 2) * (1f - elevation / 90f)
    }

    fun setAccuracy(accuracy: Float) {
        mAccuracy = accuracy
        invalidate()
    }

}