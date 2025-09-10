package com.leyou.microcleaner.ui.ads

object AdUnitId {
    //正式上线时需要替换成上架号申请的广告位ID和您的应用ID
    //目前是测试ID
    const val APPID: String = " 0E42DD4E0E994170DACCFD3417F0C411"

    // 测试的adUnitId在TradPlus后台配置了部分广告平台
    // 如果复制adUnitId到自己项目中但是项目中没集成这些广告平台就会报adapter找不到
    const val REWRDVIDEO_ADUNITID: String = "7915306952D52F2E5AC6E2819D39D7E2"
    const val INTERSTITIAL_ADUNITID: String = "193365AD1742E8DD975C8333775EE3FA"
    const val BANNER_ADUNITID: String = "5528FFE6BBC0C7AB9CC7B2D56FCEC9F8"
    const val NATIVE_ADUNITID: String = "A95800046261F9C2A0336FA6BC85B60D"
    const val SPLASH_ADUNITID: String = "52B8CF3B7210A4AF8F6B55C87843C376"
}