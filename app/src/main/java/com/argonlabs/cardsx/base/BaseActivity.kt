package com.argonlabs.cardsx.base

import android.app.Activity
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.argonlabs.cardsx.utils.Utils
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.ActivityResult
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

abstract class BaseActivity : AppCompatActivity() {
    @JvmField
    var mAuth: FirebaseAuth? = null
    @JvmField
    var db = FirebaseFirestore.getInstance()
    @JvmField
    var sharedPreferences: SharedPreferences? = null
    private var appUpdateManager: AppUpdateManager? = null
    private var UPDATE_REQUEST_CODE = 111
    private val installStateUpdatedListener = InstallStateUpdatedListener { installState ->
        if (installState.installStatus() == InstallStatus.DOWNLOADED) {


            // After the update is downloaded, show a notification
            // and request user confirmation to restart the app.

            //Will restart the App to show up changes
            appUpdateManager!!.completeUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAuth = FirebaseAuth.getInstance()
        sharedPreferences = BaseApplication.getmInstance()?.sharedPreferences
        Utils.disableScreenRecords(this)
        Utils.setUpStatusBar(this)
        appUpdateManager = AppUpdateManagerFactory.create(this)

        //Step 1 : Check if Update Available
        checkUpdate()
    }

    private fun checkUpdate() {
        val appUpdateInfoTask = appUpdateManager?.appUpdateInfo
        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask?.addOnSuccessListener { result: AppUpdateInfo ->
            if (result.updateAvailability() != UpdateAvailability.UPDATE_AVAILABLE) return@addOnSuccessListener
            if (result.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                //Using Immediate Update feature
                //Next Step Request the Update
                //Step 2: Request for Update
                requestImmediateUpdate(result)
            }
        }


        //Add listener to manager
        appUpdateManager?.registerListener(installStateUpdatedListener)
    }

    private fun requestImmediateUpdate(appUpdateInfo: AppUpdateInfo) {
        try {
            appUpdateManager!!.startUpdateFlowForResult( // Pass the intent that is returned by 'getAppUpdateInfo()'.
                    appUpdateInfo,  // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                    AppUpdateType.IMMEDIATE,  // The current activity making the update request.
                    this,  // Include a request code to later monitor this update request.
                    UPDATE_REQUEST_CODE)
        } catch (e: SendIntentException) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager!!.unregisterListener(installStateUpdatedListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == UPDATE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                }
                Activity.RESULT_CANCELED, ActivityResult.RESULT_IN_APP_UPDATE_FAILED ->                     //Failed in update
                    //Cancelled the update request
                    checkUpdate()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        appUpdateManager
                ?.appUpdateInfo
                ?.addOnSuccessListener { appUpdateInfo: AppUpdateInfo ->
                    // If the update is downloaded but not installed,
                    // notify the user to complete the update.
                    if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {

                        //Pop user to install update by calling
                        appUpdateManager!!.completeUpdate()
                        // return;
                    }
                }
    }

    abstract fun initComponents()
}