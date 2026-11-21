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

    func testACreatedCodeIsSharedAndSavedAsAnImage() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()

        // A text, the kind the editor opens on
        let textField = textInput(labelled: "Text", in: app)
        XCTAssertTrue(textField.waitForExistence(timeout: 5))
        waitUntilStill(textField)
        textField.tap()
        textField.typeText("Shared from Scanny")
        // A touch out of the fields closes the keyboard, over the button
        let preview = app.images["The code"]
        XCTAssertTrue(preview.waitForExistence(timeout: 5))
        preview.tap()
        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        createButton.tap()
        XCTAssertTrue(app.buttons["Save the image"].waitForExistence(timeout: 5))

        app.buttons["Share"].tap()
        let shareSheet = app.otherElements["ActivityListView"]
        XCTAssertTrue(shareSheet.waitForExistence(timeout: 5))
        keepScreenshot(named: "created-code-share", of: app)
        // The share sheet has no button to close it: a touch above it does
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.2)).tap()
        XCTAssertTrue(shareSheet.waitForNonExistence(timeout: 5))

        app.buttons["Save the image"].tap()
        // The photos are asked for at the first save only: otherwise the message, shown a few seconds,
        // comes right away
        let savedMessage = app.staticTexts["The image is saved in your photos"]
        if !savedMessage.waitForExistence(timeout: 3) {
            allowTheSystemAlertIfAsked(timeout: 2)
            XCTAssertTrue(savedMessage.waitForExistence(timeout: 10))
        }
        keepScreenshot(named: "created-code-saved", of: app)
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