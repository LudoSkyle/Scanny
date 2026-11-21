import XCTest

/// The settings and the feedback, reached from the scan, on the simulator.
final class SettingsUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testTheRawContentSettingShowsTheRawContentOfACode() throws {
        let network = "RawNet\(Int(Date().timeIntervalSince1970))"
        let app = launchScanning(code: "WIFI:T:WPA;S:\(network);P:secret123;;")
        XCTAssertTrue(app.buttons["Close sheet"].waitForExistence(timeout: 10))
        app.buttons["Close sheet"].tap()

        app.buttons["Settings"].tap()
        let rawContentSwitch = app.switches["Show the raw content"]
        XCTAssertTrue(rawContentSwitch.waitForExistence(timeout: 5))
        // The settings are kept from a launch to the next: the test starts from the setting off
        if isOn(rawContentSwitch) {
            rawContentSwitch.tap()
        }
        rawContentSwitch.tap()
        XCTAssertTrue(isOn(rawContentSwitch))
        keepScreenshot(named: "settings", of: app)

        app.buttons["Back"].tap()
        // A touch during the transition back to the scan would be lost
        XCTAssertTrue(app.staticTexts["Settings"].waitForNonExistence(timeout: 5))
        app.buttons["History"].tap()
        let row = element(labelled: network, in: app)
        XCTAssertTrue(row.waitForExistence(timeout: 5))
        row.tap()

        XCTAssertTrue(app.otherElements["Raw content"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["WIFI:T:WPA;S:\(network);P:secret123;;"].exists)
        keepScreenshot(named: "detail-with-raw-content", of: app)

        // Back to the setting off, as the other tests expect it, one screen after the other
        app.buttons["Back"].tap()
        XCTAssertTrue(app.otherElements["Raw content"].waitForNonExistence(timeout: 5))
        app.buttons["Back"].tap()
        XCTAssertTrue(app.staticTexts["History"].waitForNonExistence(timeout: 5))
        app.buttons["Settings"].tap()
        XCTAssertTrue(rawContentSwitch.waitForExistence(timeout: 5))
        rawContentSwitch.tap()
        XCTAssertFalse(isOn(rawContentSwitch))
    }

    func testTheSettingsShowTheAppAndLeadToTheFeedback() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()

        XCTAssertTrue(app.staticTexts["Version 2.0 (build 20)"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Scanny"].exists)
        XCTAssertTrue(app.otherElements["Scan preferences"].exists)
        XCTAssertTrue(app.switches["Beep after a scan"].exists)
        XCTAssertTrue(app.switches["Copy the code after a scan"].exists)
        keepScreenshot(named: "settings-top", of: app)

        let feedbackRow = element(labelled: "Give your feedback", in: app)
        scroll(to: feedbackRow, in: app)
        XCTAssertTrue(app.otherElements["Support"].exists)
        // Not yet on the App Store
        XCTAssertFalse(element(labelled: "Rate the app", in: app).exists)
        keepScreenshot(named: "settings-bottom", of: app)

        feedbackRow.tap()
        let sendButton = app.buttons["Send the e-mail"]
        XCTAssertTrue(sendButton.waitForExistence(timeout: 5))
        XCTAssertFalse(sendButton.isEnabled)

        // The subject is chosen in a menu that unfolds under its field
        app.textViews["Subject"].tap()
        let scanSubject = element(labelled: "The scan", in: app)
        XCTAssertTrue(scanSubject.waitForExistence(timeout: 5))
        keepScreenshot(named: "feedback-subjects", of: app)
        scanSubject.tap()
        XCTAssertFalse(sendButton.isEnabled)

        let messageField = app.textViews["Message"].exists ? app.textViews["Message"] : app.textFields["Message"]
        messageField.tap()
        messageField.typeText("The scan is slow on a curved bottle")
        XCTAssertTrue(sendButton.isEnabled)
        keepScreenshot(named: "feedback", of: app)
    }

    func testTheCopySettingCopiesTheScannedCode() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()
        let copySwitch = app.switches["Copy the code after a scan"]
        XCTAssertTrue(copySwitch.waitForExistence(timeout: 5))
        if !isOn(copySwitch) {
            copySwitch.tap()
        }
        XCTAssertTrue(isOn(copySwitch))
        app.terminate()

        let code = "Copied\(Int(Date().timeIntervalSince1970))"
        let scanningApp = launchScanning(code: code)
        XCTAssertTrue(scanningApp.buttons["Close sheet"].waitForExistence(timeout: 10))
        scanningApp.buttons["Close sheet"].tap()

        // Pasted in the message of a feedback: a paste the user asks for, without the alert of iOS
        scanningApp.buttons["Settings"].tap()
        let feedbackRow = element(labelled: "Give your feedback", in: scanningApp)
        scroll(to: feedbackRow, in: scanningApp)
        feedbackRow.tap()
        let messageField = scanningApp.textViews["Message"].exists ? scanningApp.textViews["Message"] : scanningApp.textFields["Message"]
        XCTAssertTrue(messageField.waitForExistence(timeout: 5))
        messageField.press(forDuration: 1.2)
        let pasteItem = scanningApp.menuItems["Paste"]
        XCTAssertTrue(pasteItem.waitForExistence(timeout: 5))
        pasteItem.tap()
        XCTAssertTrue(element(labelled: code, in: scanningApp).waitForExistence(timeout: 5))
        keepScreenshot(named: "pasted-code", of: scanningApp)

        // Back to the setting off, as the other tests expect it
        scanningApp.buttons["Back"].tap()
        XCTAssertTrue(scanningApp.buttons["Send the e-mail"].waitForNonExistence(timeout: 5))
        scroll(to: scanningApp.switches["Copy the code after a scan"], in: scanningApp, upward: true)
        scanningApp.switches["Copy the code after a scan"].tap()
        XCTAssertFalse(isOn(scanningApp.switches["Copy the code after a scan"]))
    }

