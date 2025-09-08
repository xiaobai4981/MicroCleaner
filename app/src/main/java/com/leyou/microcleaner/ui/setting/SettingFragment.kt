package com.leyou.microcleaner.ui.setting

import com.bonepeople.android.base.activity.StandardActivity
import com.bonepeople.android.base.viewbinding.ViewBindingFragment
import com.leyou.microcleaner.R
import com.leyou.microcleaner.databinding.FragmentSettingBinding
import com.leyou.microcleaner.ui.setting.path.CleanPathListFragment
import com.bonepeople.android.widget.ApplicationHolder
import com.bonepeople.android.widget.util.AppTime
import com.bonepeople.android.widget.util.AppView.singleClick
import com.leyou.microcleaner.App
import java.util.TimeZone

class SettingFragment : ViewBindingFragment<FragmentSettingBinding>() {
    override fun initView() {
        views.titleView.title = getString(R.string.caption_text_set)
        views.textViewWhite.singleClick { StandardActivity.open(CleanPathListFragment.newInstance(CleanPathListFragment.Mode.White)) }
        views.textViewBlack.singleClick { StandardActivity.open(CleanPathListFragment.newInstance(CleanPathListFragment.Mode.Black)) }
        views.textViewAbout.singleClick { StandardActivity.open(AboutFragment()) }
        views.textViewVersion.run {
            val versionName = if (ApplicationHolder.debug) "${ApplicationHolder.getVersionName()} - debug" else ApplicationHolder.getVersionName()
            val buildTime = AppTime.getDateTimeString(App.BUILD_TIME, timeZone = TimeZone.getTimeZone("GMT+8"))
            text = getString(R.string.app_version, versionName, buildTime)
        }
    }
}