package com.argonlabs.cardsx.ui.activities

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import com.argonlabs.cardsx.base.BaseActivity
import com.argonlabs.cardsx.databinding.ActivitySplashBinding
import com.argonlabs.cardsx.managers.ActivitySwitchManager
import com.argonlabs.cardsx.managers.DialogCreator
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import java.util.*

class SplashActivity : BaseActivity() {
    private lateinit var binding: ActivitySplashBinding
    private var DELAY = 2000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (sharedPreferences!!.getBoolean("fastAccess", false)) DELAY = 800
        initComponents()
    }

    override fun initComponents() {
        Handler().postDelayed({ if (mAuth!!.uid == null) ActivitySwitchManager(this@SplashActivity, LoginActivity::class.java).openActivity() else if (mAuth!!.uid != null && DELAY != 2000) ActivitySwitchManager(this@SplashActivity, AuthenticateActivity::class.java).openActivity() else checkIfUserExist() }, DELAY.toLong())
    }

    private fun checkIfUserExist() {
        db.collection("userProfiles").document(mAuth!!.uid!!)
                .get().addOnCompleteListener { task: Task<DocumentSnapshot?> ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document!!.exists()) {


                            //Check Status
                            if (document.getBoolean("isSetupDone")!!) {
                                sharedPreferences!!.edit().putString("hexCode", document.getString("encryptedData")).commit()
                                ActivitySwitchManager(this@SplashActivity, AuthenticateActivity::class.java).openActivity()
                                sharedPreferences!!.edit().putBoolean("fastAccess", true).apply()
                            } else {
                                ActivitySwitchManager(this@SplashActivity, SetupActivity::class.java).openActivity()
                            }
                        } else {
                            //NOT EXIST
                            createNewUser()
                        }
                    } else {
                        Toast.makeText(this@SplashActivity, "Something went wrong. Try later.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
    }

    private fun createNewUser() {
        val dialogClass = DialogCreator(this)
        dialogClass.showLoadingDialog()
        val userData: MutableMap<String, Any?> = HashMap()
        userData["NAME"] = mAuth!!.currentUser!!.displayName
        userData["isSetupDone"] = false
        userData["writePermissions"] = true
        userData["DEVICE_NAME"] = Build.MANUFACTURER + " " + Build.MODEL
        db.collection("userProfiles").document(mAuth!!.uid!!)
                .set(userData)
                .addOnSuccessListener { aVoid: Void? ->

                    // for cancel and finish waiting progress dialog:
                    dialogClass.dialog.dismiss()
                    ActivitySwitchManager(this@SplashActivity, SetupActivity::class.java).openActivity()
                    updateDeviceToken()
                }
                .addOnFailureListener { e: Exception? ->
                    dialogClass.dialog.dismiss()
                    mAuth!!.signOut()
                    ActivitySwitchManager(this@SplashActivity, LoginActivity::class.java).openActivity()
                }
    }

    private fun updateDeviceToken() {


        //Update Device Token
        FirebaseInstanceId.getInstance().instanceId
                .addOnCompleteListener { task: Task<InstanceIdResult> ->
                    if (task.isSuccessful) {
                        val userData: MutableMap<String, Any> = HashMap()
                        userData["DEVICE_TOKEN"] = task.result!!.token
                        db.collection("userProfiles")
                                .document(mAuth!!.uid!!)[userData] = SetOptions.merge()
                    }
                }
    }
}