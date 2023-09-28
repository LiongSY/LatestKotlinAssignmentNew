package com.example.assignment.UserManagement
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.assignment.R

class UserAdapter(
    private val context: Context,
    private var userAccounts: List<UserData>
) : RecyclerView.Adapter<UserAdapter.ViewHolder>(), Filterable {

    private var userAccountsFiltered: List<UserData> = userAccounts

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val emailTextView: TextView = itemView.findViewById(R.id.emailValueTextView)
        val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameValueTextView)
        val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameValueTextView)
        val phoneNumTextView: TextView = itemView.findViewById(R.id.phoneNumValueTextView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(context).inflate(R.layout.item_user_account, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val userAccount = userAccountsFiltered[position]

        holder.emailTextView.text = userAccount.email
        holder.firstNameTextView.text = userAccount.firstName
        holder.lastNameTextView.text = userAccount.lastName
        holder.phoneNumTextView.text = userAccount.phoneNum
    }

    override fun getItemCount(): Int {
        return userAccountsFiltered.size
    }

    // Update user account data
    fun setUserAccounts(userAccounts: List<UserData>) {
        this.userAccounts = userAccounts
        notifyDataSetChanged()
    }

    // Filter logic for search
    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val searchText = constraint.toString().toLowerCase().trim()

                userAccountsFiltered = if (searchText.isEmpty()) {
                    userAccounts // If the search text is empty, return the original list
                } else {
                    userAccounts.filter { user ->
                        user.email.toLowerCase().contains(searchText) ||
                                user.firstName.toLowerCase().contains(searchText) ||
                                user.lastName.toLowerCase().contains(searchText) ||
                                user.phoneNum.toLowerCase().contains(searchText)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = userAccountsFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                userAccountsFiltered = results?.values as List<UserData>
                notifyDataSetChanged()
            }
        }
    }
}
