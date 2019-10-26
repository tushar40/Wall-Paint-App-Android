package com.example.paintapp

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.view.View
import android.util.AttributeSet
import android.graphics.Bitmap
import android.view.MotionEvent
import android.graphics.BitmapFactory
import android.graphics.Shader
import android.graphics.BitmapShader


class DrawingView(context: Context, attrs: AttributeSet) : View(context, attrs) {
    //drawing path
    private var drawPath: Path? = null
    //drawing and canvas paint
    private var drawPaint: Paint? = null
    private var canvasPaint: Paint? = null
    //initial color
    private var paintColor = -0x1000000
    //canvas
    private var drawCanvas: Canvas? = null
    //canvas bitmap
    private var canvasBitmap: Bitmap? = null

    @SuppressLint("NewApi")
    constructor(context: Context, attrs: AttributeSet, paintColor: Color) : this(context,attrs) {
        this.paintColor = paintColor.toArgb()
        setupDrawing()
    }

    private fun setupDrawing() {
        drawPath = Path()
        drawPaint = Paint()
        drawPaint?.color = paintColor
        drawPaint?.isAntiAlias = true
        drawPaint?.strokeWidth = 50f
        drawPaint?.style = Paint.Style.STROKE
        drawPaint?.strokeJoin = Paint.Join.ROUND
        drawPaint?.strokeCap = Paint.Cap.ROUND
        canvasPaint = Paint(Paint.DITHER_FLAG)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        drawCanvas = Canvas(canvasBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawBitmap(canvasBitmap!!, Rect(0,0,0,0),Rect(0,0,0,0) , canvasPaint)
        canvas.drawPath(drawPath!!, drawPaint!!)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y
        //respond to down, move and up events
        when (event.action) {
            MotionEvent.ACTION_DOWN -> drawPath?.moveTo(touchX, touchY)
            MotionEvent.ACTION_MOVE -> drawPath?.lineTo(touchX, touchY)
            MotionEvent.ACTION_UP -> {
                drawPath?.lineTo(touchX, touchY)
                drawCanvas?.drawPath(drawPath!!, drawPaint!!)
                drawPath?.reset()
            }
            else -> return false
        }
        //redraw
        invalidate()
        return true
    }

    @SuppressLint("NewApi")
    fun setPattern(newPattern: String) {
        invalidate()
        val patternID = resources.getIdentifier(newPattern, "drawable", "com.example.paintapp")
        val patternBMP = BitmapFactory.decodeResource(resources, patternID)
        val patternBMPshader = BitmapShader(
            patternBMP,
            Shader.TileMode.REPEAT, Shader.TileMode.REPEAT
        )
        drawPaint?.setColor(0xFFFFFFFF);
        drawPaint?.setShader(patternBMPshader);
    }
}