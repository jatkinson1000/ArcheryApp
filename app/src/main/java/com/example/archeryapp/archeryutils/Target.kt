package com.example.archeryapp.archeryutils

class Target (var diameter: Double, val scoring_system: String, var distance: Double,
              var dist_unit: String, val indoor: Boolean){

    private val systems = listOf<String>("5_zone",
        "10_zone",
        "10_zone_compound",
        "10_zone_6_ring",
        "10_zone_6_ring_compound",
        "10_zone_5_ring",
        "10_zone_5_ring_compound",
        "WA_field",
        "IFAA_field",
        "IFAA_field_expert",
        "Worcester",
        "Worcester_2_ring",
        "Beiter_hit_miss"
    )
    // No error handling for

    private val yards = listOf<String>(
        "Yard",
        "yard",
        "Yards",
        "yards",
        "Y",
        "y",
        "Yd",
        "yd",
        "Yds",
        "yds",
    )

    var maxScore: Int = 0

    init {
        getMaxScore()

        dist_unit = if (yards.contains(dist_unit)) {
            "yard"
        } else {
            "metre"
        }
    }

    private fun getMaxScore() {
        if (systems.slice(0..0).contains(scoring_system)) {
            maxScore = 9
        } else if (systems.slice(1..6).contains(scoring_system)) {
            maxScore = 10
        } else if (systems.slice(7..7).contains(scoring_system)) {
            maxScore = 6
        } else if (systems.slice(8..11).contains(scoring_system)) {
            maxScore = 5
        } else if (systems.slice(12..12).contains(scoring_system)) {
            maxScore = 1
        }
    }
}