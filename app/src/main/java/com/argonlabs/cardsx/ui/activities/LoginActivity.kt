package com.argonlabs.cardsx.ui.activities

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.base.BaseActivity
import com.argonlabs.cardsx.databinding.ActivityLoginBinding
import com.argonlabs.cardsx.managers.ActivitySwitchManager
import com.argonlabs.cardsx.managers.DialogCreator
import com.argonlabs.cardsx.utils.Utils
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.SetOptions
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import java.util.*

class LoginActivity : BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    var mCallbackManager: CallbackManager? = null
    var mGoogleSignInClient: GoogleSignInClient? = null
    var dialogClass: DialogCreator? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponents()
    }

    private fun showSnackBar(msg: String?) {
        Snackbar.make(binding.loginLayout, msg!!, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.ok)) { }
                .show()
    }

    override fun initComponents() {

        //Setup DialogClass
        dialogClass = DialogCreator(this)
        setUpClickListeners()
        setUpSocialLogin()
    }

    private fun setUpClickListeners() {
        binding.fbLogin.setOnClickListener { view: View? ->
            if (Utils.isNetworkConnected(this@LoginActivity)) {
                binding.signInFacebook.performClick()
                showSnackBar("Facebook Auth Started...Hold on...")
            }
        }
        binding.googleLogin.setOnClickListener { if (Utils.isNetworkConnected(this@LoginActivity)) signIn() }

        binding.terms.setOnClickListener { val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://docs.google.com/document/d/1OIk_3CYjV0aqvc98p-J44lP2fnOkOmlTtUHuht6zOkQ/edit?usp=sharing"))
            startActivity(browserIntent) }

        binding.closeDialog.setOnClickListener { finish() }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 808) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                e.printStackTrace()
            }
        } else  // Pass the activity result back to the Facebook SDK
            mCallbackManager!!.onActivityResult(requestCode, resultCode, data)
    }

    private fun setUpSocialLogin() {
        binding.signInFacebook.setPermissions("email", "public_profile")
        mCallbackManager = CallbackManager.Factory.create()


        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken("")
                .requestEmail()
                .requestProfile()
                .build()

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        binding.signInFacebook.registerCallback(mCallbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult) {
                Log.d("OPERATOR1", "facebook:onSuccess:$loginResult")
                handleFacebookAccessToken(loginResult.accessToken)
            }

            override fun onCancel() {
                //Cancelled
            }

            override fun onError(error: FacebookException) {
                Log.d("OPERATOR3", "facebook:onError", error)
            }
        })
    }

    private fun signIn() {
        showSnackBar("Google Auth Started...Hold on...")
        val signInIntent = mGoogleSignInClient!!.signInIntent
        startActivityForResult(signInIntent, 808)
    }

    private fun handleFacebookAccessToken(token: AccessToken) {
        Log.d("", "handleFacebookAccessToken:$token")
        val credential = FacebookAuthProvider.getCredential(token.token)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d("TESTING", "signInWithCredential:success")
                        checkIfUserExist()
                    } else {
                        Log.w("TESTING99", "signInWithCredential:failure", task.exception)
                        showSnackBar("Authentication Failed. Code: 101")
                    }
                }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount?) {
        val credential = GoogleAuthProvider.getCredential(acct!!.idToken, null)
        mAuth!!.signInWithCredential(credential)
                .addOnCompleteListener(this) { task: Task<AuthResult?> ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        checkIfUserExist()
                    } else {
                        // If sign in fails, display a message to the user.
                        showSnackBar("Authentication Failed. Code: 102")
                    }
                }
    }

    private fun checkIfUserExist() {
        dialogClass!!.showLoadingDialog()
        db.collection("userProfiles").document(mAuth!!.uid!!)
                .get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val document = task.result
                        if (document!!.exists()) {
                            dialogClass!!.dialog.dismiss()
                            updateDeviceToken()

                            //Check Status
                            if (document.getBoolean("isSetupDone")!!) {
                                sharedPreferences!!.edit().putString("hexCode", document.getString("encryptedData")).commit()
                                ActivitySwitchManager(this@LoginActivity, AuthenticateActivity::class.java).openActivity()
                                sharedPreferences!!.edit().putBoolean("fastAccess", true).apply()
                            } else {
                                ActivitySwitchManager(this@LoginActivity, SetupActivity::class.java).openActivity()
                            }
                        } else {
                            //NOT EXIST
                            createNewUser()
                        }
                    } else {
                        dialogClass!!.dialog.dismiss()
                        showSnackBar("Please try again. ER111")
                    }
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

    private fun createNewUser() {
        val userData: MutableMap<String, Any?> = HashMap()
        userData["NAME"] = mAuth!!.currentUser!!.displayName
        userData["isSetupDone"] = false
        userData["writePermissions"] = true
        userData["DEVICE_NAME"] = Build.MANUFACTURER + " " + Build.MODEL
        db.collection("userProfiles").document(mAuth!!.uid!!)
                .set(userData)
                .addOnSuccessListener {

                    // for cancel and finish waiting progress dialog:
                    dialogClass!!.dialog.dismiss()
                    ActivitySwitchManager(this@LoginActivity, SetupActivity::class.java).openActivity()
                    updateDeviceToken()
                }
                .addOnFailureListener { e: Exception? ->
                    dialogClass!!.dialog.dismiss()
                    mAuth!!.signOut()
                    showSnackBar("FATAL ERROR CODE 105")
                }
    }
}