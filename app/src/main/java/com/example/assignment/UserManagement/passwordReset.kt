package com.example.assignment.UserManagement

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.assignment.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class passwordReset : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var otp: EditText
    private lateinit var newPasswordInput: EditText
    private lateinit var confirmPasswordInput: EditText
    private lateinit var requestOTPButton: Button
    private lateinit var submitButton: Button
    private lateinit var dbh: DBHelper

    private val auth = FirebaseAuth.getInstance()

    // Notification channel ID (Must be unique)
    private val CHANNEL_ID = "OTP_Channel"

    // Store the generated OTP
    private var generatedOTP: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_password_reset)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        email = findViewById(R.id.emailInput)
        otp = findViewById(R.id.otpInput)
        newPasswordInput = findViewById(R.id.newPasswordInput)
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput)
        requestOTPButton = findViewById(R.id.requestOTPButton)
        submitButton = findViewById(R.id.submitButton)
        dbh = DBHelper(this)

        // Initially, hide the OTP and password fields
        otp.visibility = View.GONE
        newPasswordInput.visibility = View.GONE
        confirmPasswordInput.visibility = View.GONE
        submitButton.visibility = View.GONE

        createNotificationChannel()

        requestOTPButton.setOnClickListener {
            val inputEmail = email.text.toString()

            if (dbh.isEmailExists(inputEmail) == true) {
                // Generate the OTP
                generatedOTP = sendOTP()

                otp.visibility = View.VISIBLE
                requestOTPButton.visibility = View.GONE

                newPasswordInput.visibility = View.VISIBLE
                confirmPasswordInput.visibility = View.VISIBLE
                submitButton.visibility = View.VISIBLE
                resetPassword(inputEmail)
            } else {
                Toast.makeText(this, "Email does not exist.", Toast.LENGTH_SHORT).show()
            }
        }

// Handle Submit button click
        submitButton.setOnClickListener {
            val inputEmail = email.text.toString()
            val inputOTP = otp.text.toString()
            val newPassword = newPasswordInput.text.toString()
            val confirmPassword = confirmPasswordInput.text.toString()

            // Check if the email exists in the database
            if (dbh.isEmailExists(inputEmail) == true) {
                // Validate OTP
                if (inputOTP == generatedOTP.toString()) {
                    if (newPassword == confirmPassword) {

                        // Update the password in the database
                        updatePasswordInFirebaseDatabase(inputEmail, newPassword)
                        if (dbh.updatePassword(inputEmail, newPassword, confirmPassword)) {
                            Toast.makeText(
                                this,
                                "Password updated successfully.",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Finish the activity or navigate to the login screen
                            finish() // Finish the password reset activity
                        } else {
                            Toast.makeText(this, "Password update failed.", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(this, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Invalid OTP.", Toast.LENGTH_SHORT).show()
                    generatedOTP = sendOTP()

                    // Show the OTP field
                    otp.visibility = View.VISIBLE
                }
            } else {
                Toast.makeText(this, "Email does not exist.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun sendOTP(): Int {
        // Generate a random 6-digit OTP
        val generatedOTP = generateRandomSixDigitNumber()

        // Simulate sending the OTP as a notification
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("OTP Received")
            .setContentText("Your OTP: $generatedOTP")
            .setPriority(NotificationCompat.PRIORITY_HIGH)

        // Generate a unique notification ID (e.g., using a timestamp)
        val notificationId = System.currentTimeMillis().toInt()

        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }

        return generatedOTP
    }

    // Create a notification channel (for Android 8.0 and above)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "OTP Channel"
            val descriptionText = "Channel for OTP notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Placeholder for OTP validation logic
    private fun isValidOTP(otp: String): Boolean {
        return otp.length == 6 // Replace with your OTP validation
    }

    // Generate a random 6-digit number
    private fun generateRandomSixDigitNumber(): Int {
        val random = java.util.Random()
        return 100000 + random.nextInt(900000)
    }

    private fun resetPassword(email: String) {
        val auth = FirebaseAuth.getInstance()

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this@passwordReset,
                        "Password reset email sent successfully.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        this@passwordReset,
                        "Failed to send password reset email.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }


    private fun updatePasswordInFirebaseDatabase(email: String, newPassword: String) {
        val firebaseDatabase = FirebaseDatabase.getInstance()
        val usersReference = firebaseDatabase.reference.child("users")

        usersReference.orderByChild("email").equalTo(email)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // 在这里处理数据变化事件
                    for (userSnapshot in dataSnapshot.children) {
                        val userKey = userSnapshot.key
                        if (userKey != null) {
                            usersReference.child(userKey).child("password").setValue(newPassword)
                            usersReference.child(userKey).child("confirmPass").setValue(newPassword)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this@passwordReset,
                                        "Password updated successfully.",
                                        Toast.LENGTH_SHORT
                                    ).show()

                                }
                                .addOnFailureListener {
                                    Toast.makeText(
                                        this@passwordReset,
                                        "Password update failed.",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Toast.makeText(
                        this@passwordReset,
                        "Database operation cancelled.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


}

