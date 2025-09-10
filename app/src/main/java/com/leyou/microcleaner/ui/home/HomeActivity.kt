package com.leyou.microcleaner.ui.home

import android.os.Bundle
import android.widget.FrameLayout
import com.bonepeople.android.base.viewbinding.ViewBindingActivity
import com.leyou.microcleaner.R
import com.leyou.microcleaner.databinding.ActivityHomeBinding
import com.leyou.microcleaner.ui.ads.AdManager
import com.leyou.microcleaner.ui.ads.AdUnitId
import com.tradplus.ads.open.nativead.TPNative

class HomeActivity : ViewBindingActivity<ActivityHomeBinding>() {
    private var bannerContainer: FrameLayout? = null
    private var bannerHandle: AdManager.BannerHandle? = null
    private var tpNative: TPNative? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AdManager.init(applicationContext)
        AdManager.preloadRewarded(this, AdUnitId.REWRDVIDEO_ADUNITID)
        AdManager.preloadInterstitial(this, AdUnitId.INTERSTITIAL_ADUNITID)
        bannerHandle = AdManager.attachBanner(this, findViewById(R.id.banner_container), AdUnitId.BANNER_ADUNITID)
        showNative()
    }

    override fun initView() {
        var fragment = supportFragmentManager.findFragmentByTag("HomeFragment")
        if (fragment == null) {
            fragment = HomeFragment()
            supportFragmentManager.beginTransaction().add(R.id.fragmentContainerView, fragment, "HomeFragment").commit()
        }
    }
    private fun showNative() {
        AdManager.loadNative(
            activity = this,
            nativeId = AdUnitId.NATIVE_ADUNITID,
            onLoaded = { ad ->
                tpNative = ad
                val container = findViewById<FrameLayout>(R.id.native_container)
                container.removeAllViews()
                // 广告加载成功
                // 展示广告。需要在loaded回调后调用。参数2 layoutId布局,布局文件从Download的SDK文件中获取。
                tpNative!!.showAd(container, R.layout.tp_native_ad_list_item)
            }
        )
    }

    override fun onDestroy() {
        AdManager.clearAll()
        tpNative?.onDestroy()
        tpNative = null
        bannerHandle?.destroy()
        bannerHandle = null
        super.onDestroy()
    }
}