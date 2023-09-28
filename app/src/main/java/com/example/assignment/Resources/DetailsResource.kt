package com.example.assignment.Resources

import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import android.media.MediaScannerConnection
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.assignment.DonationModule.Donation
import com.example.assignment.Homepage
import com.example.assignment.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.database.FirebaseDatabase
import java.io.File
import java.io.FileOutputStream

class DetailsResource : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var viewPDFBtn: Button
    private lateinit var tvName: TextView
    private lateinit var btnEdit: ImageButton
    private lateinit var btnDelete: ImageButton

    private lateinit var stringFileName : String
    private val FILE_PICKER_REQUEST_CODE = 123

    private lateinit var botNav: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details_resource)


        var resId =  intent.getStringExtra("resourceId").toString()
        var resName = intent.getStringExtra("resourceName").toString()
        var resEmail = intent.getStringExtra("resourceEmail").toString()
        var resTitle = intent.getStringExtra("resourceTitle").toString()
        var resDesc = intent.getStringExtra("resourceDesc").toString()
        var resPath = intent.getStringExtra("resourcePath").toString()
        initView()
        setValuesToViews()

        //edit
        btnEdit.setOnClickListener {
                openUpdateDialog(
                    resId, resName, resEmail, resTitle, resDesc, resPath
                )
        }


        //delete
        btnDelete.setOnClickListener {
            if(isNetworkAvailable()){
                confirmationDialog(resId)
            }else{
                Toast.makeText(this, "Please ensure that you have an internet connection", Toast.LENGTH_LONG).show()
            }

        }

        viewPDFBtn.setOnClickListener {

        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun confirmationDialog(resourceId: String) {
        val dDialogView = layoutInflater.inflate(R.layout.delete_dialog, null)
        val dialogTitle = dDialogView.findViewById<TextView>(R.id.dialogTitle)
        val dialogMessage = dDialogView.findViewById<TextView>(R.id.dialogMessage)
        val btnCancel = dDialogView.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dDialogView.findViewById<Button>(R.id.btnConfirm)

        val dDialog = AlertDialog.Builder(this)
            .setView(dDialogView)
            .create()

        dialogTitle.text = "Confirmation"
        dialogMessage.text = "Are you sure you want to perform this action?"

        btnCancel.setOnClickListener {

            dDialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            if(isNetworkAvailable()){
                deleteRecord(resourceId)
                deleteInLocal(resourceId)
                dDialog.dismiss()
            }else{
                Toast.makeText(this, "Please ensure that you have an internet connection", Toast.LENGTH_LONG).show()
                dDialog.dismiss()
            }
            dDialog.dismiss()
        }

        dDialog.show()
    }
    private fun deleteInLocal(resourceId: String){
        val sqLiteHelper = SQLiteHelper(this)
        Log.d("delete local",resourceId)
        sqLiteHelper.deleteResource(resourceId)
        Toast.makeText(this, "Resource has been deleted", Toast.LENGTH_LONG).show()
        finish()
    }

    //delete
    private fun deleteRecord(id: String) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Resources").child(id)
        val mTask = dbRef.removeValue()

        mTask.addOnSuccessListener {

        }.addOnFailureListener { error ->
            Toast.makeText(this,"Deleting Err ${error.message}", Toast.LENGTH_LONG).show()
        }

    }



    private fun initView() {
        tvName = findViewById(R.id.detailName)
        tvTitle = findViewById(R.id.detailTitle)
        tvDescription = findViewById(R.id.detailDesc)
        viewPDFBtn = findViewById(R.id.detailFileBtn)

        btnEdit = findViewById(R.id.editBtn)
        btnDelete = findViewById(R.id.deleteBtn)
    }

    private fun setValuesToViews() {
        tvName.text = intent.getStringExtra("resourceName")
        tvTitle.text = intent.getStringExtra("resourceTitle")
        tvDescription.text = intent.getStringExtra("resourceDesc")
        var filePath = intent.getStringExtra("resourcePath")
        viewPDFBtn.setOnClickListener {

            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(filePath), "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FILE_PICKER_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val selectedFileUri = data.data
            if (selectedFileUri != null) {

                // Handle the selected file URI here (e.g., display its path)
                stringFileName = selectedFileUri.toString()
            }
        }
    }

    private fun openUpdateDialog(
        resourceId: String,
        name:String,
        email:String,
        title: String,
        description: String,
        fileUrl:String,
    ) {
        val mDialog = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val mDialogView = inflater.inflate(R.layout.update_dialog, null)

        mDialog.setView(mDialogView)


        val updateTitle = mDialogView.findViewById<EditText>(R.id.updateTitle)
        val updateDesc = mDialogView.findViewById<EditText>(R.id.updateDesc)
        val btnUpload = mDialogView.findViewById<Button>(R.id.btnUpload)
        var fileName = mDialogView.findViewById<TextView>(R.id.viewPDF)
        val updateDataBtn = mDialogView.findViewById<Button>(R.id.updateDataBtn)

        updateTitle.setText(title)
        updateDesc.setText(description)
        fileName.setText(fileUrl)
        stringFileName=fileUrl
        mDialog.setTitle("Updating $title Resource Info")

        val alertDialog = mDialog.create()
        alertDialog.show()

        //btnUpload = upload new pdf file
        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf" //specify PDF files
            startActivityForResult(intent,FILE_PICKER_REQUEST_CODE)


        }
        updateDataBtn.setOnClickListener {
            if(isNetworkAvailable()) {
                updateResData(
                    resourceId,
                    email,
                    updateTitle.text.toString(),
                    updateDesc.text.toString(),
                    stringFileName
                )
                updateResDataLocal(
                    resourceId,
                    name,
                    email,
                    updateTitle.text.toString(),
                    updateDesc.text.toString(),
                    stringFileName
                )

                Toast.makeText(applicationContext, "Resource have been updated", Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(applicationContext, "Please ensure that you have an internet connection", Toast.LENGTH_LONG).show()
            }
            val intent = Intent(this, ResourcesList::class.java)
            finish()
            startActivity(intent)
            alertDialog.dismiss()
        }


//        updateDataBtn.setOnClickListener {
//            updateResDataLocal(
//                resourceId,
//                name,
//                email,
//                updateTitle.text.toString(),
//                updateDesc.text.toString(),
//                stringFileName
//            )
//
//            Toast.makeText(applicationContext, "Resource have been updated", Toast.LENGTH_LONG).show()
//
////            tvTitle.text = updateTitle.text.toString()
////            tvDescription.text = updateDesc.text.toString()
////            viewPDF.text = viewPDF.text.toString()
//            val intent = Intent(this, ResourcesList::class.java)
//            finish()
//            startActivity(intent)
//            alertDialog.dismiss()
//        }

    }

    private fun updateResDataLocal(
        resourceId: String,
        name:String,
        email:String,
        title: String,
        description: String,
        fileUrl:String,
    ) {

        val sqLiteHelper = SQLiteHelper(this)
        val resource = sqLiteHelper.getResource(resourceId)

        Log.d("This is selected updated quest",resource.resourceId)
        Log.d("This is selected updated quest",resource.resourceTitle)
        if(resource != null) {
            val resource = ResourceModel(resourceId,name,email,title,fileUrl,description)

            val rowsAffected = sqLiteHelper.updateResource(resource)
            if (rowsAffected) {
                Toast.makeText(
                    applicationContext,
                    "Network error, question has been updated.",
                    Toast.LENGTH_LONG
                ).show()

            }
        }
    }

    //Update
    private fun updateResData(
        resourceId: String,
        email:String,
        title: String,
        description: String,
        fileUrl:String,
    ) {

        val dbRef = FirebaseDatabase.getInstance().getReference("Resources").child(resourceId)
        val resource = ResourceModel(resourceId,"Chin@gmail.com",email,title,fileUrl,description)
        dbRef.setValue(resource)
        finish()
    }
}