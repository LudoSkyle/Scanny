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
        return app
    }

    /**
     * Launches the app, whose debug build reads [codes], one after the other, [delayInMilliseconds]
     * after its start: the time for the test to start a multiple scan.
     */
    func launchScanning(
        codes: [String],
        delayInMilliseconds: Int = 5_000,
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["SCANNY_SIMULATED_CODES"] = codes.joined(separator: "\u{1E}")
        app.launchEnvironment["SCANNY_SIMULATED_DELAY_MS"] = String(delayInMilliseconds)
        app.launchArguments += [
            "-AppleLanguages", "(en)",
            "-AppleLocale", "en_US",
        ]
        app.launch()
        return app
    }

    /// Launches the app with no code to read: the simulator then has no camera.
    func launchApp() -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += [
            "-AppleLanguages", "(en)",
            "-AppleLocale", "en_US",
        ]
        app.launch()
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
        // A list still glides by less than a point once it looks still, and Compose takes the first touch
        // to stop it: the touch would be lost
        if swipeCount > 0 {
            Thread.sleep(forTimeInterval: 1)
        }
    }

    /// Opens the detail of the code shown in the result sheet, once the sheet stops rising: a touch on
    /// a sheet still moving is lost.
    func openTheDetail(
        in app: XCUIApplication,
        labelled label: String = "See the detail",
    ) {
        let button = app.buttons[label]
        XCTAssertTrue(button.waitForExistence(timeout: 10))
        waitUntilStill(button)
        button.tap()
    }

    /// Closes the sheet of a scan result. The simulated camera reads its code once: closed, the sheet
    /// leaves the scan free.
    func closeTheResult(of app: XCUIApplication) {
        let closeButton = app.buttons["Close sheet"]
        XCTAssertTrue(closeButton.waitForExistence(timeout: 10))
        closeButton.tap()
        XCTAssertTrue(closeButton.waitForNonExistence(timeout: 5))
    }

    /// Types a text in the editor, then closes the keyboard by a touch on the preview, out of the fields.
    func typeATextAndCloseTheKeyboard(
        _ text: String,
        in app: XCUIApplication,
    ) {
        let textField = textInput(labelled: "Text", in: app)
        XCTAssertTrue(textField.waitForExistence(timeout: 5))
        waitUntilStill(textField)
        textField.tap()
        textField.typeText(text)
        let preview = app.images["The code"]
        XCTAssertTrue(preview.waitForExistence(timeout: 5))
        preview.tap()
    }

    /// Goes from the content of a QR code being created to its customization, the second step.
    func goToTheCustomization(in app: XCUIApplication) {
        let nextButton = app.buttons["Next"]
        scroll(to: nextButton, in: app)
        XCTAssertTrue(nextButton.isEnabled)
        nextButton.tap()
        let title = app.staticTexts["Customization"]
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        waitUntilStill(title)
    }

    /// Opens the sheet of the code from a detail, and waits for the code drawn in it to stop rising.
    func showTheCode(in app: XCUIApplication) {
        let showCodeButton = app.buttons["Show the code"]
        XCTAssertTrue(showCodeButton.waitForExistence(timeout: 5))
        waitUntilStill(showCodeButton)
        showCodeButton.tap()
        let code = app.images["The code"]
        XCTAssertTrue(code.waitForExistence(timeout: 5))
        waitUntilStill(code)
    }

    /// Goes back by the gesture from the left edge of the screen, as iOS users do.
    func goBackByTheEdge(in app: XCUIApplication) {
        let edge = app.coordinate(withNormalizedOffset: CGVector(dx: 0.01, dy: 0.5))
        edge.press(forDuration: 0.05, thenDragTo: app.coordinate(withNormalizedOffset: CGVector(dx: 0.8, dy: 0.5)))
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

    /**
     * A text field of Compose, seen as a text view when it may hold several lines. Which one is only
     * known once the screen shows it: chosen before, on a screen still coming in, it could be the one
     * that never comes.
     */
    func textInput(
        labelled label: String,
        in app: XCUIApplication,
        timeout: TimeInterval = 5,
    ) -> XCUIElement {
        let textView = app.textViews[label]
        let textField = app.textFields[label]
        let deadline = Date().addingTimeInterval(timeout)
        while !textView.exists && !textField.exists && Date() < deadline {
            Thread.sleep(forTimeInterval: 0.2)
        }
        return textView.exists ? textView : textField
    }

    /**
     * Allows what the system asks for the app whenever its alert gets in the way of a touch, at no cost
     * when none shows: the simulator remembers the answer, so an alert only shows on a new simulator.
     * Waiting for it at each launch would cost two seconds a test.
     */
    func allowTheSystemAlertsInTheWay() {
        addUIInterruptionMonitor(withDescription: "System alert") { alert in
            // The button that allows is the last one of the alert
            alert.buttons.element(boundBy: alert.buttons.count - 1).tap()
            return true
        }
    }

    /// Allows what the system asks for the app, when no touch would meet its alert: asked once only, the
    /// simulator remembering the answer.
    func allowTheSystemAlertIfAsked(timeout: TimeInterval = 2) {
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let alert = springboard.alerts.firstMatch
        if alert.waitForExistence(timeout: timeout) {
            // The button that allows is the last one of the alert
            alert.buttons.element(boundBy: alert.buttons.count - 1).tap()
        }
    }
}
