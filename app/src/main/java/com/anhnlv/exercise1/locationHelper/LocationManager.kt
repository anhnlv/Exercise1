package com.anhnlv.exercise1.locationHelper

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.anhnlv.exercise1.R
import com.anhnlv.exercise1.utils.LogUtil
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog
import com.github.javiersantos.materialstyleddialogs.enums.Style
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener

class LocationManager(
    private val context: Activity,
    mListener: LocationObserver? = null
) {

    private var mSettingsClient: SettingsClient? = null
    private var mLocationSettingsRequest: LocationSettingsRequest? = null
    private lateinit var locationProvider: LocationProvider

    init {
        val arrayPermission = arrayListOf(Manifest.permission.ACCESS_FINE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            arrayPermission.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        locationProvider = LocationProvider.Builder(context)
            .intervalMs(5000)// interval update milliseconds : sample 5s
            .locationPermission(
                arrayPermission.toTypedArray()
            )
            .accuracy(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .locationObserver(mListener ?: LocationObserver { })
            .onPermissionDeniedFirstTime {
                LogUtil.d("Denied!")
            }
            .onPermissionDeniedAgain {
                if (!context.isFinishing && !context.isDestroyed) {
                    MaterialStyledDialog.Builder(context)
                        .setStyle(Style.HEADER_WITH_ICON)
                        .setHeaderColor(R.color.header)
                        .setIcon(R.drawable.ic_arrow)
                        .setTitle(context.getString(R.string.location_permission_title))
                        .setDescription(context.getString(R.string.location_permission_des))
                        .setPositiveText(context.getString(R.string.location_permission_ok))
                        .setCancelable(false)
                        .onPositive {
                            locationProvider.startTrackingLocation()
                        }
                        .setNegativeText(context.getString(R.string.location_permission_cancel))
                        .show()
                }
            }
            .onPermissionDeniedForever {
                // Need to go to the settings
                if (!context.isFinishing && !context.isDestroyed) {
                    MaterialStyledDialog.Builder(context)
                        .setStyle(Style.HEADER_WITH_ICON)
                        .setHeaderColor(R.color.header)
                        .setIcon(R.drawable.ic_arrow)
                        .setTitle(context.getString(R.string.location_permission_title))
                        .setDescription(context.getString(R.string.location_permission_des))
                        .setPositiveText(context.getString(R.string.location_permission_ok))
                        .setCancelable(false)
                        .onPositive {
                            locationProvider.startAppSettings(context.packageName)
                        }
                        .show()

//
                }
            }
            .build()
    }

    fun startLocationUpdates() {
        if (!isLocationEnabled()) {
            MaterialStyledDialog.Builder(context)
                .setStyle(Style.HEADER_WITH_ICON)
                .setHeaderColor(R.color.header)
                .setIcon(R.drawable.ic_arrow)
                .setTitle(context.getString(R.string.location_permission_title))
                .setDescription(context.getString(R.string.location_permission_des))
                .setPositiveText(context.getString(R.string.location_permission_ok))
                .setNegativeText(context.getString(R.string.location_permission_cancel))
                .setCancelable(false)
                .onPositive {
                    if (!context.isFinishing && !context.isDestroyed)
                        context.startActivityForResult(
                            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                            requestEnableGPSSetting
                        )
                }.onNegative {
                    if (!context.isFinishing && !context.isDestroyed)
                        context.finish()
                }
                .show()

        } else {
            // Check to show instruction
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                MaterialStyledDialog.Builder(context)
                    .setStyle(Style.HEADER_WITH_ICON)
                    .setHeaderColor(R.color.header)
                    .setIcon(R.drawable.ic_arrow)
                    .setTitle(context.getString(R.string.location_permission_title))
                    .setDescription(context.getString(R.string.location_permission_des))
                    .setPositiveText(context.getString(R.string.location_permission_ok))
                    .setCancelable(false)
                    .onPositive { initLocationUpdate() }
                    .setNegativeText(context.getString(R.string.location_permission_cancel))
                    .show()
            } else {
                initLocationUpdate()
            }
        }
    }

    private fun initLocationUpdate() {
        val mLocationRequest = LocationRequest.create()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 60000
        mLocationRequest.fastestInterval = 30000

        val builder =
            LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest)
        mLocationSettingsRequest = builder.build()
        mSettingsClient = LocationServices.getSettingsClient(context)
        mSettingsClient!!
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(context) {
                locationProvider.startTrackingLocation()
            }
            .addOnFailureListener(context) { e ->
                when ((e as ApiException).statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                context,
                                requestCheckSettings
                            )
                        } catch (sie: IntentSender.SendIntentException) {
                            locationProvider.startTrackingLocation()
                        }
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                        // Need to go to the settings
                        if (!context.isFinishing && !context.isDestroyed) {
                            MaterialStyledDialog.Builder(context)
                                .setStyle(Style.HEADER_WITH_ICON)
                                .setHeaderColor(R.color.header)
                                .setIcon(R.drawable.ic_arrow)
                                .setTitle(context.getString(R.string.location_permission_title))
                                .setDescription(context.getString(R.string.location_permission_des))
                                .setPositiveText(context.getString(R.string.location_permission_ok))
                                .setCancelable(false)
                                .onPositive { locationProvider.startAppSettings(context.packageName) }
                                .show()
                        }
                    }
                }
            }
    }

    fun stopLocationUpdate() {
        if (locationProvider.isTrackingLocation) {
            locationProvider.stopTrackingLocation()
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationMode = 0

        try {
            locationMode = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE
            )
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
            return false
        }
        return  locationMode != Settings.Secure.LOCATION_MODE_OFF
    }

    private var onGetLastLocation: ((Location) -> Unit)? = null

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData(onGetLocation: ((Location) -> Unit)?) {
        onGetLastLocation = onGetLocation
        if (!isLocationEnabled()) {
            if (!context.isFinishing) {
                MaterialStyledDialog.Builder(context)
                    .setStyle(Style.HEADER_WITH_ICON)
                    .setHeaderColor(R.color.header)
                    .setIcon(R.drawable.ic_arrow)
                    .setDescription(context.getString(R.string.location_permission_des))
                    .setPositiveText(context.getString(R.string.location_permission_ok))
                    .setCancelable(false)
                    .onPositive {
                        if (!context.isFinishing && !context.isDestroyed)
                            context.startActivityForResult(
                                Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
                                requestEnableGPSSettingLastLocation
                            )
                    }
                    .setNegativeText(context.getString(R.string.location_permission_cancel))
                    .onNegative {
                        if (!context.isFinishing && !context.isDestroyed)
                            context.finish()
                    }
                    .show()

            }
        } else {
            val mLocationRequest = LocationRequest()
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            mLocationRequest.interval = 0
            mLocationRequest.fastestInterval = 0
            mLocationRequest.numUpdates = 1
            val builder =
                LocationSettingsRequest.Builder()
            builder.addLocationRequest(mLocationRequest)
            LocationServices.getSettingsClient(context)
                .checkLocationSettings(builder.build())
                .addOnSuccessListener(context) {
                    LocationServices.getFusedLocationProviderClient(context).requestLocationUpdates(
                        mLocationRequest,
                        object : LocationCallback() {
                            override fun onLocationResult(location: LocationResult?) {
                                location?.let {
                                    onGetLocation?.invoke(it.lastLocation)
                                }
                            }
                        },
                        null
                    )
                }
                .addOnFailureListener(context) { e ->
                    when ((e as ApiException).statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            try {
                                // Show the dialog by calling startResolutionForResult(), and check the
                                // result in onActivityResult().
                                val rae = e as ResolvableApiException
                                rae.startResolutionForResult(
                                    context,
                                    requestCheckSettingsLastLocation
                                )
                            } catch (sie: IntentSender.SendIntentException) {
                            }
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            // Need to go to the settings
                            if (!context.isFinishing && !context.isDestroyed) {
                                MaterialStyledDialog.Builder(context)
                                    .setStyle(Style.HEADER_WITH_ICON)
                                    .setHeaderColor(R.color.header)
                                    .setIcon(R.drawable.ic_arrow)
                                    .setDescription(context.getString(R.string.location_permission_des))
                                    .setPositiveText(context.getString(R.string.location_permission_ok))
                                    .setCancelable(false)
                                    .onPositive {
                                        locationProvider.startAppSettings(context.packageName)
                                    }
                                    .setNegativeText(context.getString(R.string.location_permission_cancel))
                                    .show()
                            }
                        }
                    }
                }
        }
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        locationProvider.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(onGetLocation: ((Location) -> Unit)?) {
        val onSuccessListener = OnSuccessListener<Location> { location ->
            if (location != null) {
                onGetLocation?.invoke(location)
            } else {
                requestNewLocationData(onGetLocation)
            }
        }
        val onFailureListener = OnFailureListener { e ->
            e.printStackTrace()
        }
        if (checkPermissions()) {
            LocationServices.getFusedLocationProviderClient(context)
                .lastLocation
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener)
        }
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == requestCheckSettings) {
            locationProvider.startTrackingLocation()
        }
        if (requestCode == requestEnableGPSSetting) {
            startLocationUpdates()
        }
        if (requestCode == requestCheckSettingsLastLocation) {
            if (resultCode == RESULT_OK) {
                requestNewLocationData(onGetLastLocation)
            } else {
                getLastLocation(onGetLastLocation)
            }
        }
        if (requestCode == requestEnableGPSSettingLastLocation) {
            requestNewLocationData(onGetLastLocation)
        }
    }

    companion object {
        const val requestCheckSettings = 1001  //?
        const val requestEnableGPSSetting = 1002  //?
        const val requestCheckSettingsLastLocation = 1003  //?
        const val requestEnableGPSSettingLastLocation = 1004  //?
    }
}