package com.example.ceygo

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {

    private val fragments = mutableMapOf<Int, Fragment>()
    private var activeTabId: Int = R.id.tab_home   // track which one is visible

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the splash screen as early as possible
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Keep splash for 2 seconds
        var keepOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepOnScreen }
        Handler(Looper.getMainLooper()).postDelayed({ keepOnScreen = false }, 2000)

        setContentView(R.layout.activity_main)

        val bottom = findViewById<BottomNavigationView>(R.id.bottomNav)

        // restore already-added fragments after process death / config change
        restoreFragments()

        if (savedInstanceState == null) {
            // create and show Home as the first screen
            val home = obtainFragment(R.id.tab_home)
            supportFragmentManager.beginTransaction()
                .add(R.id.nav_host, home, tagOf(R.id.tab_home))
                .show(home)
                .commit()
            fragments[R.id.tab_home] = home
            activeTabId = R.id.tab_home
            bottom.selectedItemId = R.id.tab_home
        }

        bottom.setOnItemSelectedListener {
            showTab(it.itemId)
            true
        }
        bottom.setOnItemReselectedListener { /* no-op */ }
    }

    private fun showTab(tabId: Int) {
        if (tabId == activeTabId) return

        val fm = supportFragmentManager
        val next = fragments.getOrPut(tabId) {
            obtainFragment(tabId).also { frag ->
                fm.beginTransaction()
                    .add(R.id.nav_host, frag, tagOf(tabId))
                    .hide(frag)                // add hidden first
                    .commitNow()
            }
        }
        val current = fragments[activeTabId]

        fm.beginTransaction().apply {
            current?.let { hide(it) }
            show(next)
            setPrimaryNavigationFragment(next)  // optional, helps back press
        }.commit()

        activeTabId = tabId
    }

    private fun obtainFragment(tabId: Int): Fragment = when (tabId) {
        R.id.tab_home    -> HomeFragment()
        R.id.tab_explore -> ExploreFragment()
        R.id.tab_saved   -> SavedFragment()
        R.id.tab_profile -> ProfileFragment()
        else -> HomeFragment()
    }

    private fun tagOf(tabId: Int) = "tab:$tabId"

    private fun restoreFragments() {
        val fm = supportFragmentManager
        listOf(
            R.id.tab_home,
            R.id.tab_explore,
            R.id.tab_saved,
            R.id.tab_profile
        ).forEach { id ->
            fm.findFragmentByTag(tagOf(id))?.let { fragments[id] = it }
        }
        // figure out which one is currently visible (if any)
        fragments.forEach { (id, f) -> if (!f.isHidden) activeTabId = id }
    }
}