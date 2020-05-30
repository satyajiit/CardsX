package com.argonlabs.cardsx.adapters

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.models.CardsModel
import com.argonlabs.cardsx.ui.activities.CardDetailsDialogFragment
import com.google.android.material.card.MaterialCardView

open class CardsAdapter(private var cardsList: List<CardsModel>, var mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_cards_recycler, parent, false)
        return ViewHolderData(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ViewHolderData).bindData(cardsList[position])
    }

    override fun getItemCount(): Int {
        return cardsList.size
    }

    /**
     * bind ViewHolder
     */
    protected inner class ViewHolderData internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val bankNameTv: TextView = itemView.findViewById(R.id.bankNameTv)
        private val cardNumber: TextView = itemView.findViewById(R.id.cardNumber)
        private val cardHolderName: TextView = itemView.findViewById(R.id.cardHolderName)
        private val cardExpiryTv: TextView = itemView.findViewById(R.id.cardExpiryTv)
        private var cardLogo: ImageView = itemView.findViewById(R.id.cardLogo)
        private var mainCard: MaterialCardView = itemView.findViewById(R.id.mainCard)
        fun bindData(item: CardsModel) {
            bankNameTv.text = item.bankName
            var temp = item.cardNumber
            temp = (temp?.substring(0, 7) ?: "XXXXX") + "XXXXXXXXX"
            cardNumber.text = temp.replace("....".toRegex(), "$0 ")
            cardHolderName.text = item.cardHolderName
            temp = item.expiryDate
            temp = StringBuilder(temp.toString()).insert(2, "/").toString()
            cardExpiryTv.text = temp
            bankNameTv.text = item.bankName
            val resourceID: Int = when (item.cardType) {
                "VISA" -> R.drawable.ic_visa
                "MASTERCARD" -> R.drawable.ic_mastercard
                "AMERICAN_EXPRESS" -> R.drawable.ic_amex
                "DINERS_CLUB" -> R.drawable.ic_dinners_club
                "DISCOVER" -> R.drawable.ic_discover
                "JCB" -> R.drawable.ic_jcb
                "CHINA_UNION_PAY" -> R.drawable.ic_union_pay
                else -> R.drawable.ic_credit_card
            }
            cardLogo.setImageResource(resourceID)
            mainCard.setCardBackgroundColor(item.cardColor!!)
            mainCard.setOnClickListener {
                val addNewCardDialogFragment = CardDetailsDialogFragment()
                val bundle = Bundle()
                bundle.putString("BANK_NAME", item.bankName)
                bundle.putString("CARD_TYPE", item.cardType)
                bundle.putString("CARD_NUM", item.cardNumber)
                bundle.putString("USER_NAME", item.cardHolderName)
                bundle.putString("EXPIRY", item.expiryDate)
                bundle.putString("CVV", item.cardCVV)
                bundle.putLong("DATE_ADDED", item.addedOnTimestamp)
                bundle.putString("DOCUMENT_ID", item.docID)
                bundle.putInt("CARD_COLOR", item.cardColor!!)
                addNewCardDialogFragment.arguments = bundle
                addNewCardDialogFragment.show((mContext as AppCompatActivity).supportFragmentManager, "DETAILS_CARD")
            }
        }

    }

}