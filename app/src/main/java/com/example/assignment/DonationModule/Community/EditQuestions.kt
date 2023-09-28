package com.example.assignment.DonationModule.Community

import android.app.Activity
import android.content.Context
import android.content.Intent
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
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.assignment.R
import com.example.assignment.databinding.QuestionFormLayoutBinding
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.lang.Exception

class EditQuestions : AppCompatActivity() {
    private lateinit var bindingEditQuestionsBinding: QuestionFormLayoutBinding
    private val GALLERY_REQUEST_CODE = 1
    private lateinit var  title: EditText
    private lateinit var  question: EditText
    private lateinit var updatedQuestImage: String
    private lateinit var byteArrayimage: ByteArray
    private var imageUri : Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingEditQuestionsBinding =
            DataBindingUtil.setContentView(this, R.layout.question_form_layout)

        val questionId = intent.getStringExtra("questionId")
        val questionTitle = intent.getStringExtra("questionTitle")
        val questionDesc = intent.getStringExtra("questionDescription")
        val stringImage = intent.getStringExtra("questionImage")
        val email = intent.getStringExtra("email")

        if(stringImage != null){
            updatedQuestImage=stringImage!!
        }

        title = bindingEditQuestionsBinding.inputTitle
        question = bindingEditQuestionsBinding.inputQuest


        populateFormFields(questionTitle!!, questionDesc!!,stringImage!!)
        bindingEditQuestionsBinding.galleryButton.setOnClickListener {
            val galleryIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            ActivityResultLauncher.launch(galleryIntent)
        }

