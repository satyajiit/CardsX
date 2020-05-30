package com.argonlabs.cardsx.ui.fragments

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.View.OnFocusChangeListener
import android.widget.CompoundButton
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.fragment.app.DialogFragment
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.base.BaseApplication.Companion.getmInstance
import com.argonlabs.cardsx.databinding.FragmentAddNewCardDialogBinding
import com.argonlabs.cardsx.helpers.DetectCardTypeEnum.Companion.detect
import com.argonlabs.cardsx.managers.DialogCreator
import com.argonlabs.cardsx.utils.Locker
import com.argonlabs.cardsx.utils.MaterialColorPalette.randomColor
import com.argonlabs.cardsx.utils.Utils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

/**
 * A simple [Fragment] subclass.
 */
class AddNewCardDialogFragment : DialogFragment() {
    private lateinit var binding: FragmentAddNewCardDialogBinding
    var activity: Activity? = null
    var firebaseAuth = FirebaseAuth.getInstance()
    var sessionKey: String? = null
    private val db = FirebaseFirestore.getInstance()
    var cardType: String? = null
    var cardColor = -0xfc560c
    var locker: Locker? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomDialogTheme)
        activity = getmInstance()!!.activity
        sessionKey = getmInstance()!!.sessionKey
        locker = Locker(sessionKey)
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
        binding = FragmentAddNewCardDialogBinding.inflate(inflater, container, false)
        setUpClickListeners()
        setUpFocusListeners()
        setUpTextWatchers()
        return binding.root
    }

    private fun setUpClickListeners() {
        binding.closeDialog.setOnClickListener { dismiss() }
        binding.randomizeCardColor.setOnClickListener {
            cardColor = randomColor
            binding.mainCard.setCardBackgroundColor(cardColor)
        }
    }

    fun setUpFocusListeners() {
        binding.nameEt.onFocusChangeListener = OnFocusChangeListener { view: View?, b: Boolean -> if (b) binding!!.nameTl.error = null }
        binding.cardCVVEt.onFocusChangeListener = OnFocusChangeListener { view: View?, b: Boolean -> if (b) binding!!.cardCVVTl.error = null }
        binding.cardExpiryEt.onFocusChangeListener = OnFocusChangeListener { view: View?, b: Boolean -> if (b) binding!!.cardExpiryTl.error = null }
        binding.bankNameEt.onFocusChangeListener = OnFocusChangeListener { view: View?, b: Boolean -> if (b) binding!!.bankNameTl.error = null }
        binding.cardNumberEt.onFocusChangeListener = OnFocusChangeListener { view: View?, b: Boolean -> if (b) binding!!.cardNumberTl.error = null }
    }

    private fun setUpTextWatchers() {
        binding.nameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length == 0) {
                    binding.cardHolderName.text = getString(R.string.cardholder_name)
                } else binding.cardHolderName.text = charSequence.toString()
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.bankNameEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.isEmpty()) {
                    binding.bankNameTv.text = getString(R.string.bank_name)
                } else binding.bankNameTv.text = charSequence.toString()
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.cardNumberEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length == 0) {
                    binding.cardNumber.text = getString(R.string.number_space)
                    binding.cardLogo.visibility = View.INVISIBLE
                } else {
                    cardType = detect(charSequence.toString().replace(" ", "")).toString()
                    binding.cardNumber.text = charSequence.toString().replace("....".toRegex(), "$0 ")
                    val resourceID: Int = when (cardType) {
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
                    binding.cardLogo.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.cardExpiryEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length == 0) {
                    binding.cardExpiryTv.text = getString(R.string.mm_yy)
                } else {
                    binding.cardExpiryTv.text = charSequence.toString().replace("..".toRegex(), "$0/")
                    if (binding.cardExpiryTv.text.length >= 6) binding.cardExpiryTv.text = binding.cardExpiryTv.text.toString().substring(0, 5)
                }
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.cvvSwitch.setOnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            if (b) binding.cardCVVTl.isEnabled = true else {
                binding.cardCVVTl.isEnabled = false
                binding.cardCVVEt.setText("")
            }
        }
        binding.bottomNav.setOnNavigationItemSelectedListener { item: MenuItem ->
            val itemId = item.itemId
            if (itemId == R.id.nav_back) dismiss() else if (itemId == R.id.nav_save) validateData() else if (itemId == R.id.nav_help) Utils().showHelpDialog(activity, "Use this page to add cards to your database.\nPlease note all these data are highly encrypted and can only be accessed by you.")
            true
        }
        var drawable = binding.bottomNav.menu.getItem(1).icon
        drawable = DrawableCompat.wrap(drawable!!)
        DrawableCompat.setTint(drawable, ContextCompat.getColor(activity!!, R.color.black))
        binding.bottomNav.menu.getItem(1).icon = drawable
    }

    private fun validateData() {
        if (binding.nameEt.length() < 3) binding.nameTl.error = "Invalid name." else if (binding!!.cardNumberEt.length() < 11) binding!!.cardNumberTl.error = "Invalid Card Number." else if (binding!!.bankNameEt.length() < 5) binding!!.bankNameTl.error = "Invalid Bank Name." else if (binding!!.cardExpiryEt.length() != 4) binding!!.cardExpiryTl.error = "Invalid Expiry. Should be MMYY" else if (binding!!.cvvSwitch.isChecked && binding!!.cardCVVEt.length() < 3) binding!!.cardCVVTl.error = "Invalid CVV." else uploadData()
    }

    private fun uploadData() {
        val dialogCreator = DialogCreator(activity!!)
        dialogCreator.showLoadingDialog()
        dialogCreator.updateDialogText("Adding Your Awesome Card...")
        db.collection("data/" + firebaseAuth.uid + "/cardsData")
                .add(createCardsObj())
                .addOnSuccessListener { documentReference: DocumentReference? ->
                    showSnackBar("Card Added Successful!")
                    dialogCreator.dialog.dismiss()
                    resetData()
                }
                .addOnFailureListener {
                    showSnackBar("Failed. Error Code 105")
                    dialogCreator.dialog.dismiss()
                }
    }

    private fun resetData() {
        binding.cardNumberEt.setText("")
        binding.bankNameEt.setText("")
        binding.cardCVVEt.setText("")
        binding.cardExpiryEt.setText("")
        binding.nameEt.setText("")
    }

    private fun createCardsObj(): Map<String, Any> {
        val cardData: MutableMap<String, Any> = HashMap()
        cardData["addedOnTimestamp"] = System.currentTimeMillis()
        cardData["cardColor"] = cardColor.toString()
        cardData[locker!!.encryptDataForUpload("bankName")] = locker!!.encryptDataForUpload(binding.bankNameEt.text.toString())
        cardData[locker!!.encryptDataForUpload("cardHolderName")] = locker!!.encryptDataForUpload(binding.nameEt.text.toString())
        cardData[locker!!.encryptDataForUpload("cardType")] = locker!!.encryptDataForUpload(cardType!!)
        cardData[locker!!.encryptDataForUpload("cardNumber")] = locker!!.encryptDataForUpload(binding.cardNumberEt.text.toString())
        cardData[locker!!.encryptDataForUpload("expiryDate")] = locker!!.encryptDataForUpload(binding.cardExpiryEt.text.toString())
        cardData[locker!!.encryptDataForUpload("cardCVV")] = locker!!.encryptDataForUpload(binding.cardCVVEt.text.toString())
        return cardData
    }

    private fun showSnackBar(msg: String) {
        Snackbar.make(binding.addLayout, msg, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.bottomNav)
                .setAction(getString(R.string.ok)) { view: View? -> }
                .show()
    }
}