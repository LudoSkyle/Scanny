import XCTest

/// The settings, the feedback and the about screen, reached from the scan, on the simulator.
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

    func testTheSettingsLeadToTheAboutScreenAndTheFeedback() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()

        XCTAssertTrue(app.staticTexts["Version 2.0 (build 20)"].waitForExistence(timeout: 5))
        // Not yet on the App Store
        XCTAssertFalse(element(labelled: "Rate the app", in: app).exists)

        element(labelled: "About", in: app).tap()
        XCTAssertTrue(app.staticTexts["Created by"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Version 2.0 (build 20)"].exists)
        keepScreenshot(named: "about", of: app)
        app.buttons["Back"].tap()

        element(labelled: "Feedback", in: app).tap()
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

    func testThePrivacyPolicyOpensInSafari() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()

        element(labelled: "Privacy policy", in: app).tap()

        let safari = XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")
        XCTAssertTrue(safari.wait(for: .runningForeground, timeout: 10))
        keepScreenshot(named: "privacy-policy", of: safari)
    }

    func testTheSettingsAreInFrench() throws {
        let app = launchApp(language: "fr")

        app.buttons["Réglages"].tap()

        XCTAssertTrue(app.switches["Vibrer après un scan"].waitForExistence(timeout: 5))
        XCTAssertTrue(element(labelled: "Politique de confidentialité", in: app).exists)
        keepScreenshot(named: "french-settings", of: app)

        element(labelled: "Votre avis", in: app).tap()
        XCTAssertTrue(app.buttons["Envoyer l'e-mail"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.textViews["Objet"].exists)
        keepScreenshot(named: "french-feedback", of: app)
    }

    // A switch of Compose tells its state as selected, and has no value
    private func isOn(_ toggle: XCUIElement) -> Bool {
        toggle.isSelected
    }
}