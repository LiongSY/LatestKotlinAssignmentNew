package com.example.assignment.UserManagement

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class emailDetails : AppCompatActivity() {

    private lateinit var emailType: EmailType
    private lateinit var schoolNameEditText: EditText
    private lateinit var emailTypeEditText: EditText
    private lateinit var saveButton: Button
    private lateinit var dbHelper: DBHelper
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_email_details)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Initialize views
        schoolNameEditText = findViewById(R.id.schoolNameEditText)
        emailTypeEditText = findViewById(R.id.emailTypeEditText)
        saveButton = findViewById(R.id.saveButton)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Initialize the database helper
        dbHelper = DBHelper(this)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_dashboard -> {
                    val homeIntent = Intent(this, adminPanel::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.navigation_user -> {
                    val dashboardIntent = Intent(this, viewAllAcc::class.java)
                    startActivity(dashboardIntent)
                    true
                }
                R.id.navigation_institution -> {
                    val notificationsIntent = Intent(this, addInstituition::class.java)
                    startActivity(notificationsIntent)
                    true
                }
                else -> false
            }
        }

        // Get the EmailType object from the intent
        emailType = intent.getParcelableExtra("emailType")!!
        val emailTypeId = emailType.id
        databaseReference = FirebaseDatabase.getInstance().reference.child("emailTypes").child(emailTypeId.toString())
        // Populate the EditText fields with the EmailType data
        schoolNameEditText.setText(emailType.schoolName)
        emailTypeEditText.setText(emailType.typeName)

        saveButton.setOnClickListener {
            // Update the EmailType object with the edited data
            emailType.schoolName = schoolNameEditText.text.toString()
            emailType.typeName = emailTypeEditText.text.toString()

            val emailTypeKey = emailType.id.toString()

            databaseReference.setValue(emailType)
                .addOnSuccessListener {
                    setResult(RESULT_OK)
                }
                .addOnFailureListener { e ->
                    // 数据更新失败
                    Log.e("EmailDetailsActivity", "Firebase update failed: ${e.message}", e)
                    Toast.makeText(this, "Firebase update failed", Toast.LENGTH_LONG).show()
                }

            val updatedRows = dbHelper.updateEmailType(emailType)

            if (updatedRows > 0) {
                setResult(RESULT_OK)
                finish()
            } else {
                Log.e("EmailDetailsActivity", "Database update failed")
                Toast.makeText(this, "Database update failed", Toast.LENGTH_LONG).show()
            }
        }


    }
    fun EmailType.toMap(): Map<String, Any> {
        val map = HashMap<String, Any>()
        map["id"] = id
        map["schoolName"] = schoolName
        map["typeName"] = typeName
        return map
    }

}
