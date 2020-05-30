package com.argonlabs.cardsx.utils

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.net.ConnectivityManager
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.argonlabs.cardsx.R
import java.text.SimpleDateFormat
import java.util.*

class Utils {
    fun showHelpDialog(activity: Activity?, helpText: String?) {
        val builder = AlertDialog.Builder(activity!!)
        builder.setTitle("Help")
        builder.setMessage(helpText)
        builder.setCancelable(false)
        builder.setPositiveButton("Ok") { dialog: DialogInterface?, which: Int -> }
        builder.show()
    }

    companion object {
        fun isNetworkConnected(context: Context): Boolean {
            val cm = (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            return cm.activeNetworkInfo != null && cm.activeNetworkInfo.isConnected
        }

        fun resetStatusBar(activity: Activity) {
            val window = activity.window
            var flags = window.decorView.systemUiVisibility
            flags = flags and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            window.decorView.systemUiVisibility = flags
            window.statusBarColor = ContextCompat.getColor(activity, R.color.colorPrimaryDark)
        }

        fun disableScreenRecords(activity: Activity) {
            activity.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        }

        fun setUpStatusBar(activity: Activity) {
            val window = activity.window
            window.addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
        }

        @JvmStatic
        fun getFormattedTime(date: Date): String {
            return SimpleDateFormat("dd MMM hh:mm a", Locale.ENGLISH).format(date)
        }

        fun getFormattedTime2(date: Date): String {
            return SimpleDateFormat("dd MMM YYYY", Locale.ENGLISH).format(date)
        }
    }
}