package com.example.ceygo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host, HomeFragment())
                .commit()
        }

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.tab_home -> { swap(HomeFragment()); true }
                else -> { /* stub fragments */ true }
            }
        }
        bottom.selectedItemId = R.id.tab_home
    }

    private fun swap(f: Fragment) =
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host, f)
            .commit()
}