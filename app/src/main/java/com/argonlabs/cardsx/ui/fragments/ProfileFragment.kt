package com.argonlabs.cardsx.ui.fragments

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.base.BaseApplication.Companion.getmInstance
import com.argonlabs.cardsx.databinding.FragmentProfileBinding
import com.argonlabs.cardsx.managers.ActivitySwitchManager
import com.argonlabs.cardsx.ui.activities.SplashActivity
import com.facebook.login.LoginManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener
import com.google.firebase.auth.FirebaseAuth

/**
 * A simple [Fragment] subclass.
 */
class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    var activity: Activity? = null
    private lateinit var mRewardedVideoAd: RewardedVideoAd
    var firebaseAuth = FirebaseAuth.getInstance()
    var rewardedVideoAdListener: RewardedVideoAdListener = object : RewardedVideoAdListener {
        override fun onRewardedVideoAdLoaded() {
            binding.watchAD.visibility = View.VISIBLE
        }

        override fun onRewardedVideoAdOpened() {}
        override fun onRewardedVideoStarted() {}
        override fun onRewardedVideoAdClosed() {
            loadAgain()
        }

        override fun onRewarded(rewardItem: RewardItem) {
            binding.watchAD.visibility = View.GONE
        }

        override fun onRewardedVideoAdLeftApplication() {}
        override fun onRewardedVideoAdFailedToLoad(i: Int) {}
        override fun onRewardedVideoCompleted() {
            loadAgain()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = getmInstance()!!.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        initialiseAds()
        setupListeners()
        loadAgain()
        return binding.root
    }

    private fun initialiseAds() {
        MobileAds.initialize(activity, getString(R.string.google_ads_app_id))
    }

    private fun setupListeners() {
        binding.shareCard.setOnClickListener { shareApp() }
        binding.signOut.setOnClickListener { showConfirm("SignOut", "Are you sure about SignOut? ", 1) }
        binding.rateUsCard.setOnClickListener { rateUs() }
        binding.developer.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://argonlabs.in"))
            startActivity(browserIntent)
        }
        binding.watchAD.setOnClickListener {
            if (mRewardedVideoAd.isLoaded) {
                mRewardedVideoAd.show()
            }
        }
        binding.aboutCard.setOnClickListener { showConfirm("About", "CardsX help you store your cards digitally with AES encryption and can only be accessed by the security key.", 0) }
    }

    fun loadAgain() {
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(activity)
        mRewardedVideoAd.rewardedVideoAdListener = rewardedVideoAdListener
        mRewardedVideoAd.loadAd(getString(R.string.video_ad_unit_id), AdRequest.Builder().build())
    }

    private fun rateUs() {
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.argonlabs.cardsx"))
        startActivity(browserIntent)
    }

    private fun showConfirm(title: String?, data: String?, type: Int) {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle(title)
        builder.setMessage(data)
        builder.setCancelable(false)
        if (type != 0) builder.setNegativeButton("Cancel") { _: DialogInterface?, _: Int -> }
        builder.setPositiveButton("OK") { _: DialogInterface?, _: Int ->
            if (type == 1) //Remove all data
                signOut()
        }
        builder.show()
    }

    private fun signOut() {
        getmInstance()!!.sharedPreferences!!.edit().remove("hexCode").remove("fastAccess").apply()
        if (LoginManager.getInstance() != null) LoginManager.getInstance().logOut()
        firebaseAuth.signOut()
        ActivitySwitchManager(activity!!, SplashActivity::class.java).openActivity()
    }

    private fun shareApp() {

        /*Create an ACTION_SEND Intent*/
        val intent = Intent(Intent.ACTION_SEND)
        /*This will be the actual content you wish you share.*/
        val shareBody = "Meet CardsX the one touch platform for all your cards! With high end AES Encryption Algorithm.\nDownload the App now https://play.google.com/store/apps/details?id=com.argonlabs.cardsx"
        /*The type of the content is text, obviously.*/intent.type = "text/plain"
        /*Applying information Subject and Body.*/intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        intent.putExtra(Intent.EXTRA_TEXT, shareBody)
        /*Fire!*/startActivity(Intent.createChooser(intent, getString(R.string.share_using)))
    }

    override fun onResume() {
        mRewardedVideoAd.resume(activity)
        super.onResume()
    }

    override fun onPause() {
        mRewardedVideoAd.pause(activity)
        super.onPause()
    }

    override fun onDestroy() {
        mRewardedVideoAd.destroy(activity)
        super.onDestroy()
    }
}