package com.argonlabs.cardsx.ui.activities

import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import androidx.annotation.IdRes
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.argonlabs.cardsx.R
import com.argonlabs.cardsx.base.BaseActivity
import com.argonlabs.cardsx.base.BaseApplication.Companion.getmInstance
import com.argonlabs.cardsx.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    var navController: NavController? = null
    var navStartPos = 2
    var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initComponents()
        setupListeners()
    }

    override fun initComponents() {
        getmInstance()!!.activity = this@MainActivity

        //Getting the Navigation Controller
        navController = Navigation.findNavController(this, R.id.fragment)

        //Setting up the action bar
        NavigationUI.setupWithNavController(binding.bottomNav, navController!!)
    }

    fun setupListeners() {
        binding!!.bottomNav.setOnNavigationItemSelectedListener { item: MenuItem ->
            if (item.itemId == binding!!.bottomNav.selectedItemId) return@setOnNavigationItemSelectedListener false
            @IdRes val id = item.itemId
            val optionsBuilder = NavOptions.Builder()
            when (id) {
                R.id.nav_tracker -> setAnim(optionsBuilder, 1)
                R.id.nav_wallet -> setAnim(optionsBuilder, 2)
                R.id.nav_profile -> setAnim(optionsBuilder, 3)
            }
            navStartPos = item.order
            navController!!.popBackStack()
            navController!!.navigate(id, null, optionsBuilder.build())
            true
        }
    }

    private fun setAnim(optionsBuilder: NavOptions.Builder, pos: Int): NavOptions.Builder {
        if (navStartPos > pos) {
            optionsBuilder
                    .setEnterAnim(R.anim.slide_in_left)
                    .setExitAnim(R.anim.slide_out_right)
        } else if (navStartPos < pos) {
            optionsBuilder
                    .setEnterAnim(R.anim.slide_in_right)
                    .setExitAnim(R.anim.slide_out_left)
        }
        return optionsBuilder
    }

    private fun showSnackBar(msg: String?) {
        Snackbar.make(binding.mainActivity, msg!!, Snackbar.LENGTH_LONG)
                .setAnchorView(binding.bottomNav)
                .setAction(getString(R.string.ok)) { }
                .show()
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        showSnackBar("Hit BACK again to leave us!")
        Handler().postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }
}