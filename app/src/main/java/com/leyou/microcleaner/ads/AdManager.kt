package com.leyou.microcleaner.ads

import android.app.Activity
import android.content.Context
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.leyou.microcleaner.ui.home.HomeActivity
import kotlin.random.Random


object AdManager {
    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null

    /** 在 Application 或首个 Activity 启动时调用一次 */
    fun init(context: Context) {
        MobileAds.initialize(context) {}
        preloadInterstitial(context)
        preloadRewarded(context)
    }

    private fun preloadInterstitial(context: Context) {
        InterstitialAd.load(
            context,
            AdUnitId.INTERSTITIAL_ADUNITID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() { interstitialAd = null }
                        override fun onAdDismissedFullScreenContent() { preloadInterstitial(context) }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            interstitialAd = null
                            preloadInterstitial(context)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    // 真实项目里可加退避重试
                }
            }
        )
    }

    private fun preloadRewarded(context: Context) {
        RewardedAd.load(
            context,
            AdUnitId.REWARDVIDEO_ADUNITID,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdShowedFullScreenContent() { rewardedAd = null }
                        override fun onAdDismissedFullScreenContent() { preloadRewarded(context) }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            rewardedAd = null
                            preloadRewarded(context)
                        }
                    }
                }
                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            }
        )
    }

    fun showInterstitial(activity: Activity): Boolean {
        val ad = interstitialAd ?: return false
        activity.runOnUiThread { ad.show(activity) }
        return true
    }

    fun showRewarded(
        activity: Activity,
        onRewardEarned: ((RewardItem) -> Unit)? = null
    ): Boolean {
        val ad = rewardedAd ?: return false
        activity.runOnUiThread {
            ad.show(activity) { reward -> onRewardEarned?.invoke(reward) }
        }
        return true
    }

    /** 统一的“也许展示广告”入口：50% 触发；触发后 40% 激励、60% 插屏 */
    fun maybeShowAd(
        activity: Activity,
        triggerProbability: Double = 0.5,
        rewardedProbability: Double = 0.4,
        onRewardEarned: ((RewardItem) -> Unit)? = null
    ): Boolean {
        if (Random.nextDouble() >= triggerProbability) return false
        return if (Random.nextDouble() < rewardedProbability) {
            showRewarded(activity, onRewardEarned)
        } else {
            showInterstitial(activity)
        }
    }
}