    func testTheContentOfAScannedCodeOpensWhenTheSettingAsks() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()
        let openSwitch = app.switches["Open the content after a scan"]
        XCTAssertTrue(openSwitch.waitForExistence(timeout: 5))
        if !isOn(openSwitch) {
            openSwitch.tap()
        }
        XCTAssertTrue(isOn(openSwitch))
        app.terminate()

        // A link opens in Safari, without the sheet of the result
        let scanningApp = launchScanning(code: "https://ludoskyle.github.io/scanny.github.io/privacy-policy.html")
        let safari = XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")
        XCTAssertTrue(safari.wait(for: .runningForeground, timeout: 10))
        keepScreenshot(named: "content-opened", of: safari)

        // Back to the setting off, as the other tests expect it
        scanningApp.activate()
        XCTAssertTrue(scanningApp.buttons["Settings"].waitForExistence(timeout: 5))
        scanningApp.buttons["Settings"].tap()
        let switchBack = scanningApp.switches["Open the content after a scan"]
        XCTAssertTrue(switchBack.waitForExistence(timeout: 5))
        switchBack.tap()
        XCTAssertFalse(isOn(switchBack))
    }

    func testAScanIsLeftOutOfTheHistoryWhenTheSettingAsks() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()
        let historySwitch = app.switches["Save the scans in the history"]
        scroll(to: historySwitch, in: app)
        if isOn(historySwitch) {
            historySwitch.tap()
        }
        XCTAssertFalse(isOn(historySwitch))
        app.terminate()

        let code = "Left out \(Int(Date().timeIntervalSince1970))"
        let scanningApp = launchScanning(code: code)
        XCTAssertTrue(scanningApp.buttons["Close sheet"].waitForExistence(timeout: 10))
        scanningApp.buttons["Close sheet"].tap()
        scanningApp.buttons["History"].tap()
        XCTAssertTrue(scanningApp.staticTexts["History"].waitForExistence(timeout: 5))
        XCTAssertFalse(element(labelled: code, in: scanningApp).exists)
        keepScreenshot(named: "history-without-the-scan", of: scanningApp)

        // Back to the history kept, as the other tests expect it
        scanningApp.buttons["Back"].tap()
        XCTAssertTrue(scanningApp.staticTexts["History"].waitForNonExistence(timeout: 5))
        scanningApp.buttons["Settings"].tap()
        let switchBack = scanningApp.switches["Save the scans in the history"]
        scroll(to: switchBack, in: scanningApp)
        switchBack.tap()
        XCTAssertTrue(isOn(switchBack))
    }

    func testTheSameCodeScannedAgainRightAwayIsKeptOnce() throws {
        let code = "Twice \(Int(Date().timeIntervalSince1970))"
        let firstApp = launchScanning(code: code)
        XCTAssertTrue(firstApp.buttons["Close sheet"].waitForExistence(timeout: 10))
        firstApp.buttons["Close sheet"].tap()
        firstApp.terminate()

        let app = launchScanning(code: code)
        XCTAssertTrue(app.buttons["Close sheet"].waitForExistence(timeout: 10))
        app.buttons["Close sheet"].tap()
        app.buttons["History"].tap()
        XCTAssertTrue(app.staticTexts["History"].waitForExistence(timeout: 5))

        // A row is a button, its texts read together; its text is also seen apart
        let rows = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] %@", code))
        XCTAssertTrue(rows.firstMatch.waitForExistence(timeout: 5))
        XCTAssertEqual(rows.count, 1)
        keepScreenshot(named: "history-with-the-code-once", of: app)
    }

    func testTheRetentionOfTheHistoryIsChosenInAMenu() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()
        let retentionRow = element(labelled: "Keep the history", in: app)
        scroll(to: retentionRow, in: app)
        XCTAssertTrue(retentionRow.label.contains("30 days"))

        retentionRow.tap()
        let forever = element(labelled: "Forever", in: app)
        XCTAssertTrue(forever.waitForExistence(timeout: 5))
        keepScreenshot(named: "retention-menu", of: app)
        forever.tap()
        XCTAssertTrue(forever.waitForNonExistence(timeout: 5) || retentionRow.label.contains("Forever"))
        XCTAssertTrue(element(labelled: "Keep the history", in: app).label.contains("Forever"))

        // Back to 30 days, as the other tests expect it
        element(labelled: "Keep the history", in: app).tap()
        let thirtyDays = element(labelled: "30 days", in: app)
        XCTAssertTrue(thirtyDays.waitForExistence(timeout: 5))
        thirtyDays.tap()
        XCTAssertTrue(
            NSPredicate(format: "label CONTAINS[c] %@", "30 days")
                .evaluate(with: element(labelled: "Keep the history", in: app)),
        )
    }

    func testThePrivacyPolicyOpensInSafari() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()

        let privacyPolicyRow = element(labelled: "Privacy policy", in: app)
        scroll(to: privacyPolicyRow, in: app)
        privacyPolicyRow.tap()

        let safari = XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")
        XCTAssertTrue(safari.wait(for: .runningForeground, timeout: 10))
        keepScreenshot(named: "privacy-policy", of: safari)
    }

    func testTheSettingsAreInFrench() throws {
        let app = launchApp(language: "fr")

        app.buttons["Réglages"].tap()

        XCTAssertTrue(app.switches["Vibrer après un scan"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.otherElements["Préférences du scan"].exists)
        keepScreenshot(named: "french-settings", of: app)

        let privacyPolicyRow = element(labelled: "Politique de confidentialité", in: app)
        scroll(to: privacyPolicyRow, in: app)
        XCTAssertTrue(privacyPolicyRow.exists)

        let feedbackRow = element(labelled: "Donner votre avis", in: app)
        scroll(to: feedbackRow, in: app)
        feedbackRow.tap()
        XCTAssertTrue(app.buttons["Envoyer l'e-mail"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.textViews["Objet"].exists)
        keepScreenshot(named: "french-feedback", of: app)
    }

    /**
     * The settings are longer than the screen: a row out of it is not seen at all, and a row cut by its
     * edge is seen cut. Scrolled to until it stands clear of the edges, then until it stops moving: a
     * touch on a list that still moves, or on a screen still coming in, only stops it.
     */
    private func scroll(
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

    private func waitUntilStill(_ element: XCUIElement) {
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

    // A switch of Compose tells its state as selected, and has no value
    private func isOn(_ toggle: XCUIElement) -> Bool {
        toggle.isSelected
    }
}