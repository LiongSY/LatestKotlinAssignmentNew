package com.example.assignment.DonationModule.Community

import android.graphics.Bitmap
import android.net.Uri

data class QuestionEntity(
    val questionId: String = "",
    val questTitle: String = "",
    val email: String = "",
    val question: String = "",
    val questImage:String = "",
    val syncStatus: String = ""
)
