package com.example.assignment.DonationModule.Community

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R

class CommentsAdapter(private val comments: List<CommentEntity>) : RecyclerView.Adapter<CommentsAdapter.CommentViewHolder>()  {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.comments_section_layout, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]

        holder.commentorEmail.text = comment.commentorEmail
        holder.comments.text = comment.comment



    }

    override fun getItemCount(): Int {
        return comments.size
    }

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val commentorEmail: TextView = itemView.findViewById(R.id.commentorEmail)
        val comments: TextView = itemView.findViewById(R.id.userComments)

    }
}