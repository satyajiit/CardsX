package com.argonlabs.cardsx.managers

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.View
import android.widget.TextView
import com.argonlabs.cardsx.R

class DialogCreator(activity: Activity) {
    var dialog: Dialog = Dialog(activity)
    fun showLoadingDialog() {
        dialog.setContentView(R.layout.dialog_loading)
        dialog.show()
    }

    fun updateDialogText(text: String?) {
        (dialog.findViewById<View>(R.id.loadTv) as TextView).text = text
    }

    init {
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}