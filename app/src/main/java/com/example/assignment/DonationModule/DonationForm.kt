package com.example.assignment.DonationModule


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.example.assignment.R
import java.util.regex.Pattern

class DonationForm : AppCompatActivity() {


    private lateinit var name: EditText
    private lateinit var contact: EditText
    private lateinit var amount: EditText
    private lateinit var donateBtn: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       setContentView(R.layout.activity_donation_form)
        setSupportActionBar(findViewById(R.id.formToolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        name = findViewById(R.id.name_field)
        contact = findViewById(R.id.contact_field)
        amount = findViewById(R.id.amount_field)

        donateBtn = findViewById<Button>(R.id.donate_button)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val password = sharedPreferences.getString("password", "")

       donateBtn.setOnClickListener(View.OnClickListener {
            val sName = name.text.toString().trim()
            //after done change to get email from session
            val sEmail = email
            val sContact = contact.text.toString().trim()
            val sAmount = amount.text.toString().trim()

           if (sName.isEmpty()) {
               Toast.makeText(this, "Please Enter Your Name", Toast.LENGTH_SHORT).show()
           } else if (sContact.isEmpty()) {
               Toast.makeText(this, "Please Enter Your Contact No.", Toast.LENGTH_SHORT).show()
           }else if( !validateContactFormat(sContact)){
               Toast.makeText(this, "Invalid contact number format", Toast.LENGTH_SHORT).show()
           }
           else if (sAmount.isEmpty()) {
               Toast.makeText(this, "Please Enter Your Amount", Toast.LENGTH_SHORT).show()
           } else {
               val donationData = hashMapOf(
                   "name" to sName,
                   "email" to sEmail,
                   "contact" to sContact,
                   "amount" to sAmount
               )

               val intent = Intent(this, DonationPayment::class.java)
               intent.putExtra("donationData", donationData)
               startActivity(intent)
           }
       })
    }

    private fun validateContactFormat(contact: String): Boolean {
        // Define a regular expression pattern for a 10 or 11 digit phone number
        val phonePattern = "^(01\\d{8,9})$"

        // Use the Pattern class to compile the pattern
        val pattern = Pattern.compile(phonePattern)

        // Use the Matcher class to match the input string against the pattern
        val matcher = pattern.matcher(contact)

        // Return true if the input matches the pattern, indicating a valid format
        return matcher.matches()
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