package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM

import android.location.Location
import com.example.comunicationwearmobile.ui.model.dto.AreaTrack
import com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM.GeofenceTrackStore.isStationaryUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class StationaryTest  {

    private lateinit var track: AreaTrack

    @Before
    fun setUp() {
        track = AreaTrack()
    }

    private fun location(
        latitude: Double,
        longitude: Double,
        speed: Float? = null
    ): Location {
        return Location("test").apply {
            this.latitude = latitude
            this.longitude = longitude

            if (speed != null) {
                this.speed = speed
            }
        }
    }

    @Test
    fun firstLocation_isNotStationary() {
        val location = location(
            latitude = -34.6700,
            longitude = -58.5600
        )

        val result = isStationaryUpdate(
            track,
            location,
            1_000L
        )

        assertFalse(result)
        assertEquals(1_000L, track.stationarySince)
        assertEquals(0f, track.stationaryAccumMove, 0.001f)
    }

    @Test
    fun before90Seconds_isNotStationary() {
        val location = location(-34.6700, -58.5600)

        isStationaryUpdate(track, location, 1_000L)

        val result = isStationaryUpdate(
            track,
            location,
            90_999L
        )

        assertFalse(result)
    }

    @Test
    fun exactly90Seconds_isStationary() {
        val location = location(-34.6700, -58.5600)

        isStationaryUpdate(track, location, 1_000L)

        val result = isStationaryUpdate(
            track,
            location,
            91_000L
        )

        assertTrue(result)
    }

    @Test
    fun speedBelowLimit_isStationary() {
        val initial = location(-34.6700, -58.5600)

        isStationaryUpdate(track, initial, 1_000L)

        val current = location(
            -34.6700,
            -58.5600,
            0.39f
        )

        val result = isStationaryUpdate(
            track,
            current,
            91_000L
        )

        assertTrue(result)
    }

    @Test
    fun speedExactlyLimit_isStationary() {
        val initial = location(-34.6700, -58.5600)

        isStationaryUpdate(track, initial, 1_000L)

        val current = location(
            -34.6700,
            -58.5600,
            0.4f
        )

        val result = isStationaryUpdate(
            track,
            current,
            91_000L
        )

        assertTrue(result)
    }

    @Test
    fun speedAboveLimit_isNotStationary() {
        val initial = location(-34.6700, -58.5600)

        isStationaryUpdate(track, initial, 1_000L)

        val current = location(
            -34.6700,
            -58.5600,
            0.41f
        )

        val result = isStationaryUpdate(
            track,
            current,
            91_000L
        )

        assertFalse(result)
    }

    @Test
    fun unknownSpeed_isStationary() {
        val initial = location(-34.6700, -58.5600)

        isStationaryUpdate(track, initial, 1_000L)

        val current = location(-34.6700, -58.5600)

        assertFalse(current.hasSpeed())

        val result = isStationaryUpdate(
            track,
            current,
            91_000L
        )

        assertTrue(result)
    }

    private fun locationMetersNorth(
        baseLat: Double,
        baseLon: Double,
        meters: Double,
        speed: Float? = null
    ): Location {

        val earthRadius = 6_371_000.0

        val newLat = baseLat +
                Math.toDegrees(meters / earthRadius)

        return location(
            newLat,
            baseLon,
            speed
        )
    }

    @Test
    fun movementGreaterThan8Meters_resetsWindow() {
        val lat = -34.6700
        val lon = -58.5600

        val initial = location(lat, lon)

        isStationaryUpdate(
            track,
            initial,
            1_000L
        )

        val moved = locationMetersNorth(
            lat,
            lon,
            9.0
        )

        val result = isStationaryUpdate(
            track,
            moved,
            91_000L
        )

        assertFalse(result)

        assertEquals(
            91_000L,
            track.stationarySince
        )

        assertEquals(
            0f,
            track.stationaryAccumMove,
            0.001f
        )
    }
}