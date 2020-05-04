package com.csci442.geolocatr

import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.csci442.geolocatr.ui.map.MapFragment

class LocatrActivity : AppCompatActivity() {
    companion object {
        fun createIntent(context: Context, location: Location): Intent {
            return Intent(context, LocatrActivity::class.java).apply {
                putExtra(MapFragment.ARGS_LATITUDE, location.latitude)
                putExtra(MapFragment.ARGS_LONGITUDE, location.longitude)
            }
        }
    }

    private val logTag = "448.MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(logTag, "onCreate() called")
        setContentView(R.layout.activity_main)

        if(intent.extras != null) {
            val navController = findNavController(R.id.nav_host_fragment)
            navController.setGraph(R.navigation.nav_graph, intent.extras)
        }

        NavigationUI.setupActionBarWithNavController(this,
            findNavController(R.id.nav_host_fragment))
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.nav_host_fragment).navigateUp() || super.onSupportNavigateUp()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == MapFragment.REQUEST_LOC_ON) {
            val navHostFragment = supportFragmentManager.primaryNavigationFragment
            val locatrFragment = navHostFragment?.childFragmentManager?.fragments?.get(0) as MapFragment?
            locatrFragment?.onActivityResult(requestCode, resultCode, data)
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(logTag, "onStart() called")
    }

    override fun onResume() {
        super.onResume()
        Log.d(logTag, "onResume() called")
    }

    override fun onPause() {
        Log.d(logTag, "onPause() called")
        super.onPause()
    }

    override fun onStop() {
        Log.d(logTag, "onStop() called")
        super.onStop()
    }

    override fun onDestroy() {
        Log.d(logTag, "onDestroy() called")
        super.onDestroy()
    }

}
