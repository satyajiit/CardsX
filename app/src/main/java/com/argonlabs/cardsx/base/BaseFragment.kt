package com.argonlabs.cardsx.base

import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

open class BaseFragment : Fragment() {
    @JvmField
    protected var db = FirebaseFirestore.getInstance()
    @JvmField
    protected var mAuth = FirebaseAuth.getInstance()
}