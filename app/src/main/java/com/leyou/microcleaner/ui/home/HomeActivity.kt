package com.leyou.microcleaner.ui.home

import com.bonepeople.android.base.viewbinding.ViewBindingActivity
import com.leyou.microcleaner.R
import com.leyou.microcleaner.databinding.ActivityHomeBinding
class HomeActivity : ViewBindingActivity<ActivityHomeBinding>() {
    override fun initView() {
        var fragment = supportFragmentManager.findFragmentByTag("HomeFragment")
        if (fragment == null) {
            fragment = HomeFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainerView, fragment, "HomeFragment").commit()
        }
    }
}