package com.example.archeryapp.handicapfunctions
import com.example.archeryapp.handicapcalcs.score_for_round
import com.example.archeryapp.handicapcalcs.HcParams

import com.example.archeryapp.archeryutils.Round
import kotlin.math.*


fun handicap_from_score(score: Double,
rnd: Round,
hc_sys: String,
hc_dat: HcParams,
arw_d: Double = -1.0,
int_prec: Boolean = false,
): Double{
    var maxScore = rnd.maxScore
//    if score > max_score:
//    raise ValueError(
//    f"The score of {score} provided is greater that the maximum of {max_score} "
//    f"for a {rnd.name}."
//    )
//    elif score <= 0.0:
//    raise ValueError(
//    f"The score of {score} provided is less than or equal to zero so cannot "
//    "have a handicap."
//    )

    var hc: Double = -75.0
    var dhc: Double = 0.01

    if (score == maxScore.toDouble()){
        // Deal with max score before root finding
        // start high and drop down until no longer ceiling to max score
        // (i.e. >= max_score - 1.0)
        if (listOf("AA", "AA2").contains(hc_sys)){
                hc = 175.0
                dhc = -0.01
            }

        var scoreTop: Double = score_for_round(rnd, hc, hc_sys, hc_dat, arw_d, false)

        // Work down to where we would round up (ceil) to max score - ceiling approach
        while (scoreTop > maxScore - 1.0) {
            hc += dhc
            scoreTop = score_for_round(rnd, hc, hc_sys, hc_dat, arw_d, false)
            println(hc)
            println(scoreTop)
//            println("here")
        }
//        println("here")
        hc -= dhc  // Undo final iteration that overshoots
    }
    else{
        // ROOT FINDING
        fun f_root(h: Double, scr: Double, rd: Round,
                   sys: String, hc_data: HcParams, arw_dia: Double): Double {
            val sc: Double = score_for_round(rd, h, sys, hc_data, arw_dia, false)
            return sc - scr
        }

        var x = listOf(-75.0, 300.0)
        if (listOf("AA", "AA2").contains(hc_sys)) {
            x = listOf(-250.0, 175.0)
        }

        var f =listOf(f_root(x[0], score, rnd, hc_sys, hc_dat, arw_d),
            f_root(x[1], score, rnd, hc_sys, hc_dat, arw_d))

        var xpre: Double = 0.0
        var fpre: Double = 0.0
        var xcur: Double = 0.0
        var fcur: Double = 0.0
        val xtol: Double = 1.0e-16
        val rtol: Double = 0.001
        var xblk: Double = 0.0
        var fblk: Double = 0.0
        var scur: Double = 0.0
        var spre: Double = 0.0
        var delta: Double = 0.0
        var sbis: Double = 0.0
        var dpre: Double = 0.0
        var dblk: Double = 0.0
        var stry: Double = 0.0

        if (abs(f[1])<=f[0]){
            xcur = x[0]
            xpre = x[1]
            fcur = f[0]
            fpre = f[1]
        }
        else{
            xpre = x[1]
            xcur = x[0]
            fpre = f[1]
            fcur = f[0]        }

        for (i in 0..50){
            if ((fpre != 0.0) && (fcur != 0.0) && (sign(fpre) != sign(fcur))){
                xblk = xpre
                fblk = fpre
                spre = xcur - xpre
                scur = xcur - xpre
            }
            if (abs(fblk) < abs(fcur)){
                xpre = xcur
                xcur = xblk
                xblk = xpre

                fpre = fcur
                fcur = fblk
                fblk = fpre
            }
            delta = (xtol + rtol * abs(xcur))/2.0
            sbis = (xblk - xcur) / 2.0
            if ((abs(spre) > delta) && (abs(fcur) < abs(fpre))){
                if (xpre == xblk){
                    stry = -fcur * (xcur - xpre) / (fcur - xpre)
                }
                else{
                    dpre = (fpre - fcur) / (xpre - xcur)
                    dblk = (fblk - fcur) / (xblk - xcur)
                    stry = -fcur * (fblk - fpre) / (fblk * dpre - fpre * dblk)
                }

                if (2*abs(stry) < min(abs(spre), 3*abs(sbis) - delta)) {
                    // accept step
                    spre = scur
                    scur = stry
                }
                else {
                    // bisect
                    spre = sbis
                    scur = sbis
                }
            }
            else {
                // bisect
                spre = sbis
                scur = sbis
            }
            xpre = xcur
            fpre = fcur
            if (abs(scur) > delta) {
                xcur += scur
            }
            else {
                xcur += if(sbis > 0){
                    delta}
                else{
                    -delta}
            }

            fcur = f_root(xcur, score, rnd, hc_sys, hc_dat, arw_d)
            hc = xcur
        }
    }

    // Force integer precision if required. If Aus systems force down, other systems
    // force up.
    // Not trivial as we require asymmetric rounding, hence the if <0 clause
    if (int_prec){
        if (sign(hc) < 0) {
            if (listOf("AA", "AA2").contains(hc_sys)) {
                hc = ceil(hc)
            } else {
                hc = floor(hc)
            }
        }
        else {
            if (listOf("AA", "AA2").contains(hc_sys)) {
                hc = floor(hc)
            }
            else {
                hc = ceil(hc)
            }
        }

        // Check that you can't get the same score from a larger handicap when
        // working in integers
        var min_h_flag: Boolean = false
        var hstep: Double = 1.0
        if (listOf("AA", "AA2").contains(hc_sys)) {
            hstep = -1.0
        }
        while (!min_h_flag){
            hc += hstep
            var sc: Double = score_for_round(rnd, hc, hc_sys, hc_dat, arw_d, true
            )
            if (sc < score) {
                hc -= hstep  // undo the iteration that caused the flag to raise
                min_h_flag = true
            }
        }
    }
    return hc
}
