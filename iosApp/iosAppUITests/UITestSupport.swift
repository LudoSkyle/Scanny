import XCTest

/// What the UI tests share: launching the app on a simulated code, finding an element, keeping a
/// screenshot.
extension XCTestCase {

    /// Launches the app, whose debug build reads [code] as if its camera saw it (see `SimulatedScan`).
    func launchScanning(
        code: String,
        format: String = "QR_CODE",
        language: String = "en",
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["SCANNY_SIMULATED_CODE"] = code
        app.launchEnvironment["SCANNY_SIMULATED_FORMAT"] = format
        app.launchArguments += [
            "-AppleLanguages", "(\(language))",
            "-AppleLocale", language == "fr" ? "fr_FR" : "en_US",
        ]
        app.launch()
        allowTheCameraIfAsked()
        return app
    }

    /**
     * Launches the app, whose debug build reads [codes], one after the other, [delayInMilliseconds]
     * after its start: the time for the test to start a multiple scan.
     */
    func launchScanning(
        codes: [String],
        delayInMilliseconds: Int = 5_000,
        language: String = "en",
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["SCANNY_SIMULATED_CODES"] = codes.joined(separator: "\u{1E}")
        app.launchEnvironment["SCANNY_SIMULATED_DELAY_MS"] = String(delayInMilliseconds)
        app.launchArguments += [
            "-AppleLanguages", "(\(language))",
            "-AppleLocale", language == "fr" ? "fr_FR" : "en_US",
        ]
        app.launch()
        allowTheCameraIfAsked()
        return app
    }

    /// Launches the app with no code to read: the simulator then has no camera.
    func launchApp(language: String = "en") -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += [
            "-AppleLanguages", "(\(language))",
            "-AppleLocale", language == "fr" ? "fr_FR" : "en_US",
        ]
        app.launch()
        allowTheCameraIfAsked()
        return app
    }

    func element(
        labelled label: String,
        in app: XCUIApplication,
    ) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS[c] %@", label))
            .firstMatch
    }

    func keepScreenshot(
        named name: String,
        of app: XCUIApplication,
    ) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    // Asked at the first launch only: the simulator remembers the answer
    private func allowTheCameraIfAsked() {
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let alert = springboard.alerts.firstMatch
        if alert.waitForExistence(timeout: 2) {
            // The button that allows is the last one of the alert
            alert.buttons.element(boundBy: alert.buttons.count - 1).tap()
        }
    }
}