package com.example.assignment.UserManagement

data class UserData(
    val email: String,
    val firstName: String,
    val lastName: String,
    val phoneNum: String,
    val password: String,
    val confirmPass: String,
    val imageByteArray: ByteArray?,
    val position: String
)
