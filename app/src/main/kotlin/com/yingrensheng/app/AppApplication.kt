package com.yingrensheng.app

import android.app.Application
import com.yingrensheng.app.data.KeystoreAuthTokenStore
import com.yingrensheng.core.network.AuthSessionManager
import com.yingrensheng.core.network.YrsApiConfig

class AppApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        YrsApiConfig.configureBackendOrigin(BuildConfig.YRS_BACKEND_ORIGIN)
        AuthSessionManager.initialize(
            store = KeystoreAuthTokenStore(this),
            onExpired = {
                // UI observes repository state; the next app resume/request will route to login.
            },
        )
    }
}
