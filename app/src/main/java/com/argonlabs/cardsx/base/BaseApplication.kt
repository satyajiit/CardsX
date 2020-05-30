package com.argonlabs.cardsx.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.argonlabs.cardsx.R
import java.util.*

class BaseApplication : Application() {
    var sharedPreferences: SharedPreferences? = null
        private set
    var activity: Activity? = null
    var sessionKey: String? = null

    override fun onCreate() {
        super.onCreate()
        mInstance = this
        sharedPreferences = getSharedPreferences(getString(R.string.app_name), Context.MODE_PRIVATE)
    }



    /**
     * Call when application is close
     */
    override fun onTerminate() {
        super.onTerminate()
        if (mInstance != null) {
            mInstance = null
        }
    }


    companion object {
        private var mInstance: BaseApplication? = null
        @JvmStatic
        fun getmInstance(): BaseApplication? {
            return mInstance
        }
    }
}