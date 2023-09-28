package com.example.assignment.UserManagement
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class addNewEmailType : AppCompatActivity() {
    private lateinit var schoolNameEditText: EditText
    private lateinit var emailTypeEditText: EditText
    private lateinit var addEmailTypeButton: Button
    private lateinit var dbHelper: DBHelper
    private lateinit var databaseReference: DatabaseReference
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_new_email_type)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        schoolNameEditText = findViewById(R.id.schoolNameEditText)
        emailTypeEditText = findViewById(R.id.emailTypeEditText)
        addEmailTypeButton = findViewById(R.id.addEmailTypeButton)
        dbHelper = DBHelper(this)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        databaseReference = FirebaseDatabase.getInstance().reference.child("emailTypes")
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
        addEmailTypeButton.setOnClickListener {
            val schoolName = schoolNameEditText.text.toString().trim()
            val emailType = emailTypeEditText.text.toString().trim()

            if (schoolName.isNotEmpty() && emailType.isNotEmpty()) {

                val id = dbHelper.insertEmailType(schoolName, emailType)
                val latestLocalId = dbHelper.getLatestEmailTypeId()
                val emailTypeData = EmailType(latestLocalId, schoolName, emailType)

                val emailTypeKey = databaseReference.push().key
                if (emailTypeKey != null) {
                    databaseReference.child(id.toString()).setValue(emailTypeData)
                        .addOnSuccessListener {
                            schoolNameEditText.text.clear()
                            emailTypeEditText.text.clear()
                            Toast.makeText(this, "Email Type added successfully", Toast.LENGTH_SHORT).show()
                            // Update the data in the addInstituition activity
                            val updateIntent = Intent(this, addInstituition::class.java)
                            startActivity(updateIntent)
                            finish()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to add Email Type", Toast.LENGTH_SHORT).show()
                        }
                }
            } else {
                Toast.makeText(this, "Please enter School Name and Email Type", Toast.LENGTH_SHORT).show()
            }
        }

    }
}
