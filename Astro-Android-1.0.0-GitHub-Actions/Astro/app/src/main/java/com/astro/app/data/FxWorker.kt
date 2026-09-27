package com.astro.app.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class FxWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = try { FxService().latest(inputData.getString("base") ?: "EUR"); Result.success() } catch (_: Throwable) { Result.retry() }
}
