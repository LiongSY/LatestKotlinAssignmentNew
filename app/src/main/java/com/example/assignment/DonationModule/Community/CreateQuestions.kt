package com.example.assignment.DonationModule.Community


import android.app.Activity
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.databinding.DataBindingUtil
import com.example.assignment.R
import com.example.assignment.databinding.QuestionFormLayoutBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import android.Manifest
import java.io.ByteArrayOutputStream
import java.lang.Exception

typealias LumaListener = (luma: Double) -> Unit
private lateinit var questionList: ArrayList<QuestionEntity>
private lateinit var qAdapter : QuestionAdapter

class CreateQuestions : AppCompatActivity() {
    private lateinit var viewBinding : QuestionFormLayoutBinding
    private lateinit var binding : QuestionFormLayoutBinding
    private lateinit var galleryButton: ImageButton
    private lateinit var  title: EditText
    private lateinit var  question: EditText

    private val GALLERY_REQUEST_CODE = 1
    private val CAMERA = 2
//    private var questImage : String = ""
    private lateinit var questImage:String
    private var imageUri : Uri? = null
    private lateinit var qAdapter : QuestionAdapter
    private lateinit var camButton: ImageButton

    private lateinit var dbRef: DatabaseReference
    private val emailP = "Chin@gmail.com"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.question_form_layout)
        viewBinding = QuestionFormLayoutBinding.inflate(layoutInflater)
        questionList = arrayListOf<QuestionEntity>()
        galleryButton = binding.galleryButton
        title = binding.inputTitle
        question = binding.inputQuest
        camButton = binding.cameraButton
        dbRef = FirebaseDatabase.getInstance().getReference("questions")
        galleryButton.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            Log.d("Gallery",galleryIntent.toString())
            ActivityResultLauncher.launch(galleryIntent)
        }
        questImage = "empty image"


        binding.backButton.setOnClickListener {
            finish()
        }

        binding.uploadBtn.setOnClickListener {
            if (checkForInternet(this)) {
                saveQuestion()
                saveQuestionInLocal()
            } else {
                saveQuestionInLocal()
            }

        }
    }


    private fun saveQuestionInLocal() {
        val length = 10
        val localQuestionId = getRandomString(length)
        val questTitle = title.text.toString()
        val questDesc = question.text.toString()
        if(questImage == null){
            questImage = "image"
        }
        if(questTitle.isEmpty()){
            Toast.makeText(this, "Please fill in your title.", Toast.LENGTH_LONG).show()
        }

        if(questDesc.isEmpty()){
            Toast.makeText(this, "Please fill in your question.", Toast.LENGTH_LONG).show()
        }

        if(questTitle.isNotEmpty() && questDesc.isNotEmpty()){
            if(questTitle.length > 60){
                Toast.makeText(this, "Title cannot more than 60 characters", Toast.LENGTH_LONG).show()
            }
            if(questDesc.length > 500){
                Toast.makeText(this, "Question cannot more than 500 characters", Toast.LENGTH_LONG).show()
            }
            val sqlHelper = SqlHelper(this)
            val quest = QuestionEntity(questionId = localQuestionId, questTitle = questTitle, email = emailP, question = questDesc, questImage = questImage!!)
            val result = sqlHelper.insertQuestion(quest,checkForInternet(this))
            if(result){
                Toast.makeText(this, "Network error, question successfully stored in local", Toast.LENGTH_LONG).show()
                qAdapter = QuestionAdapter(questionList,questionFrag = false)
                questionList.add(quest)
                qAdapter.notifyDataSetChanged()
                val sharedPreferences = getSharedPreferences("unSyncData", Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putString("syncStatus", "pending")
                editor.apply()
                finish()
            }else{
                Toast.makeText(this, "Failed to save the question. ", Toast.LENGTH_LONG).show()

            }
        }


    }

    fun getRandomString(length: Int) : String {
        val charset = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        return (1..length)
            .map { charset.random() }
            .joinToString("")
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



    private val ActivityResultLauncher = registerForActivityResult<Intent,ActivityResult>(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val selectedImageUri = result.data!!.data

            try {

                val inputStream = contentResolver.openInputStream(selectedImageUri!!)
                val imageBitmap = BitmapFactory.decodeStream(inputStream)
                imageUri = selectedImageUri
                val maxWidth = 800
                val maxHeight = 1270

                // Resize the image using the calculated dimensions
                val resizedBitmap = Bitmap.createScaledBitmap(
                    imageBitmap,
                    (maxWidth),
                    (maxHeight),
                    true
                )

                inputStream?.close()

                val stream = ByteArrayOutputStream()
                resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val bytes = stream.toByteArray()

                questImage = Base64.encodeToString(bytes, Base64.DEFAULT)

                binding.imageView.setImageBitmap(imageBitmap)
                binding.imageView.visibility = View.VISIBLE
                inputStream!!.close()
                Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show()

            } catch (ex: Exception) {
                Log.d("Error",ex.toString())
                Toast.makeText(this, ex.toString(), Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun saveQuestion() {
         Log.d("Save questions byte",questImage.toString())
        val questTitle = title.text.toString()
        val questDesc = question.text.toString()
        if (questTitle.isEmpty()) {
            Toast.makeText(this, "Please insert the title", Toast.LENGTH_LONG).show()
        }
        if (questDesc.isEmpty()) {
            Toast.makeText(this, "Please insert the questions", Toast.LENGTH_LONG).show()
        }

        if(questDesc.isNotEmpty() && questTitle.isNotEmpty()) {
            if(questTitle.length > 60){
                Toast.makeText(this, "Title cannot more than 60 characters", Toast.LENGTH_LONG).show()
            }
            if(questDesc.length > 500){
                Toast.makeText(this, "Question cannot more than 500 characters", Toast.LENGTH_LONG).show()
            }

            val questionId = dbRef.push().key!!


            val quest = QuestionEntity(
                questionId = questionId,
                questTitle = questTitle,
                email = emailP,
                question = questDesc,
                questImage = questImage,
                syncStatus = "Done"
            )

            val firebaseMap = HashMap<String, Any>()
//            val stringImage = Base64.encodeToString(questImage, Base64.DEFAULT)
            firebaseMap["questionId"] = questionId
            firebaseMap["questTitle"] = questTitle
            firebaseMap["email"] = emailP
            firebaseMap["question"] = questDesc
//            firebaseMap["questImage"] = stringImage
            firebaseMap["syncStatus"] = "Done"

            Log.d("I'm here", quest.questionId)
            val sqlHelper = SqlHelper(this)
            val result = sqlHelper.insertQuestion(quest,checkForInternet(this))
//            dbRef.child(questionId).setValue(firebaseMap)
            dbRef.child(questionId).setValue(quest)
                .addOnCompleteListener {
                    Log.d("Sucess FB", "Success db")

                    if(result){
                        Toast.makeText(this, "Question has successfully created.", Toast.LENGTH_LONG).show()
                    }else{
                        Toast.makeText(this, "Question failed to create.", Toast.LENGTH_LONG).show()
                    }

                    binding.inputTitle.text.clear()
                    binding.inputQuest.text.clear()
                    binding.imageView.setBackgroundDrawable(null)

                    finish()

                }.addOnFailureListener { err ->
                    Toast.makeText(this, "Error ${err.message}", Toast.LENGTH_LONG).show()
                }
        }
    }


}