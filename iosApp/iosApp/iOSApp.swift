import FirebaseCore
import Shared
import SwiftUI

@main
struct iOSApp: App {
    init() {
        // Crashlytics only when Firebase starts: a build without the private Firebase file has none
        if Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") != nil {
            FirebaseApp.configure()
            CrashReportingKt.startCrashReporting()
        }
        KoinInitializerKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
