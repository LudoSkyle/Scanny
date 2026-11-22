import XCTest

/// A code edited into a new creation, whether scanned or created, on the simulator.
final class EditCodeUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        allowTheSystemAlertsInTheWay()
    }

    func testACreatedCodeIsEditedIntoANewCreation() throws {
        let text = "To edit \(Int(Date().timeIntervalSince1970))"
        let app = launchApp()
        app.buttons["Create a code"].tap()
        typeATextAndCloseTheKeyboard(text, in: app)
        goToTheCustomization(in: app)
        let green = element(labelled: "Green", in: app)
        scroll(to: green, in: app)
        green.tap()
        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        createButton.tap()

        // The editor opens filled with its content, and its customization after it
        XCTAssertTrue(app.buttons["Edit the code"].waitForExistence(timeout: 5))
        app.buttons["Edit the code"].tap()
        let textField = textInput(labelled: "Text *", in: app)
        XCTAssertTrue(textField.waitForExistence(timeout: 5))
        XCTAssertEqual(textField.value as? String, text)
        goToTheCustomization(in: app)
        let greenAgain = element(labelled: "Green", in: app)
        scroll(to: greenAgain, in: app)
        XCTAssertTrue(greenAgain.isSelected)
        keepScreenshot(named: "editor-from-a-creation", of: app)

        // A new code takes the place of the editor: back returns to the creation it came from
        scroll(to: createButton, in: app)
        createButton.tap()
        XCTAssertTrue(app.buttons["Edit the code"].waitForExistence(timeout: 5))
        app.buttons["Back"].tap()
        XCTAssertTrue(app.buttons["Edit the code"].waitForExistence(timeout: 5))
        XCTAssertTrue(element(labelled: text, in: app).exists)
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
        let linkField = textInput(labelled: "Link *", in: app)
        XCTAssertTrue(linkField.waitForExistence(timeout: 5))
        XCTAssertEqual(linkField.value as? String, url)
        keepScreenshot(named: "editor-from-a-scan", of: app)

        goToTheCustomization(in: app)
        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        createButton.tap()
        XCTAssertTrue(app.buttons["Show the code"].waitForExistence(timeout: 5))
        XCTAssertTrue(element(labelled: url, in: app).exists)

        // The creation takes the place of the editor: back returns to the scan it came from
        app.buttons["Back"].tap()
        XCTAssertTrue(app.buttons["Edit the code"].waitForExistence(timeout: 5))
    }
}
