package com.example.assignment.DonationModule.Community

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R

class QuestionAdapter(private val questionList: ArrayList<QuestionEntity>, private val questionFrag:Boolean)
    : RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder>() {

    private var questionArray: ArrayList<QuestionEntity> = questionList
    private lateinit var qListener: onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        qListener = clickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.question_list_layout, parent, false)
        return QuestionViewHolder(itemView,qListener)
    }



    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val actionButtons = holder.itemView.findViewById<View>(R.id.actionButtons)
        val question = questionArray[position]

        if (questionFrag) {

            holder.email.visibility = View.GONE
            actionButtons.visibility = View.VISIBLE


        } else {
            holder.email.visibility = View.VISIBLE
            actionButtons.visibility = View.GONE

        }
        holder.title.text = question.questTitle
        holder.questDesc.text = question.question
        holder.email.text = question.email

        holder.btnEdit.setOnClickListener{
            val editIntent = Intent(holder.itemView.context, EditQuestions::class.java)
            editIntent.putExtra("questionId", question.questionId)
            editIntent.putExtra("questionTitle", question.questTitle)
            editIntent.putExtra("questionDescription", question.question)
            editIntent.putExtra("questionImage", question.questImage)
            editIntent.putExtra("email", question.email)
            holder.itemView.context.startActivity(editIntent)
        }


        holder.itemView.setOnClickListener {
            val intent = Intent(holder.itemView.context, QuestionDetails::class.java)
            intent.putExtra("questionId", question.questionId)
            intent.putExtra("questionTitle", question.questTitle)
            intent.putExtra("questionDescription", question.question)
            intent.putExtra("questionImage", question.questImage)
            intent.putExtra("email", question.email)
            holder.itemView.context.startActivity(intent)
        }


    }




    override fun getItemCount(): Int {
        return questionList.size
    }

    inner class QuestionViewHolder(itemView: View, clickListener: onItemClickListener): RecyclerView.ViewHolder(itemView){
        val title : TextView = itemView.findViewById(R.id.questionTitleTextView)
        val questDesc : TextView = itemView.findViewById(R.id.userComments)
        val email: TextView = itemView.findViewById(R.id.commentorEmail)
        val btnEdit : ImageButton = itemView.findViewById(R.id.editButton)
        val context = itemView.context
        init {
            itemView.setOnClickListener {
                clickListener.onItemClick(adapterPosition)
            }

        }

    }
}
