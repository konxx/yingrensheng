package com.yingrensheng.app

import android.app.Application
import com.yingrensheng.core.network.YrsApiConfig

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        YrsApiConfig.configureBackendOrigin(BuildConfig.YRS_BACKEND_ORIGIN)
    }
}
