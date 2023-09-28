package com.example.assignment.Resources

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import com.example.assignment.DonationModule.Donation
import com.example.assignment.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase

class Report : AppCompatActivity() {

    private lateinit var noOfResource: TextView
    private lateinit var databaseHelper: SQLiteHelper
    private lateinit var botNav: BottomNavigationView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report)
        noOfResource = findViewById(R.id.noOfResources)

        var noOfResources = getResourceCount()

        noOfResource.setText(noOfResources.toString())
//        noOfPost = findViewById(R.id.noOfPost)
//        databaseHelper = SQLiteHelper(this)
////        FirebaseAuth.getInstance().currentUser?.email
//        val educatorEmail = "Liong@gmail.com"
//
//        if (educatorEmail != null) {
//            val resourcesCount = databaseHelper.geResourcesCountForEmail(educatorEmail)
//
//            noOfPost.text = resourcesCount.toString()
//        } else {
//            noOfPost.text = "0"
//        }

        botNav = findViewById(R.id.botNav)
        botNav.menu.findItem(R.id.resourceMenu)?.isChecked = true
        botNav.setOnNavigationItemReselectedListener { item ->
            when (item.itemId) {
                R.id.homeMenu -> {
                    val intent = Intent(this, Donation::class.java)
                    startActivity(intent)
                    true
                }
                R.id.donationMenu -> {

                }
                R.id.resourceMenu -> {

                }
                R.id.communityMenu -> {

                }
            }
        }
    }

    private fun getResourceCount() {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        var dbRef = firebaseDatabase.getReference("Resources")
        var countResources = 0
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (resSnap in snapshot.children) {
                        val resData = resSnap.getValue(ResourceModel::class.java)
                        if (resData != null) {
                            if (resData.email == "Liong@gmail.com") {
                                ++countResources
                            }
                        }
                    }
                    noOfResource.text = countResources.toString()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }
}

