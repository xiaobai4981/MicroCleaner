package com.leyou.microcleaner.ads

import androidx.fragment.app.Fragment
import com.google.android.gms.ads.rewarded.RewardItem

fun Fragment.maybeShowAd(
    triggerProbability: Double = 0.5,
    rewardedProbability: Double = 0.4,
    onRewardEarned: ((RewardItem) -> Unit)? = null
): Boolean {
    val act = activity ?: return false
    return AdManager.maybeShowAd(
        activity = act,
        triggerProbability = triggerProbability,
        rewardedProbability = rewardedProbability,
        onRewardEarned = onRewardEarned
    )
}
