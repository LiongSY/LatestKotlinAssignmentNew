package com.example.assignment.UserManagement



import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.assignment.MainActivity
import com.example.assignment.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
class adminPanel : AppCompatActivity() {
    private lateinit var  logoutBtn : Button
    private lateinit var addInstitutionbtn: Button
    private lateinit var userInfoLayout: FrameLayout
    private lateinit var userImageView: ImageView
    private lateinit var usernameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var accountDetailsLauncher: ActivityResultLauncher<Intent>
    private lateinit var deleteBtn : Button

    private lateinit var adminPanelViewModel: AdminPanelViewModel
    private val ACCOUNT_DETAILS_REQUEST_CODE = 1


    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var dataReference: DatabaseReference
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_panel)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        adminPanelViewModel = ViewModelProvider(this).get(AdminPanelViewModel::class.java)
        // Initialize UI elements
        firebaseAuth = FirebaseAuth.getInstance()
        dataReference = FirebaseDatabase.getInstance().reference
        addInstitutionbtn = findViewById(R.id.addInstitutionButton)
        userInfoLayout = findViewById(R.id.userInfoLayout)
        userImageView = findViewById(R.id.userImageView)
        usernameTextView = findViewById(R.id.usernameTextView)
        emailTextView = findViewById(R.id.emailTextView)
        logoutBtn = findViewById(R.id.logoutButton)
//        deleteBtn = findViewById(R.id.deleteAccButton)
        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        adminPanelViewModel.getUserDataLiveData().observe(this, { userData ->

            updateUI(userData)
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
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)

        val email = sharedPreferences.getString("email", "")
        val firstName = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")
        val phone = sharedPreferences.getString("phone", "")
        val password = sharedPreferences.getString("password", "")
        val confirmPass = sharedPreferences.getString("confirmPass", "")
        val position = sharedPreferences.getString("position", "")




        val userEmail = intent.getStringExtra("userEmail")
        val userPassword = intent.getStringExtra("userPassword")

        val viewAllUsersButton: Button = findViewById(R.id.viewAllUsersButton)

        val userData = fetchUserData(email,password)



        // Set the user's email
        emailTextView.text = email


        if (userData != null) {
            // Set the user's name
            val fullName = "${userData.firstName} ${userData.lastName}"
            usernameTextView.text = fullName

            // Check and set the user's image
            val imageByteArray = userData.imageByteArray
            if (imageByteArray != null) {
                val imageBitmap =
                    BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)
                userImageView.setImageBitmap(imageBitmap)
            }
        }
        logoutBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Logout Confirmation")
            builder.setMessage("Are you sure you want to logout?")

            builder.setPositiveButton("Logout") { _, _ ->

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            }

            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            val dialog = builder.create()
            dialog.show()
        }


        // Click listeners for buttons
        addInstitutionbtn.setOnClickListener {
            val intent = Intent(this, addInstituition::class.java)
            startActivity(intent)
        }
