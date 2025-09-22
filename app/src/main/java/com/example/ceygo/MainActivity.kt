package com.example.ceygo

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // keep one instance per tab (so scroll position etc. is preserved)
    private val fragments: MutableMap<Int, Fragment> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            // default tab
            bottom.selectedItemId = R.id.tab_home
            showTab(R.id.tab_home)
        }

        bottom.setOnItemSelectedListener {
            showTab(it.itemId)
            true
        }

        // optional: do nothing when reselecting same tab
        bottom.setOnItemReselectedListener { /* no-op */ }
    }

    private fun showTab(tabId: Int) {
        val tag = tabId.toString()
        val fm = supportFragmentManager
        val current = fm.findFragmentById(R.id.nav_host)

        // find or create the fragment for the selected tab
        val next = fragments.getOrPut(tabId) {
            when (tabId) {
                R.id.tab_home    -> HomeFragment()
                R.id.tab_explore -> ExploreFragment()
                R.id.tab_saved   -> SavedFragment()
                R.id.tab_profile -> ProfileFragment()
                else -> HomeFragment()
            }.also { frag ->
                fm.beginTransaction()
                    .add(R.id.nav_host, frag, tag)
                    .hide(frag)     // will show below
                    .commitNow()
            }
        }

        fm.beginTransaction().apply {
            current?.let { hide(it) }
            show(next)
        }.commit()
    }
}