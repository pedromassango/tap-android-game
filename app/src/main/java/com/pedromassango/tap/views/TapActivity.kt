package com.pedromassango.tap.views

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.TextView

import com.pedromassango.tap.R
import com.pedromassango.tap.Utils.DificultType
import com.pedromassango.tap.model.TapButton
import com.pedromassango.tap.model.TreeButtons
import com.pedromassango.tap.storage.Database
import kotlinx.android.synthetic.main.activity_tap.*;

import java.util.Date

class TapActivity : AppCompatActivity(), View.OnClickListener {

    //the game timer
    internal var countDownTimer: CountDownTimer? = null //.start();

    private var isInFullScreen: Boolean = false
    //Data
    private var correctButton: TapButton? = null

    private var time: Long = 0
    private var currentScore: Int = 0
    private var currentPlayed: Int = 0
    private var current_doneSuccessful: Int = 0
    private var current_doneUnSuccessful: Int = 0

    // Database
    private var database: Database? = null

    private val mHideHandler = Handler()
    private val mHidePart2Runnable = Runnable {
        // Delayed removal of status and navigation bar

        // Note that some of these constants are new as of API 16 (Jelly Bean)
        // and API 19 (KitKat). It is safe to use them, as they are inlined
        // at compile-time and do nothing on earlier devices.
        root!!.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
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

        btn_1.setOnClickListener(this)
        btn_2.setOnClickListener(this)
        btn_3.setOnClickListener(this)

        btn_start!!.setOnClickListener {
            startGame(time)
            btn_start!!.visibility = View.GONE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.fullScreen()
        setContentView(R.layout.activity_tap)
        this.initializeViews()

        database = Database(this)
        loadPreferences(database)
    }

    private fun loadPreferences(database: Database?) {

        time = database!!.currentGameDificult.toLong()
        val currentLevel = Database.getDificultString(this, time.toInt())
        tv_level!!.text = currentLevel
        tv_points!!.text = 0.toString()

        val d = Date(time)
        tv_time!!.text = d.seconds.toString()
    }

    override fun onResume() {
        super.onResume()
        this.fullScreen()
    }

    override fun onClick(v: View) {
        val buttonId = v.id
        if (buttonId != correctButton!!.id) {
            currentScore -= SUB_VALUE
            database!!.setTotalFails(1)
            current_doneUnSuccessful--
            current_doneUnSuccessful = if (current_doneUnSuccessful > 0) current_doneUnSuccessful else 0
            currentScore = if (currentScore > 0) currentScore else 0
            database!!.subScore(-1)
        } else {
            currentScore += SUM_VALUE
            database!!.totalAcerts = 1
            current_doneSuccessful++
            database!!.sumScore(1)
        }

        tv_points!!.text = Database.getValue(currentScore)
        currentPlayed++
        randomNewValues()
    }

    private fun startGame(mTime: Long) {

        if (btn_start!!.visibility != View.VISIBLE) {
            btn_start!!.visibility = View.VISIBLE
            return
        }

        fullScreen()

        hideShowViews(View.VISIBLE)

        randomNewValues()

        startTimer(mTime)
    }

    private fun randomNewValues() {

        val b1Id = btn_1!!.id
        val b2Id = btn_2!!.id
        val b3Id = btn_3!!.id

        val treeButtons = TreeButtons()
        treeButtons.add(b1Id)
        treeButtons.add(b2Id)
        treeButtons.add(b3Id)

        btn_1!!.setBackgroundColor(treeButtons.getColor(b1Id))
        btn_2!!.setBackgroundColor(treeButtons.getColor(b2Id))
        btn_3!!.setBackgroundColor(treeButtons.getColor(b3Id))

        correctButton = treeButtons.randomButton
        val falseButton = treeButtons.getDiferentOf(correctButton!!.id)

        tv_button_color!!.text = correctButton!!.name
        tv_button_color!!.setTextColor(falseButton.color)
    }

    fun startTimer(mTime: Long) {

        countDownTimer = object : CountDownTimer(mTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val d = Date(millisUntilFinished)

                tv_time!!.text = d.seconds.toString()
            }

            override fun onFinish() {
                val resetValue = 0.toString()
                tv_time!!.text = resetValue
                tv_points!!.text = resetValue

                // Hide views on main layout
                hideShowViews(View.GONE)

                if (mTime == DificultType.EASY_TIME.toLong() && currentScore >= DificultType.EASY_SCORE) { // Dificult EASY Passed

                    database!!.currentGameDificult = DificultType.MEDIUM_TIME
                    database!!.currentGameScore = DificultType.MEDIUM_SCORE
                    database!!.setCanPlayLevel(DificultType.MEDIUM)
                    showDialogGameSuccess(false)
                    return
                } else if (mTime == DificultType.MEDIUM_TIME.toLong() && currentScore >= DificultType.MEDIUM_SCORE) { // Dificult MEDIUM Passed

                    database!!.currentGameDificult = DificultType.HARD_TIME
                    database!!.currentGameScore = DificultType.HARD_SCORE
                    database!!.setCanPlayLevel(DificultType.HARD)
                    showDialogGameSuccess(false)
                    return
                } else if (mTime == DificultType.HARD_TIME.toLong() && currentScore >= DificultType.HARD_SCORE) { // Dificult HARD Passed

                    database!!.currentGameDificult = DificultType.HARD_TIME
                    database!!.currentGameScore = DificultType.HARD_SCORE
                    database!!.setCanPlayLevel(DificultType.ALL)
                    showDialogGameSuccess(true)
                    return
                }

                // Here the user do not successfull finish the current level
                showDialogGameFailed()
            }
        }

        countDownTimer!!.start()
    }

