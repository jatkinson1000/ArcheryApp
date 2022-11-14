package com.example.archeryapp.handicapcalcs

import com.example.archeryapp.archeryutils.Target
import com.example.archeryapp.archeryutils.Round
import kotlin.math.ceil

import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

data class HcParams(val name: String) {
    // KEY PARAMETERS AND CONSTANTS FOR NEW AGB HANDICAP SCHEME
    val AGB_datum: Double = 6.0  // offset required to set handicap 0 at desired score
    val AGB_step: Double = 3.5  // percentage change in group size for each handicap step
    val AGB_ang_0: Double = 5.0e-4  // baseline angle used for group size 0.5 [millirad]
    val AGB_kd: Double = 0.00365  // distance scaling factor [1/metres]

    // KEY PARAMETERS AND CONSTANTS FOR OLD AGB HANDICAP SCHEME
    val AGBo_datum: Double = 12.9  // offset required to set handicap 0 at desired score
    val AGBo_step: Double = 3.5  // percentage change in group size for each handicap step
    val AGBo_ang_0: Double = 5.0e-4  // baseline angle used for group size 0.5 [millirad]
    val AGBo_k1: Double = 1.429e-6  // constant used in handicap equation
    val AGBo_k2: Double = 1.07  // constant used in handicap equation
    val AGBo_k3: Double = 4.3  // constant used in handicap equation
    val AGBo_p1: Double = 2.0  // exponent of distance scaling
    val AGB0_arw_d: Double = 7.14e-3  // arrow diameter used in the old AGB algorithm by D. Lane

    // KEY PARAMETERS AND CONSTANTS FOR THE ARCHERY AUSTRALIA SCHEME
    val AA_k0: Double = 2.37  // offset required to set handicap 100 at desired score
    val AA_ks: Double = 0.027  // change with each step of geometric progression
    val AA_kd: Double = 0.004  // distance scaling factor [1/metres]

    // KEY PARAMETERS AND CONSTANTS FOR THE UPDATED ARCHERY AUSTRALIA SCHEME
    val AA2_k0: Double = 2.57  // offset required to set handicap 100 at desired score
    val AA2_ks: Double = 0.027  // change with each step of geometric progression
    val AA2_f1: Double = 0.815  // 'linear' scaling factor
    val AA2_f2: Double = 0.185  // 'quadratic' scaling factor
    val AA2_d0: Double = 50.0  // Normalisation distance [metres]

    // DEFAULT ARROW DIAMETER
    val arw_d_in: Double = 9.3e-3  // Diameter of an indoor arrow [metres]
    val arw_d_out: Double = 5.5e-3  // Diameter of an outdoor arrow [metres]

}

fun sigma_t(h: Double, hc_sys: String, dist: Double, hc_params: HcParams): Double{

    val sig_t = when (hc_sys) {
        // New AGB (Archery GB) System - Written by Jack Atkinson
        "AGB" -> (hc_params.AGB_ang_0
                * ((1.0 + hc_params.AGB_step / 100.0).pow(h + hc_params.AGB_datum))
                * exp(hc_params.AGB_kd * dist))
        // Old AGB (Archery GB) System - Written by David Lane (2013)
        "AGB_old" -> (hc_params.AGBo_ang_0
                * ((1.0 + hc_params.AGBo_step / 100.0).pow(h + hc_params.AGBo_datum))
                * (1 + (hc_params.AGBo_k1 * hc_params.AGBo_k2.pow(h + hc_params.AGBo_k3))
                * dist.pow(hc_params.AGBo_p1)))
        // Original Archery Australia System
        // Factor of sqrt(2) to deal with factor of 2 in differing AGB and AA definitions of sigma
        // Required so code elsewhere is unchanged
        // Factor of 1.0e-3 due to AA algorithm specifying sigma t in millirad, so convert to rad
        "AA" -> (1.0e-3
                * sqrt(2.0)
                * exp(hc_params.AA_k0 - hc_params.AA_ks * h + hc_params.AA_kd * dist))
        // Updated Archery Australia (AA) System
        "AA2" -> (sqrt(2.0)
                * 1.0e-3
                * exp(hc_params.AA2_k0 - hc_params.AA2_ks * h)
                * (hc_params.AA2_f1 + hc_params.AA2_f2 * dist / hc_params.AA2_d0))
        else -> 0.0
    }

    return sig_t
}

fun sigma_r(h: Double, hc_sys: String, dist: Double, hc_params: HcParams): Double{

    return dist * sigma_t(h, hc_sys, dist, hc_params)
}

