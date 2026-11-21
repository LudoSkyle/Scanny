import XCTest

/// The creation of a code, from the scan, on the simulator.
final class CreateCodeUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testALinkIsCreatedAndShownAsAQrCode() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()

        // The kind is chosen in a menu that unfolds under its field
        let kindField = app.textViews["Type"]
        XCTAssertTrue(kindField.waitForExistence(timeout: 5))
        // Touched once still: the screen fades in, then the menu unfolds
        waitUntilStill(kindField)
        kindField.tap()
        let linkKind = element(labelled: "Link", in: app)
        XCTAssertTrue(linkKind.waitForExistence(timeout: 5))
        waitUntilStill(linkKind)
        linkKind.tap()

        XCTAssertTrue(element(labelled: "Link", in: app).waitForExistence(timeout: 5))
        let linkField = textInput(labelled: "Link", in: app)
        XCTAssertTrue(linkField.waitForExistence(timeout: 5))
        linkField.tap()
        linkField.typeText("ludoskyle.github.io/scanny.github.io/\n")
        XCTAssertTrue(app.images["The code"].waitForExistence(timeout: 5))
        keepScreenshot(named: "editor-with-a-link", of: app)

        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        XCTAssertTrue(createButton.isEnabled)
        createButton.tap()

        // The creation takes the place of the editor
        XCTAssertTrue(app.staticTexts["Link"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.images["The code"].exists)
        XCTAssertTrue(element(labelled: "https://ludoskyle.github.io/scanny.github.io/", in: app).exists)
        keepScreenshot(named: "created-link", of: app)

        // Back on the scan, which the simulator shows without a camera
        app.buttons["Back"].tap()
        XCTAssertTrue(app.images["The code"].waitForNonExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Create a code"].exists)
    }

    func testABarcodeWithAWrongCheckDigitCannotBeCreated() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()

        let kindField = app.textViews["Type"]
        XCTAssertTrue(kindField.waitForExistence(timeout: 5))
        // Touched once still: the screen fades in, then the menu unfolds
        waitUntilStill(kindField)
        kindField.tap()
        let barcodeKind = element(labelled: "Barcode", in: app)
        XCTAssertTrue(barcodeKind.waitForExistence(timeout: 5))
        waitUntilStill(barcodeKind)
        barcodeKind.tap()

        XCTAssertTrue(app.textViews["Format"].waitForExistence(timeout: 5))
        let valueField = textInput(labelled: "Value", in: app)
        XCTAssertTrue(valueField.waitForExistence(timeout: 5))
        valueField.tap()
        valueField.typeText("4006381333932\n")

        XCTAssertTrue(app.staticTexts["This value does not suit this format."].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["Create"].isEnabled)
        keepScreenshot(named: "refused-barcode", of: app)
    }
}