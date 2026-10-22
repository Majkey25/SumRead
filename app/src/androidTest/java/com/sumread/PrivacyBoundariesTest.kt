package com.sumread

import android.content.ContextWrapper
import android.graphics.Bitmap
import android.os.Build
import android.os.SystemClock
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import android.view.InputDevice
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.sumread.data.local.TemporaryCaptureStore
import com.sumread.data.local.offlineVoice
import com.sumread.util.DefaultDispatchersProvider
import java.io.File
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PrivacyBoundariesTest {
    private val context = object : ContextWrapper(InstrumentationRegistry.getInstrumentation().targetContext) {
        override fun getCacheDir(): File = File(super.getCacheDir(), "privacy-test").apply { mkdirs() }
    }

    private fun store() = TemporaryCaptureStore(context, DefaultDispatchersProvider())

    @Test
    fun captureIsRemovedAfterDecodeAndAbandonedCaptureIsRemovedOnRestart() = runBlocking {
        val store = store()
        val bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888)
        bitmap.eraseColor(android.graphics.Color.BLUE)
        try {
            val path = store.save(bitmap)
            store.clearOrphans()
            assertTrue(File(path).exists())
            val decoded = store.load(path)
            assertNotNull(decoded)
            assertEquals(android.graphics.Color.BLUE, decoded!!.getPixel(0, 0))
            decoded.recycle()
            assertFalse(File(path).exists())

            val abandoned = store.save(bitmap)
            store().clearOrphans()
            assertFalse(File(abandoned).exists())
        } finally {
            bitmap.recycle()
        }
    }

    @Test
    fun invalidCaptureIsRemovedButUnrelatedFileIsNotTouched() = runBlocking {
        val store = store()
        val bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
        val path = try { store.save(bitmap) } finally { bitmap.recycle() }
        File(path).writeText("invalid synthetic image")
        assertNull(store.load(path))
        assertFalse(File(path).exists())
        val unrelated = File(context.cacheDir, "unrelated.png").apply { writeText("synthetic") }
        try {
            assertNull(store.load(unrelated.absolutePath))
            assertTrue(unrelated.exists())
        } finally {
            assertTrue(unrelated.delete())
        }
    }

    @Test
    fun speechRejectsNetworkAndMissingVoicesAndPrefersInstalledLocale() {
        val online = voice("online", Locale.US, network = true)
        val download = voice("download", Locale.US, features = setOf(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED))
        val wrongLanguage = voice("french", Locale.FRANCE)
        assertNull(offlineVoice(setOf(online, download, wrongLanguage), Locale.US))
        val english = voice("english", Locale.UK)
        val american = voice("american", Locale.US)
        assertEquals(american, offlineVoice(setOf(online, download, english, american), Locale.US))
        assertEquals(english, offlineVoice(setOf(english), Locale.US))
    }

    @Test
    fun settingsWindowRemainsSecureAfterRecreation() {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { assertTrue(it.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0) }
            scenario.recreate()
            scenario.onActivity { assertTrue(it.window.attributes.flags and WindowManager.LayoutParams.FLAG_SECURE != 0) }
        }
    }

    @Test
    fun settingsRejectObscuredTouchesButAcceptNormalTouches() {
        var receivedDownEvents = 0
        lateinit var target: View
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                target = View(activity).apply {
                    setOnTouchListener { _, event ->
                        if (event.action == MotionEvent.ACTION_DOWN) receivedDownEvents++
                        true
                    }
                }
                activity.setContentView(target)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                val flags = mutableListOf(0, MotionEvent.FLAG_WINDOW_IS_OBSCURED)
                if (Build.VERSION.SDK_INT >= 29) flags += MotionEvent.FLAG_WINDOW_IS_PARTIALLY_OBSCURED
                for (flag in flags) {
                    val coordinates = MotionEvent.PointerCoords().apply {
                        x = target.width / 2f
                        y = target.height / 2f
                        pressure = 1f
                        size = 1f
                    }
                    val pointer = MotionEvent.PointerProperties().apply { id = 0; toolType = MotionEvent.TOOL_TYPE_FINGER }
                    val downTime = SystemClock.uptimeMillis()
                    for (action in listOf(MotionEvent.ACTION_DOWN, MotionEvent.ACTION_UP)) {
                        val event = MotionEvent.obtain(downTime, downTime, action, 1, arrayOf(pointer), arrayOf(coordinates),
                            0, 0, 1f, 1f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, flag)
                        try { activity.dispatchTouchEvent(event) } finally { event.recycle() }
                    }
                    assertEquals(1, receivedDownEvents)
                }
            }
        }
    }

    private fun voice(name: String, locale: Locale, network: Boolean = false, features: Set<String> = emptySet()) =
        Voice(name, locale, Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, network, features)
}
