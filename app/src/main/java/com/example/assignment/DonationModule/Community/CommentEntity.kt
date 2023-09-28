package com.example.assignment.DonationModule.Community

data class CommentEntity(
    val questionId : String ="",
    val commentId : String ="",
    val comment: String = "",
    val commentorEmail: String = "",

    ) {
    // No-argument constructor required by Firebase
    constructor() : this("", "","","")
}
