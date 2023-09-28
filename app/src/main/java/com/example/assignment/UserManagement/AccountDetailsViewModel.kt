package com.example.assignment.UserManagement

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountDetailsViewModel : ViewModel() {
    private val userDataLiveData = MutableLiveData<UserData>()

    fun getUserDataLiveData(): LiveData<UserData> {
        return userDataLiveData
    }

    fun setUserData(userData: UserData) {
        userDataLiveData.value = userData
    }
}
