package com.example.assignment.DonationModule.Community

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.R


import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.util.Base64
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.databinding.ActivityQuestionDetailsBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class QuestionDetails : AppCompatActivity() {
    private lateinit var questionDetailsBinding: ActivityQuestionDetailsBinding
    private lateinit var  commentField: EditText
    private lateinit var  commentBtn: Button
    private lateinit var dbRef: DatabaseReference
    private lateinit var commentList: ArrayList<CommentEntity>
    private lateinit var commentRecyclerView: RecyclerView
    private lateinit var cAdapter : CommentsAdapter
    private lateinit var commentsCount : TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        questionDetailsBinding = DataBindingUtil.setContentView(this,R.layout.activity_question_details)
        var actionBar = getSupportActionBar()

        if (actionBar != null) {

            // Customize the back button
            actionBar.setHomeAsUpIndicator(R.drawable.baseline_arrow_back_ios_24);

            // showing the back button in action bar
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        val questionTitle = intent.getStringExtra("questionTitle")
        val questionDescription = intent.getStringExtra("questionDescription")
        val stringImage = intent.getStringExtra("questionImage")
        val questionId = intent.getStringExtra("questionId")

        commentRecyclerView = questionDetailsBinding.commentsRecyclerView
        commentRecyclerView.layoutManager = LinearLayoutManager(this)
        commentRecyclerView.setHasFixedSize(true)
        commentList = arrayListOf<CommentEntity>()
        commentsCount = questionDetailsBinding.commentsCount

        var countComments = getComments(questionId!!)
//        commentsCount.setText()
        if(stringImage != null){
            val byteArrayImage = Base64.decode(stringImage, Base64.DEFAULT)
            val questionBitmap = BitmapFactory.decodeByteArray(
                byteArrayImage,
                0,
                byteArrayImage.size
            )
            questionDetailsBinding.imageDetails.setImageBitmap(questionBitmap)



            questionDetailsBinding.imageDetails.setImageBitmap(questionBitmap)
            questionDetailsBinding.imageDetails.setOnClickListener{
                showFullSizeImageDialog(questionBitmap!!)
            }
        }else{
            questionDetailsBinding.imageDetails.visibility = View.GONE
        }
        questionDetailsBinding.titleDetailsText.setText(questionTitle)
        questionDetailsBinding.descriptionDetails.setText(questionDescription)
        questionDetailsBinding.titleDetailsText.setText(questionTitle)
        commentField = questionDetailsBinding.commentField
        commentBtn = questionDetailsBinding.commentButton

        commentBtn.setOnClickListener {
            var comment = commentField.text.toString()
            if(checkForInternet(this)){
                saveComments(questionId!!,comment)
            }else{
                Toast.makeText(this, "Please ensure that you have internet connection.", Toast.LENGTH_LONG).show()
            }
        }


        questionDetailsBinding.deleteButton.setOnClickListener {

            confirmationDialog(questionId)
        }
    }

    private fun getComments(questionId: String):Int {
        Log.d("Check","Im in getCommnets")
        dbRef = FirebaseDatabase.getInstance().getReference("comments")
        var count = 0
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                commentList.clear()
                if (snapshot.exists()) {
                    for (questSnap in snapshot.children) {
                        val commentData = questSnap.getValue(CommentEntity::class.java)
                        Log.d("Check",commentData.toString())

                        if (commentData != null) {
                            if(commentData.questionId == questionId ){
                                 count++
                                Log.d("comments count",count.toString())
                                Log.d("Check","validated")

                                cAdapter = CommentsAdapter(commentList)
                                commentRecyclerView.adapter = cAdapter
                                commentList.add(commentData!!)

                            }
                        }


                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        return count

    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onContextItemSelected(item)
    }

    private fun saveComments(questionId: String, comment: String) {
        if (comment.isEmpty()) {
            Toast.makeText(this, "Please comment before submit.", Toast.LENGTH_LONG).show()
        }

        if(comment.length > 500){
            Toast.makeText(this, "Comment cannot more than 500 characters", Toast.LENGTH_LONG).show()
        }
        dbRef = FirebaseDatabase.getInstance().getReference("comments")
        val commentsId = dbRef.push().key!!
        val userComment = CommentEntity(
            questionId,commentsId,comment,"Lee@gmail.com"
        )

        dbRef.child(commentsId).setValue(userComment)
            .addOnCompleteListener {
                Toast.makeText(this, "Comments successfully created", Toast.LENGTH_LONG).show()
                commentField.text.clear()

            }.addOnFailureListener { err ->
                Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
            }

    }


    private fun confirmationDialog(questionId: String?) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Delete confirmation")
        alertDialogBuilder.setView(R.layout.confirmation_dialog)
        alertDialogBuilder.setPositiveButton("Confirm") { dialog, which ->
            if(checkForInternet(this)){
                deleteQuestions(questionId)
                deleteQuestionsFromFirebase(questionId)
                Toast.makeText(this, "Question has been successfully deleted.", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(this, "Please ensure your network connection is on!", Toast.LENGTH_LONG).show()

            }
            dialog.dismiss()
        }
        alertDialogBuilder.setNegativeButton("No") { dialog, which ->
            dialog.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
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

    private fun deleteQuestionsFromFirebase(questionId: String?) {
        val firebaseReference = FirebaseDatabase.getInstance().getReference("questions").child(questionId!!)
        val reference = firebaseReference.removeValue()

        reference.addOnSuccessListener {
            Toast.makeText(this, "Questions has been deleted!", Toast.LENGTH_LONG).show()
            finish()
        }.addOnFailureListener { error ->
            Toast.makeText(this,"Unable to delete question.: ${error.message}", Toast.LENGTH_LONG).show()
        }

    }

    private fun deleteQuestions(questionId: String?) {
        val sqlHelper = SqlHelper(this)
        var result = sqlHelper.deleteQuestion(questionId!!)

    }




    private fun showFullSizeImageDialog(image: Bitmap) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.full_size_image)

        val fullSizeImageView = dialog.findViewById<ImageView>(R.id.fullSizeImageView)
        fullSizeImageView.setImageBitmap(image)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )

        fullSizeImageView.setOnClickListener {
            dialog.dismiss() // Close the dialog
        }

        dialog.show()
    }

}