package com.example.assignment.DonationModule

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.DonationModule.Database.DBConnection
import com.example.assignment.DonationModule.DonationModuleAdapter.DonationReportAdapter
import com.example.assignment.R
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DonationReport : AppCompatActivity() {
    private lateinit var db: FirebaseFirestore
    private lateinit var userRecyclerview: RecyclerView
    private lateinit var userArrayList: ArrayList<Donation>
    private lateinit var myAdapter: DonationReportAdapter

    private var totalDonationAmount: Float = 0.0f
    private val calendar: Calendar = Calendar.getInstance()
    private val currentMonth: Int = calendar.get(Calendar.MONTH)
    private val currentWeek: Int = calendar.get(Calendar.WEEK_OF_YEAR)

    private val currentDate: Date = calendar.time
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd")
    private val formattedDate: String = dateFormat.format(currentDate)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_report)

        setSupportActionBar(findViewById(R.id.toolbar5))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val date:TextView = findViewById(R.id.date)
        date.text = formattedDate.toString()
        userRecyclerview = findViewById(R.id.donation_list)
        userRecyclerview.layoutManager = LinearLayoutManager(this)
        userRecyclerview.setHasFixedSize(true)

        userArrayList = arrayListOf()

        myAdapter = DonationReportAdapter(userArrayList)

        userRecyclerview.adapter = myAdapter


        EventChangeListener()

    }

    private fun EventChangeListener() {
        db = FirebaseFirestore.getInstance()

        db.collection("donations")
            .addSnapshotListener(object : EventListener<QuerySnapshot> {
                override fun onEvent(
                    value: QuerySnapshot?,
                    error: FirebaseFirestoreException?
                ) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    if (value == null || value.isEmpty()) {
                        // No records found, display an alert message
                        showNoRecordsFoundAlert()
                        return
                    }
                    userArrayList.clear() // Clear the previous data

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            userArrayList.add(dc.document.toObject(Donation::class.java))
                        }
                    }

                    totalDonationAmount = 0.0f // Reset total donation amount
                    var currentWeekDonationAmount: Float = 0.0f // Initialize week total
                    var currentMonthDonationAmount: Float = 0.0f // Initialize month total

                    for (donation: Donation in userArrayList) {
                        // Convert the amount from String to Float
                        val amount: Float = donation.amount?.toFloatOrNull() ?: 0.0f

                        // Calculate week total
                        if (isDateInCurrentWeek(donation.date.toString())) {
                            currentWeekDonationAmount += amount
                        }

                        // Calculate month total
                        if (isDateInCurrentMonth(donation.date.toString())) {
                            currentMonthDonationAmount += amount
                        }

                        // Calculate total donation
                        totalDonationAmount += amount
                    }

                    val weekTtl = findViewById<TextView>(R.id.weekTotalTV)
                    val monthTtl = findViewById<TextView>(R.id.monthTotalTV)
                    val total = findViewById<TextView>(R.id.totalDonationTV)

                    weekTtl.text = currentWeekDonationAmount.toString()
                    monthTtl.text = currentMonthDonationAmount.toString()
                    total.text = totalDonationAmount.toString()

                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }

                    userArrayList.clear() // Clear the previous data

                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            userArrayList.add(dc.document.toObject(Donation::class.java))
                        }
                    }

                    myAdapter.notifyDataSetChanged()
                }
            })

    }


    private fun isDateInCurrentMonth(date: String): Boolean {
        // Parse the date string and compare with the current month
        // You may need to adjust the date format according to your Firestore data
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate: Date = sdf.parse(date) ?: return false

        val donationCalendar: Calendar = Calendar.getInstance()
        donationCalendar.time = parsedDate
        val donationMonth: Int = donationCalendar.get(Calendar.MONTH)

        return donationMonth == currentMonth
    }


    private fun isDateInCurrentWeek(date: String): Boolean {
        // Parse the date string and compare with the current week
        // You may need to adjust the date format according to your Firestore data
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val parsedDate: Date = sdf.parse(date) ?: return false

        val donationCalendar: Calendar = Calendar.getInstance()
        donationCalendar.time = parsedDate
        val donationWeek: Int = donationCalendar.get(Calendar.WEEK_OF_YEAR)

        return donationWeek == currentWeek
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

    private fun showNoRecordsFoundAlert() {
        val message = "No donation records found."
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        // You can also show a dialog or any other UI component to inform the user.
    }


}