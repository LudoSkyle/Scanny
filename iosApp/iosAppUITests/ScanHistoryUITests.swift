import XCTest

/// The history of the scans and of the creations, reached from the scan, on the simulator: each test
/// first scans or creates its own code (see `launchScanning`).
final class ScanHistoryUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testAScanAppearsInTheHistoryAndOpensItsDetail() throws {
        let code = "In the history \(Date().timeIntervalSince1970)"
        let app = launchScanning(code: code)
        closeTheResult(of: app)

        app.buttons["History"].tap()

        XCTAssertTrue(app.staticTexts["History"].waitForExistence(timeout: 5))
        // A day is a heading for the accessibility, not a plain text
        XCTAssertTrue(app.otherElements["Today"].waitForExistence(timeout: 5))
        // A row is one element for the accessibility: its texts are read together
        let row = element(labelled: code, in: app)
        XCTAssertTrue(row.exists)
        keepScreenshot(named: "history", of: app)

        row.tap()

        XCTAssertTrue(app.otherElements["Content"].waitForExistence(timeout: 5))
        XCTAssertTrue(element(labelled: "Today at ", in: app).exists)
        keepScreenshot(named: "history-detail", of: app)

        app.buttons["Back"].tap()
        XCTAssertTrue(app.staticTexts["History"].waitForExistence(timeout: 5))
        app.buttons["Back"].tap()
        XCTAssertTrue(app.staticTexts["Place a code in the frame"].waitForExistence(timeout: 5))
    }

    func testACreationAppearsInTheCreationsOfTheHistoryAndOpens() throws {
        let text = "Created for the history \(Int(Date().timeIntervalSince1970))"
        let app = launchApp()
        app.buttons["Create a code"].tap()
        let textField = textInput(labelled: "Text", in: app)
        XCTAssertTrue(textField.waitForExistence(timeout: 5))
        waitUntilStill(textField)
        textField.tap()
        textField.typeText(text)
        // A touch out of the fields closes the keyboard, over the button
        app.images["The code"].tap()
        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        createButton.tap()
        XCTAssertTrue(app.buttons["Save the image"].waitForExistence(timeout: 5))
        app.buttons["Back"].tap()

        // The history opens on the scans, its creations a tab away
        app.buttons["History"].tap()
        let creationsTab = element(labelled: "Creations", in: app)
        XCTAssertTrue(creationsTab.waitForExistence(timeout: 5))
        waitUntilStill(creationsTab)
        creationsTab.tap()
        XCTAssertTrue(creationsTab.isSelected)
        let row = element(labelled: text, in: app)
        XCTAssertTrue(row.waitForExistence(timeout: 5))
        keepScreenshot(named: "history-creations", of: app)

        row.tap()
        XCTAssertTrue(app.buttons["Save the image"].waitForExistence(timeout: 5))
        XCTAssertTrue(element(labelled: text, in: app).exists)

        // Back on the creations, which are cleared on their own
        app.buttons["Back"].tap()
        XCTAssertTrue(app.buttons["Clear the history"].waitForExistence(timeout: 5))
        app.buttons["Clear the history"].tap()
        XCTAssertTrue(app.staticTexts["Every creation will be deleted."].waitForExistence(timeout: 5))
        app.buttons["Clear"].tap()
        XCTAssertTrue(app.staticTexts["No creation yet"].waitForExistence(timeout: 5))
        keepScreenshot(named: "history-creations-empty", of: app)
    }

    func testAScannedCodeIsEditedIntoANewCreation() throws {
        let url = "https://scanny.app/edited/\(Int(Date().timeIntervalSince1970))"
        let app = launchScanning(code: url)
        closeTheResult(of: app)
        app.buttons["History"].tap()
        let row = element(labelled: url, in: app)
        XCTAssertTrue(row.waitForExistence(timeout: 5))
        row.tap()

        // The editor opens filled with the content of the scan
        XCTAssertTrue(app.buttons["Edit the code"].waitForExistence(timeout: 5))
        app.buttons["Edit the code"].tap()
        let linkField = textInput(labelled: "Link", in: app)
        XCTAssertTrue(linkField.waitForExistence(timeout: 5))
        XCTAssertEqual(linkField.value as? String, url)
        keepScreenshot(named: "editor-from-a-scan", of: app)

        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        createButton.tap()
        XCTAssertTrue(app.buttons["Save the image"].waitForExistence(timeout: 5))
        XCTAssertTrue(element(labelled: url, in: app).exists)

        // The creation takes the place of the editor: back returns to the scan it came from
        app.buttons["Back"].tap()
        XCTAssertTrue(app.buttons["Edit the code"].waitForExistence(timeout: 5))
    }

    func testAClearedHistoryIsEmpty() throws {
        let app = launchScanning(code: "To clear")
        closeTheResult(of: app)
        app.buttons["History"].tap()
        XCTAssertTrue(app.buttons["Clear the history"].waitForExistence(timeout: 5))

        app.buttons["Clear the history"].tap()
        XCTAssertTrue(app.staticTexts["Clear the history?"].waitForExistence(timeout: 5))
        keepScreenshot(named: "history-clear-dialog", of: app)
        app.buttons["Clear"].tap()

        XCTAssertTrue(app.staticTexts["No scan yet"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["Clear the history"].exists)
        keepScreenshot(named: "history-empty", of: app)
    }

    func testTheHistoryIsInFrench() throws {
        let code = "Dans l'historique \(Date().timeIntervalSince1970)"
        let app = launchScanning(
            code: code,
            language: "fr",
        )
        closeTheResult(
            of: app,
            closeLabel: "Fermer la feuille",
        )

        app.buttons["Historique"].tap()

        XCTAssertTrue(app.otherElements["Aujourd'hui"].waitForExistence(timeout: 5))
        keepScreenshot(named: "french-history", of: app)

        element(labelled: code, in: app).tap()
        XCTAssertTrue(element(labelled: "Aujourd'hui à ", in: app).waitForExistence(timeout: 5))
        keepScreenshot(named: "french-history-detail", of: app)
    }

    // The simulated camera reads its code once: closed, the result leaves the scan free
    private func closeTheResult(
        of app: XCUIApplication,
        closeLabel: String = "Close sheet",
    ) {
        let closeButton = app.buttons[closeLabel]
        XCTAssertTrue(closeButton.waitForExistence(timeout: 10))
        closeButton.tap()
        XCTAssertTrue(closeButton.waitForNonExistence(timeout: 5))
    }
}