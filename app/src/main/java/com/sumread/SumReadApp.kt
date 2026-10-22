package com.sumread

import android.app.Application
import com.sumread.data.local.TemporaryCaptureStore
import com.sumread.util.DispatchersProvider
import com.sumread.util.SecureLogger
import dagger.hilt.android.HiltAndroidApp
import java.io.IOException
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@HiltAndroidApp
class SumReadApp : Application() {
    @Inject lateinit var temporaryCaptureStore: TemporaryCaptureStore
    @Inject lateinit var dispatchersProvider: DispatchersProvider

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(dispatchersProvider.io).launch {
            try {
                temporaryCaptureStore.clearOrphans()
            } catch (_: IOException) {
                SecureLogger.warning("Unable to clear abandoned captures")
            }
        }
    }
}
