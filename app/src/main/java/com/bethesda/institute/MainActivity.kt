package com.bethesda.institute

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bethesda.institute.ui.AdmissionsFragment
import com.bethesda.institute.ui.CoursesFragment
import com.bethesda.institute.ui.HomeFragment
import com.bethesda.institute.ui.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            showFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> showFragment(HomeFragment())
                R.id.nav_courses -> showFragment(CoursesFragment())
                R.id.nav_admissions -> showFragment(AdmissionsFragment())
                R.id.nav_profile -> showFragment(ProfileFragment())
            }
            true
        }
    }

    private fun showFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
