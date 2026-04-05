package com.sumread.data.local

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.sumread.domain.model.OperationFailure
import com.sumread.domain.model.OperationException
import com.sumread.domain.model.SpeechOptions
import com.sumread.util.DispatchersProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.suspendCancellableCoroutine

@Singleton
class TtsEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchersProvider: DispatchersProvider,
) {

    private val initializationMutex = Mutex()
    private var textToSpeech: TextToSpeech? = null

    suspend fun speak(text: String, options: SpeechOptions) {
        val engine = getOrCreateEngine()
        withContext(dispatchersProvider.main) {
            val locale = if (options.languageTag == "system") {
                Locale.getDefault()
            } else {
                Locale.forLanguageTag(options.languageTag)
            }
            engine.language = locale
            engine.setSpeechRate(options.rate)
            engine.setPitch(options.pitch)
            val utteranceId = UUID.randomUUID().toString()
            suspendCancellableCoroutine { continuation ->
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) = Unit

                    override fun onDone(utteranceId: String?) {
                        if (continuation.isActive) {
                            continuation.resume(Unit)
                        }
                    }

                    override fun onError(utteranceId: String?) {
                        if (continuation.isActive) {
                            continuation.resumeWithException(
                                OperationException(OperationFailure.TtsUnavailable),
                            )
                        }
                    }
                })
                val result = engine.speak(text, TextToSpeech.QUEUE_FLUSH, Bundle(), utteranceId)
                if (result == TextToSpeech.ERROR && continuation.isActive) {
                    continuation.resumeWithException(OperationException(OperationFailure.TtsUnavailable))
                }
            }
        }
    }

    private suspend fun getOrCreateEngine(): TextToSpeech {
        initializationMutex.withLock {
            textToSpeech?.let { return it }
            return withContext(dispatchersProvider.main) {
                suspendCancellableCoroutine { continuation ->
                    var createdEngine: TextToSpeech? = null
                    createdEngine = TextToSpeech(context) { status ->
                        val initializedEngine = createdEngine
                        if (status == TextToSpeech.SUCCESS && initializedEngine != null) {
                            textToSpeech = initializedEngine
                            continuation.resume(initializedEngine)
                        } else {
                            continuation.resumeWithException(OperationException(OperationFailure.TtsUnavailable))
                        }
                    }
                }
            }
        }
    }
}
