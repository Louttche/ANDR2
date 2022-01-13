package com.example.stalkr.services

import android.app.*
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlin.math.round


class CompassService : Service(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)


    companion object SensorServiceKeys {
        // Global service keys
        val KEY_ANGLE = "angle"
        val KEY_DIRECTION = "direction"
        val KEY_ON_SENSOR_CHANGED_ACTION =
            "com.example.stalkr.services.SensorService.ON_SENSOR_CHANGED"
    }

    override fun onCreate() {
        super.onCreate()
        // Get sensor manager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        // Get accelerometer sensor and register its listener to the sensor manager
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER).also { accelerometer ->
            sensorManager.registerListener(
                this,
                accelerometer,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
        // Get magneticField sensor and register its listener to the sensor manager
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD).also { magneticField ->
            sensorManager.registerListener(
                this,
                magneticField,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_UI
            )
        }
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Get instance background value from activity
        intent?.let {
        }
        return START_STICKY
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) {
            return
        }
        // Check which sensor has sent an event
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, accelerometerReading.size)
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, magnetometerReading.size)
        }
        // Update the orientation angle
        updateOrientationAngles()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {}

    private fun updateOrientationAngles() {
        // Get orientation and convert to degrees
        SensorManager.getRotationMatrix(
            rotationMatrix,
            null,
            accelerometerReading,
            magnetometerReading
        )
        val orientation = SensorManager.getOrientation(rotationMatrix, orientationAngles)
        val degrees = (Math.toDegrees(orientation[0].toDouble()) + 360) % 360
        val angle = round(degrees * 100) / 100
        val direction = getDirection(angle)

        // Send direction to connected activities via broadcast
        val intent = Intent()
        intent.putExtra(KEY_ANGLE, angle)
        intent.putExtra(KEY_DIRECTION, direction)
        intent.action = KEY_ON_SENSOR_CHANGED_ACTION
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(intent)

    }

    private fun getDirection(angle: Double): String {
        // Determine the direction based on degrees
        var direction = ""

        if (angle >= 350 || angle <= 10)
            direction = "N"
        if (angle < 350 && angle > 280)
            direction = "NW"
        if (angle <= 280 && angle > 260)
            direction = "W"
        if (angle <= 260 && angle > 190)
            direction = "SW"
        if (angle <= 190 && angle > 170)
            direction = "S"
        if (angle <= 170 && angle > 100)
            direction = "SE"
        if (angle <= 100 && angle > 80)
            direction = "E"
        if (angle <= 80 && angle > 10)
            direction = "NE"

        return direction
    }
}