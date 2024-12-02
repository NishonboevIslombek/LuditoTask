package com.ludito.task.presentation.utils

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.ludito.task.domain.core.Permissions

/**
 * Launches a request for location permissions and handles the result.
 *
 * @param grantedAction The action to execute if all requested permissions are granted.
 * @param deniedAction The action to execute if any of the requested permissions are denied.
 * @param permissions The permissions to request. Defaults to `Permissions.REQUIRED_LOCATION_PERMISSIONS`.
 *
 *  * @return An `ActivityResultLauncher<Array<String>>` that can be used to trigger the permission request.
 *
 */
fun ComponentActivity.launchActivityResultForLocationPermissions(
    grantedAction: () -> Unit,
    deniedAction: () -> Unit,
    permissions: Array<String> = Permissions.REQUIRED_LOCATION_PERMISSIONS
): ActivityResultLauncher<Array<String>> {
    return this.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permission ->
        // Handle Permission granted/rejected
        var permissionGranted = true
        permission.entries.forEach {
            if (it.key in permissions && !it.value) permissionGranted = false
        }
        if (!permissionGranted) {
            deniedAction()
        } else {
            grantedAction()
        }
    }
}

/**
 * Checks if the activity has all the required location permissions and if the GPS provider is enabled.
 *
 * @param permissions The permissions to check. Defaults to `Permissions.REQUIRED_LOCATION_PERMISSIONS`.
 * @return `true` if all specified permissions are granted and the GPS provider is enabled, otherwise `false`.
 */
fun ComponentActivity.hasRequiredLocationPermissions(permissions: Array<String> = Permissions.REQUIRED_LOCATION_PERMISSIONS) =
    permissions.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    } && (this.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isProviderEnabled(
        LocationManager.GPS_PROVIDER
    )