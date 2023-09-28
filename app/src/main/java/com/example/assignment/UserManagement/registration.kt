package com.example.assignment.UserManagement

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.R
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore

class registration : AppCompatActivity() {
    private lateinit var fname: EditText
    private lateinit var lname: EditText
    private lateinit var email: EditText
    private lateinit var phonenum: EditText
    private lateinit var pword: EditText
    private lateinit var confPass: EditText
    private lateinit var signupbtn: MaterialButton
    private lateinit var signintxt : TextView
    private lateinit var db: DBHelper
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        fname = findViewById(R.id.firstName)
        lname = findViewById(R.id.lastName)
        email = findViewById(R.id.editTextTextEmailAddress)
        phonenum = findViewById(R.id.editTextPhone)
        pword = findViewById(R.id.passwordEtd)
        confPass = findViewById(R.id.confirmPasswordEtd)
        signupbtn = findViewById(R.id.signupbtn)
        signintxt = findViewById(R.id.signintxt)
        db = DBHelper(this)
        auth = FirebaseAuth.getInstance()
        val firestore = FirebaseFirestore.getInstance()

        signintxt.setOnClickListener {
            var intent = Intent(this, login::class.java)
            startActivity(intent)
        }
        signupbtn.setOnClickListener {
            val inputfname = fname.text.toString()
            val inputlname = lname.text.toString()
            val inputemail = email.text.toString()
            val inputphonenum = phonenum.text.toString()
            val inputpword = pword.text.toString()
            val inputconfPass = confPass.text.toString()

            val emailPattern = "[a-zA-Z0-9._-]*[a-zA-Z][a-zA-Z0-9._-]*"
            val passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$"

            val phonePattern = "(01[0-9]|011|012|013|014|015|016|017|018|019)[0-9]{7,}"

            if (TextUtils.isEmpty(inputfname) || TextUtils.isEmpty(inputlname) || TextUtils.isEmpty(
                    inputemail
                ) ||
                TextUtils.isEmpty(inputphonenum) || TextUtils.isEmpty(inputpword) || TextUtils.isEmpty(
                    inputconfPass
                )
            ) {
                Toast.makeText(this, "Please check your input information", Toast.LENGTH_SHORT)
                    .show()
             } else if (inputpword != inputconfPass) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
        } else if (inputfname.length > 20 || inputlname.length > 20) {
            Toast.makeText(this, "First name and last name should not exceed 20 characters", Toast.LENGTH_SHORT).show()
        } else if (!inputemail.split("@")[0].matches(emailPattern.toRegex())) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show()
        } else if (!inputphonenum.matches(phonePattern.toRegex())) {
            Toast.makeText(this, "Invalid Malaysian phone number format", Toast.LENGTH_SHORT).show()
        }else if (!inputpword.matches(passwordPattern.toRegex())) {
                Toast.makeText(this, "Password is too weak", Toast.LENGTH_SHORT).show()
            }else {
                if (inputpword == inputconfPass) {
                    auth.createUserWithEmailAndPassword(inputemail, inputpword)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser

                                user?.sendEmailVerification()
                                    ?.addOnCompleteListener { verificationTask ->
                                        if (verificationTask.isSuccessful) {
                                            val isteacher = db.isTeacher(inputemail)
                                            val position = if (isteacher) "teacher" else "student"

                                            val isUserInserted = db.insertData(
                                                inputemail,
                                                inputpword, // Store the password
                                                inputfname,
                                                inputlname,
                                                inputphonenum,
                                                inputconfPass, // Store confirmation password
                                                null,
                                                position
                                            )

                                            val databaseReference =
                                                FirebaseDatabase.getInstance().reference
                                            val userRef = databaseReference.child("users")
                                                .child(user?.uid ?: "")

                                            val userData = HashMap<String, Any>()
                                            userData["email"] = inputemail
                                            userData["firstName"] = inputfname
                                            userData["lastName"] = inputlname
                                            userData["phone"] = inputphonenum
                                            userData["position"] = position
                                            userData["password"] = inputpword // Store the password
                                            userData["confirmPass"] = inputconfPass // Store confirmation password

                                            userRef.setValue(userData)
                                                .addOnCompleteListener { databaseTask ->
                                                    if (databaseTask.isSuccessful) {
                                                        showSuccessDialog(inputemail, position)
                                                    } else {
                                                        Toast.makeText(
                                                            this,
                                                            "Failed to store user data in Realtime Database",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                        } else {
                                            Toast.makeText(
                                                this,
                                                "Failed to send verification email: ${verificationTask.exception}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                            } else {
                                if (task.exception is FirebaseAuthUserCollisionException) {
                                    Toast.makeText(
                                        this,
                                        "User with this email already exists",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    Log.d("Error","Registration failed: ${task.exception}")
                                    Toast.makeText(
                                        this,
                                        "Registration failed: ${task.exception}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                } else {
                    Toast.makeText(this, "Password does not match", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showSuccessDialog(email: String, position: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Registration Successful")
        builder.setMessage("Your registration was successful.")
        builder.setPositiveButton("OK") { _, _ ->
            val loginIntent = Intent(this, login::class.java)
            loginIntent.putExtra("email", email)
            loginIntent.putExtra("userType", position)
            startActivity(loginIntent)
        }

        val dialog = builder.create()
        dialog.show()
    }
}
