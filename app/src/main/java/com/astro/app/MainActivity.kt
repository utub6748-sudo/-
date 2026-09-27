package com.astro.app
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.work.*
import java.util.concurrent.TimeUnit
import com.astro.app.ui.*
import com.astro.app.ui.theme.AstroTheme
class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);val request=PeriodicWorkRequestBuilder<com.astro.app.data.FxWorker>(6,TimeUnit.HOURS).setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build();WorkManager.getInstance(this).enqueueUniquePeriodicWork("astro_fx_sync",ExistingPeriodicWorkPolicy.KEEP,request);setContent{val vm:AstroViewModel=viewModel(factory=AstroViewModel.Factory(applicationContext));AstroTheme(vm){AstroApp(vm)}}}}
