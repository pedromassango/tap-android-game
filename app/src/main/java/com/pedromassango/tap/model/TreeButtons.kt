package com.pedromassango.tap.model

import android.util.Log
import android.util.SparseArray

import com.pedromassango.tap.storage.Database

import java.util.ArrayList
import java.util.Random


/**
 * Created by Pedro Massango on 07/06/2017 at 20:11.
 */

class TreeButtons {

    private val buttons: MutableList<TapButton>
    private val currentUsedColors: MutableList<String>

    val randomButton: TapButton
        get() {
            val r = Random()
            return buttons[r.nextInt(buttons.size)]
        }

    init {
        this.buttons = ArrayList()
        this.currentUsedColors = ArrayList()
    }

    fun add(buttonId: Int) {
        var colorName: String?
        var color: Int
        do {
            val colors = Database.getColors()
            val r = Random()
            val rId = r.nextInt(colors.size())
            Log.v("output","RANDOM COLOR ID: " + rId)
            val newColor = colors.get(rId)
            color = newColor.color
            colorName = newColor.name

        } while (currentUsedColors.contains(colorName!!))

        val tapButton = TapButton()
        tapButton.id = buttonId
        tapButton.color = color
        tapButton.name = colorName
        currentUsedColors.add(colorName)
        this.buttons.add(tapButton)
    }

    fun getColor(buttonId: Int): Int {
        var tap: TapButton
        do {
            tap = randomButton
        } while (tap.id != buttonId)
        return tap.color
    }

    fun getDiferentOf(buttonId: Int): TapButton {
        var tap: TapButton
        do {
            tap = randomButton
        } while (tap.id == buttonId)
        return tap
    }


}
