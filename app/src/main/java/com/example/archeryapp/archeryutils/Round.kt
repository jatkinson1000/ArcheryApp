package com.example.archeryapp.archeryutils

import com.example.archeryapp.archeryutils.Target


class Pass (val n_arrows: Int, val diameter: Double, val scoring_system: String,
            val distance: Double, val dist_unit: String, val indoor: Boolean){

    val passTarget = Target(diameter, scoring_system, distance, dist_unit, indoor)

    val maxScore: Int = n_arrows * passTarget.maxScore
}

class Round (val name: String, val passes: List<Pass>){

    var maxScore: Int = 0

    init {
        getMaxScore()
    }

    private fun getMaxScore() {
        for (pass_i in passes){
            maxScore += pass_i.maxScore
        }
    }
}