package fr.skyle.scanny

import android.app.Application
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import com.google.firebase.crashlytics.FirebaseCrashlytics
import fr.skyle.scanny.di.sharedModule
import fr.skyle.scanny.log.CrashlyticsLogWriter
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

open class ScannyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Logger.setLogWriters(logWriters())

        startKoin {
            androidContext(this@ScannyApplication)
            modules(sharedModule)
        }
    }

    protected open fun logWriters(): List<LogWriter> =
        listOf(CrashlyticsLogWriter(crashlytics = FirebaseCrashlytics.getInstance()))
}