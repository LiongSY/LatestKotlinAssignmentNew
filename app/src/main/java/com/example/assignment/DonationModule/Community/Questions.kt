package com.example.assignment.DonationModule.Community

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

private lateinit var dbRef: DatabaseReference
private lateinit var questionList: ArrayList<QuestionEntity>
private lateinit var questionRecycleView: RecyclerView
private lateinit var qAdapter : QuestionAdapter

class Questions : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_questions, container, false)
//        val sqlHelper = SqlHelper(requireContext())
        questionRecycleView = view.findViewById<RecyclerView>(R.id.questionRecycleView)
//        adapter = QuestionAdapter(questList,true)
        questionRecycleView.layoutManager = LinearLayoutManager(requireContext())
        questionRecycleView.setHasFixedSize(true)
        questionList = arrayListOf<QuestionEntity>()
        if (checkForInternet(requireContext())) {
            getQuestionsData()
            Log.d("Network","here")

        } else {
            getQuestionsFromLocal()
        }

        return view
    }



    private fun getQuestionsFromLocal() {
        val sqlHelper = SqlHelper(requireContext())
        val result = sqlHelper.retrieveQuestions()

        if(result.size == 0){
            Toast.makeText(requireContext(), "No data in the record.", Toast.LENGTH_LONG)
                    .show()
        }

        Log.d("Size of string:", result.size.toString())
        questionList.clear()
        for(question in result)
        {

            Log.d("Title:", "question.questTitle")
            Log.d("Title:", question.questTitle)
            Log.d("ID:", question.questionId)


            qAdapter = QuestionAdapter(questionList,questionFrag = false)
            questionRecycleView.adapter = qAdapter
            questionList.add(question!!)
            qAdapter.setOnItemClickListener(object : QuestionAdapter.onItemClickListener {
                override fun onItemClick(position: Int) {
                    val context: Context? = activity

                    val intent = Intent(context, QuestionDetails::class.java)

                    intent.putExtra("questId", questionList[position].questionId)
                    intent.putExtra("questTitle", questionList[position].questTitle)
                    intent.putExtra("questDesc", questionList[position].question)
                    intent.putExtra("questImage", questionList[position].questImage)
                    startActivity(intent)
                }

            })
        }


    }

    private fun checkForInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork: Network? = connectivityManager.activeNetwork
        val networkCapabilities: NetworkCapabilities? =
            connectivityManager.getNetworkCapabilities(activeNetwork)

        return networkCapabilities != null


    }


    private fun getQuestionsData() {
        Log.d("I got it","I'm in questionsData")

//        empRecyclerView.visibility = View.GONE
//        tvLoadingData.visibility = View.VISIBLE

        dbRef = FirebaseDatabase.getInstance().getReference("questions")
        Log.d("I got it","I'm in questionsData2")

        dbRef.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("I got it","I'm in questionsData3")

                questionList.clear()
                if (snapshot.exists()){
                    for (questSnap in snapshot.children){
                        qAdapter = QuestionAdapter(questionList,questionFrag = false)
                        questionRecycleView.adapter = qAdapter
                        val questionId = questSnap.child("questionId").getValue(String::class.java)
                        val questTitle = questSnap.child("questTitle").getValue(String::class.java)
                        val email = questSnap.child("email").getValue(String::class.java)
                        val question = questSnap.child("question").getValue(String::class.java)
                        val questImage = questSnap.child("questImage").getValue(String::class.java)
                        val syncStatus = questSnap.child("syncStatus").getValue(String::class.java)

                        Log.d("Quest image",questImage!!)
                        val byteArrayImage = Base64.decode(questImage, Base64.DEFAULT)
                        Log.d("Byte array image",byteArrayImage.toString())
                        val questData = QuestionEntity(questionId!!,questTitle!!,email!!,question!!,questImage!!,syncStatus!!)
                        Log.d("Quest data",questData.toString())
                        questionList.add(questData!!)
                        Log.d("I got it","I gooooott")
                        qAdapter.setOnItemClickListener(object :
                            QuestionAdapter.onItemClickListener {
                            override fun onItemClick(position: Int) {
                                val context: Context? = activity


                                val intent = Intent(context, QuestionDetails::class.java)

                                intent.putExtra("questId", questionList[position].questionId)
                                intent.putExtra("questTitle", questionList[position].questTitle)
                                intent.putExtra("questDesc", questionList[position].question)
                                intent.putExtra("questImage", questionList[position].questImage)
                                startActivity(intent)
                            }

                        })
                    }
                }else{
                    Toast.makeText(requireContext(), "No data in the record.", Toast.LENGTH_LONG)
                        .show()

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }



}