package com.leyou.microcleaner.ui.ads

import android.app.Activity
import android.content.Context
import android.widget.FrameLayout
import com.tradplus.ads.base.bean.TPAdError
import com.tradplus.ads.base.bean.TPAdInfo
import com.tradplus.ads.base.bean.TPBaseAd
import com.tradplus.ads.open.banner.BannerAdListener
import com.tradplus.ads.open.banner.TPBanner
import com.tradplus.ads.open.interstitial.InterstitialAdListener
import com.tradplus.ads.open.interstitial.TPInterstitial
import com.tradplus.ads.open.nativead.NativeAdListener
import com.tradplus.ads.open.nativead.TPNative
import com.tradplus.ads.open.reward.RewardAdListener
import com.tradplus.ads.open.reward.TPReward
import kotlin.random.Random

object AdManager {
    private var tpInterstitial: TPInterstitial? = null
    private var tpReward: TPReward? = null
    fun init(context: Context) {
        val appCtx = context.applicationContext
    }
    // ============== Interstitial ==============
    fun preloadInterstitial(context: Context, interstitialId : String){
        val appCtx = context.applicationContext
        // 第一次创建实例时设置监听
        if (tpInterstitial == null) {
            tpInterstitial = TPInterstitial(appCtx, interstitialId).apply {
                setAdListener(object : InterstitialAdListener {
                    override fun onAdLoaded(info: TPAdInfo?) {
                        // 可展示
                    }

                    override fun onAdFailed(error: TPAdError?) {
                    }

                    override fun onAdImpression(info: TPAdInfo?) {}
                    override fun onAdClicked(info: TPAdInfo?) {}

                    override fun onAdClosed(info: TPAdInfo?) {
                        // 关闭后销毁旧实例并新建 + 预载下一条，避免监听叠加/状态残留
                        tpInterstitial?.onDestroy()
                        tpInterstitial = TPInterstitial(appCtx, interstitialId).also { newAd ->
                            newAd.setAdListener(this) // 复用同一监听对象
                            newAd.loadAd()
                        }
                    }
                    override fun onAdVideoStart(info: TPAdInfo?) {}
                    override fun onAdVideoEnd(info: TPAdInfo?) {}
                    override fun onAdVideoError(info: TPAdInfo?, error: TPAdError?) {
                        loadAd()
                    }
                })
            }
        }
        // 开始加载（已存在则复用对象直接加载）
        tpInterstitial!!.loadAd()
    }
    fun showInterstitial(activity: Activity) : Boolean {
        val ad = tpInterstitial ?: return false
        val ready = runCatching { ad.isReady }.getOrDefault(false) // 不同 SDK 可能是 isAdReady
        if (!ready) return false
        ad.showAd(activity,"")
        return true
    }
    fun destroyInterstitial() {
        tpInterstitial?.onDestroy()
        tpInterstitial = null
    }
    // ============== Rewarded ==============
    fun preloadRewarded(context: Context, rewardedId : String){
        val appCtx = context.applicationContext
        if (tpReward == null){
            tpReward = TPReward(appCtx, rewardedId).apply {
                setAdListener(object : RewardAdListener {
                    override fun onAdLoaded(info: TPAdInfo?) {
                        // 可展示
                    }

                    override fun onAdFailed(error: TPAdError?) {
                    }

                    override fun onAdImpression(info: TPAdInfo?) {}
                    override fun onAdClicked(info: TPAdInfo?) {}

                    override fun onAdClosed(info: TPAdInfo?) {
                        // 关闭后销毁旧实例并新建 + 预载下一条，避免监听叠加/状态残留
                        tpReward?.onDestroy()
                        tpReward = TPReward(appCtx, rewardedId).also { newAd ->
                            newAd.setAdListener(this) // 复用同一监听对象
                            newAd.loadAd()
                        }
                    }
                    override fun onAdVideoStart(info: TPAdInfo?) {}
                    override fun onAdVideoEnd(info: TPAdInfo?) {}
                    override fun onAdVideoError(info: TPAdInfo?, error: TPAdError?) {
                        loadAd()
                    }
                    override fun onAdReward(info: TPAdInfo?) {

                    }
                })
            }
        }
        // 开始加载（已存在则复用对象直接加载）
        tpReward!!.loadAd()
    }
    fun showRewarded(activity: Activity): Boolean {
        val ad = tpReward ?: return false
        val ready = runCatching { ad.isReady }.getOrDefault(false)
        if (!ready) return false

        ad.showAd(activity,  "")
        return true
    }
    fun destroyRewarded() {
        tpReward?.onDestroy()
        tpReward = null
    }
    // ============== Banner ==============
    fun attachBanner(activity: Activity, container: FrameLayout, bannerId : String) : BannerHandle{
        val bannerAd = TPBanner(activity)
        bannerAd.setAdListener(object : BannerAdListener() {
            override fun onAdLoaded(tpAdInfo: TPAdInfo?) {
            }

            override fun onAdLoadFailed(error: TPAdError?) {
            }

            override fun onAdClicked(tpAdInfo: TPAdInfo?) {
                // 点击处理
            }
        })
        bannerAd.loadAd(bannerId)
        container.addView(bannerAd)

        return BannerHandle(bannerAd, container)
    }
    class BannerHandle(
        private var banner: TPBanner?,
        private var container: FrameLayout?
    ) {
        fun destroy() {
            runCatching { banner?.onDestroy() }
            banner = null
            container?.removeAllViews()
            container = null
        }
    }
    // ---------------- Native ----------------
    fun loadNative(
        activity: Activity,
        nativeId: String,
        onLoaded: (TPNative) -> Unit
    ) {
        val ad = TPNative(activity, nativeId)
        ad.setAdListener(object : NativeAdListener() {
            override fun onAdLoaded(info: TPAdInfo, tpBaseAd: TPBaseAd) {
                if (activity.isFinishing || activity.isDestroyed) {
                    ad.onDestroy()
                    return
                }
                onLoaded(ad)
            }
            override fun onAdClicked(tpAdInfo: TPAdInfo?) {}

            override fun onAdImpression(tpAdInfo: TPAdInfo?) {}

            override fun onAdShowFailed(tpAdError: TPAdError?, tpAdInfo: TPAdInfo?) {}

            override fun onAdLoadFailed(tpAdError: TPAdError?) {
            }

            override fun onAdClosed(tpAdInfo: TPAdInfo?) {}
        })
        ad.loadAd()
    }

    // ---------------- 统一清理插屏和激励资源 ----------------
    fun clearAll() {
        destroyInterstitial()
        destroyRewarded()
    }
    fun maybeShowAd(
        activity: Activity,
        triggerProbability: Double = 0.5,
        rewardedProbability: Double = 0.4,
    ): Boolean {
        if (Random.nextDouble() >= triggerProbability) return false
        return if (Random.nextDouble() < rewardedProbability) {
            showRewarded(activity)
        } else {
            showInterstitial(activity)
        }
    }

}