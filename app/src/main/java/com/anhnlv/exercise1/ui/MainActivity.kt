package com.anhnlv.exercise1.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.anhnlv.exercise1.R
import com.anhnlv.exercise1.databinding.ActivityMainBinding
import com.anhnlv.exercise1.locationHelper.LocationManager

class MainActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var locationManager: LocationManager
    private var sensorManager: SensorManager? = null
    private var sensorAccelerometer: Sensor? = null
    private var sensorMagneticField: Sensor? = null
    private lateinit var valuesAccelerometer: FloatArray
    private lateinit var valuesMagneticField: FloatArray
    private lateinit var matrixR: FloatArray
    private lateinit var matrixI: FloatArray
    private lateinit var matrixValues: FloatArray

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        sensorAccelerometer = sensorManager!!.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        sensorMagneticField = sensorManager!!.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        valuesAccelerometer = FloatArray(3)
        valuesMagneticField = FloatArray(3)
        matrixR = FloatArray(9)
        matrixI = FloatArray(9)
        matrixValues = FloatArray(3)

        setContentView(binding.root)
        updateLocation()
        initClick()

    }

    /**
     * init click listener
     * */
    private fun initClick() {
        binding.tvLocation.setOnClickListener {
            reloadLocation()
        }
    }

    /**
     * update location
     * */
    private fun updateLocation() {
        locationManager = LocationManager(
            this
        ) {

            binding.tvLocation.text = String.format(
                getString(R.string.tv_location),
                it.latitude.toString(),
                it.longitude.toString()
            )

            if (it.accuracy > 0 && it.accuracy < 100) {
                binding.rvCircle.setAccuracy(it.accuracy)
            }
        }
        locationManager.startLocationUpdates()
    }

    /**
     * reload new location
     * */
    private fun reloadLocation() {
        locationManager.startLocationUpdates()
    }

    /**
     * handle permission request result
     *
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onDestroy() {
        super.onDestroy()
        locationManager.stopLocationUpdate()
    }


    override fun onResume() {
        sensorManager!!.registerListener(
            this,
            sensorAccelerometer,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        sensorManager!!.registerListener(
            this,
            sensorMagneticField,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        super.onResume()
    }

    override fun onPause() {
        sensorManager!!.unregisterListener(
            this,
            sensorAccelerometer
        )
        sensorManager!!.unregisterListener(
            this,
            sensorMagneticField
        )
        super.onPause()
    }

    override fun onAccuracyChanged(arg0: Sensor, arg1: Int) {
        // TODO Auto-generated method stub
    }

    override fun onSensorChanged(event: SensorEvent) {
        // TODO Auto-generated method stub
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                var i = 0
                while (i < 3) {
                    valuesAccelerometer[i] = event.values[i]
                    i++
                }
            }
            Sensor.TYPE_MAGNETIC_FIELD -> {
                var i = 0
                while (i < 3) {
                    valuesMagneticField[i] = event.values[i]
                    i++
                }
            }
        }
        val success = SensorManager.getRotationMatrix(
            matrixR,
            matrixI,
            valuesAccelerometer,
            valuesMagneticField
        )
        if (success) {
            SensorManager.getOrientation(matrixR, matrixValues)
            binding.ivDirection.rotation  = matrixValues[0]
        }
    }


}