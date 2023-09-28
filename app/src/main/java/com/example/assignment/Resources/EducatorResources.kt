package com.example.assignment.Resources

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.example.assignment.databinding.ActivityEducatorResourcesBinding

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EducatorResources : AppCompatActivity() {
    private lateinit var bindingResourceList : ActivityEducatorResourcesBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseHelper: SQLiteHelper // Replace with your database helper
    private lateinit var resourceArray : ArrayList<ResourceModel>
    private lateinit var tvloadingData: TextView
    private lateinit var viewReport:ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingResourceList = DataBindingUtil.setContentView(this, R.layout.activity_educator_resources)

        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val name = sharedPreferences.getString("lastName", "")
        val email = sharedPreferences.getString("email", "")

                viewReport = findViewById(R.id.viewReport)
                viewReport.setOnClickListener {
                    report()
                }
                bindingResourceList.educatorAddBtn.setOnClickListener {
                    val intentAdd = Intent(this, AddResources::class.java)
                    // start your next activity
                    startActivity(intentAdd)
                }
                // Initialize your database helper
                databaseHelper = SQLiteHelper(this)
                Log.d("First","pass")
                // Initialize RecyclerView
                recyclerView = bindingResourceList.resourceRecycleView
                recyclerView.layoutManager = LinearLayoutManager(this)
                recyclerView.setHasFixedSize(true)

//                tvloadingData = bindingResourceList.tvloadingData

                resourceArray = arrayListOf<ResourceModel>()
        if(isNetworkAvailable()){
            getDataFromFirebase(email!!)

        }else{
            getDataFromDatabase(email!!)

        }

    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun report() {
        val intent = Intent(this@EducatorResources,Report::class.java)
        startActivity(intent)
    }

    private fun getDataFromFirebase(email:String) {
//        recyclerView.visibility = View.GONE
//        tvloadingData.visibility = View.VISIBLE
        val firebaseDatabase = FirebaseDatabase.getInstance()
        var dbRef = firebaseDatabase.getReference("Resources")
        var subscirptionRef = firebaseDatabase.getReference("Subscription")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                resourceArray.clear()
                if (snapshot.exists()) {
                    for (resSnap in snapshot.children) {
                        val resData = resSnap.getValue(ResourceModel::class.java)
                        if(resData!!.email == email ){
                            resourceArray.add(resData!!)
                        }

                    }
                    val mAdapter = ResourcesAdapter()
                    recyclerView.adapter = mAdapter
                    recyclerView.setHasFixedSize(true)

                    mAdapter.setData(resourceArray)
                    mAdapter.setOnClickItem {
                        saveResource(it)
                    }


//                    mAdapter.setOnItemClickListener(object : ResourcesAdapter.onItemClickListener {
//                        override fun onItemClick(position: Int) {
//
//                            val intent = Intent(this@EducatorResources, DetailsResource::class.java)
//
//                            //put extras
//                            intent.putExtra("resourceId", resourceArray[position].resourceId)
//                            intent.putExtra("resourceTitle", resourceArray[position].resourceTitle)
//                            intent.putExtra("resourceDesc", resourceArray[position].resourceDesc)
//                            intent.putExtra("resourcePath", resourceArray[position].resourceDocument)
//                            intent.putExtra("resourceEmail", resourceArray[position].email)
//                            intent.putExtra("resourceName", resourceArray[position].name)
//
//                            startActivity(intent)
//                        }
//
//                    })

//                    recyclerView.visibility = View.VISIBLE
//                    tvloadingData.visibility = View.GONE
                }
            }


            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun getDataFromDatabase(email:String) {
        val resourcesList = ArrayList<ResourceModel>()
        recyclerView = bindingResourceList.resourceRecycleView
        recyclerView.layoutManager = LinearLayoutManager(this)
        // Retrieve data from the database using your database helper
        resourcesList.addAll(databaseHelper.educatorOwnResources(email))
        Log.d("Third",resourcesList.size.toString())
        val adapter = ResourcesAdapter()
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        adapter.setData(resourcesList)
        adapter.setOnClickItem {
            saveResource(it)
        }





        }

    private fun saveResource(it: ResourceModel) {

    }


    //imageView for viewReport
}