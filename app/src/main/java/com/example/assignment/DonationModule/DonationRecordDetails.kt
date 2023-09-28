package com.example.assignment.DonationModule

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import com.example.assignment.R
import com.google.firebase.firestore.FirebaseFirestore

class DonationRecordDetails : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_record_details)
        setSupportActionBar(findViewById(R.id.toolbar3))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val donationData = intent.getSerializableExtra("id")

        getRecordDetails(donationData as String?)


    }

    private fun getRecordDetails(id: String?) {
        // Initialize Firestore
        val db = FirebaseFirestore.getInstance()

// Reference a Firestore collection
        val collectionRef = db.collection("donations")

// Create a query (optional)
        val query = collectionRef.whereEqualTo("id", id)

// Retrieve data using a listener (real-time updates)
        query.addSnapshotListener { querySnapshot, firestoreException ->
            if (firestoreException != null) {
                // Handle the error
                return@addSnapshotListener
            }

            // Process the querySnapshot to access the documents and data
            for (document in querySnapshot!!) {
                val data = document.data // Access document data as a map
                val name = data["name"] // Access a specific field
                val email = data["email"]
                val amount = data["amount"]
                val method = data["method"]
                val date = data["date"]

                // Perform your desired actions with the data here

                val nameTv = findViewById<TextView>(R.id.name_tv)
                val emailTv = findViewById<TextView>(R.id.email_tv)
                val amountTv = findViewById<TextView>(R.id.amount_tv)
                val dateTv = findViewById<TextView>(R.id.date_tv)
                val methodTv = findViewById<TextView>(R.id.method_tv)

                nameTv.text = name.toString()
                emailTv.text = email.toString()
                amountTv.text = amount.toString()
                methodTv.text = method.toString()
                dateTv.text = date.toString()

            }
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