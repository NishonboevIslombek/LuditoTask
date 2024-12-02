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
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.ludito.task.R
import com.ludito.task.databinding.ActivityMainBinding
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
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(this)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        window.statusBarColor = ContextCompat.getColor(this, R.color.onPrimaryColor)
        window.navigationBarColor = ContextCompat.getColor(this, R.color.onPrimaryColor)
        binding.bottomNavBar.menu.findItem(R.id.main_map).isChecked = true

        if (!hasRequiredLocationPermissions()) requestPermissions()
        else setupBottomNavigation()
    }

    private fun setupBottomNavigation() {
        val navOptions: NavOptions =
            NavOptions.Builder().setPopUpTo(R.id.mapFragment, true).build()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.container_map) as NavHostFragment

        navController = navHostFragment.navController
        binding.bottomNavBar.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.mapFragment ->  binding.bottomNavBar.menu.findItem(R.id.main_map).isChecked =
                    true
            }
        }

        binding.bottomNavBar.setOnItemSelectedListener {
            if (it.itemId ==  binding.bottomNavBar.selectedItemId || it.itemId == R.id.main_profile) return@setOnItemSelectedListener false
            when (it.itemId) {
                R.id.main_map -> {
                    navController.navigate(
                        BookmarkFragmentDirections.actionBookmarkToMap(
                            0f,
                            0f
                        ), navOptions
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