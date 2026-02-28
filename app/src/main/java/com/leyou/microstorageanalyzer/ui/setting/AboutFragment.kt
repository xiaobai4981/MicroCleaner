package com.leyou.microstorageanalyzer.ui.setting

import com.bonepeople.android.base.viewbinding.ViewBindingFragment
import com.leyou.microstorageanalyzer.R
import com.leyou.microstorageanalyzer.databinding.FragmentAboutBinding

class AboutFragment : ViewBindingFragment<FragmentAboutBinding>() {
    override fun initView() {
        views.titleView.title = getString(R.string.caption_text_about)
    }
}