package com.example.tcg_manager.ui

import com.example.tcg_manager.R

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.core.view.isVisible
import android.view.Menu
import android.view.MenuItem
import com.example.tcg_manager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            // Consumed here so the bottom bar doesn't add the same padding a second time.
            WindowInsetsCompat.CONSUMED
        }
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment_content_main) as NavHostFragment
        val navController = navHostFragment.navController

        // The three bottom-bar tabs are top-level: no back arrow on them.
        appBarConfiguration = AppBarConfiguration(TOP_LEVEL_DESTINATIONS)
        setupActionBarWithNavController(navController, appBarConfiguration)

        val content = binding.content
        content.bottomNav.setupWithNavController(navController)
        content.fab.setOnClickListener { navController.navigate(R.id.scanFragment) }

        // Bottom bar only on the tabs; scan button only where scanning makes sense.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            content.bottomNav.isVisible = destination.id in TOP_LEVEL_DESTINATIONS
            if (destination.id in SCAN_BUTTON_DESTINATIONS) content.fab.show() else content.fab.hide()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private companion object {
        val TOP_LEVEL_DESTINATIONS = setOf(R.id.homeFragment, R.id.collectionFragment, R.id.decksFragment)
        val SCAN_BUTTON_DESTINATIONS = setOf(R.id.homeFragment, R.id.collectionFragment)
    }
}