package com.leyou.microstorageanalyzer.ui.home

import android.os.Bundle
import com.bonepeople.android.base.viewbinding.ViewBindingActivity
import com.leyou.microstorageanalyzer.R
import com.leyou.microstorageanalyzer.databinding.ActivityHomeBinding
class HomeActivity : ViewBindingActivity<ActivityHomeBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //AdManager.init(applicationContext)
    }
    override fun initView() {
        var fragment = supportFragmentManager.findFragmentByTag("HomeFragment")
        if (fragment == null) {
            fragment = HomeFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainerView, fragment, "HomeFragment").commit()
        }
    }
}