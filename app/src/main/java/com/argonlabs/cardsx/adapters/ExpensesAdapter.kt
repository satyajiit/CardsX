package com.argonlabs.cardsx.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.models.ExpenseModel
import com.argonlabs.cardsx.utils.Utils
import java.util.*

open class ExpensesAdapter(private var expenseList: List<ExpenseModel>, var mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_tracker_single, parent, false)
        return ViewHolderData(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolderData).bindData(expenseList[position], position)
    }

    override fun getItemCount(): Int {
        return expenseList.size
    }



    /**
     * bind ViewHolder
     */
    protected inner class ViewHolderData internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val mainTitle: TextView = itemView.findViewById(R.id.mainTitle)
        private val description: TextView = itemView.findViewById(R.id.descriptionRow)
        private val cost: TextView = itemView.findViewById(R.id.costTv)
        private val date: TextView = itemView.findViewById(R.id.todayTv)
        fun bindData(item: ExpenseModel, position: Int) {
            mainTitle.text = item.mainTitle
            description.text = item.description
            cost.text = mContext.getString(R.string.rs_cost).replace("%cost%", item.cost.toString())
            if (!checkIfToDay(item.timeStamp))
             date.text = Utils.getFormattedTime2(Date(item.timeStamp))
            if (position > 0 && checkIfSameDay(expenseList[position - 1].timeStamp, item.timeStamp)) date.visibility = View.GONE
        }

    }

    fun checkIfToDay(epochInMillis: Long): Boolean {
        val now = Calendar.getInstance()
        val timeToCheck = Calendar.getInstance()
        timeToCheck.timeInMillis = epochInMillis
        return if (now[Calendar.YEAR] == timeToCheck[Calendar.YEAR]) now[Calendar.DAY_OF_YEAR] == timeToCheck[Calendar.DAY_OF_YEAR]
        else false
    }

    fun checkIfSameDay(epochFirst: Long, epochSecond: Long): Boolean {
        val now = Calendar.getInstance()
        now.timeInMillis = epochFirst
        val timeToCheck = Calendar.getInstance()
        timeToCheck.timeInMillis = epochSecond
        return if (now[Calendar.YEAR] == timeToCheck[Calendar.YEAR]) now[Calendar.DAY_OF_YEAR] == timeToCheck[Calendar.DAY_OF_YEAR]
        else false
    }

}