package com.example.assignment
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.assignment.DonationModule.Community.NetworkCheckService
import com.example.assignment.DonationModule.Community.Questions
import com.example.assignment.UserManagement.login
import com.example.assignment.UserManagement.registration


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val loginBtn  = findViewById<Button>(R.id.login)
        val signUpBtn  = findViewById<Button>(R.id.signup)

        loginBtn.setOnClickListener(){
            val intent = Intent(this, login::class.java)
            startActivity(intent)
            val serviceIntent = Intent(this, NetworkCheckService::class.java)
            startService(serviceIntent)
        }

        signUpBtn.setOnClickListener(){
            val intent = Intent(this, registration::class.java)
            startActivity(intent)
        }





    }

}