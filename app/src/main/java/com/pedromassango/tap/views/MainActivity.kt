package com.pedromassango.tap.views

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView

import com.pedromassango.tap.R
import kotlinx.android.synthetic.main.activity_main.*;
import com.pedromassango.tap.Utils.DificultType
import com.pedromassango.tap.storage.Database

class MainActivity : AppCompatActivity() {

    private var isInFullScreen: Boolean = false

    // Database
    private var database: Database? = null

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        findViewById(R.id.app_logo).systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    private fun fullScreen() {

        if (!isInFullScreen) {
            mHideHandler.post(mHidePart2Runnable)
            isInFullScreen = true
        } else {
            isInFullScreen = false
        }
    }

    private fun initializeViews() {

        findViewById(R.id.about)
                .setOnClickListener { showAppDialog() }
        findViewById(R.id.btn_start)
                .setOnClickListener { startPressed() }
        findViewById(R.id.btn_instrucoes)
                .setOnClickListener { showInstrucoes() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.fullScreen()
        setContentView(R.layout.activity_main)
        this.initializeViews()
        this.bindData(true)
        this.showAppDialog()
    }

    override fun onResume() {
        super.onResume()
        this.fullScreen()
        this.bindData(true)
    }

    private fun bindData(showCurrentDificult: Boolean) {
        database = Database(this)
        val acerts = Database.getValue(database!!.totalAcerts)
        val fails = Database.getValue(database!!.totalFais)
        val score = Database.getValue(database!!.score)

        if (showCurrentDificult) {
            // set TextView dificult text AND color
            Database.getDificultLevelInfo(this, database!!.currentGameDificult, tv_current_dificult)
        }

        tv_acerts.text = acerts
        tv_fails.text = fails
        tv_max_score.text = score
    }

    internal fun startPressed() {
        val currentDificult = database!!.currentGameDificult
        val currentScore = database!!.currentGameDificult

        val gameOverView = LayoutInflater.from(this).inflate(R.layout.dialog_level, null, false)
        val rbEsay: RadioButton
        val rbMedium: RadioButton
        val rbHard: RadioButton
        val rg = gameOverView.findViewById(R.id.rg_dificult) as RadioGroup
        val tvInfo: TextView
        rbEsay = gameOverView.findViewById(R.id.rb_easy) as RadioButton
        rbMedium = gameOverView.findViewById(R.id.rb_medium) as RadioButton
        rbHard = gameOverView.findViewById(R.id.rb_hard) as RadioButton
        tvInfo = gameOverView.findViewById(R.id.tv_info) as TextView

        when (currentDificult) {
            DificultType.EASY_TIME -> {
                rbEsay.isEnabled = true
                rbEsay.isChecked = true
                rbMedium.isEnabled = database!!.canPlayLevel(DificultType.MEDIUM)
                rbHard.isEnabled = database!!.canPlayLevel(DificultType.HARD)
                tvInfo.text = getString(R.string.level_easy_only)
            }
            DificultType.MEDIUM_TIME -> {
                rbEsay.isEnabled = true
                rbMedium.isEnabled = true
                rbMedium.isChecked = true
                rbHard.isEnabled = database!!.canPlayLevel(DificultType.HARD)
                tvInfo.text = getString(R.string.level_medium_only)
            }
            DificultType.HARD_TIME -> {
                rbEsay.isEnabled = database!!.canPlayLevel(DificultType.ALL)
                rbMedium.isEnabled = database!!.canPlayLevel(DificultType.ALL)
                rbHard.isEnabled = true
                rbHard.isChecked = true
                tvInfo.visibility = View.GONE
            }
        }

        rg.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rb_easy -> {
                    database!!.currentGameDificult = DificultType.EASY_TIME
                    database!!.currentGameScore = DificultType.EASY_SCORE
                }
                R.id.rb_medium -> {
                    database!!.currentGameDificult = DificultType.MEDIUM_TIME
                    database!!.currentGameScore = DificultType.MEDIUM_SCORE
                }
                R.id.rb_hard -> {
                    database!!.currentGameDificult = DificultType.HARD_TIME
                    database!!.currentGameScore = DificultType.HARD_SCORE
                }
            }

            bindData(false)
        }


        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
                .setView(gameOverView)
                .setPositiveButton(R.string.start) { dialog, which -> startGame() }
                .setOnCancelListener {
                    database!!.currentGameDificult = currentDificult
                    database!!.currentGameScore = currentScore
                }

        val dialog = builder.create()
        dialog.show()
    }

    private fun showAppDialog() {
        if (!database!!.canShowAppDialog()) {
            return
        }

        val gameOverView = LayoutInflater.from(this).inflate(R.layout.dialog_about_app, null, false)
        val cb = gameOverView.findViewById(R.id.show_on_start) as CheckBox

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
                .setView(gameOverView)
                .setPositiveButton(R.string.ok) { dialog, which ->
                    database!!.canShowAppDialog(cb.isChecked)
                    fullScreen()
                }
                .setNeutralButton(R.string.on_facebook) { dialog, which -> gotoFacebook() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun gotoFacebook() {

        var iFacebook: Intent
        try {
            packageManager.getPackageInfo("com.facebook.katana", 0)
            iFacebook = Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/Pedro-Massango-1042994049086570"))
        } catch (e: Exception) {
            iFacebook = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/Pedro-Massango-1042994049086570/"))
        }

        startActivity(iFacebook)
    }

    private fun showInstrucoes() {
        val gameOverView = LayoutInflater.from(this).inflate(R.layout.dialog_instrucoes, null, false)
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
                .setView(gameOverView)
                .setPositiveButton(R.string.ok) { dialog, which -> fullScreen() }
        val dialog = builder.create()
        dialog.show()
    }

    private fun startGame() {

        val i = Intent(this, TapActivity::class.java)
        startActivity(i)
    }
}
