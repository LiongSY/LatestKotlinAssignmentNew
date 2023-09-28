package com.example.assignment.DonationModule
import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.Spinner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.DonationModule.Database.DBConnection
import com.example.assignment.DonationModule.DonationModuleAdapter.DonationDraftAdapter
import com.example.assignment.R
import com.example.assignment.UserManagement.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DonationDraftRecord : AppCompatActivity() {
    // Initialize the DBConnection instance
    private var dbConnection:DBHelper = DBHelper(this)

    private lateinit var donationRecyclerview: RecyclerView
    private lateinit var donationArrayList: ArrayList<Donation>
    private lateinit var myAdapter: DonationDraftAdapter
    private lateinit var sortSpinner: Spinner
    private lateinit var sortButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_draft_record)
        setSupportActionBar(findViewById(R.id.toolbar4))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        donationRecyclerview = findViewById(R.id.userList)
        donationRecyclerview.layoutManager = LinearLayoutManager(this)
        donationRecyclerview.setHasFixedSize(true)

        sortSpinner = findViewById(R.id.sort_spn)
        sortButton = findViewById(R.id.sort_btn)
        val sortOptions = arrayOf("Default", "Amount", "Date")
        val defaultPosition = sortOptions.indexOf("Default")
        sortButton.setOnClickListener {
            // Check the selected sort option and perform sorting accordingly
            when (sortSpinner.selectedItem.toString()) {
                "Amount" -> {
                    // Sort by amount
                    donationArrayList.sortBy { it.amount }
                }
                "Date" -> {
                    // Sort by date
                    donationArrayList.sortBy { it.date }
                }
                else -> {
                    sortSpinner.setSelection(defaultPosition)
                }
            }

            // Notify the adapter that the data has changed due to sorting
            myAdapter.notifyDataSetChanged()
        }

        donationArrayList = arrayListOf()



        myAdapter = DonationDraftAdapter(donationArrayList, donationRecyclerview, dbConnection)
        donationRecyclerview.adapter = myAdapter

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")

        loadDonationDraftsAsync(email)
    }

    private fun loadDonationDraftsAsync(email: String?) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val donations = retrieveDonationDraftsFromCursor(dbConnection.retrieveDonationDrafts(email))
                donationArrayList.clear()
                donationArrayList.addAll(donations)
                withContext(Dispatchers.Main) {
                    myAdapter.notifyDataSetChanged()
                }
            } catch (e: Exception) {
                Log.e("Database Error", "Error loading donation drafts: ${e.message}")
            }
        }
    }
    @SuppressLint("Range")
    private fun retrieveDonationDraftsFromCursor(cursor: Cursor): List<Donation> {
        val donations = mutableListOf<Donation>()
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(cursor.getColumnIndex(DBConnection.col1))
                val name = cursor.getString(cursor.getColumnIndex(DBConnection.col2))
                val date = cursor.getString(cursor.getColumnIndex(DBConnection.col3))
                val amount = cursor.getString(cursor.getColumnIndex(DBConnection.col4))
                val email = cursor.getString(cursor.getColumnIndex(DBConnection.col5))
                val method = cursor.getString(cursor.getColumnIndex(DBConnection.col6))
                val contact = cursor.getString(cursor.getColumnIndex(DBConnection.col7))

                // Create a Donation object and add it to the list
                val donation = Donation(id.toString(), name, date, amount, email, method, contact)
                donations.add(donation)
            } while (cursor.moveToNext())
        }
        cursor.close()
        return donations
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
