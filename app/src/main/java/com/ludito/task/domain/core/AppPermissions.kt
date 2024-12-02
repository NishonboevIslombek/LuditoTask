package com.ludito.task.domain.core

import android.Manifest

interface Permissions {
    companion object {
        val REQUIRED_LOCATION_PERMISSIONS =
            mutableListOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ).toTypedArray()
    }
}