        bindingEditQuestionsBinding.uploadBtn.setOnClickListener {
            var questTitle = title.text.toString()
            var questDesc = question.text.toString()


            if (checkForInternet(this)) {
                updateQuestData(questionId!!,questTitle!!,questDesc!!,email!!,updatedQuestImage!!)
            } else {
                updateQuestDataInLocal(questionId!!,questTitle!!,questDesc!!,email!!,updatedQuestImage!!)

            }



            Toast.makeText(applicationContext, "Question has been updated", Toast.LENGTH_LONG).show()

            finish()
        }

    }
    private fun populateFormFields(questionTitle:String,questionDesc:String,questionImage:String) {
        bindingEditQuestionsBinding.inputTitle.setText(questionTitle)
        bindingEditQuestionsBinding.inputQuest.setText(questionDesc)

        if(questionImage==null){
            bindingEditQuestionsBinding.imageView.visibility = View.GONE
        }else {
            byteArrayimage = Base64.decode(questionImage, Base64.DEFAULT)

            val questionBitmap = BitmapFactory.decodeByteArray(
                byteArrayimage,
                0,
                byteArrayimage.size
            )
            bindingEditQuestionsBinding.imageView.setImageBitmap(questionBitmap)
            bindingEditQuestionsBinding.imageView.visibility = View.VISIBLE
        }


    }

    private fun updateQuestDataInLocal(questionId: String, questTitle: String, questDesc: String, email: String, updatedQuestImage: String) {

        if (questTitle.isEmpty()) {
            Toast.makeText(this, "Please fill in your title.", Toast.LENGTH_LONG).show()
        }

        if (questDesc.isEmpty()) {
            Toast.makeText(this, "Please fill in your question.", Toast.LENGTH_LONG).show()
        }

        if (questTitle.isNotEmpty() && questDesc.isNotEmpty()) {

            if (questTitle.length > 60) {
                Toast.makeText(this, "Title cannot more than 60 characters", Toast.LENGTH_LONG)
                    .show()
            }
            if (questDesc.length > 500) {
                Toast.makeText(this, "Question cannot more than 500 characters", Toast.LENGTH_LONG)
                    .show()
            }

            val sqlHelper = SqlHelper(this)
            val existingQuestion = sqlHelper.retrieveQuestion(questionId)
            val quest = QuestionEntity(
                questionId = questionId,
                questTitle = questTitle,
                email = email,
                question = questDesc,
                questImage = updatedQuestImage
            )

            if (existingQuestion != null) {

                val rowsAffected = sqlHelper.updateQuestion(quest, false)
                if (rowsAffected > 0) {
                    Toast.makeText(
                        applicationContext,
                        "Network error, question has been updated in local.",
                        Toast.LENGTH_LONG
                    ).show()
                    val sharedPreferences = getSharedPreferences("unSyncData", Context.MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("syncStatus", "pending")
                    editor.apply()
                    finish()
                }else{
                    Toast.makeText(applicationContext, "Failed to update question.", Toast.LENGTH_LONG)
                        .show()
                }
            } else {
                Toast.makeText(applicationContext, "No question found.", Toast.LENGTH_LONG)
                    .show()

            }
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

    private val ActivityResultLauncher = registerForActivityResult<Intent, ActivityResult>(
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

                updatedQuestImage = Base64.encodeToString(bytes, Base64.DEFAULT)
                bindingEditQuestionsBinding.imageView.setImageURI(imageUri)
                Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show()

            } catch (ex: Exception) {
                Toast.makeText(this, "Failed to add image", Toast.LENGTH_SHORT).show()

            }
        }
    }



    private fun updateQuestData(questionId:String,questTitle:String,questDesc:String,email:String,questImage:String) {

        if (questTitle.isEmpty()) {
            Toast.makeText(this, "Please fill in your title.", Toast.LENGTH_LONG).show()
        }

        if (questDesc.isEmpty()) {
            Toast.makeText(this, "Please fill in your question.", Toast.LENGTH_LONG).show()
        }

        if (questTitle.isNotEmpty() && questDesc.isNotEmpty()) {

            if (questTitle.length > 60) {
                Toast.makeText(this, "Title cannot more than 60 characters", Toast.LENGTH_LONG)
                    .show()
            }
            if (questDesc.length > 500) {
                Toast.makeText(this, "Question cannot more than 500 characters", Toast.LENGTH_LONG)
                    .show()
            }

            val dbRef = FirebaseDatabase.getInstance().getReference("questions").child(questionId)
            val questInfo = QuestionEntity(
                questionId = questionId,
                questTitle = questTitle,
                email = email,
                question = questDesc,
                questImage = questImage
            )


            dbRef.setValue(questInfo)

            val sqlHelper = SqlHelper(this)
            val existingQuestion = sqlHelper.retrieveQuestion(questionId)
            val rowsAffected = sqlHelper.updateQuestion(existingQuestion, wifiConnection = true)
            if (rowsAffected > 0) {
                Toast.makeText(applicationContext, "Question has been updated.", Toast.LENGTH_LONG)
                    .show()
                finish() // Close this activity after successful update
            } else {
                Toast.makeText(applicationContext, "Failed to update question", Toast.LENGTH_LONG)
                    .show()

            }
        }
    }

//        bindingEditQuestionsBinding.uploadBtn.setOnClickListener {
//            // Retrieve updated question data from the form
//            val updatedTitle = bindingEditQuestionsBinding.inputTitle.text.toString()
//            val updatedDescription = bindingEditQuestionsBinding.inputQuest.text.toString()
//            val sqlHelper = SqlHelper(this)
//            val existingQuestion = sqlHelper.retrieveQuestion(questionId)
//
//            if (existingQuestion != null) {
//                // Create a new Question object with updated data
//                // Update the question in the database
//                val rowsAffected = sqlHelper.updateQuestion(
//                    questionId,
//                    updatedTitle,
//                    updatedDescription,
//                    questImage
//                )
//                if (rowsAffected > 0) {
//
//                    finish() // Close this activity after successful update
//                }
//            } else {
//                // Update failed
//                // Handle the error, e.g., show an error message to the user
//            }
//        }
//    }
//
//
//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == GALLERY_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
//            val selectedImageUri = data.data
//            bindingEditQuestionsBinding.imageView.visibility = View.VISIBLE
//            bindingEditQuestionsBinding.imageView.setImageURI(selectedImageUri)
//
//
//            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedImageUri)
//            questImage = bitmapToByteArray(bitmap)
//            Toast.makeText(this, "Image added", Toast.LENGTH_SHORT).show()
//
//        }
//    }
//
//    fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
//        return stream.toByteArray()
//    }

//
//    private fun fetchQuestionById(context: Context, questionId: Int):QuestionEntity {
//        val sqlHelper = SqlHelper(context)
//        val questList = sqlHelper.retrieveQuestion(questionId)
//        return questList
//    }





}