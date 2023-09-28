// EmailAdapter.kt

package com.example.assignment.UserManagement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R

class EmailAdapter(private val context: Context, private val itemClickListener: OnItemClickListener) :
    RecyclerView.Adapter<EmailAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(emailType: EmailType)
    }

    private val emailTypes: MutableList<EmailType> = mutableListOf()
    private val selectedItems: MutableList<EmailType> = mutableListOf()

    fun setData(data: List<EmailType>) {
        emailTypes.clear()
        emailTypes.addAll(data)
        notifyDataSetChanged()
    }

    fun clearData() {
        emailTypes.clear()
        notifyDataSetChanged()
    }

    fun getSelectedItems(): List<EmailType> {
        return selectedItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.list_item_email_type, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val emailType = emailTypes[position]
        holder.emailTypeNameTextView.text = emailType.typeName
        holder.schoolNameTextView.text = emailType.schoolName // Display school name

        holder.emailTypeCheckBox.isChecked = selectedItems.contains(emailType)

        holder.itemView.setOnClickListener {
            // Notify the click event to the click listener
            itemClickListener.onItemClick(emailType)
        }

        holder.emailTypeCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(emailType)
            } else {
                selectedItems.remove(emailType)
            }
        }
    }

    fun clearSelectedItems() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
    fun removeItem(emailType: EmailType) {
        emailTypes.remove(emailType)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return emailTypes.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTypeCheckBox: CheckBox = itemView.findViewById(R.id.emailTypeCheckBox)
        val emailTypeNameTextView: TextView = itemView.findViewById(R.id.emailTypeNameTextView)
        val schoolNameTextView: TextView = itemView.findViewById(R.id.schoolNameTextView)
    }
}
