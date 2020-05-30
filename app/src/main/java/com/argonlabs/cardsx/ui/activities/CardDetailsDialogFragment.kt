package com.argonlabs.cardsx.ui.activities

import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.base.BaseApplication.Companion.getmInstance
import com.argonlabs.cardsx.databinding.FragmentDialogCardDetailsBinding
import com.argonlabs.cardsx.managers.DialogCreator
import com.argonlabs.cardsx.utils.Utils
import com.argonlabs.cardsx.utils.Utils.Companion.getFormattedTime
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CardDetailsDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentDialogCardDetailsBinding
    var activity: Activity? = null
    var firebaseAuth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    var documentID: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialogTheme)
        activity = getmInstance()!!.activity
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window!!.attributes.windowAnimations = R.style.MyAnimation_Window
        dialog.window!!.setLayout(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT)
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentDialogCardDetailsBinding.inflate(inflater, container, false)
        setUpClickListeners()
        loadDataToTvs()
        return binding.root
    }

    private fun loadDataToTvs() {
        val bundle = arguments
        binding.bankNameTv.text = bundle!!.getString("BANK_NAME")
        binding.cardNumber.text = bundle.getString("CARD_NUM")!!.replace("....".toRegex(), "$0 ")
        binding.cardHolderName.text = bundle.getString("USER_NAME")
        var temp = bundle.getString("EXPIRY")
        temp = StringBuilder(temp!!).insert(2, "/").toString()
        binding.cardExpiryTv.text = temp
        binding.dataCardNumber.text = bundle.getString("CARD_NUM")
        binding.dataCardOwnerName.text = bundle.getString("USER_NAME")
        binding.dataCardBankName.text = bundle.getString("BANK_NAME")
        binding.dataCardExpiry.text = bundle.getString("EXPIRY")
        val tempp = bundle.getString("CVV")
        if (tempp!!.length < 3) binding.dataCardCVV.text = "Not Available." else binding.dataCardCVV.text = tempp
        binding.dataCardType.text = bundle.getString("CARD_TYPE")
        binding.dataCardAddedON.text = getFormattedTime(Date(bundle.getLong("DATE_ADDED")))

        //Set Card Type
        val resourceID: Int
        resourceID = when (bundle.getString("CARD_TYPE")) {
            "VISA" -> R.drawable.ic_visa
            "MASTERCARD" -> R.drawable.ic_mastercard
            "AMERICAN_EXPRESS" -> R.drawable.ic_amex
            "DINERS_CLUB" -> R.drawable.ic_dinners_club
            "DISCOVER" -> R.drawable.ic_discover
            "JCB" -> R.drawable.ic_jcb
            "CHINA_UNION_PAY" -> R.drawable.ic_union_pay
            else -> R.drawable.ic_credit_card
        }
        binding.cardLogo.setImageResource(resourceID)
        binding.mainCard.setCardBackgroundColor(bundle.getInt("CARD_COLOR"))
        documentID = bundle.getString("DOCUMENT_ID")
    }

    private fun setUpClickListeners() {
        binding.copyCardNumber.setOnClickListener { copyDataToClip(binding.dataCardNumber.text.toString()) }
        binding.copyCardOwnerName.setOnClickListener {  copyDataToClip(binding.dataCardOwnerName.text.toString()) }
        binding.copyCardBankName.setOnClickListener {  copyDataToClip(binding.dataCardNumber.text.toString()) }
        binding.copyCardExpiry.setOnClickListener { copyDataToClip(binding.dataCardOwnerName.text.toString()) }
        binding.copyCardCVV.setOnClickListener {  copyDataToClip(binding.dataCardNumber.text.toString()) }
        binding.copyCardType.setOnClickListener {  copyDataToClip(binding.dataCardOwnerName.text.toString()) }
        binding.bottomNav.setOnNavigationItemSelectedListener { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.nav_back) dismiss() else if (itemId == R.id.nav_remove) confirmDialog() else if (itemId == R.id.nav_help) Utils().showHelpDialog(activity, "This page shows you all the card details that you have stored.\nPlease note all these data are highly encrypted and can only be accessed by you.")
            true
        }
        binding.closeDialog.setOnClickListener { view: View? -> dismiss() }
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(binding.addLayout, msg, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.bottomNav)
                .setAction(getString(R.string.ok)) { }
                .show()
    }

    private fun deleteCard() {
        val dialogCreator = DialogCreator(activity!!)
        dialogCreator.showLoadingDialog()
        dialogCreator.updateDialogText("Deleting your card...")
        db.collection("/data/" + firebaseAuth.uid + "/cardsData")
                .document(documentID!!)
                .delete()
                .addOnSuccessListener {
                    showSnackBar("Deleted Card.")
                    dismiss()
                    dialogCreator.dialog.dismiss()
                }.addOnFailureListener {
                    showSnackBar("Delete Failed.")
                    dialogCreator.dialog.dismiss()
                }
    }

    private fun confirmDialog() {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Final Confirmation")
        builder.setMessage("You are about to delete the card from records.\nDo you really want to proceed ?")
        builder.setCancelable(false)
        builder.setPositiveButton("Yes") { _: DialogInterface?, _: Int -> deleteCard() }
        builder.setNegativeButton("No") { _: DialogInterface?, _: Int -> }
        builder.show()
    }

    private fun copyDataToClip(text: String?) {
        val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(getString(R.string.app_name), text)
        clipboard.setPrimaryClip(clip)
        showSnackBar("Copied Data.")
    }
}