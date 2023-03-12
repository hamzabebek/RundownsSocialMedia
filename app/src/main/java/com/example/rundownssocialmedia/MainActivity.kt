package com.example.rundownssocialmedia

import android.content.Intent
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.rundownssocialmedia.Fragments.*
import com.example.rundownssocialmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.nav_home -> {
                moveToFragment(HomeFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_search -> {
                moveToFragment(SearchFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_add_post -> {
                item.isChecked = false
                startActivity(Intent(this@MainActivity,AddPostActivity::class.java))
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_notifications -> {
                moveToFragment(NotificationsFragment())
                return@OnNavigationItemSelectedListener true
            }
            R.id.nav_profile -> {
                moveToFragment(ProfileFragment())
                return@OnNavigationItemSelectedListener true
            }
        }

        false
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        moveToFragment(HomeFragment())
        val profileIntent = intent.getStringExtra("profileFragment")
        if (profileIntent == "profileFragment"){
            moveToFragment(ProfileFragment())
        }else if (profileIntent == "postDetailFragment"){
            moveToFragment(PostDetailsFragment())
        }
    }

    private fun moveToFragment(fragment: Fragment){
        val fragmentTrans = supportFragmentManager.beginTransaction()
        fragmentTrans.replace(R.id.fragment_container, fragment)
        fragmentTrans.commit()
    }
}