package com.leyou.microcleaner.global.utils

import com.bonepeople.android.widget.util.AppLog

object LogUtil {
    val app by lazy { AppLog.tag("SDAppLog") }
    val test by lazy { AppLog.tag("SDAppLog.Test").apply { showStackInfo = true } }
}