package com.anto.colorpalette

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.anto.colorpalette.databinding.ActivityMainBinding
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var selectedView: MaterialButton? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    private fun initViews() {
        setInitialColors()
        binding.segmentedContainer.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                onColorStateSelected(checkedId)
            }
        }

        // Check the default one after adding the listener so that color state will be in sync for the first time
        binding.optionOne.isChecked = true

        binding.colorPicker.colorChangeListener = { color ->
            selectedView?.setIndicatorColor(color)
        }
    }

    private fun onColorStateSelected(id: Int) {
        val checkedView = getCheckedView(id)
        val color = checkedView?.getIndicatorColor()
        selectedView = checkedView

        if (color == null) {
            Log.w(TAG, "Unable to parse color from the view $id -> $checkedView ")
            return
        }

        binding.colorPicker.setColor(color)
    }

    private fun setInitialColors() {
        binding.optionOne.setIndicatorColor(Color.parseColor("#00c2a3"))
        binding.optionTwo.setIndicatorColor(Color.parseColor("#4ba54f"))
        binding.optionThree.setIndicatorColor(Color.parseColor("#ff6100"))
    }

    private fun getCheckedView(id: Int): MaterialButton? = when (id) {
        R.id.option_one -> binding.optionOne
        R.id.option_two -> binding.optionTwo
        R.id.option_three -> binding.optionThree
        else -> null
    }

    private fun MaterialButton.setIndicatorColor(color: Int) {
        tag = color
        iconTint = ColorStateList.valueOf(color)
    }

    private fun MaterialButton.getIndicatorColor() = tag as? Int

    companion object {
        const val TAG = "MainActivity"
    }
}