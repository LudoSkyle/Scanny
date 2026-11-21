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

    /**
     * A screen longer than the screen: an element out of it is not seen at all, and an element cut by
     * its edge is seen cut. Scrolled to until it stands clear of the edges, then until it stops moving:
     * a touch on a list that still moves, or on a screen still coming in, only stops it.
     */
    func scroll(
        to element: XCUIElement,
        in app: XCUIApplication,
        upward: Bool = false,
    ) {
        let window = app.windows.firstMatch
        let clearArea = CGRect(
            x: window.frame.minX,
            y: window.frame.minY + 120,
            width: window.frame.width,
            height: window.frame.height - 220,
        )
        waitUntilStill(element)
        var swipeCount = 0
        while !(element.exists && clearArea.contains(element.frame)) && swipeCount < 5 {
            if upward {
                app.swipeDown(velocity: .slow)
            } else {
                app.swipeUp(velocity: .slow)
            }
            swipeCount += 1
            waitUntilStill(element)
        }
    }

    func waitUntilStill(_ element: XCUIElement) {
        var frame = element.exists ? element.frame : .zero
        for _ in 0..<10 {
            Thread.sleep(forTimeInterval: 0.3)
            let newFrame = element.exists ? element.frame : .zero
            if newFrame == frame {
                return
            }
            frame = newFrame
        }
    }

    /// A text field of Compose, seen as a text view when it may hold several lines.
    func textInput(
        labelled label: String,
        in app: XCUIApplication,
    ) -> XCUIElement {
        app.textViews[label].exists ? app.textViews[label] : app.textFields[label]
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