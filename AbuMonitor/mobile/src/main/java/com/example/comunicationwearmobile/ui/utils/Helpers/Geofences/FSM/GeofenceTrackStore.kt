package com.example.comunicationwearmobile.ui.utils.Helpers.Geofences.FSM

import android.location.Location
import com.example.abumonitor.constants.Definition
import com.example.abumonitor.data.model.EntityAreaGeofence
import com.example.comunicationwearmobile.ui.model.dto.AreaTrack
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

 object GeofenceTrackStore {

    private val track = mutableMapOf<Long, AreaTrack>()
    private val trackMutex = Mutex()

    // Estacionario
    private const val STATIONARY_WINDOW_MS    = 90_000L
    private const val STATIONARY_MAX_MOVE_M   = 8f
    private const val STATIONARY_MAX_SPEED_MS = 0.4f

    private suspend fun <T> withTrack(areaId: Long, block: (AreaTrack) -> T): T {
        return trackMutex.withLock {
            val t = track.getOrPut(areaId) { AreaTrack() }
            block(t)
        }
    }

    fun isStationaryUpdate(t: AreaTrack, location: Location, now: Long): Boolean {
        if (t.lastLocAt == 0L) {
            t.lastLocAt = now
            t.lastLat = location.latitude
            t.lastLon = location.longitude
            t.stationarySince = now
            t.stationaryAccumMove = 0f
            return false
        }

        val prev = Location("prev").apply {
            latitude = t.lastLat
            longitude = t.lastLon
        }
        val d = location.distanceTo(prev)

        t.stationaryAccumMove += d
        t.lastLocAt = now
        t.lastLat = location.latitude
        t.lastLon = location.longitude

        //si la persona se movio una cantidad determinada de metros acumulada
        //se reinicia el contador de tiempo de inactividad
        if (t.stationaryAccumMove > STATIONARY_MAX_MOVE_M) {
            t.stationarySince = now
            t.stationaryAccumMove = 0f
            return false
        }

        // Se verifica que haya transcurrido el tiempo suficiente desde el inicio
        // de la ventana actual sin superar STATIONARY_MAX_MOVE_M metros acumulados.
        val enoughTimePassed = (now - t.stationarySince) >= STATIONARY_WINDOW_MS

        //se verifica si la persona tiene poca o nula velocidad en ese momento
        val hasLowOrUnknownSpeed  = (!location.hasSpeed()) || (location.speed <= STATIONARY_MAX_SPEED_MS)

        return enoughTimePassed && hasLowOrUnknownSpeed
    }

    suspend fun getStationaryInHitorialLocation(area: EntityAreaGeofence, location: Location, now: Long): Boolean {
        return withTrack(area.id_area) { t ->
            isStationaryUpdate(t, location, now)
        }
    }

    suspend fun resetStreaks(areaId: Long) {
        withTrack(areaId) { t ->
            t.insideStreak = 0
            t.outsideStreak = 0
        }
    }

    suspend fun resetOutsideStreak(areaId: Long) {
        withTrack(areaId) { t -> t.outsideStreak = 0 }
    }

    suspend fun isSpamBlocked(areaId: Long, now: Long, minGapMs: Long): Boolean {
        return withTrack(areaId) { t -> (now - t.lastEventAt) < minGapMs }
    }

    suspend fun isFlipBlocked(areaId: Long, now: Long, minFlipMs: Long): Boolean {
        return withTrack(areaId) { t -> (now - t.lastFlipAt) < minFlipMs }
    }

    suspend fun getLastFlipAt(areaId: Long): Long = withTrack(areaId) { it.lastFlipAt }

    suspend fun getLastDistToCenter(areaId: Long): Float = withTrack(areaId) { it.lastDistToCenter }

    suspend fun getInsideStreak(areaId: Long): Int = withTrack(areaId) { it.insideStreak }

    suspend fun getOutsideStreak(areaId: Long): Int = withTrack(areaId) { it.outsideStreak }

    suspend fun acceptEvent(areaId: Long, now: Long, distance: Float) {
        withTrack(areaId) { t ->
            t.lastEventAt = now
            t.lastFlipAt = now
            t.lastDistToCenter = distance
            t.insideStreak = 0
            t.outsideStreak = 0
        }
    }

    suspend fun confirmByStreaks(
        areaId: Long,
        candidate: String,
        requiredEnterConfirmCount: Int,
        requiredExitConfirmCount: Int
    ): Boolean {
        return withTrack(areaId) { t ->
            when (candidate) {
                Definition.EVT_ENTER -> {
                    t.insideStreak += 1
                    t.outsideStreak = 0
                    t.insideStreak >= requiredEnterConfirmCount
                }

                Definition.EVT_EXIT -> {
                    t.outsideStreak += 1
                    t.insideStreak = 0
                    t.outsideStreak >= requiredExitConfirmCount
                }

                else -> false
            }
        }
    }
}
