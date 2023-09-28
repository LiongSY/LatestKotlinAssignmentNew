package com.example.assignment.DonationModule

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.LiveFolders.INTENT
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.example.assignment.R

class DonationCompleted : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_completed)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val donatedAmount = intent.getSerializableExtra("donationData") as? HashMap<String, String>

        // Check if the donation data is not null
        donatedAmount?.let {
            val amount = it["amount"]


            val amountText = findViewById<TextView>(R.id.amount)
            amountText.text = amount

        }

        val viewRecordBtn:Button = findViewById(R.id.view_record)

        viewRecordBtn.setOnClickListener{

            val i=Intent(this, DonationRecord::class.java)
            startActivity(i)
        }

        val home = findViewById<ImageButton>(R.id.homeBtn)
        home.setOnClickListener{
            val intent = Intent(this,DonationPage::class.java)
            startActivity(intent)
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