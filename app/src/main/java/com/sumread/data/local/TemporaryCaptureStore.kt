package com.sumread.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.sumread.util.AppConfig
import com.sumread.util.DispatchersProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

@Singleton
class TemporaryCaptureStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchersProvider: DispatchersProvider,
) {
    private val directory = File(context.cacheDir, AppConfig.temporaryCaptureFolder)
    private val mutex = Mutex()
    private var initialized = false

    suspend fun clearOrphans() = withContext(dispatchersProvider.io) {
        mutex.withLock { initialize() }
    }

    suspend fun save(bitmap: Bitmap): String {
        val file = File(directory, "${UUID.randomUUID()}.png")
        try {
            return withContext(dispatchersProvider.io) {
                mutex.withLock {
                    initialize()
                    FileOutputStream(file).use { stream ->
                        if (!bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)) {
                            throw IOException("Unable to save capture")
                        }
                    }
                    file.absolutePath
                }
            }
        } catch (error: Throwable) {
            withContext(NonCancellable + dispatchersProvider.io) {
                remove(file)
            }
            throw error
        }
    }

    suspend fun load(path: String): Bitmap? {
        try {
            return withContext(dispatchersProvider.io) {
                mutex.withLock {
                    initialize()
                    val file = captureFile(path) ?: return@withLock null
                    BitmapFactory.decodeFile(file.absolutePath)
                }
            }
        } finally {
            delete(path)
        }
    }

    suspend fun delete(path: String) {
        withContext(NonCancellable + dispatchersProvider.io) {
            mutex.withLock { captureFile(path)?.let(::remove) }
        }
    }

    private fun initialize() {
        if (initialized) return
        if (!directory.isDirectory && !directory.mkdirs()) throw IOException("Unable to open capture cache")
        val files = directory.listFiles() ?: throw IOException("Unable to read capture cache")
        files.filter { it.isFile && it.extension == "png" }.forEach(::remove)
        initialized = true
    }

    private fun captureFile(path: String): File? = File(path).takeIf {
        it.parentFile?.canonicalFile == directory.canonicalFile && it.extension == "png"
    }

    private fun remove(file: File) {
        if (file.exists() && !file.delete()) {
            throw IOException("Unable to remove capture")
        }
    }
}
