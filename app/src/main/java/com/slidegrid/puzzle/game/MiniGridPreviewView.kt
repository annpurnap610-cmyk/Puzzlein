package com.slidegrid.puzzle.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import com.slidegrid.puzzle.R

class MiniGridPreviewView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var gridSize: Int = 4
    private var isSelectedState: Boolean = false

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val blankPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val rect = RectF()

    private val colorBgSelected = ContextCompat.getColor(context, R.color.surface_elevated)
    private val colorBgDefault = ContextCompat.getColor(context, R.color.board_recess)
    private val colorTileSelected = ContextCompat.getColor(context, R.color.accent_amber)
    private val colorTileDefault = ContextCompat.getColor(context, R.color.ink_muted)
    private val colorBlank = ContextCompat.getColor(context, R.color.surface_arena)

    fun configure(size: Int, selected: Boolean) {
        gridSize = size.coerceIn(3, 5)
        isSelectedState = selected
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        if (w <= 0f || h <= 0f) return

        bgPaint.color = if (isSelectedState) colorBgSelected else colorBgDefault
        val outerCorner = w * 0.14f
        rect.set(0f, 0f, w, h)
        canvas.drawRoundRect(rect, outerCorner, outerCorner, bgPaint)

        val pad = w * 0.10f
        val inner = w - pad * 2f
        val cell = inner / gridSize
        val gap = cell * 0.12f
        val tileCorner = cell * 0.22f

        tilePaint.color = if (isSelectedState) colorTileSelected else colorTileDefault
        blankPaint.color = colorBlank

        val total = gridSize * gridSize
        for (idx in 0 until total) {
            val row = idx / gridSize
            val col = idx % gridSize
            val left = pad + col * cell + gap
            val top = pad + row * cell + gap
            val right = pad + (col + 1) * cell - gap
            val bottom = pad + (row + 1) * cell - gap
            rect.set(left, top, right, bottom)
            val paint = if (idx == total - 1) blankPaint else tilePaint
            canvas.drawRoundRect(rect, tileCorner, tileCorner, paint)
        }
    }
}
