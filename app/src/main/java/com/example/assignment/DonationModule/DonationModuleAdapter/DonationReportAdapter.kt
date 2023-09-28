package com.example.assignment.DonationModule.DonationModuleAdapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.DonationModule.Donation
import com.example.assignment.R
import com.google.firebase.firestore.FirebaseFirestore

class DonationReportAdapter(private val donationList : ArrayList<Donation>) : RecyclerView.Adapter<DonationReportAdapter.MyViewHolder>() {

    val db = FirebaseFirestore.getInstance()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.donation_report_item,
            parent,false)
        return MyViewHolder(itemView)

    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentitem = donationList[position]

        holder.id.text = currentitem.id
        holder.date.text = currentitem.date
        holder.amount.text = currentitem.amount

    }


    override fun getItemCount(): Int {

        return donationList.size
    }


    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val id : TextView = itemView.findViewById(R.id.idTv)
        val date : TextView = itemView.findViewById(R.id.dateText)
        val amount : TextView = itemView.findViewById(R.id.amountText)

    }

}

