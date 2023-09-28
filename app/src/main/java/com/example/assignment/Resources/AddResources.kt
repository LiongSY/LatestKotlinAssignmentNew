package com.example.assignment.Resources

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.example.assignment.DonationModule.Community.Community
import com.example.assignment.DonationModule.Donation
import com.example.assignment.DonationModule.DonationPage
import com.example.assignment.Homepage
import com.example.assignment.R
import com.example.assignment.databinding.ActivityAddResourcesBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random

class AddResources : AppCompatActivity() {

    private lateinit var etTitle: EditText
    private lateinit var etDesc: EditText
    private lateinit var btnUpload: Button
    private lateinit var btnSaveData: Button
    private lateinit var fileUpload: TextView //textview to display the file path that i selected

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dbRef: DatabaseReference
    private lateinit var storageRef: StorageReference

    private val PICK_FILE_REQUEST_CODE = 123
    private var selectedFileUri: Uri? = null //store the selected file URI

    private lateinit var addResourceBinding: ActivityAddResourcesBinding
    private lateinit var SqlHelper: SQLiteHelper



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addResourceBinding = DataBindingUtil.setContentView(this, R.layout.activity_add_resources)
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("lastName", "")
        val email = sharedPreferences.getString("email", "")

        etTitle = addResourceBinding.titleInput
        etDesc = addResourceBinding.inputDescription
        btnUpload = addResourceBinding.uploadBtn
        btnSaveData = addResourceBinding.addBtn
        fileUpload = addResourceBinding.inputFilePath

        firebaseAuth = FirebaseAuth.getInstance()
        storageRef = FirebaseStorage.getInstance().reference
        dbRef = FirebaseDatabase.getInstance().getReference("Resources")

        btnSaveData.setOnClickListener {
            if(isNetworkAvailable()){
                storeInFirebase(name!!,email!!)
                storeInDatabase(name!!,email!!)
            }else{
                Toast.makeText(
                    this,
                    "Please ensure that you have internet connection.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


        btnUpload.setOnClickListener {
            openFilePicker()
        }
        SqlHelper = SQLiteHelper(this)

    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf" //specific the type for PDF files
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            selectedFileUri = data.data

            //display the selected file URI in the Textview
            fileUpload.text = selectedFileUri.toString()
        }
    }

    private fun storeInFirebase(name:String,email:String) {
        //getting values
        val resTitle = etTitle.text.toString()
        val resDesc = etDesc.text.toString()

        if (selectedFileUri != null) {
//            //Upload the selected file to Firebase Storage
//            val fileRef = storageRef.child("uploads/${selectedFileUri!!.lastPathSegment}")
//            val uploadTask: UploadTask = fileRef.putFile(selectedFileUri!!)
//
//            uploadTask.addOnSuccessListener { taskSnapshot ->
//                // File uploaded successfully, get the download URL
//                fileRef.downloadUrl.addOnSuccessListener { downloadUri ->
//                    //create a hashmap to store resource data

            //push the data to the realtime database
            val resourceKey = dbRef.push().key
            val resource = ResourceModel(resourceKey!!,name,email,resTitle,selectedFileUri.toString(),resDesc)

            if (resourceKey != null) {
                dbRef.child(resourceKey).setValue(resource)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                this,
                                "Resource saved successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            finish()

                            etTitle.text.clear()
                            etDesc.text.clear()
                            selectedFileUri = null // Reset the selected file URI

                        } else {
                            Toast.makeText(this, "Resource save failed", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }

        }
    }
    fun generateSimpleRandomId(length: Int): String {
        val characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_!@#$%^&*()[]{}|;:,.<>?".toCharArray()
        val random = Random()
        val sb = StringBuilder()

        for (i in 0 until length) {
            val randomChar = characters[random.nextInt(characters.size)]
            sb.append(randomChar)
        }

        return sb.toString()
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
        }

    private fun storeInDatabase(name:String,email: String) {
        val SQLiteHelper = SQLiteHelper(this)
       // add shared preferences name
        val resTitle = etTitle.text.toString()
        val resDesc = etDesc.text.toString()
        val resourceId = generateSimpleRandomId(8)

        val resource = ResourceModel(resourceId,name,email,resTitle,selectedFileUri.toString(),resDesc)
        val status = SQLiteHelper.insertResources(resource)
        //check insert success or not success
        if(status){
            Toast.makeText(this, "Resource Added...", Toast.LENGTH_SHORT).show()
            finish()
        }else{
            Toast.makeText(this, "Record not saved",Toast.LENGTH_SHORT).show()
        }

    }



}