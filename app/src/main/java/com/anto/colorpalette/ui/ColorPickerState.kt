package com.anto.colorpalette.ui

import android.os.Parcel
import android.os.Parcelable
import android.view.View
import com.anto.colorpalette.ui.thumb.ThumbDrawableState
import com.anto.colorpalette.ui.thumb.readThumbState
import com.anto.colorpalette.ui.thumb.writeThumbState
import com.anto.colorpalette.ui.utils.readBooleanCompat
import com.anto.colorpalette.ui.utils.writeBooleanCompat

internal class ColorPickerState : View.BaseSavedState {

    val thumbState: ThumbDrawableState
    val interceptTouchEvent: Boolean
    val rgb: Int

    constructor(
        superState: Parcelable?,
        view: ColorPicker,
        thumbState: ThumbDrawableState
    ) : super(superState) {
        this.thumbState = thumbState
        interceptTouchEvent = view.interceptTouchEvent
        rgb = view.rgb
    }

    constructor(source: Parcel) : super(source) {
        thumbState = source.readThumbState()
        interceptTouchEvent = source.readBooleanCompat()
        rgb = source.readInt()
    }

    override fun writeToParcel(out: Parcel, flags: Int) {
        super.writeToParcel(out, flags)
        out.writeThumbState(thumbState, flags)
        out.writeBooleanCompat(interceptTouchEvent)
        out.writeInt(rgb)
    }

    companion object CREATOR : Parcelable.Creator<ColorPickerState> {

        override fun createFromParcel(source: Parcel) = ColorPickerState(source)

        override fun newArray(size: Int) = arrayOfNulls<ColorPickerState>(size)
    }
}