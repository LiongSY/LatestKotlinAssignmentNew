package com.example.assignment

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.assignment.DonationModule.Community.Community
import com.example.assignment.DonationModule.DonationPage
import com.example.assignment.Resources.ResourcesList
import com.google.android.material.bottomnavigation.BottomNavigationView

class Homepage : AppCompatActivity() {
    private lateinit var botNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        botNav = findViewById(R.id.botNavigation)
        botNav.menu.findItem(R.id.resourceMenu)?.isChecked = true
        botNav.setOnNavigationItemReselectedListener { item ->
            when (item.itemId) {
                R.id.homeMenu -> {
                    val intent = Intent(this, Homepage::class.java)
                    startActivity(intent)
                    true
                }
                R.id.donationMenu -> {
                    val intent = Intent(this, DonationPage::class.java)
                    startActivity(intent)
                    true
                }
                R.id.resourceMenu -> {
                    val intent = Intent(this, ResourcesList::class.java)
                    startActivity(intent)
                    true
                }
                R.id.communityMenu -> {
                    val intent = Intent(this, Community::class.java)
                    startActivity(intent)
                    true
                }
            }
        }
    }
}