package com.example.assignment.UserManagement

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.assignment.DonationModule.Community.Community
import com.example.assignment.DonationModule.Community.Questions
import com.example.assignment.Homepage
import com.example.assignment.R
import com.example.assignment.Resources.ResourcesList
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class login : AppCompatActivity() {
    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var loginbtn: MaterialButton
    private lateinit var dbh: DBHelper
    private lateinit var forgotPassword: TextView
    private lateinit var signuptxt : TextView
    private lateinit var firebaseAuth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        firebaseAuth = FirebaseAuth.getInstance()
        email = findViewById(R.id.emailEtd)
        password = findViewById(R.id.passwordEtd)
        loginbtn = findViewById(R.id.loginbtn)
        signuptxt = findViewById(R.id.registerTxt)
        dbh = DBHelper(this)
        forgotPassword = findViewById(R.id.forgotPassword)



        loginbtn.setOnClickListener {
            val inputemail = email.text.toString()
            val inputpass = password.text.toString()

            getDataReferencePath()
            if (TextUtils.isEmpty(inputemail) || TextUtils.isEmpty(inputpass)) {
                Toast.makeText(this, "Please input the email & password", Toast.LENGTH_SHORT).show()
            } else {
                if (inputemail == "education@admin.com" && inputpass == "admin") {
                    val intent = Intent(this, adminPanel::class.java)
                    startActivity(intent)
                    saveUserDataToSharedPreferences(inputemail, inputpass)
                } else {
                    val checkuser = dbh.checkUserPassword(inputemail, inputpass)
                    if (checkuser == true) {
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                        val userType = dbh.getUserType(inputemail, inputpass)

                        if (userType == "teacher") {
                            val intent = Intent(this, teacherPanel::class.java)
                            startActivity(intent)
                            saveUserDataToSharedPreferences(inputemail, inputpass)
                        } else if (userType == "student") {
//                            val intent = Intent(this, studentPanel::class.java)
//                            startActivity(intent)

                            saveUserDataToSharedPreferences(inputemail, inputpass)
//                            val intent = Intent(this, Community::class.java)
//                            startActivity(intent)

                       val intent = Intent(this, Homepage::class.java)
                        startActivity(intent)

                        }
                    } else {
                        Toast.makeText(this, "Wrong email OR password", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            forgotPassword.setOnClickListener {
                val intent = Intent(this, passwordReset::class.java)
                intent.putExtra("user_email", email.text.toString())
                startActivity(intent)
                Toast.makeText(this, "Forgot Password? Clicked!", Toast.LENGTH_SHORT).show()
            }

            signuptxt.setOnClickListener{
                val intent = Intent(this, registration::class.java)
                startActivity(intent)
            }
        }
    }

    private fun saveUserDataToSharedPreferences(email: String, password: String) {
        val userData = dbh.retrieveUserData(email, password)

        if (userData != null) {
            val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = sharedPreferences.edit()

            editor.putString("email", userData.email)
            editor.putString("firstName", userData.firstName)
            editor.putString("lastName", userData.lastName)
            editor.putString("phone", userData.phoneNum)
            editor.putString("password", userData.password)
            editor.putString("confirmPass", userData.confirmPass)
            editor.putString("position", userData.position)

            if (userData.imageByteArray != null) {
                val imageBase64 = Base64.encodeToString(userData.imageByteArray, Base64.DEFAULT)
                editor.putString("imageBase64", imageBase64)
            }

            editor.apply()
        }
    }
    private fun getDataReferencePath() {
        val inputemail = email.text.toString()
        val inputpass = password.text.toString()

        if (TextUtils.isEmpty(inputemail) || TextUtils.isEmpty(inputpass)) {
            Toast.makeText(this, "Please input the email & password", Toast.LENGTH_SHORT).show()
        } else {
            firebaseAuth.signInWithEmailAndPassword(inputemail, inputpass)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null) {
                            val dataReferencePath = "users/${currentUser.uid}"
//                            Toast.makeText(this, "Data Reference Path: $dataReferencePath", Toast.LENGTH_SHORT).show()
                        }
                    } else {
//                        Toast.makeText(this, "Login failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
