package com.example.assignment.Resources


import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.example.assignment.databinding.ActivityResourcesListBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ResourcesList : AppCompatActivity() {
    private lateinit var bindingResourceList : ActivityResourcesListBinding
    private lateinit var recyclerView: RecyclerView
    private lateinit var databaseHelper: SQLiteHelper // Replace with your database helper
    private lateinit var resourceArray : ArrayList<ResourceModel>
    private lateinit var tvloadingData: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingResourceList = DataBindingUtil.setContentView(this, R.layout.activity_resources_list)

        // Initialize your database helper
        databaseHelper = SQLiteHelper(this)
        Log.d("First","pass")
        // Initialize RecyclerView



//        tvloadingData = bindingResourceList.tvloadingData
        initRecyclerView()
        resourceArray = arrayListOf()

        /*
                getDataFromFirebase()
        */
        val sharedPreferences = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "")
        val firstName = sharedPreferences.getString("firstName", "")
        val lastName = sharedPreferences.getString("lastName", "")
        val phone = sharedPreferences.getString("phone", "")
        val password = sharedPreferences.getString("password", "")
        val confirmPass = sharedPreferences.getString("confirmPass", "")
        val position = sharedPreferences.getString("position", "")


        if(position=="teacher") {
            bindingResourceList.educatorResourceBtn.visibility = View.GONE
        }else{
            bindingResourceList.educatorResourceBtn.visibility = View.VISIBLE
            bindingResourceList.educatorResourceBtn.setOnClickListener {
                val intentAdd = Intent(this, EducatorResources::class.java)
                // start your next activity
                startActivity(intentAdd)
            }
        }



    }

    private fun initRecyclerView() {
        recyclerView = bindingResourceList.resourceRecycleView
        recyclerView.layoutManager = LinearLayoutManager(this)

        val adapter = ResourcesAdapter()
        recyclerView.adapter = adapter
        recyclerView.setHasFixedSize(true)

        val database = FirebaseDatabase.getInstance()
        val resources = database.getReference("Resources")

        resources.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val retrievedData = ArrayList<ResourceModel>()
                for (childSnapshot in snapshot.children) {
                    val resourceValues = childSnapshot.getValue(ResourceModel::class.java)
                    if (resourceValues != null) {
                        retrievedData.add(resourceValues)
                    }
                }
                adapter.setData(retrievedData)
                adapter.setOnClickItem {
                    saveResource(it)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })



    }

    private fun saveResource(retrievedData: ResourceModel) {
        //retrieve data
    }

    /*private fun getDataFromFirebase() {
//        recyclerView.visibility = View.GONE
//        tvloadingData.visibility = View.VISIBLE
        val firebaseDatabase = FirebaseDatabase.getInstance()
        var dbRef = firebaseDatabase.getReference("Resources")
        var subscriptionRef = firebaseDatabase.getReference("Subscription")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                resourceArray.clear()
                if (snapshot.exists()) {
                    for (resSnap in snapshot.children) {
                        val resData = resSnap.getValue(ResourceModel::class.java)
                        resourceArray.add(resData!!)

                    }
                    val mAdapter = ResourcesAdapter(resourceArray,false)
                    recyclerView.adapter = mAdapter

                    mAdapter.setOnItemClickListener(object : ResourcesAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {

                            val intent = Intent(this@ResourcesList, DetailsResource::class.java)

                            //put extras
                            intent.putExtra("resourceId", resourceArray[position].resourceId)
                            intent.putExtra("resourceTitle", resourceArray[position].resourceTitle)
                            intent.putExtra("resourceDesc", resourceArray[position].resourceDesc)
                            intent.putExtra("resourcePath", resourceArray[position].resourceDocument)
                            intent.putExtra("resourceEmail", resourceArray[position].email)
                            intent.putExtra("resourceName", resourceArray[position].name)

                            startActivity(intent)
                        }

                    })

//                    recyclerView.visibility = View.VISIBLE
//                    tvloadingData.visibility = View.GONE
                }
            }


            override fun onCancelled(error: DatabaseError) {

            }

        })    }*/

    /*private fun getDataFromDatabase(): ArrayList<ResourceModel> {
        val resourcesList = ArrayList<ResourceModel>()

        // Retrieve data from the database using your database helper
        resourcesList.addAll(databaseHelper.getResources())
        Log.d("Third",resourcesList.size.toString())

        val mAdapter = ResourcesAdapter()
        recyclerView.adapter = mAdapter

        mAdapter.setOnItemClickListener(object : ResourcesAdapter.onItemClickListener {
            override fun onItemClick(position: Int) {

                val intent = Intent(this@ResourcesList, DetailsResource::class.java)

                //put extras
                intent.putExtra("resourceId", resourcesList[position].resourceId)
                intent.putExtra("resourceTitle", resourcesList[position].resourceTitle)
                intent.putExtra("resourceDesc", resourcesList[position].resourceDesc)
                intent.putExtra("resourcePath", resourcesList[position].resourceDocument)
                intent.putExtra("resourceEmail", resourcesList[position].email)
                intent.putExtra("resourceName", resourcesList[position].name)

                startActivity(intent)
            }

        })
        return resourcesList

    }*/

}