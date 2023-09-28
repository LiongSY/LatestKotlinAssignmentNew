

package com.example.assignment.UserManagement

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.app.AlertDialog
import android.util.Log
import android.view.WindowManager
import com.google.android.material.bottomnavigation.BottomNavigationView


class addInstituition : AppCompatActivity(), EmailAdapter.OnItemClickListener {
    private lateinit var typeNameEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var deleteButton: Button
    private lateinit var viewButton : Button
    private lateinit var emailTypeRecyclerView: RecyclerView
    private lateinit var dbHelper: DBHelper
    private lateinit var adapter: EmailAdapter
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val EDIT_EMAIL_REQUEST_CODE = 123
    private lateinit var deletedDataReference: DatabaseReference

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_instituition)
        FirebaseApp.initializeApp(this)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        database = FirebaseDatabase.getInstance()
        storage = FirebaseStorage.getInstance()
        deletedDataReference = database.getReference("deleted_data")
        typeNameEditText = findViewById(R.id.typeNameEditText)
        searchButton = findViewById(R.id.searchTypeButton)
        deleteButton = findViewById(R.id.deleteButton)
        emailTypeRecyclerView = findViewById(R.id.recyclerView)
        viewButton = findViewById(R.id.viewRecordButton)
        dbHelper = DBHelper(this)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Initialize the RecyclerView with email types
        adapter = EmailAdapter(this, this) // Pass 'this' as the click listener
        emailTypeRecyclerView.adapter = adapter
        emailTypeRecyclerView.layoutManager = LinearLayoutManager(this)

        // Update the RecyclerView with data
        updateEmailTypeRecyclerView()

        typeNameEditText.clearFocus()

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

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

        searchButton.setOnClickListener {
            val typeName = typeNameEditText.text.toString().trim()
            if (typeName.isNotEmpty()) {
                val cursor = dbHelper.searchEmailTypes(typeName)
                val emailTypes = mutableListOf<EmailType>()

                cursor?.use {
                    while (cursor.moveToNext()) {
                        val id = cursor.getLong(cursor.getColumnIndex("_id"))
                        val school = cursor.getString(cursor.getColumnIndex("schoolName"))
                        val name = cursor.getString(cursor.getColumnIndex("typeName"))
                        emailTypes.add(EmailType(id, school, name))
                    }
                }

                adapter.setData(emailTypes)
            }
        }

        viewButton.setOnClickListener{
            val intent = Intent(this, CheckBackDeleteRecord::class.java)
            startActivity(intent)
        }

        deleteButton.setOnClickListener {
            val selectedEmailTypes = adapter.getSelectedItems()
            if (selectedEmailTypes.isNotEmpty()) {
                showDeleteConfirmationDialog(selectedEmailTypes)
            } else {
                Toast.makeText(this, "Select items to delete", Toast.LENGTH_SHORT).show()
            }
        }




        // FloatingActionButton (Add Button) Click Listener
        val fabAdd = findViewById<FloatingActionButton>(R.id.fabAdd)
        fabAdd.setOnClickListener {
            val intent = Intent(this, addNewEmailType::class.java)
            startActivity(intent)
        }
    }

    private fun showDeleteConfirmationDialog(selectedEmailTypes: List<EmailType>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Confirmation")

        val message = buildDeleteConfirmationMessage(selectedEmailTypes)
        builder.setMessage(message)

        builder.setPositiveButton("Delete") { _, _ ->
            deleteSelectedEmailTypes(selectedEmailTypes)
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()
    }

    private fun buildDeleteConfirmationMessage(selectedEmailTypes: List<EmailType>): String {
        val emailTypeNames = selectedEmailTypes.joinToString { it.typeName }
        return "Are you sure you want to delete the following items?"
    }

    private fun deleteSelectedEmailTypes(selectedEmailTypes: List<EmailType>) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("emailTypes")
        var anyDeletionFailed = false
        for (emailType in selectedEmailTypes) {
             val firebaseKey = emailType.id.toString()

            // 使用 Firebase 自动生成的键来删除数据
            databaseReference.child(firebaseKey).removeValue()
            val deletedRows = dbHelper.deleteEmailType(emailType.id)
            if (deletedRows > 0) {

                storeDeletedDataToFirebase(emailType)
            } else {
                anyDeletionFailed = true
                Log.e("DeleteEmailTypes", "Local database deletion failed for ${emailType.typeName}")
            }
        }

        // Clear the selected items
        adapter.clearSelectedItems()

        if (anyDeletionFailed) {
            Toast.makeText(this, "Some items failed to delete", Toast.LENGTH_SHORT).show()
        } else {
            updateEmailTypeRecyclerView()
            Toast.makeText(this, "Items deleted successfully", Toast.LENGTH_SHORT).show()
        }
    }





    private fun updateEmailTypeRecyclerView() {
        val cursor = dbHelper.getAllEmailTypes()
        val emailTypes = mutableListOf<EmailType>()

        cursor?.use {
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndex("_id"))
                val school = cursor.getString(cursor.getColumnIndex("schoolName"))
                val name = cursor.getString(cursor.getColumnIndex("typeName"))
                emailTypes.add(EmailType(id, school, name))
            }
        }

        adapter.setData(emailTypes)
    }

    private fun deleteEmailType(emailType: EmailType) {
        val deletedRows = dbHelper.deleteEmailType(emailType.id)
        if (deletedRows > 0) {
            storeDeletedDataToFirebase(emailType) // 存储到 Firebase
            Toast.makeText(this, "Deleted ${emailType.typeName}", Toast.LENGTH_SHORT).show()

            updateEmailTypeRecyclerView()
        } else {
            Toast.makeText(this, "Failed to delete ${emailType.typeName}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun storeDeletedDataToFirebase(emailType: EmailType) {
        val deletedDataReference = FirebaseDatabase.getInstance().getReference("deleted_data")
        val key = deletedDataReference.push().key
        if (key != null) {
            deletedDataReference.child(key).setValue(emailType)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data deleted and stored in Firebase", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Failed to store deleted data in Firebase: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }


    // Implement the onItemClick method to handle item click events
    override fun onItemClick(emailType: EmailType) {
        val intent = Intent(this, emailDetails::class.java)
        intent.putExtra("emailType", emailType)
        startActivityForResult(intent, EDIT_EMAIL_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == EDIT_EMAIL_REQUEST_CODE && resultCode == RESULT_OK) {
            updateEmailTypeRecyclerView()
        }
    }

}
