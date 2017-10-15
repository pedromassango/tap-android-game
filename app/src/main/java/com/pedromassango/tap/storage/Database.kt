package com.pedromassango.tap.storage

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.preference.PreferenceManager
import android.util.SparseArray
import android.widget.TextView

import com.pedromassango.tap.R
import com.pedromassango.tap.Utils.DificultType
import com.pedromassango.tap.model.TapColor
import com.pedromassango.tap.views.TapActivity.Companion.log

import java.text.DecimalFormat

import java.lang.Double

/**
 * Created by Pedro Massango on 07/06/2017 at 18:45.
 */

class Database(context: Context) {

    private val preferences: SharedPreferences

    var currentGameDificult: Int
        get() {
            log("GET CURRENT DIFICULT: " + preferences.getInt(CURRENT_GAME_DIFICULT, DificultType.EASY_TIME))
            return preferences.getInt(CURRENT_GAME_DIFICULT, DificultType.EASY_TIME)
        }
        set(dificult) {

            log("SAVE CURRENT DIFICULT: " + dificult)
            preferences.edit().putInt(CURRENT_GAME_DIFICULT, dificult).apply()
        }

    var currentGameScore: Int
        get() = preferences.getInt(CURRENT_GAME_SCORE, DificultType.EASY_SCORE)
        set(score) = preferences.edit().putInt(CURRENT_GAME_SCORE, score).apply()

    val score: Int
        get() = preferences.getInt("score", 0)

    val totalFais: Int
        get() = preferences.getInt(TOTAL_FAILS, 0)

    var totalAcerts: Int
        get() = preferences.getInt(TOTAL_ACERTS, 0)
        set(acert) {
            var acert = acert
            acert += totalAcerts
            preferences.edit().putInt(TOTAL_ACERTS, acert).apply()
        }

    init {
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun canPlayLevel(level: String): Boolean {

        return preferences.getBoolean(level, false)
    }

    fun setCanPlayLevel(level: String) {

        preferences.edit().putBoolean(level, true).apply()
    }

    fun sumScore(score: Int) {
        val cScore = score + score
        preferences.edit().putInt("score", cScore).apply()
    }

    fun subScore(score: Int) {
        var cScore = score - score
        cScore = if (cScore != 0) cScore else 0
        preferences.edit().putInt("score", cScore).apply()
    }

    fun setTotalFails(fail: Int) {
        var fail = fail
        fail += totalFais
        preferences.edit().putInt(TOTAL_FAILS, fail).apply()
    }

    fun canShowAppDialog(): Boolean {
        return preferences.getBoolean("show_about", true)
    }

    fun canShowAppDialog(state: Boolean) {

        preferences.edit().putBoolean("show_about", state).apply()
    }

    companion object {

        private val CURRENT_GAME_DIFICULT = "CURRENT_GAME_DIFICULT"
        private val TOTAL_FAILS = "TOTAL_FAILS"
        private val TOTAL_ACERTS = "TOTAL_ACERTS"
        private val CURRENT_GAME_SCORE = "CURRENT_GAME_SCORE"

        fun getColors(): SparseArray<TapColor> {
            val colors: SparseArray<TapColor> = SparseArray()
            colors.put(0, TapColor("PRETO", -0x1000000))
            colors.put(1, TapColor("CINZA ESCURO", -0xbbbbbc))
            colors.put(2, TapColor("VERMELHO", -0x10000))
            colors.put(3, TapColor("CINZA", -0x777778))
            colors.put(4, TapColor("CINZA CLARO", -0x333334))
            colors.put(5, TapColor("VERDE", -0xff0100))
            colors.put(6, TapColor("AZUL", -0xffff01))
            colors.put(7, TapColor("AMARELO", -0x100))
            colors.put(8, TapColor("AZUL CLÁRO", -0xff0001))
            colors.put(9, TapColor("ROSA", -0xff01))
            return colors
        }

        fun getValue(value: Int): String {
            val number = value.toString()
            var amount = Double.parseDouble(number)
            val formatter = if (amount <= 1000)
                DecimalFormat("#,###")
            else
                DecimalFormat("##,###")

            amount = if (amount >= 0) amount else 0.0
            //return formatter.format(amount).replace(" ", ".")
            return formatter.format(amount)
        }

        fun getDificultLevelInfo(context: Context, currentGameDificult: Int, textView: TextView) {

            var dificult: String? = null
            var textColor = 0
            when (currentGameDificult) {
                DificultType.EASY_TIME -> {
                    dificult = context.getString(R.string.dificult_facil)
                    textColor = Color.RED
                }
                DificultType.MEDIUM_TIME -> {
                    dificult = context.getString(R.string.dificult_intermedio)
                    textColor = Color.BLUE
                }
                DificultType.HARD_TIME -> {
                    dificult = context.getString(R.string.dificult_dificil)
                    textColor = Color.GREEN
                }
            }
            textView.text = dificult
            textView.setTextColor(textColor)
        }

        fun getDificultString(context: Context, currentGameDificult: Int): String? {
            when (currentGameDificult) {
                DificultType.EASY_TIME -> return context.getString(R.string.dificult_facil)
                DificultType.MEDIUM_TIME -> return context.getString(R.string.dificult_intermedio)
                DificultType.HARD_TIME -> return context.getString(R.string.dificult_dificil)
            }
            return null
        }
    }
}
