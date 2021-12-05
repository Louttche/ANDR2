package com.example.stalkr

class MapUtils {
    companion object {

        fun metersToLat(meters: Double) : Double {
            // assume 111,111 meters is 1 degree of latitude in y direction
            return meters/111111
        }

        fun metersToLong(meters: Double, lat: Double) : Double {
            // assume 111,111 * cos(latitude) meters is 1 degree of longitude in the x direction
            return meters/111111 * kotlin.math.cos(lat)
        }
    }
}