fun arrow_score(target: Target, h: Double, hc_sys: String, hc_params: HcParams,
                arw_d_user: Double = -1.0): Double {

    var arwRad: Double = arw_d_user / 2.0

    if (arw_d_user == -1.0) {
        arwRad = if (hc_sys == "AGBold") {
            hc_params.AGB0_arw_d / 2.0}
        else {
            if (target.indoor) {hc_params.arw_d_in / 2.0}
            else {hc_params.arw_d_out / 2.0}
        }
    }

    var tar_dia: Double = target.diameter
    var sig_r: Double = sigma_r(h, hc_sys, target.distance, hc_params)

    var sBar: Double = 0.0
    var scoreSum: Double = 0.0

    if (target.scoring_system == "5_zone") {
        for (n in 1..4) {
            scoreSum += exp(-((((n * tar_dia / 10) + arwRad) / sig_r).pow(2)))
        }
        sBar = (9 - 2 * scoreSum - exp(-((((5 * tar_dia / 10) + arwRad) / sig_r).pow(2))))
    }
    else if(target.scoring_system == "10_zone") {
        for (n in 1..10) {
            scoreSum += exp(-((((n * tar_dia / 20) + arwRad) / sig_r).pow(2)))
            sBar = 10 - scoreSum
        }
    }

    else if(target.scoring_system == "10_zone_6_ring") {
        for (n in 1..5) {
            scoreSum += exp(-((((n * tar_dia / 20) + arwRad) / sig_r).pow(2)))
        }
        sBar = 10 - scoreSum - 5.0 * exp(-((((6 * tar_dia / 20) + arwRad) / sig_r).pow(2)))
    }

    else if(target.scoring_system == "10_zone_compound") {
        for (n in 2..10) {
            scoreSum += exp(-((((n * tar_dia / 20) + arwRad) / sig_r).pow(2)))
        }
        sBar = 10 - exp(-((((tar_dia / 40) + arwRad) / sig_r).pow(2))) - scoreSum
    }

    else if(target.scoring_system == "10_zone_5_ring") {
        for (n in 1..4) {
            scoreSum += exp(-((((n * tar_dia / 20) + arwRad) / sig_r).pow(2)))
        }
        sBar = 10 - scoreSum - 6.0 * exp(-((((5 * tar_dia / 20) + arwRad) / sig_r).pow(2)))
    }

    else if(target.scoring_system == "10_zone_5_ring_compound") {
        for (n in 2..5) {
            scoreSum += exp(-((((n * tar_dia / 20) + arwRad) / sig_r).pow(2)))
        }
        sBar = (10
                - exp(-((((tar_dia / 40) + arwRad) / sig_r).pow(2)))
                - scoreSum
                - 6.0 * exp(-((((5 * tar_dia / 20) + arwRad) / sig_r).pow(2))))
    }

    else if(target.scoring_system == "WA_field") {
        for (n in 2..6) {
            scoreSum += exp(-((((n * tar_dia / 10) + arwRad) / sig_r).pow(2)))
        }
        sBar = 6 - exp(-((((tar_dia / 20) + arwRad) / sig_r).pow(2))) - scoreSum
    }

    else if(target.scoring_system == "IFAA_field") {
        sBar = (5
                - exp(-((((tar_dia / 10) + arwRad) / sig_r).pow(2)))
                - exp(-((((3 * tar_dia / 10) + arwRad) / sig_r).pow(2)))
                -3.0 * exp(-((((5 * tar_dia / 10) + arwRad) / sig_r).pow(2)))
                )
    }

    else if(target.scoring_system == "Beiter_hit_miss") {
        sBar = 1 - exp(-((((tar_dia / 2) + arwRad) / sig_r).pow(2)))
    }

    else if(target.scoring_system == "Worcester") {
        for (n in 2..5) {
            scoreSum += exp(-((((n * tar_dia / 10) + arwRad) / sig_r).pow(2)))
        }
        sBar = 5 - scoreSum
    }

    else if(target.scoring_system == "Worcester_2_ring") {
        sBar = (5
                - exp(-((((tar_dia / 10) + arwRad) / sig_r).pow(2)))
                -4.0 * exp(-((((2 * tar_dia / 10) + arwRad) / sig_r).pow(2))))
    }

//    else:
//    raise ValueError(
//            f"No rule for calculating scoring for face type {target.scoring_system}."
//    )

    return sBar

}

fun score_for_passes(rnd: Round, h: Double, hc_sys: String, hc_params: HcParams,
                    arw_d_user: Double = -1.0): List<Double> {

    val pass_score: MutableList<Double> = mutableListOf()

    for (pass_i in rnd.passes) {
        pass_score.add(
            pass_i.n_arrows * arrow_score(pass_i.passTarget, h, hc_sys, hc_params, arw_d_user)
        )
    }

    return pass_score
}

fun score_for_round(rnd: Round, h: Double, hc_sys: String, hc_params: HcParams,
                     arw_d_user: Double = -1.0, round_up: Boolean = true): Double {

    var roundScore = score_for_passes(rnd, h, hc_sys, hc_params, arw_d_user).sum()

    if (round_up) {
        roundScore = ceil(roundScore)
    }

    return roundScore
}