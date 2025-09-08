package com.leyou.microcleaner.ui.setting

import com.bonepeople.android.base.viewbinding.ViewBindingFragment
import com.leyou.microcleaner.R
import com.leyou.microcleaner.databinding.FragmentAboutBinding

class AboutFragment : ViewBindingFragment<FragmentAboutBinding>() {
    override fun initView() {
        views.titleView.title = getString(R.string.caption_text_about)
    }
}