package com.leyou.microcleaner.ui.ads

import androidx.fragment.app.Fragment

fun Fragment.maybeShowAd(
    triggerProbability: Double = 0.5,
    rewardedProbability: Double = 0.4,
): Boolean {
    val act = activity ?: return false
    return AdManager.maybeShowAd(
        activity = act,
        triggerProbability = triggerProbability,
        rewardedProbability = rewardedProbability,
    )
}
