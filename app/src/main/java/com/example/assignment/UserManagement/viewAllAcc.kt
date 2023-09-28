package com.example.assignment.UserManagement
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class viewAllAcc : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAccountAdapter: UserAdapter
    private lateinit var searchButton: Button
    private lateinit var searchEditText: EditText
    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_all_acc)
        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        recyclerView = findViewById(R.id.recyclerView)
        searchButton = findViewById(R.id.searchTypeButton)
        searchEditText = findViewById(R.id.typeNameEditText)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        bottomNavigationView.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_dashboard -> {
                    val homeIntent = Intent(this, adminPanel::class.java)
                    startActivity(homeIntent)
                    true
                }
                R.id.navigation_user -> {
                    val dashboardIntent = Intent(this, viewAllAcc::class.java)
                    startActivity(dashboardIntent)
                    true
                }
                R.id.navigation_institution -> {
                    val notificationsIntent = Intent(this, addInstituition::class.java)
                    startActivity(notificationsIntent)
                    true
                }
                else -> false
            }
        }

        val userAccounts = fetchUserAccounts()

        userAccountAdapter = UserAdapter(this, userAccounts)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAccountAdapter

        searchButton.setOnClickListener {
            val query = searchEditText.text.toString()
            userAccountAdapter.filter.filter(query)
        }
    }

    private fun fetchUserAccounts(): List<UserData> {
        val dbHelper = DBHelper(this)
        val userAccounts = dbHelper.getAllUserAccounts()
        dbHelper.close()
        return userAccounts
    }

}
