package com.example.assignment.UserManagement

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModelProvider
import com.example.assignment.R
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.ByteArrayOutputStream

class AccountDetails : AppCompatActivity() {

    // Define your UI elements
    private lateinit var profileImageView: ImageView
    private lateinit var uploadPictureButton: Button
    private lateinit var firstNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var phoneNumEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var conPasswordEditText: EditText
    private lateinit var positionEditText: EditText // Added for Position
    private lateinit var saveButton: Button

    private var userEmail: String = ""
    private lateinit var dbHelper: DBHelper
    private lateinit var firebaseAuth: FirebaseAuth
    // Constants for image selection
    private val PICK_IMAGE = 1
    private val MAX_IMAGE_SIZE_BYTES = 2 * 1024 * 1024 // 2MB (adjust as needed)

    private lateinit var databaseReference: DatabaseReference
    private lateinit var storageReference: StorageReference

    // Flags to track edits
    private var isImageEdited = false
    private var isInformationEdited = false
    private lateinit var accountDetailsViewModel: AccountDetailsViewModel
    private val ACCOUNT_DETAILS_REQUEST_CODE = 1001
    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account_details) // Set content view first
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        // Initialize UI elements
        profileImageView = findViewById(R.id.profileImageView)
        uploadPictureButton = findViewById(R.id.uploadPictureButton)
        firstNameEditText = findViewById(R.id.firstNameEditText)
        lastNameEditText = findViewById(R.id.lastNameEditText)
        phoneNumEditText = findViewById(R.id.phoneNumEditText)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        conPasswordEditText = findViewById(R.id.conPasswordEditText)
        positionEditText = findViewById(R.id.positionEditText) // Initialize Position EditText
        saveButton = findViewById(R.id.saveButton)

        databaseReference = FirebaseDatabase.getInstance().reference
        storageReference = FirebaseStorage.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
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
        userEmail = intent.getStringExtra("userEmail") ?: ""


        dbHelper = DBHelper(this)
        accountDetailsViewModel = ViewModelProvider(this).get(AccountDetailsViewModel::class.java)
        val cursor: Cursor? = dbHelper.retrieveOneRecord(emailP.toString())

        if (cursor != null && cursor.moveToFirst()) {
            // Extract user details from the cursor
            val firstName = cursor.getString(cursor.getColumnIndex("firstName"))
            val lastName = cursor.getString(cursor.getColumnIndex("lastName"))
            val phoneNum = cursor.getString(cursor.getColumnIndex("phoneNum"))
            val email = cursor.getString(cursor.getColumnIndex("email"))
            val password = cursor.getString(cursor.getColumnIndex("password"))
            val conPassword = cursor.getString(cursor.getColumnIndex("confirmPass"))
            val position = cursor.getString(cursor.getColumnIndex("position")) // Added for Position

            // Populate the EditText fields with retrieved data
            firstNameEditText.setText(firstName)
            lastNameEditText.setText(lastName)
            phoneNumEditText.setText(phoneNum)
            emailEditText.setText(email)
            passwordEditText.setText(password)
            conPasswordEditText.setText(conPassword)
            positionEditText.setText(position) // Set Position EditText

            // Load and display the profile picture if available
            val imageByteArray = cursor.getBlob(cursor.getColumnIndex("image"))
            if (imageByteArray != null) {
                val profileImageBitmap = BitmapFactory.decodeByteArray(
                    imageByteArray,
                    0,
                    imageByteArray.size
                )
                profileImageView.setImageBitmap(profileImageBitmap)
            } else {
                // If there's no profile picture in the database, you can set a default image here
                profileImageView.setImageResource(R.drawable.profile)
            }

            // Close the cursor
            cursor.close()
        } else {
            // Handle the case where no data was found for the user's email
            Toast.makeText(this, "No user data found for email: $userEmail", Toast.LENGTH_SHORT)
                .show()
        }

        // Set an onClickListener for the "Upload Picture" button
        uploadPictureButton.setOnClickListener {
            // Implement image selection functionality to open the image gallery
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE)
            isImageEdited = true // Image has been edited
        }


        // Set an onClickListener for the "Save" button
        saveButton.setOnClickListener {
            // Implement data saving functionality here
            // Retrieve the data from EditText fields and update the database
            val updatedFirstName = firstNameEditText.text.toString()
            val updatedLastName = lastNameEditText.text.toString()
            val updatedPhoneNum = phoneNumEditText.text.toString()
            val updatedEmail = emailEditText.text.toString()
            val updatedPassword = passwordEditText.text.toString()
            val updatedConPassword = conPasswordEditText.text.toString()
            val updatedPosition = positionEditText.text.toString() // Retrieve Position

            // Check if at least one of image or information is edited
            if (!isImageEdited && !isInformationEdited) {
                // No edits were made, display an error message
                Toast.makeText(
                    this,
                    "No changes were made",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            // Check if password and confirm password match
            if (updatedPassword == updatedConPassword) {
                // Passwords match, update user data in the database using DBHelper
                val updatedImageByteArray =
                    if (isImageEdited) convertBitmapToByteArray(profileImageView.drawable.toBitmap()) else null

                if (updatedImageByteArray != null && updatedImageByteArray.size > MAX_IMAGE_SIZE_BYTES) {
                    // Check if the image size exceeds the maximum allowed
                    Toast.makeText(
                        this,
                        "Image size exceeds the maximum allowed size",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    uploadProfileImageToStorage(updatedImageByteArray!!, updatedEmail)
                    val isUpdated = dbHelper.updateUserData(
                        updatedEmail,
                        updatedFirstName,
                        updatedLastName,
                        updatedPhoneNum,
                        updatedPassword,
                        updatedConPassword,
                        updatedImageByteArray,
                        updatedPosition // Pass the position here
                    )


                    if (isUpdated) {
                        updateFirebaseUserData(
                            updatedEmail,
                            updatedFirstName,
                            updatedLastName,
                            updatedPhoneNum,
                            updatedPassword,
                            updatedPosition
                        )
                        val resultIntent = Intent()
                        resultIntent.putExtra("updatedEmail", updatedEmail)
                        resultIntent.putExtra("updatedFirstName", updatedFirstName)
                        resultIntent.putExtra("updatedLastName", updatedLastName)
                        resultIntent.putExtra("updatedImageByteArray", updatedImageByteArray)

                        // 添加其他需要传递的数据，例如密码
                        resultIntent.putExtra("updatedPassword", updatedPassword)

                        setResult(RESULT_OK, resultIntent)

                        Toast.makeText(this, "User data updated successfully", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Failed to update user data", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            } else {
                // Passwords do not match, display an error message
                Toast.makeText(
                    this,
                    "Password and Confirm Password do not match",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        accountDetailsViewModel.getUserDataLiveData().observe(this, { userData ->
            // Update UI with new data
            updateUI(userData)
        })

    }



    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
    private fun updateUI(userData: UserData) {
        // Update UI elements with LiveData
        // Example:
        firstNameEditText.setText(userData.firstName)
        lastNameEditText.setText(userData.lastName)
        emailEditText.setText(userData.email)
        phoneNumEditText.setText(userData.phoneNum)
        passwordEditText.setText(userData.password)
        conPasswordEditText.setText(userData.confirmPass)
        positionEditText.setText(userData.position)

        // Update profile picture if available
        if (userData.imageByteArray != null) {
            val profileImageBitmap = BitmapFactory.decodeByteArray(
                userData.imageByteArray,
                0,
                userData.imageByteArray.size
            )
            profileImageView.setImageBitmap(profileImageBitmap)
        } else {
            // If there's no profile picture in the database, you can set a default image here
            profileImageView.setImageResource(R.drawable.profile)
        }
    }


    // Convert Bitmap to ByteArray
    private fun convertBitmapToByteArray(bitmap: Bitmap): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    // Handle image selection result from the gallery
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            // Inside onActivityResult when an image is selected
            if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
                // Get the selected image URI from the intent
                val imageUri = data.data

                // Ensure the imageUri is not null before loading and resizing the image
                if (imageUri != null) {
                    // Store the selected image URI in SharedPreferences
                    val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("imageUri", imageUri.toString())
                    editor.apply()

                    // Load and set the resized image to the ImageView
                    val imageBitmap = loadAndResizeImage(imageUri)
                    profileImageView.setImageBitmap(imageBitmap)
                } else {
                    // Handle the case where the imageUri is null
                    Toast.makeText(this, "Failed to load the selected image", Toast.LENGTH_SHORT)
                        .show()
                }
                isImageEdited = true // Image has been edited
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "An error occurred: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadAndResizeImage(imageUri: Uri): Bitmap {
        try {
            // Load the original image from the URI
            val inputStream = contentResolver.openInputStream(imageUri)
            val originalBitmap = BitmapFactory.decodeStream(inputStream)

            // Calculate the image dimensions while maintaining aspect ratio
            val maxWidth = 800 // Set your desired maximum width here
            val maxHeight = 800 // Set your desired maximum height here
            val scaleFactor = calculateScaleFactor(originalBitmap, maxWidth, maxHeight)

            // Resize the image using the calculated dimensions
            val resizedBitmap = Bitmap.createScaledBitmap(
                originalBitmap,
                (originalBitmap.width * scaleFactor).toInt(),
                (originalBitmap.height * scaleFactor).toInt(),
                true
            )

            // Close the input stream
            inputStream?.close()

            return resizedBitmap
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "An error occurred while loading the image: ${e.message}", Toast.LENGTH_SHORT).show()
            // Return a default bitmap or handle the error as needed
            return BitmapFactory.decodeResource(resources, R.drawable.profile)
        }
    }

    // Calculate the scale factor to resize the image
    private fun calculateScaleFactor(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Float {
        val width = bitmap.width
        val height = bitmap.height

        if (width <= maxWidth && height <= maxHeight) {
            // No need to resize
            return 1.0f
        }

        val widthRatio = maxWidth.toFloat() / width.toFloat()
        val heightRatio = maxHeight.toFloat() / height.toFloat()

        return if (widthRatio < heightRatio) {
            widthRatio
        } else {
            heightRatio
        }
    }

    private fun updateFirebaseUserData(
        email: String,
        firstName: String,
        lastName: String,
        phoneNum: String,
        password: String,
        position: String
    ) {
        val currentUser = firebaseAuth.currentUser
        currentUser?.let { user ->
            val updatedUserData = UserData(
                email,
                firstName,
                lastName,
                phoneNum,
                password,
                conPasswordEditText.text.toString(), // Assuming this is the confirm password
                null, // You can update the profile picture here if needed
                position
            )

            val userDataMap = userDataToMap(updatedUserData)

            // 更新 Firebase 上的用户数据
            databaseReference.child("users").child(user.uid).setValue(userDataMap)
                .addOnSuccessListener {
                    Log.d("Firebase", "User data updated on Firebase")
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Error updating user data on Firebase: ${e.message}", e)
                }
        }
    }
    private fun userDataToMap(userData: UserData): Map<String, Any?> {
        val map = mutableMapOf<String, Any?>()
        map["email"] = userData.email
        map["firstName"] = userData.firstName
        map["lastName"] = userData.lastName
        map["phoneNum"] = userData.phoneNum
        map["password"] = userData.password
        map["confirmPass"] = userData.confirmPass
        map["image"] = userData.imageByteArray
        map["position"] = userData.position
        return map
    }



    private fun uploadProfileImageToStorage(imageByteArray: ByteArray, email: String) {
        val storageReference = FirebaseStorage.getInstance().reference.child("profile_pictures/$email.jpg")

        // 开始上传
        storageReference.putBytes(imageByteArray)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl
                    .addOnSuccessListener { uri ->
                        saveImageUrlToDatabase(email, uri.toString())
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase Storage", "Error getting image URL: ${e.message}", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("Firebase Storage", "Error uploading image: ${e.message}", e)
            }
    }


    private fun saveImageUrlToDatabase(email: String, imageUrl: String) {
        val databaseReference = FirebaseDatabase.getInstance().reference
        databaseReference.child("users").child(email.replace(".", ",")).child("imageURL").setValue(imageUrl)
            .addOnSuccessListener {
                Log.d("Firebase Realtime DB", "Image URL saved to database")
            }
            .addOnFailureListener { e ->
                Log.e("Firebase Realtime DB", "Error saving image URL to database: ${e.message}", e)
            }
    }



}
