package com.jaydroid.base_lib.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import android.util.Log
import com.facebook.stetho.Stetho
import com.jaydroid.base_lib.app.appdelegate.IAppLife
import com.jaydroid.base_lib.app.appdelegate.PriorityLevel

/**
 * BaseLibApp,反射调用
 *
 * @author wangxuejie
 * @version 1.0
 * @date 2019-10-15 10:57
 */
class BaseLibApp : IAppLife {

    override fun attachBaseContext(base: Context) {
        Log.d(TAG, "attachBaseContext")

    }

    override fun onCreate(application: Application) {
        Log.d(TAG, "onCreate")
        app = application
        initStetho()
        //网络库测试类,需要依赖lib_net
//        NetTest().testNet()
    }

    /**
     * 初始化Stetho
     */
    private fun initStetho() {
        Stetho.initializeWithDefaults(getApp())
    }

    override fun onTerminate(application: Application) {
        Log.d(TAG, "onTerminate")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        Log.d(TAG, "onConfigurationChanged")
    }

    override fun onLowMemory() {
        Log.d(TAG, "onLowMemory")
    }

    override fun onTrimMemory(level: Int) {
        Log.d(TAG, "onTrimMemory")
    }

    override fun onPriority(): String {
        return PriorityLevel.HIGH
    }

    companion object {
        private const val TAG = "BaseLibApp"
        private lateinit var app: Application
        fun getApp(): Application {
            return app
        }
    }
}
