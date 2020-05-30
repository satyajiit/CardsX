package com.argonlabs.cardsx.ui.activities

import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.andrognito.pinlockview.IndicatorDots
import com.andrognito.pinlockview.PinLockListener
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.base.BaseActivity
import com.argonlabs.cardsx.base.BaseApplication.Companion.getmInstance
import com.argonlabs.cardsx.databinding.ActivityAuthenticateBinding
import com.argonlabs.cardsx.managers.ActivitySwitchManager
import com.argonlabs.cardsx.utils.Locker
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Callback
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso

class AuthenticateActivity : BaseActivity() {
    private lateinit var binding: ActivityAuthenticateBinding
    private val mPinLockListener: PinLockListener = object : PinLockListener {
        override fun onComplete(pin: String) {
            Log.d("TAG", "Pin complete: $pin")
            checkPin(pin)
        }

        override fun onEmpty() {
            Log.d("TAG", "Pin empty")
        }

        override fun onPinChange(pinLength: Int, intermediatePin: String) {
            Log.d("TAG", "Pin changed, new length $pinLength with intermediate pin $intermediatePin")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthenticateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.pinLockView.attachIndicatorDots(binding.indicatorDots)
        binding.pinLockView.setPinLockListener(mPinLockListener)
        binding.pinLockView.pinLength = 6
        binding.pinLockView.textColor = ContextCompat.getColor(this, R.color.white)
        binding.indicatorDots.indicatorType = IndicatorDots.IndicatorType.FILL_WITH_ANIMATION
        initComponents()
    }

    override fun initComponents() {

        //Load Data
        var tempName = mAuth!!.currentUser!!.displayName
        if (tempName!!.contains(" ")) tempName = tempName.substring(0, tempName.indexOf(' '))

        //Set TVS
        binding.usernameTv.text = getString(R.string.usernameHi).replace("%name%", tempName)
        loadProfilePic()
    }

    private fun loadProfilePic() {
        Picasso.get()
                .load(mAuth!!.currentUser!!.photoUrl)
                .networkPolicy(NetworkPolicy.OFFLINE)
                .into(binding.profileImage, object : Callback {
                    override fun onSuccess() {}
                    override fun onError(e: Exception) {
                        Picasso.get()
                                .load(mAuth!!.currentUser!!.photoUrl)
                                .error(R.drawable.img_no_avatar)
                                .into(binding.profileImage, object : Callback {
                                    override fun onSuccess() {}
                                    override fun onError(e: Exception) {}
                                })
                    }
                })
    }

    private fun showMsg(msg: String?) {
        val snackbar = Snackbar.make(binding.mainLayout, msg!!, Snackbar.LENGTH_SHORT)
        snackbar.setActionTextColor(getColor(R.color.white))
        snackbar.setAction(getString(R.string.ok)) { view: View? -> }
                .show()
        val snackBarView = snackbar.view
        snackBarView.setBackgroundColor(getColor(R.color.colorAccent))
        val textView = snackBarView.findViewById<TextView>(com.google.android.material.R.id.snackbar_text)
        textView.setTextColor(getColor(R.color.white))
        snackbar.show()
    }

    private fun checkPin(pin: String) {
        val lockedData = Base64.decode(sharedPreferences!!.getString("hexCode", ""), Base64.NO_WRAP)
        try {
            if (String(Locker.decryptData(lockedData, pin)!!, Charsets.UTF_8) == mAuth!!.uid) {
                getmInstance()!!.sessionKey = pin
                ActivitySwitchManager(this@AuthenticateActivity, MainActivity::class.java).openActivity()
            } else invalidPin()
        } catch (e: Exception) {
            e.printStackTrace()
            invalidPin()
        }
    }

    private fun invalidPin() {
        showMsg("Invalid Security Key!")
        binding.pinLockView.resetPinLockView()
    }
}