package com.example.assignment.DonationModule.Community

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.example.assignment.R
import com.example.assignment.databinding.ActivityCommunityBinding

class Community : AppCompatActivity() {
    private lateinit var fragmentManager: FragmentManager
    private lateinit var communityBinding: ActivityCommunityBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        communityBinding = ActivityCommunityBinding.inflate(layoutInflater)
        setContentView(communityBinding.root)

        communityBinding.buttonQuestions.setOnClickListener{
            goToFragment(Questions())
            communityBinding.buttonQuestions.setBackgroundColor(Color.parseColor("#E0AAFF"))
            communityBinding.buttonQuestions.setTextColor(Color.WHITE)
            communityBinding.buttonOwn.setBackgroundColor(Color.WHITE)
            communityBinding.buttonOwn.setTextColor(Color.BLACK)
        }

        communityBinding.buttonOwn.setOnClickListener{
            goToFragment(OwnQuestions())
            communityBinding.buttonOwn.setBackgroundColor(Color.parseColor("#E0AAFF"))
            communityBinding.buttonOwn.setTextColor(Color.WHITE)
            communityBinding.buttonQuestions.setBackgroundColor(Color.WHITE)
            communityBinding.buttonQuestions.setTextColor(Color.BLACK)
        }

        communityBinding.addQuestionsButton.setOnClickListener{
            val intent = Intent(this, CreateQuestions::class.java)
            // start your next activity
            startActivity(intent)
        }


    }


    private fun goToFragment(fragment: Fragment){
        fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().replace(R.id.fragment_container,fragment).commit()

    }
}