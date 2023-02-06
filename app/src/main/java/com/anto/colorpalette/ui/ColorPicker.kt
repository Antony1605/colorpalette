package com.anto.colorpalette.ui

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Parcelable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.anto.colorpalette.R
import com.anto.colorpalette.ui.thumb.ThumbDrawable
import com.anto.colorpalette.ui.utils.*
import com.anto.colorpalette.ui.utils.isTap
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

class ColorPicker @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val hueGradient = GradientDrawable().apply {
        gradientType = GradientDrawable.SWEEP_GRADIENT
        shape = GradientDrawable.OVAL
        colors = HUE_COLORS
    }

    private val saturationGradient = GradientDrawable().apply {
        gradientType = GradientDrawable.RADIAL_GRADIENT
        shape = GradientDrawable.OVAL
        colors = SATURATION_COLORS
    }

    private val thumbDrawable = ThumbDrawable()
    private val hsvColor = HsvColor(value = 1f)

    private var centerX = 0
    private var centerY = 0
    private var pickerRadius = 0
    private var downX = 0f
    private var downY = 0f

    var rgb
        get() = hsvColor.rgb
        set(rgb) {
            hsvColor.rgb = rgb
            hsvColor.set(value = 1f)
            fireColorListener()
            invalidate()
        }

    var thumbRadius
        get() = thumbDrawable.radius
        set(value) {
            thumbDrawable.radius = value
            invalidate()
        }

    var thumbColor
        get() = thumbDrawable.thumbColor
        set(value) {
            thumbDrawable.thumbColor = value
            invalidate()
        }

    var thumbStrokeColor
        get() = thumbDrawable.strokeColor
        set(value) {
            thumbDrawable.strokeColor = value
            invalidate()
        }

    var thumbColorCircleScale
        get() = thumbDrawable.colorCircleScale
        set(value) {
            thumbDrawable.colorCircleScale = value
            invalidate()
        }

    var colorChangeListener: ((Int) -> Unit)? = null
    var otherColorChangeListener: ((Int) -> Unit)? = null

    var interceptTouchEvent = true

    init {
        parseAttributes(context, attrs)
    }

    private fun parseAttributes(context: Context, attrs: AttributeSet?) {
        val array = context.obtainStyledAttributes(
            attrs,
            R.styleable.ColorPicker,
            0,
            R.style.ColorPickerDefaultStyle
        )
        readThumbRadius(array)
        readThumbColor(array)
        readStrokeColor(array)
        readColorCircleScale(array)
        array.recycle()
    }

    private fun readThumbRadius(array: TypedArray) {
        thumbRadius = array.getDimensionPixelSize(R.styleable.ColorPicker_tb_thumbRadius, 0)
    }

    private fun readThumbColor(array: TypedArray) {
        thumbColor = array.getColor(R.styleable.ColorPicker_tb_thumbColor, 0)
    }

    private fun readStrokeColor(array: TypedArray) {
        thumbStrokeColor = array.getColor(R.styleable.ColorPicker_tb_thumbStrokeColor, 0)
    }

    private fun readColorCircleScale(array: TypedArray) {
        thumbColorCircleScale = array.getFloat(R.styleable.ColorPicker_tb_thumbColorCircleScale, 0f)
    }

    fun setColor(color: Int) {
        setRgb(Color.red(color), Color.green(color), Color.blue(color))
    }

    private fun setRgb(r: Int, g: Int, b: Int) {
        rgb = Color.rgb(r, g, b)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minDimension = minOf(
            MeasureSpec.getSize(widthMeasureSpec),
            MeasureSpec.getSize(heightMeasureSpec)
        )

        setMeasuredDimension(
            resolveSize(minDimension, widthMeasureSpec),
            resolveSize(minDimension, heightMeasureSpec)
        )
    }

    override fun onDraw(canvas: Canvas) {
        drawColorPicker(canvas)
        drawThumb(canvas)
    }

    private fun drawColorPicker(canvas: Canvas) {
        val hSpace = width - paddingLeft - paddingRight
        val vSpace = height - paddingTop - paddingBottom

        centerX = paddingLeft + hSpace / 2
        centerY = paddingTop + vSpace / 2
        pickerRadius = maxOf(minOf(hSpace, vSpace) / 2, 0)

        val left = centerX - pickerRadius
        val top = centerY - pickerRadius
        val right = centerX + pickerRadius
        val bottom = centerY + pickerRadius

        hueGradient.setBounds(left, top, right, bottom)
        saturationGradient.setBounds(left, top, right, bottom)
        saturationGradient.gradientRadius = pickerRadius.toFloat()

        hueGradient.draw(canvas)
        saturationGradient.draw(canvas)
    }

    private fun drawThumb(canvas: Canvas) {
        val r = hsvColor.saturation * pickerRadius
        val hueRadians = toRadians(hsvColor.hue)
        val x = cos(hueRadians) * r + centerX
        val y = sin(hueRadians) * r + centerY

        thumbDrawable.indicatorColor = hsvColor.rgb
        thumbDrawable.setCoordinates(x, y)
        thumbDrawable.draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> onActionDown(event)
            MotionEvent.ACTION_MOVE -> updateColorOnMotionEvent(event)
            MotionEvent.ACTION_UP -> {
                updateColorOnMotionEvent(event)
                if (isTap(event, downX, downY)) performClick()
            }
        }

        return true
    }

    private fun onActionDown(event: MotionEvent) {
        parent.requestDisallowInterceptTouchEvent(interceptTouchEvent)
        updateColorOnMotionEvent(event)
        downX = event.x
        downY = event.y
    }

    private fun updateColorOnMotionEvent(event: MotionEvent) {
        calculateColor(event)
        fireColorListener()
        invalidate()
    }

    private fun calculateColor(event: MotionEvent) {
        val legX = event.x - centerX
        val legY = event.y - centerY
        val hypot = minOf(hypot(legX, legY), pickerRadius.toFloat())
        val hue = (toDegrees(atan2(legY, legX)) + 360) % 360
        val saturation = hypot / pickerRadius
        hsvColor.set(hue, saturation, 1f)
    }

    private fun fireColorListener() {
        colorChangeListener?.invoke(hsvColor.rgb)
        otherColorChangeListener?.invoke(hsvColor.rgb)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val thumbState = thumbDrawable.saveState()
        return ColorPickerState(superState, this, thumbState)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        if (state is ColorPickerState) {
            super.onRestoreInstanceState(state.superState)
            readColorPickerState(state)
        } else {
            super.onRestoreInstanceState(state)
        }
    }

    private fun readColorPickerState(state: ColorPickerState) {
        thumbDrawable.restoreState(state.thumbState)
        interceptTouchEvent = state.interceptTouchEvent
        rgb = state.rgb
    }
}
