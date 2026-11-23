import XCTest

/// The settings and the feedback, reached from the scan, on the simulator.
final class SettingsUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        allowTheSystemAlertsInTheWay()
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
        let messageField = textInput(labelled: "Message", in: scanningApp)
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

    func testThePrivacyPolicyOpensInSafari() throws {
        let app = launchApp()
        app.buttons["Settings"].tap()
        // The version read from the bundle of the app
        XCTAssertTrue(app.staticTexts["Version 2.0 (build 20)"].waitForExistence(timeout: 5))

        let privacyPolicyRow = element(labelled: "Privacy policy", in: app)
        scroll(to: privacyPolicyRow, in: app)
        privacyPolicyRow.tap()

        let safari = XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")
        XCTAssertTrue(safari.wait(for: .runningForeground, timeout: 10))
        keepScreenshot(named: "privacy-policy", of: safari)
    }

    // A switch of Compose tells its state as selected, and has no value
    private func isOn(_ toggle: XCUIElement) -> Bool {
        toggle.isSelected
    }
}