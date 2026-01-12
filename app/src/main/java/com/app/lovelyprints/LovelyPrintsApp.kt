package com.app.lovelyprints

import android.app.Application
import com.app.lovelyprints.di.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LovelyPrintsApp : Application() {

    lateinit var appContainer: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()

        appContainer = AppContainer(
            context = applicationContext,
            onUnauthorized = {
                // Handle 401 - force logout
                CoroutineScope(Dispatchers.Main).launch {
                    appContainer.authRepository.logout()
                    // Navigation will be handled by MainActivity observing token state
                }
            }
        )
    }
}