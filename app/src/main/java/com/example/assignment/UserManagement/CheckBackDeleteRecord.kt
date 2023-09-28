package com.example.assignment.UserManagement

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.assignment.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class CheckBackDeleteRecord : AppCompatActivity() {
    private lateinit var deletedRecyclerView: RecyclerView
    private lateinit var deletedEmailAdapter: EmailAdapter
    private lateinit var restoreButton: Button
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var deletedDataReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_check_back_delete_record)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        deletedRecyclerView = findViewById(R.id.deletedRecyclerView)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        deletedEmailAdapter = EmailAdapter(this, object : EmailAdapter.OnItemClickListener {
            override fun onItemClick(emailType: EmailType) {

            }
        })

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
        restoreButton = findViewById(R.id.restoreButton)

        deletedRecyclerView.adapter = deletedEmailAdapter
        deletedRecyclerView.layoutManager = LinearLayoutManager(this)

        deletedDataReference = FirebaseDatabase.getInstance().getReference("deleted_data")

        loadDeletedDataFromFirebase()
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val emailP = sharedPreferences.getString("email", "")
        val firstNameP = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")
        val phone = sharedPreferences.getString("phone", "")
        val password = sharedPreferences.getString("password", "")
        val confirmPass = sharedPreferences.getString("confirmPass", "")
        val position = sharedPreferences.getString("position", "")

        Log.d("SharedPreferences", "Email: $emailP")
        Log.d("SharedPreferences", "FirstName: $firstNameP")
        Log.d("SharedPreferences", "LastName: $lastName")
        Log.d("SharedPreferences", "Phone: $phone")
        Log.d("SharedPreferences", "Password: $password")
        Log.d("SharedPreferences", "ConfirmPass: $confirmPass")
        Log.d("SharedPreferences", "Position: $position")

        restoreButton.setOnClickListener {
            val selectedDeletedItems = deletedEmailAdapter.getSelectedItems()
            if (selectedDeletedItems.isNotEmpty()) {
                val emailDBHelper = DBHelper(this)
                val firebaseDatabase = FirebaseDatabase.getInstance()
                val emailTypeRef = firebaseDatabase.getReference("emailTypes") // Firebase 中的 emailType 节点

                val builder = AlertDialog.Builder(this)
                builder.setTitle("Restore Confirmation")
                builder.setMessage("Are you sure you want to restore selected items?")
                builder.setPositiveButton("Restore") { _, _ ->
                    for (emailType in selectedDeletedItems) {
                        val schoolName = emailType.schoolName ?: ""
                        val typeName = emailType.typeName ?: ""

                        // 在 SQLite 中插入数据并获取新 ID
                        val newId = emailDBHelper.insertEmailType(schoolName, typeName)

                        // 创建一个新的 EmailType 对象
                        val emailTypeData = EmailType(newId, schoolName, typeName)

                        // 使用新 ID 作为 Firebase 中的键，将数据添加到 emailType 节点
                        emailTypeRef.child(newId.toString()).setValue(emailTypeData)
                    }
                    deletedEmailAdapter.clearSelectedItems()

                    loadDeletedDataFromFirebase()

                    Toast.makeText(this, "Data restored", Toast.LENGTH_SHORT).show()
                    finish()
                }
                builder.setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }

                val dialog = builder.create()
                dialog.show()
            } else {
                Toast.makeText(this, "Select items to restore", Toast.LENGTH_SHORT).show()
            }
        }






    }

    private fun loadDeletedDataFromFirebase() {
        deletedDataReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val deletedEmailTypes = mutableListOf<EmailType>()
                for (snapshot in dataSnapshot.children) {
                    val emailType = snapshot.getValue(EmailType::class.java)
                    if (emailType != null) {
                        deletedEmailTypes.add(emailType)
                    }
                }
                deletedEmailAdapter.setData(deletedEmailTypes)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(this@CheckBackDeleteRecord, "Failed to load data from Firebase", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