//        deleteBtn.setOnClickListener {
//            showDeleteConfirmationDialog()
//        }

        viewAllUsersButton.setOnClickListener {
            val intent = Intent(this, viewAllAcc::class.java)
            startActivity(intent)
        }
        accountDetailsLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                val data: Intent? = result.data
                if (data != null) {
                    val updatedEmail = data.getStringExtra("updatedEmail")
                    val updatedPassword = data.getStringExtra("updatedPassword")

                    val updatedUserData = fetchUserData(updatedEmail, updatedPassword)

                    if (updatedUserData != null) {

                        emailTextView.text = updatedEmail
                        usernameTextView.text = "${updatedUserData.firstName} ${updatedUserData.lastName}"

                        if (updatedUserData.imageByteArray != null) {
                            val updatedImageBitmap = BitmapFactory.decodeByteArray(
                                updatedUserData.imageByteArray,
                                0,
                                updatedUserData.imageByteArray.size
                            )
                            userImageView.setImageBitmap(updatedImageBitmap)
                        }
                    }
                }
            }
        }

        userInfoLayout.setOnClickListener {
            val intent = Intent(this, AccountDetails::class.java)
            intent.putExtra("userEmail", userEmail)
            startActivityForResult(intent, ACCOUNT_DETAILS_REQUEST_CODE)
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ACCOUNT_DETAILS_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                val updatedEmail = data.getStringExtra("updatedEmail")
                val updatedPassword = data.getStringExtra("updatedPassword")

                val userData = fetchUserData(updatedEmail, updatedPassword)

                if (userData != null) {
                    emailTextView.text = updatedEmail
                    usernameTextView.text = "${userData.firstName} ${userData.lastName}"

                    if (userData.imageByteArray != null) {
                        val updatedImageBitmap = BitmapFactory.decodeByteArray(
                            userData.imageByteArray,
                            0,
                            userData.imageByteArray.size
                        )
                        userImageView.setImageBitmap(updatedImageBitmap)
                    }
                }
            }
        }
    }



    private fun updateUI(userData: UserData) {
        // 在此处更新界面上的视图元素，例如头像、用户名、邮箱等
        usernameTextView.text = "${userData.firstName} ${userData.lastName}"
        emailTextView.text = userData.email

        if (userData.imageByteArray != null) {
            val imageBitmap = BitmapFactory.decodeByteArray(userData.imageByteArray, 0, userData.imageByteArray.size)
            userImageView.setImageBitmap(imageBitmap)
        }
    }

    private fun fetchUserData(email: String?, password: String?): UserData? {
        if (email == null || password == null) {
            Log.d("FetchUserData", "Email or password is null")
            return null
        }

        val dbHelper = DBHelper(this)
        val db = dbHelper.readableDatabase
        val query = "SELECT firstName, lastName, phoneNum, password, confirmPass, image ,position FROM Userdata WHERE email = ? AND password = ?"

        val selectionArgs = arrayOf(email, password)

        var cursor: Cursor? = null
        var userData: UserData? = null

        if (userData != null) {
            // 使用 ViewModel 来设置 LiveData 的值
            adminPanelViewModel.setUserData(userData)
        }

        try {
            cursor = db.rawQuery(query, selectionArgs)

            if (cursor.moveToFirst()) {
                val firstNameIndex = cursor.getColumnIndex("firstName")
                val lastNameIndex = cursor.getColumnIndex("lastName")
                val phoneNumIndex = cursor.getColumnIndex("phoneNum")
                val passwordIndex = cursor.getColumnIndex("password")
                val confirmPassIndex = cursor.getColumnIndex("confirmPass")
                val imageIndex = cursor.getColumnIndex("image")
                val positionIndex = cursor.getColumnIndex("position")

                val firstName = cursor.getString(firstNameIndex)
                val lastName = cursor.getString(lastNameIndex)
                val phoneNum = cursor.getString(phoneNumIndex)
                val fetchedPassword = cursor.getString(passwordIndex)
                val confirmPass = cursor.getString(confirmPassIndex)
                val imageByteArray = cursor.getBlob(imageIndex)
                val position = cursor.getString(positionIndex)

                userData = UserData(
                    email,
                    firstName,
                    lastName,
                    phoneNum,
                    fetchedPassword,
                    confirmPass,
                    imageByteArray,
                    position
                )
            } else {
                Log.d("FetchUserData", "No data found for email: $email and password: $password")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
            db.close()
        }

        return userData
    }

    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Delete Account Confirmation")
        builder.setMessage("Are you sure you want to delete your account? This action cannot be undone.")

        // 添加确认按钮
        builder.setPositiveButton("Delete") { _, _ ->
            val currentUser = firebaseAuth.currentUser
            val email = currentUser?.email
            Log.d(TAG, "Data Reference Path: $currentUser")

            if (email != null) {
                currentUser.delete()
                    .addOnSuccessListener {

                        val databaseReference = FirebaseDatabase.getInstance().reference
                        val userRef = databaseReference.child("users").child(currentUser.uid)

                        userRef.removeValue()
                            .addOnSuccessListener {

                                val db = DBHelper(this)
                                db.deleteUserAccount(email)

                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error deleting user data from Firebase Realtime Database: ${e.message}", e)

                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error deleting user account from Firebase Authentication: ${e.message}", e)

                    }
            }
        }

        // 添加取消按钮
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }

        // 显示对话框
        val dialog = builder.create()
        dialog.show()
    }



    companion object {
        private const val TAG = "adminPanel"
    }

    override fun onBackPressed() {
    }
}