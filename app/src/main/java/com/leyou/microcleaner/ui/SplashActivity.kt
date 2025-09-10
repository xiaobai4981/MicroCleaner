package com.leyou.microcleaner.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.KeyEvent
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.leyou.microcleaner.App
import com.leyou.microcleaner.R
import com.leyou.microcleaner.ui.ads.AdUnitId
import com.leyou.microcleaner.ui.home.HomeActivity
import com.tradplus.ads.base.bean.TPAdError
import com.tradplus.ads.base.bean.TPAdInfo
import com.tradplus.ads.base.bean.TPBaseAd
import com.tradplus.ads.base.common.TPPrivacyManager.OnPrivacyRegionListener
import com.tradplus.ads.open.TradPlusSdk
import com.tradplus.ads.open.splash.SplashAdListener
import com.tradplus.ads.open.splash.TPSplash

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    val SPLASH_TIMEOUT: Int = 5000
    val MIN_SPLASH_TIME: Int = 2000
    val AD_INIT_DELAY: Int = 300
    val PREFS_NAME: String = "splash_prefs"
    val FIRST_LAUNCH_KEY: String = "first_launch"
    private var tpSplash: TPSplash? = null
    private var isAdLoad = false
    private var isFirstLaunch = true
    private var adContainer: FrameLayout? = null
    private val handler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var splashStartTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //  隐私权设置
        setPrivacyConsent()
        splashStartTime = System.currentTimeMillis()
        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        isFirstLaunch = prefs.getBoolean(FIRST_LAUNCH_KEY, true)

        // 创建广告容器
        adContainer = findViewById(R.id.splash_container)

        startTimeoutTimer()
    }

    private fun setPrivacyConsent() {
        val activity = this // 避免 lambda 里 this 歧义

        val params = ConsentRequestParameters.Builder()
            // true 表示未达同意年龄；未成年不会弹 GDPR 表单
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)

        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                // onConsentInfoUpdateSuccess
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { loadAndShowError ->
                    if (loadAndShowError != null) {
                        // TODO: 采集失败时可记录日志或提示
                    }
                    // 同意信息已采集完成，可请求广告
                    if (consentInformation.canRequestAds()) {
                        initTPSDK()
                    }
                }
            },
            { requestConsentError ->
                // onConsentInfoUpdateFailure
                // TODO: 这里也可以做日志上报/降级处理
            }
        )

        // 如果用户之前已经同意过，也可以直接初始化
        if (consentInformation.canRequestAds()) {
            initTPSDK()
        }

        // 如果美国加州未投放，可不调用；保留你的原逻辑
        checkAreaSetCCPA()
    }

    private fun checkAreaSetCCPA() {
        // 判断用户是否已经选择过，返回true表示已经进行过选择，就不需要再次进行GDPR弹窗
        val firstShowGDPR = TradPlusSdk.isFirstShowGDPR(this)
        // 查询地区
        TradPlusSdk.checkCurrentArea(this, object : OnPrivacyRegionListener {
            override fun onSuccess(isEu: Boolean, isCn: Boolean, isCalifornia: Boolean) {
                // 获取到相关地域配置后，设置相关隐私API

                // 集成Google UMP后无需处理欧洲地区
                // 表明是欧洲地区，设置GDPR弹窗
//                if (isEu) {
//                    if (!firstShowGDPR) {
//                        TradPlusSdk.showUploadDataNotifyDialog(application, new TradPlusSdk.TPGDPRAuthListener() {
//                            @Override
//                            public void onAuthResult(int level) {
//                                // 获取设置结果并做记录，true 表明用户 进行过选择
//                                TradPlusSdk.setIsFirstShowGDPR(application, true);
//                            }
//                        }, Const.URL.GDPR_URL); // Const.URL.GDPR_URL 为TradPlus 定义的授权页面
//                    }
//                }

                // 表明是美国加州地区，设置CCPA

                if (isCalifornia) {
                    // false 加州用户均不上报数据 ；true 接受上报数据
                    // 默认不上报，如果上报数据，需要让用户选择
                    TradPlusSdk.setCCPADoNotSell(this@SplashActivity, false)
                }


                if (!isEu) {
                    initTPSDK()
                }
            }

            override fun onFailed() {
                // 一般为网络问题导致查询失败，开发者需要自己判断地区，然后进行隐私设置
                // 然后在初始化SDK
                initTPSDK()
            }
        })
    }

    private fun initTPSDK() {
        if (!TradPlusSdk.getIsInit()) {
            // 初始化SDK
            TradPlusSdk.setTradPlusInitListener(object : TradPlusSdk.TradPlusInitListener{
                 override fun onInitSuccess() {
                    // 初始化成功，建议在该回调后 发起广告请求
                     if (isFirstLaunch) {
                         // 首次启动时延迟加载广告，确保SDK完全初始化
                         handler.postDelayed(Runnable { loadSplashAd(adContainer!!) }, AD_INIT_DELAY.toLong())
                     } else {
                         // 非首次启动立即加载广告
                         loadSplashAd(adContainer!!)
                     }
                }
            })
            TradPlusSdk.initSdk(this, AdUnitId.APPID)
        }
    }

    private fun loadSplashAd(adContainer: FrameLayout) {
        if (tpSplash == null) {
            tpSplash = TPSplash(this@SplashActivity, AdUnitId.SPLASH_ADUNITID)
        }
        // 设置监听
        tpSplash!!.setAdListener(object : SplashAdListener() {
            override fun onAdClicked(tpAdInfo: TPAdInfo?) {
            }

            override fun onAdImpression(tpAdInfo: TPAdInfo?) {
            }

            override fun onAdClosed(tpAdInfo: TPAdInfo?) {
                // 广告关闭后，要把开屏页面关闭，如果是跟内容在同一个activity，这里把开屏的容器remove掉
                isAdLoad = false
                adContainer.removeAllViews()
                checkMinSplashTimeBeforeJump()
            }

            override fun onAdLoaded(tpAdInfo: TPAdInfo?, tpBaseAd: TPBaseAd?) {
                // 加载成功后展示广告
                //======================================================================================================
                // 这里一定要注意，需要判断一下是否已经进入app内部，如果加载时间过长，已经进入到app内部，这次load结果就不展示了
                isAdLoad = true
                if (App.isMainActivityActive) {
                    checkMinSplashTimeBeforeJump()
                } else {
                    if (tpSplash != null && tpSplash!!.isReady) {
                        tpSplash!!.showAd(adContainer)
                    } else {
                        checkMinSplashTimeBeforeJump()
                    }
                }
            }

            override fun onAdLoadFailed(tpAdInfo: TPAdError?) {
                // 广告加载失败
                //======================================================================================================
                // 这里一定要注意，需要判断一下是否已经进入app内部，如果加载时间过长，已经进入到app内部，这次load结果就不展示了
                checkMinSplashTimeBeforeJump()
            }
        })

        tpSplash!!.loadAd(null)
    }

    private fun startTimeoutTimer(){
        timeoutRunnable = Runnable {
            if (!isAdLoad && !isFinishing) {
                checkMinSplashTimeBeforeJump()
            }
        }
        handler.postDelayed(timeoutRunnable!!, SPLASH_TIMEOUT.toLong())
    }

    private fun checkMinSplashTimeBeforeJump() {
        val elapsedTime = System.currentTimeMillis() - splashStartTime
        val remainingTime: Long = MIN_SPLASH_TIME - elapsedTime

        if (remainingTime > 0) {
            handler.postDelayed(Runnable { this.jumpToMain() }, remainingTime)
        } else {
            jumpToMain()
        }
    }

    private fun jumpToMain() {
        if (isFinishing) return
        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable!!)
        }

        // 保存非首次启动状态
        if (isFirstLaunch) {
            getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit {
                    putBoolean(FIRST_LAUNCH_KEY, false)
                }
        }

        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (KeyEvent.KEYCODE_BACK == keyCode || KeyEvent.KEYCODE_HOME == keyCode) {
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    override fun onDestroy() {
        // 清理资源
        if (tpSplash != null) {
            tpSplash!!.onDestroy()
        }

        if (timeoutRunnable != null) {
            handler.removeCallbacks(timeoutRunnable!!)
        }

        super.onDestroy()
    }
}