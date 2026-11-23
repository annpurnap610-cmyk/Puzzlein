package com.slidegrid.puzzle.game

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.core.content.ContextCompat
import com.slidegrid.puzzle.R
import kotlin.math.min

class PuzzleBoardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    interface OnTileInteractionListener {
        fun onTileTapped(index: Int)
        fun onAnimationStateChanged(isAnimating: Boolean)
    }

    private var gridSize: Int = 4
    private var board: List<Int> = emptyList()
    private var previousPositions: Map<Int, Pair<Int, Int>> = emptyMap()
    private var animatingTiles: Set<Int> = emptySet()
    private var animationProgress: Float = 1f
    private var animator: ValueAnimator? = null
    private var isInteractive: Boolean = true
    private var hapticsEnabled: Boolean = true
    private var listener: OnTileInteractionListener? = null

    private val recessPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val recessStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = 0x33000000
    }
    private val tilePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val tileStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
    }
    private val correctDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
    }

    private val rectBuffer = RectF()
    private val shadowRectBuffer = RectF()

    private val colorRecess = ContextCompat.getColor(context, R.color.board_recess)
    private val colorRecessStroke = ContextCompat.getColor(context, R.color.stroke_subtle)
    private val colorTileDefault = ContextCompat.getColor(context, R.color.tile_surface)
    private val colorTileDefaultStroke = ContextCompat.getColor(context, R.color.tile_border)
    private val colorTileCorrect = ContextCompat.getColor(context, R.color.tile_correct_surface)
    private val colorTileCorrectStroke = ContextCompat.getColor(context, R.color.tile_correct_border)
    private val colorTileText = ContextCompat.getColor(context, R.color.tile_text)
    private val colorCorrectDot = ContextCompat.getColor(context, R.color.tile_correct_dot)

    fun setOnTileInteractionListener(interactionListener: OnTileInteractionListener) {
        this.listener = interactionListener
    }

    fun setInteractive(enabled: Boolean) {
        isInteractive = enabled
    }

    fun setHapticsEnabled(enabled: Boolean) {
        hapticsEnabled = enabled
    }

    fun updateBoard(newSize: Int, newBoard: List<Int>, animateDiff: Boolean) {
        val sizeChanged = gridSize != newSize
        val oldBoard = board
        gridSize = newSize
        board = newBoard

        if (sizeChanged || !animateDiff || oldBoard.size != newBoard.size) {
            animator?.cancel()
            animatingTiles = emptySet()
            animationProgress = 1f
            previousPositions = emptyMap()
            listener?.onAnimationStateChanged(false)
            invalidate()
            return
        }

        val oldCoords = HashMap<Int, Pair<Int, Int>>(oldBoard.size)
        for (i in oldBoard.indices) {
            val value = oldBoard[i]
            if (value != 0) {
                oldCoords[value] = Pair(i / newSize, i % newSize)
            }
        }

        val moved = HashSet<Int>()
        for (i in newBoard.indices) {
            val value = newBoard[i]
            if (value != 0) {
                val newCoord = Pair(i / newSize, i % newSize)
                if (oldCoords[value] != newCoord) {
                    moved.add(value)
                }
            }
        }

        if (moved.isEmpty()) {
            invalidate()
            return
        }

        previousPositions = oldCoords
        animatingTiles = moved
        startSlideAnimation()
    }

    private fun startSlideAnimation() {
        animator?.cancel()
        animationProgress = 0f
        listener?.onAnimationStateChanged(true)

        val valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            duration = 150L
            interpolator = DecelerateInterpolator(1.6f)
            addUpdateListener { anim ->
                animationProgress = anim.animatedValue as Float
                invalidate()
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    animatingTiles = emptySet()
                    animationProgress = 1f
                    previousPositions = emptyMap()
                    listener?.onAnimationStateChanged(false)
                    invalidate()
                }

                override fun onAnimationCancel(animation: Animator) {
                    animatingTiles = emptySet()
                    animationProgress = 1f
                    previousPositions = emptyMap()
                    listener?.onAnimationStateChanged(false)
                }
            })
        }
        animator = valueAnimator
        valueAnimator.start()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val availWidth = MeasureSpec.getSize(widthMeasureSpec)
        val availHeight = MeasureSpec.getSize(heightMeasureSpec)
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)

        val size = when {
            widthMode == MeasureSpec.UNSPECIFIED && heightMode == MeasureSpec.UNSPECIFIED -> 600
            widthMode == MeasureSpec.UNSPECIFIED -> availHeight
            heightMode == MeasureSpec.UNSPECIFIED -> availWidth
            else -> min(availWidth, availHeight)
        }
        setMeasuredDimension(size, size)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (board.size != gridSize * gridSize) return

        val boardPadding = width * 0.03f
        val innerSize = width - boardPadding * 2f
        val cellSize = innerSize / gridSize
        val cellGap = cellSize * if (gridSize == 5) 0.065f else 0.075f
        val cornerRadius = cellSize * 0.16f
        val strokeWidth = (resources.displayMetrics.density * 1.25f).coerceAtLeast(1.5f)

        recessPaint.color = colorRecess
        recessStrokePaint.color = colorRecessStroke
        recessStrokePaint.strokeWidth = strokeWidth

        // 1. Draw recessed grid slots
        for (index in 0 until gridSize * gridSize) {
            val row = index / gridSize
            val col = index % gridSize
            val left = boardPadding + col * cellSize + cellGap
            val top = boardPadding + row * cellSize + cellGap
            val right = boardPadding + (col + 1) * cellSize - cellGap
            val bottom = boardPadding + (row + 1) * cellSize - cellGap
            rectBuffer.set(left, top, right, bottom)
            canvas.drawRoundRect(rectBuffer, cornerRadius, cornerRadius, recessPaint)
            canvas.drawRoundRect(rectBuffer, cornerRadius, cornerRadius, recessStrokePaint)
        }

        // 2. Draw tiles
        val textSizeScale = when (gridSize) {
            3 -> 0.40f
            4 -> 0.36f
            else -> 0.32f
        }
        textPaint.textSize = cellSize * textSizeScale
        textPaint.color = colorTileText
        tileStrokePaint.strokeWidth = strokeWidth
        correctDotPaint.color = colorCorrectDot

        val fontMetrics = textPaint.fontMetrics
        val textBaselineOffset = -(fontMetrics.ascent + fontMetrics.descent) / 2f
        val shadowOffset = resources.displayMetrics.density * 2.2f

        for (index in board.indices) {
            val value = board[index]
            if (value == 0) continue

            val targetRow = index / gridSize
            val targetCol = index % gridSize

            val drawRow: Float
            val drawCol: Float
            if (animatingTiles.contains(value) && animationProgress < 1f) {
                val prev = previousPositions[value]
                if (prev != null) {
                    drawRow = prev.first + (targetRow - prev.first) * animationProgress
                    drawCol = prev.second + (targetCol - prev.second) * animationProgress
                } else {
                    drawRow = targetRow.toFloat()
                    drawCol = targetCol.toFloat()
                }
            } else {
                drawRow = targetRow.toFloat()
                drawCol = targetCol.toFloat()
            }

            val left = boardPadding + drawCol * cellSize + cellGap
            val top = boardPadding + drawRow * cellSize + cellGap
            val right = left + cellSize - cellGap * 2f
            val bottom = top + cellSize - cellGap * 2f

            shadowRectBuffer.set(left, top + shadowOffset, right, bottom + shadowOffset)
            canvas.drawRoundRect(shadowRectBuffer, cornerRadius, cornerRadius, shadowPaint)

            val isCorrect = index == value - 1
            tilePaint.color = if (isCorrect) colorTileCorrect else colorTileDefault
            tileStrokePaint.color = if (isCorrect) colorTileCorrectStroke else colorTileDefaultStroke

            rectBuffer.set(left, top, right, bottom)
            canvas.drawRoundRect(rectBuffer, cornerRadius, cornerRadius, tilePaint)
            canvas.drawRoundRect(rectBuffer, cornerRadius, cornerRadius, tileStrokePaint)

            val centerX = rectBuffer.centerX()
            val centerY = rectBuffer.centerY()
            canvas.drawText(value.toString(), centerX, centerY + textBaselineOffset, textPaint)

            if (isCorrect) {
                val dotRadius = (cellSize * 0.045f).coerceAtLeast(3f)
                val dotInset = cellSize * 0.14f
                canvas.drawCircle(
                    right - dotInset,
                    top + dotInset,
                    dotRadius,
                    correctDotPaint
                )
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (!isInteractive || animatingTiles.isNotEmpty()) {
            return false
        }

        if (event.action == MotionEvent.ACTION_DOWN) {
            return true
        }

        if (event.action == MotionEvent.ACTION_UP) {
            val boardPadding = width * 0.03f
            val innerSize = width - boardPadding * 2f
            if (innerSize <= 0f) return false

            val col = ((event.x - boardPadding) / (innerSize / gridSize)).toInt()
            val row = ((event.y - boardPadding) / (innerSize / gridSize)).toInt()

            if (row in 0 until gridSize && col in 0 until gridSize) {
                val tappedIndex = row * gridSize + col
                if (tappedIndex in board.indices && board[tappedIndex] != 0) {
                    if (hapticsEnabled) {
                        performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                    }
                    performClick()
                    listener?.onTileTapped(tappedIndex)
                    return true
                }
            }
        }
        return super.onTouchEvent(event)
    }

    override fun performClick(): Boolean {
        super.performClick()
        return true
    }

    override fun onDetachedFromWindow() {
        animator?.cancel()
        super.onDetachedFromWindow()
    }
}