    private fun hideShowViews(visibility: Int) {

        root!!.visibility = visibility
    }

    private fun startNextLevel() {

        time = 0
        currentScore = 0
        currentPlayed = 0
        current_doneSuccessful = 0
        current_doneUnSuccessful = 0
        this.loadPreferences(database)
        this.startGame(time)
    }

    fun showDialogGameFailed() {

        val gameOverView = LayoutInflater.from(this).inflate(R.layout.dialog_game_finished, null, false)
        val tvScore: TextView
        val tvDones: TextView
        val tvMaxScoreToNextLevel: TextView
        val tvFails: TextView
        val tvAcerts: TextView
        tvScore = gameOverView.findViewById(R.id.tv_points) as TextView
        tvDones = gameOverView.findViewById(R.id.tv_done) as TextView
        tvFails = gameOverView.findViewById(R.id.tv_fails) as TextView
        tvAcerts = gameOverView.findViewById(R.id.tv_acerts) as TextView
        tvMaxScoreToNextLevel = gameOverView.findViewById(R.id.tv_max_score) as TextView

        tvScore.text = Database.getValue(currentScore)
        tvDones.text = currentPlayed.toString()
        tvFails.text = current_doneUnSuccessful.toString()
        tvAcerts.text = current_doneSuccessful.toString()
        tvMaxScoreToNextLevel.text = Database.getValue(database!!.currentGameScore)

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
                .setView(gameOverView)
                .setNegativeButton(R.string.quit) { dialog, which -> startMainActivity() }
                .setPositiveButton(R.string.play) { dialog, which -> startGame(time) }

        val dialog = builder.create()
        dialog.show()
    }

    fun showDialogGameSuccess(lastLevel: Boolean) {

        val gameSuccessView = LayoutInflater.from(this).inflate(R.layout.dialog_game_success, null, false)
        val tvScore: TextView
        val tvDones: TextView
        val tvFails: TextView
        val tvAcerts: TextView
        val tvSuccessInfo: TextView
        tvScore = gameSuccessView.findViewById(R.id.tv_points) as TextView
        tvDones = gameSuccessView.findViewById(R.id.tv_done) as TextView
        tvFails = gameSuccessView.findViewById(R.id.tv_fails) as TextView
        tvAcerts = gameSuccessView.findViewById(R.id.tv_acerts) as TextView
        tvSuccessInfo = gameSuccessView.findViewById(R.id.tv_success_info) as TextView

        tvScore.text = Database.getValue(currentScore)
        tvDones.text = currentPlayed.toString()
        tvFails.text = current_doneUnSuccessful.toString()
        tvAcerts.text = current_doneSuccessful.toString()

        if (lastLevel) {
            tvSuccessInfo.text = getString(R.string.last_level_info)
        }

        val builder = AlertDialog.Builder(this)
        builder.setCancelable(false)
                .setView(gameSuccessView)
                .setNegativeButton(R.string.quit) { dialog, which -> startMainActivity() }
        if (!lastLevel) {
            builder.setPositiveButton(R.string.next_level) { dialog, which -> startNextLevel() }
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun startMainActivity() {

        this@TapActivity.finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (countDownTimer != null)
            countDownTimer!!.cancel()
    }

    companion object {

        /**
         * Some older devices needs a small delay between UI widget updates
         * and a change of the status and navigation bar.
         */
        private val SUB_VALUE = 120
        private val SUM_VALUE = 150

        fun log(log: String) {
            Log.i("output", log)
        }

        fun log(info: String, args: String) {
            Log.i("output", info + " : " + args)
        }
    }
}
