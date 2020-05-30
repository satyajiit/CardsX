package com.argonlabs.cardsx.managers

import android.app.Activity
import android.content.Intent
import com.argonlabs.cardsx.R

class ActivitySwitchManager(var activity: Activity, NewActivity: Class<*>) {
    var mMenuIntent: Intent = Intent(activity,
            NewActivity)

    fun openActivity() {
        mMenuIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity.startActivity(mMenuIntent)
        activity.finish()
        activity.overridePendingTransition(R.anim.anim_right_in, R.anim.anim_left_out)
    }



}