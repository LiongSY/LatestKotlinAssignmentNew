package com.example.assignment.Resources

import com.example.assignment.R

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.DonationModule.Community.QuestionDetails


//connect recycleview and database
/*(private val resourceList: ArrayList<ResourceModel>,private val educatorResources:Boolean)*/
class ResourcesAdapter
    : RecyclerView.Adapter<ResourcesAdapter.ResourcesViewHolder>() {
    private var dataSet: ArrayList<ResourceModel> = ArrayList()
    private var onClickItem: ((ResourceModel) -> Unit)? = null

    class ResourcesViewHolder( itemView: View): RecyclerView.ViewHolder(itemView) {
        var title = itemView.findViewById<TextView>(R.id.tvResTitle)
        var description = itemView.findViewById<TextView>(R.id.tvResDescription)
        var viewPdfButton = itemView.findViewById<TextView>(R.id.fileButton)
        var name = itemView.findViewById<TextView>(R.id.tvTutor)
        var educatorResourcebtn = itemView.findViewById<Button>(R.id.educatorResourceBtn)
        var saveImageButton = itemView.findViewById<ImageButton>(R.id.saveImageButton)
    }

    override fun onBindViewHolder(holder: ResourcesViewHolder, position: Int) {
        val resources = dataSet[position]

        if (resources == null) {
            holder.name.visibility = View.GONE
        }
// need to get email from sharedpref to validate
//           holder.educatorResourcebtn.visibility = View.GONE

        //bind data to viewholder
        holder.name.text = resources.name
        holder.title.text = resources.resourceTitle
        holder.description.text = resources.resourceDesc

        holder.title.text = resources.resourceTitle
        holder.description.text = resources.resourceDesc
        holder.name.text = resources.email

        holder.viewPdfButton.setOnClickListener {

            Log.d("File",resources.resourceDocument)
            val context = holder.itemView.context
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setDataAndType(Uri.parse(resources.resourceDocument), "application/pdf")
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)
        }

        holder.saveImageButton.setOnClickListener {
            onClickItem?.invoke(resources)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, DetailsResource::class.java)
            intent.putExtra("resourceId", resources.resourceId)
            intent.putExtra("resourceTitle", resources.resourceTitle)
            intent.putExtra("resourceDesc", resources.resourceDesc)
            intent.putExtra("resourcePath", resources.resourceDocument)
            intent.putExtra("resourceEmail", resources.email)
            intent.putExtra("resourceName", resources.name)

            holder.itemView.context.startActivity(intent)


        }
    }

    fun setData(newData: ArrayList<ResourceModel>){
        dataSet = newData
        notifyDataSetChanged()
    }

    fun setOnClickItem(callback: (ResourceModel) -> Unit) {
        this.onClickItem = callback
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResourcesViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.resource_list_layout, parent, false)
        return ResourcesViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }



    //handle item click

//                val transaction = fragmentManager.beginTransaction()
//                transaction.replace(R.id.container ,fragment) // Replace with your container view ID
//                transaction.addToBackStack(null) // Optional: Allows you to navigate back to the previous Fragment
//                transaction.commit()

//    Handle when click
//           holder.itemView.setOnClickListener{
//               //create an intent to open the Details Activity
//               val intent = Intent(context,DetailsResource::class.java)
//               //pass the resource data to the detailsActivity
//                intent.putExtra("resourceModel", resources)
//
//               context.startActivity(intent)
//           }

}