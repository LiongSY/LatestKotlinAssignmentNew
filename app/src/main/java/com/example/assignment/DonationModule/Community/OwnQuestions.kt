package com.example.assignment.DonationModule.Community

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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

class OwnQuestions : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val view = inflater.inflate(R.layout.fragment_own_questions, container, false)
//        val sqlHelper = SqlHelper(requireContext())
        questionRecycleView = view.findViewById<RecyclerView>(R.id.ownQuestRecyclerView)
//        adapter = QuestionAdapter(questList,true)
        questionRecycleView.layoutManager = LinearLayoutManager(requireContext())
        questionRecycleView.setHasFixedSize(true)
        questionList = arrayListOf<QuestionEntity>()
        if (checkForInternet(requireContext())) {
            Log.d("Network","here")
            getOwnQuestionData()
        } else {
            getQuestionsFromLocal()
        }

        return view
    }

    private fun getQuestionsFromLocal() {
        val sqlHelper = SqlHelper(requireContext())
        val result = sqlHelper.retrieveOwnQuestions("Chin@gmail.com")

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


            qAdapter = QuestionAdapter(questionList,questionFrag = true)
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

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            val network = connectivityManager.activeNetwork ?: return false

            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }
    private fun getOwnQuestionData() {

        dbRef = FirebaseDatabase.getInstance().getReference("questions")

        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                questionList.clear()
                if (snapshot.exists()){
                    for (questSnap in snapshot.children){
                        val questData = questSnap.getValue(QuestionEntity::class.java)
                        if (questData!!.email == "Chin@gmail.com") {
                            questionList.add(questData!!)
                        }
                    }

                    val qAdapter = QuestionAdapter(questionList,questionFrag = true)
                    questionRecycleView.adapter = qAdapter



                    qAdapter.setOnItemClickListener(object : QuestionAdapter.onItemClickListener {
                        override fun onItemClick(position: Int) {
                            val context: Context? = activity

                            val intent = Intent(context, QuestionDetails::class.java)

                            //put extras
                            intent.putExtra("questId", questionList[position].questionId)
                            intent.putExtra("questTitle", questionList[position].questTitle)
                            intent.putExtra("questDesc", questionList[position].question)
                            intent.putExtra("questImage", questionList[position].questImage)
                            startActivity(intent)
                        }
                    })


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