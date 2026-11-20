package fr.skyle.scanny.di

import org.koin.core.context.startKoin

/** Starts Koin. Called by the SwiftUI app of iosApp when it starts, as ScannyApplication does on Android. */
fun initKoin() {
    startKoin {
        modules(sharedModule)
    }
}