package com.example.assignment.DonationModule.Community

import android.app.Service
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NetworkCheckService : Service() {
    private lateinit var dbRef: DatabaseReference

    private val handler = Handler()
    private val checkIntervalMillis = 6000// Adjust this value to set the checking interval

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Schedule the periodic network check
        handler.postDelayed(checkNetworkStatusRunnable, checkIntervalMillis.toLong())
        return START_STICKY
    }
    private var isBackgroundThreadRunning = false

    val backgroundThread = Thread {
        isBackgroundThreadRunning = true

        dbRef = FirebaseDatabase.getInstance().getReference("questions")

        Log.d("Netowrk connection","Pushing")

        // Initialize the database helper
        val dbHelper = SqlHelper(applicationContext)

        // Perform database operations
        val unSyncQuest = dbHelper.getPendingSyncData()
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    for (checkQuest in snapshot.children){
                        val questData = checkQuest.getValue(QuestionEntity::class.java)
                        for (data in unSyncQuest) {
                            if (questData != null) {
                                if(data.questionId == questData.questionId){
                                    dbRef.child(data.questionId).setValue(data)
                                }else{
                                    dbRef.child(data.questionId).setValue(data)
                                }
                            }
                        }
                        val sharedPreferences = getSharedPreferences("unSyncData", Context.MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        editor.putString("syncStatus", "Done")
                        editor.apply()
                        isBackgroundThreadRunning = false
                    }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }
    // Process the retrieved data or perform other tasks
    // For example, you can log the data




    private val checkNetworkStatusRunnable = object : Runnable {
        override fun run() {
            Log.d("Network running","Scanning")
            if (isNetworkAvailable()) {
                val sharedPreferences = getSharedPreferences("unSyncData", Context.MODE_PRIVATE)
                val syncStatus = sharedPreferences.getString("syncStatus", "")
                val dbHelper = SqlHelper(applicationContext)
                val unSyncQuests= dbHelper.getPendingSyncData()

                if(syncStatus == "pending" && unSyncQuests.isNotEmpty()){
                    backgroundThread.start()

                }
                Log.d("Sync number",unSyncQuests.size.toString())

                Log.d("Netowrk connection","Push to fb")

            } else {
                Log.d("Netowrk connection","Store local")
                // There is no internet connection
                // Store data locally or handle the offline scenario
            }

            // Schedule the next check
            handler.postDelayed(this, checkIntervalMillis.toLong())
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork = connectivityManager.activeNetworkInfo
        return activeNetwork?.isConnectedOrConnecting == true
    }

    override fun onDestroy() {
        // Remove the callbacks when the service is destroyed
        handler.removeCallbacks(checkNetworkStatusRunnable)
        super.onDestroy()
    }
}