package com.anto.colorpalette.ui.utils

import android.graphics.Color

val HUE_COLORS = intArrayOf(
    Color.RED,
    Color.YELLOW,
    Color.GREEN,
    Color.CYAN,
    Color.BLUE,
    Color.MAGENTA,
    Color.RED
)

val SATURATION_COLORS = intArrayOf(
    Color.WHITE,
    setAlpha(Color.WHITE, 0)
)

fun setAlpha(argb: Int, alpha: Int) =
    Color.argb(alpha, Color.red(argb), Color.green(argb), Color.blue(argb))
