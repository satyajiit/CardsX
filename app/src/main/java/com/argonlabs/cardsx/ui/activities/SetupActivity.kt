package com.argonlabs.cardsx.ui.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Base64
import android.view.View
import android.view.View.OnFocusChangeListener
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.base.BaseActivity
import com.argonlabs.cardsx.base.BaseApplication.Companion.getmInstance
import com.argonlabs.cardsx.databinding.ActivitySetupBinding
import com.argonlabs.cardsx.managers.ActivitySwitchManager
import com.argonlabs.cardsx.utils.Locker.Companion.encryptData
import com.google.firebase.firestore.SetOptions
import java.util.*

class SetupActivity : BaseActivity() {
    private lateinit var binding: ActivitySetupBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponents()
    }

    override fun initComponents() {
        var tempName = mAuth!!.currentUser!!.displayName
        if (tempName!!.contains(" ")) tempName = tempName.substring(0, tempName.indexOf(' '))

        //Set TVS
        binding.usernameTv.text = getString(R.string.usernameHi).replace("%name%", tempName)
        setFocusListeners()
        setListeners()
    }

    private fun setListeners() {
        binding.keyEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (charSequence.length == 6) binding.encryptBtn.visibility = View.VISIBLE else binding.encryptBtn.visibility = View.INVISIBLE
            }

            override fun afterTextChanged(editable: Editable) {}
        })
        binding.closeDialog.setOnClickListener { view: View? -> finish() }
        binding.encryptBtn.setOnClickListener { view: View? ->
            if (binding.keyEt.length() == 6) {
                val test = encryptData(mAuth!!.uid!!, binding.keyEt.text.toString())
                val data = Base64.encodeToString(test, Base64.NO_WRAP)
                sharedPreferences!!.edit().putString("hexCode", data).apply()
                sharedPreferences!!.edit().putBoolean("fastAccess", true).apply()
                updateStatus(data)
                getmInstance()!!.sessionKey = binding.keyEt.text.toString()
                ActivitySwitchManager(this@SetupActivity, MainActivity::class.java).openActivity()

//                    byte[] array = Base64.decode(sharedPreferences.getString("myByteArray", ""), Base64.NO_WRAP);
//
//
//
//                    try {
//                        Toast.makeText(SetupActivity.this, new String(Locker.decryptData(array, "tessst"), "UTF-8"), Toast.LENGTH_SHORT).show();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
            }
        }
    }

    private fun setFocusListeners() {
        binding.keyEt.onFocusChangeListener = OnFocusChangeListener { _: View?, b: Boolean -> if (b) binding.keyTl.error = null }
    }

    fun updateStatus(data: String) {
        val userData: MutableMap<String, Any> = HashMap()
        userData["isSetupDone"] = true
        userData["writePermissions"] = false
        userData["encryptedData"] = data
        db.collection("userProfiles")
                .document(mAuth!!.uid!!)[userData] = SetOptions.merge()
    }
}