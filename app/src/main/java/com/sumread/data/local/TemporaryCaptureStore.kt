package com.sumread.data.local

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.sumread.util.AppConfig
import com.sumread.util.DispatchersProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.withContext

@Singleton
class TemporaryCaptureStore @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchersProvider: DispatchersProvider,
) {

    suspend fun save(bitmap: Bitmap): String {
        return withContext(dispatchersProvider.io) {
            val directory = File(context.cacheDir, AppConfig.temporaryCaptureFolder).apply { mkdirs() }
            val file = File(directory, "${UUID.randomUUID()}.png")
            FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }
            file.absolutePath
        }
    }

    suspend fun load(path: String): Bitmap? {
        return withContext(dispatchersProvider.io) {
            BitmapFactory.decodeFile(path)
        }
    }

    suspend fun delete(path: String) {
        withContext(dispatchersProvider.io) {
            File(path).takeIf { it.exists() }?.delete()
        }
    }
}
