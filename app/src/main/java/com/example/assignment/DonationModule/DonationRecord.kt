package com.example.assignment.DonationModule

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.Spinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.DonationModule.DonationModuleAdapter.DonationAdapter
import com.example.assignment.R
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.QuerySnapshot

class DonationRecord : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var userRecyclerview: RecyclerView
    private lateinit var userArrayList: ArrayList<Donation>
    private lateinit var myAdapter: DonationAdapter
    private lateinit var sortSpinner: Spinner
    private lateinit var sortButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_record)
        setSupportActionBar(findViewById(R.id.toolbar2))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        userRecyclerview = findViewById(R.id.userList)
        userRecyclerview.layoutManager = LinearLayoutManager(this)
        userRecyclerview.setHasFixedSize(true)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")

        val draftBtn = findViewById<Button>(R.id.draft_btn)

        draftBtn.setOnClickListener(View.OnClickListener {
            val i = Intent(this, DonationDraftRecord::class.java)
            startActivity(i)
        })

        sortSpinner = findViewById(R.id.sort_spn)
        sortButton = findViewById(R.id.sort_btn)
        val sortOptions = arrayOf("Default", "Amount", "Date")
        val defaultPosition = sortOptions.indexOf("Default")
        sortButton.setOnClickListener {
            // Check the selected sort option and perform sorting accordingly
            when (sortSpinner.selectedItem.toString()) {
                "Amount" -> {
                    // Sort by amount
                    userArrayList.sortBy { it.amount }
                }
                "Date" -> {
                    // Sort by date
                    userArrayList.sortBy { it.date }
                }
                else -> {
                    sortSpinner.setSelection(defaultPosition)
                }
            }

            // Notify the adapter that the data has changed due to sorting
            myAdapter.notifyDataSetChanged()
        }



        userArrayList = arrayListOf()

        myAdapter = DonationAdapter(userArrayList,userRecyclerview)

        userRecyclerview.adapter = myAdapter

        val emailInput:String = email.toString()

        EventChangeListener(emailInput)

    }

    private lateinit var listenerRegistration: ListenerRegistration // Declare a global variable to store the listener registration

    private fun EventChangeListener(email: String) {
        db = FirebaseFirestore.getInstance()

        // Reference the Firestore collection
        val collectionRef = db.collection("donations")

        // Create a query to filter by email
        val query = collectionRef.whereEqualTo("email", email)

        // Add a real-time listener
        listenerRegistration = query.addSnapshotListener { querySnapshot, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                return@addSnapshotListener
            }

            for (dc: DocumentChange in querySnapshot?.documentChanges!!) {
                when (dc.type) {
                    DocumentChange.Type.ADDED -> {
                        // Handle added document
                        val donation = dc.document.toObject(Donation::class.java)
                        userArrayList.add(donation)
                    }
                    DocumentChange.Type.MODIFIED -> {
                        // Handle modified document
                        val donation = dc.document.toObject(Donation::class.java)
                        // Find and update the existing item in userArrayList based on a unique identifier
                        val index = userArrayList.indexOfFirst { it.id == donation.id }
                        if (index != -1) {
                            userArrayList[index] = donation
                        }
                    }
                    DocumentChange.Type.REMOVED -> {
                        // Handle removed document
                        val donation = dc.document.toObject(Donation::class.java)
                        // Remove the item from userArrayList based on a unique identifier
                        val index = userArrayList.indexOfFirst { it.id == donation.id }
                        if (index != -1) {
                            userArrayList.removeAt(index)
                        }
                    }
                }
            }

            myAdapter.notifyDataSetChanged()
        }
    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the Up button click here
                onBackPressed() // or navigate to the parent activity
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

}