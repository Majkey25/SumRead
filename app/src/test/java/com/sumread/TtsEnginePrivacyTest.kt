package com.sumread

import android.app.Application
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import com.google.common.truth.Truth.assertThat
import com.sumread.data.local.TtsEngine
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.SpeechOptions
import com.sumread.util.DispatchersProvider
import java.util.Locale
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowTextToSpeech

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TtsEnginePrivacyTest {
    @Test
    fun `network-only engine never receives private text`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val dispatchers = object : DispatchersProvider {
            override val main: CoroutineDispatcher = dispatcher
            override val io: CoroutineDispatcher = dispatcher
            override val default: CoroutineDispatcher = dispatcher
        }
        val engine = TtsEngine(RuntimeEnvironment.getApplication(), dispatchers)
        val result = async { runCatching { engine.speak("Synthetic private text", SpeechOptions(1f, 1f, "en-US")) } }
        runCurrent()
        val platformEngine = ShadowTextToSpeech.getLastTextToSpeechInstance()
        val shadow = shadowOf(platformEngine)
        ShadowTextToSpeech.addVoice(Voice("network", Locale.US, Voice.QUALITY_NORMAL, Voice.LATENCY_NORMAL, true, emptySet()))
        shadow.onInitListener.onInit(TextToSpeech.SUCCESS)
        runCurrent()

        assertThat((result.await().exceptionOrNull() as? OperationException)?.failure)
            .isEqualTo(OperationFailure.TtsUnavailable)
        assertThat(shadow.lastSpokenText).isNull()
    }
}
