package com.sumread

import android.app.Application
import android.graphics.Bitmap
import com.google.common.truth.Truth.assertThat
import com.sumread.data.local.TemporaryCaptureStore
import com.sumread.util.DispatchersProvider
import java.io.File
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TemporaryCaptureStoreTest {
    @Test
    fun `cancelling load before IO still removes the capture`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = object : DispatchersProvider {
            override val main: CoroutineDispatcher = dispatcher
            override val io: CoroutineDispatcher = dispatcher
            override val default: CoroutineDispatcher = dispatcher
        }
        val store = TemporaryCaptureStore(RuntimeEnvironment.getApplication(), dispatchers)
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val saved = async { store.save(bitmap) }
        runCurrent()
        val path = saved.await()
        bitmap.recycle()
        assertThat(File(path).exists()).isTrue()

        val loading = launch(start = CoroutineStart.UNDISPATCHED) { store.load(path) }
        loading.cancel()
        runCurrent()
        loading.join()

        assertThat(File(path).exists()).isFalse()
    }
}
