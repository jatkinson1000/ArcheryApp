package com.example.archeryapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView

import com.example.archeryapp.handicapcalcs.score_for_round
import com.example.archeryapp.handicapcalcs.HcParams
import com.example.archeryapp.archeryutils.Target
import com.example.archeryapp.archeryutils.Round
import com.example.archeryapp.archeryutils.Pass
import com.example.archeryapp.handicapfunctions.handicap_from_score

class MainActivity : AppCompatActivity() {

    private lateinit var etScore: EditText
    private lateinit var spBowstyle: Spinner
    private lateinit var spRoundFamily: Spinner
    private lateinit var spRound: Spinner
    private lateinit var tvHandicapValue: TextView
    private lateinit var tvSkillScoreValue: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etScore = findViewById(R.id.etScore)
        spBowstyle = findViewById(R.id.spBowstyle)
        spRoundFamily = findViewById(R.id.spRoundFamily)
        spRound = findViewById(R.id.spRound)
        tvHandicapValue = findViewById(R.id.tvHandicapValue)
        tvSkillScoreValue = findViewById(R.id.tvSkillScoreValue)

    }

    fun HCButtonClick(view: View?){
//        var newtar = Target(122.0, "10_zone", 70.0, "metre",
//            false)

//        tvHandicapValue.text = String.format("%d", score_for_round(rnd = Round("WA720",
//            listOf(Pass(72, 122.0, "10_zone", 70.0, dist_unit = "m", indoor = false))),
//            h = 45.0, hc_sys = "AGB",
//            hc_params = HcParams("Default"), arw_d_user= -1.0, round_up = true))

        var hc = handicap_from_score(etScore.text.toString().toDouble(),
            rnd = Round("WA720", listOf(Pass(72, 1.22, "10_zone", 70.0, dist_unit = "m", indoor = false))),
            hc_sys = "AGB",
            hc_dat = HcParams("Default"), int_prec = true)

        var ss = handicap_from_score(etScore.text.toString().toDouble(),
            rnd = Round("WA720", listOf(Pass(72, 1.22, "10_zone", 70.0, dist_unit = "m", indoor = false))),
            hc_sys = "AA2",
            hc_dat = HcParams("Default"), int_prec = true)

        tvHandicapValue.text = hc.toString()
        tvSkillScoreValue.text = ss.toString()


    }

}