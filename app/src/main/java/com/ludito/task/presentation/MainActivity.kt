package com.ludito.task.presentation

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ludito.task.R
import com.ludito.task.domain.core.Permissions
import com.ludito.task.presentation.bookmark.BookmarkFragmentDirections
import com.ludito.task.presentation.map.MapFragmentDirections
import com.ludito.task.presentation.utils.hasRequiredLocationPermissions
import com.ludito.task.presentation.utils.launchActivityResultForLocationPermissions
import com.yandex.mapkit.MapKitFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.onPrimaryColor)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.onPrimaryColor)

        if (!hasRequiredLocationPermissions()) requestPermissions()
        else setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        bottomNavigationView = findViewById(R.id.bottom_nav_bar)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container_map) as NavHostFragment

        navController = navHostFragment.navController
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.menu.findItem(R.id.main_map).isChecked = true
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mapFragment -> bottomNavigationView.menu.findItem(R.id.main_map).isChecked =
                    true
            }
        }

        bottomNavigationView.setOnItemSelectedListener {
            if (it.itemId == bottomNavigationView.selectedItemId || it.itemId == R.id.main_profile) return@setOnItemSelectedListener false
            when (it.itemId) {
                R.id.main_map -> {
                    navController.navigate(
                        BookmarkFragmentDirections.actionBookmarkToMap(
                            0f,
                            0f
                        )
                    )
                }

                R.id.main_bookmark -> {
                    navController.navigate(MapFragmentDirections.actionMapToBookmark())
                }

                else -> {}
            }
            true
        }
    }


    private fun requestPermissions() {
        val activityResultLauncher = this.launchActivityResultForLocationPermissions(
            deniedAction = {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
            },
            grantedAction = {
                setupBottomNavigation()
            }
        )
        activityResultLauncher.launch(Permissions.REQUIRED_LOCATION_PERMISSIONS)
    }
}