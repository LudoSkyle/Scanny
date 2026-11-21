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

    func testAQrCodeIsCreatedInTheColorsAndTheShapesChosen() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()

        let textField = textInput(labelled: "Text", in: app)
        XCTAssertTrue(textField.waitForExistence(timeout: 5))
        waitUntilStill(textField)
        textField.tap()
        textField.typeText("Styled by Scanny")
        // A touch out of the fields closes the keyboard, over the choices
        let preview = app.images["The code"]
        XCTAssertTrue(preview.waitForExistence(timeout: 5))
        preview.tap()

        // A color is a disc, the chosen one selected
        let indigo = element(labelled: "Indigo", in: app)
        scroll(to: indigo, in: app)
        indigo.tap()
        let cream = element(labelled: "Cream", in: app)
        cream.tap()
        XCTAssertTrue(indigo.isSelected)
        XCTAssertTrue(cream.isSelected)
        XCTAssertFalse(element(labelled: "White", in: app).isSelected)

        let dotShapeField = textInput(labelled: "Shape of the dots", in: app)
        scroll(to: dotShapeField, in: app)
        dotShapeField.tap()
        // "Round", not "Rounded"
        let roundShape = app.descendants(matching: .any).matching(NSPredicate(format: "label == %@", "Round")).firstMatch
        XCTAssertTrue(roundShape.waitForExistence(timeout: 5))
        waitUntilStill(roundShape)
        roundShape.tap()
        keepScreenshot(named: "editor-with-a-style", of: app)

        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        createButton.tap()

        // The creation shows its code in the colors chosen: indigo on cream, its margin cream too. The
        // frame of the image is the code itself, inside its margin
        XCTAssertTrue(app.buttons["Save the image"].waitForExistence(timeout: 5))
        let code = app.images["The code"]
        XCTAssertTrue(code.exists)
        waitUntilStill(code)
        let margin = screenColor(at: CGPoint(x: code.frame.minX - 8, y: code.frame.minY - 8), in: app)
        let topLeftCorner = screenColor(at: CGPoint(x: code.frame.minX + 3, y: code.frame.minY + 3), in: app)
        XCTAssertTrue(margin.isClose(to: 0xFFF8E1), "The margin is \(margin)")
        XCTAssertTrue(topLeftCorner.isClose(to: 0x3B43BE), "The corner is \(topLeftCorner)")
        keepScreenshot(named: "created-code-with-a-style", of: app)
    }

    func testALogoPickedInThePhotosIsPutAtTheCenterOfTheCode() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()

        let textField = textInput(labelled: "Text", in: app)
        XCTAssertTrue(textField.waitForExistence(timeout: 5))
        waitUntilStill(textField)
        textField.tap()
        textField.typeText("Logo by Scanny")
        // A touch out of the fields closes the keyboard, over the choices
        let preview = app.images["The code"]
        XCTAssertTrue(preview.waitForExistence(timeout: 5))
        preview.tap()

        let addLogo = app.buttons["Add a logo"]
        scroll(to: addLogo, in: app)
        addLogo.tap()
        pickTheNewestPhoto(in: app)
        XCTAssertTrue(app.images["The logo"].waitForExistence(timeout: 10))
        keepScreenshot(named: "editor-with-a-logo", of: app)

        // Removed, the logo leaves the code
        app.buttons["Remove the logo"].tap()
        XCTAssertTrue(app.buttons["Add a logo"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.images["The logo"].exists)

        // Picked again, it goes with the creation
        app.buttons["Add a logo"].tap()
        pickTheNewestPhoto(in: app)
        XCTAssertTrue(app.buttons["Change the logo"].waitForExistence(timeout: 10))
        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        createButton.tap()
        XCTAssertTrue(app.buttons["Save the image"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.images["The code"].exists)
        keepScreenshot(named: "created-code-with-a-logo", of: app)
    }

    // The first photo of the grid, whatever the photos the other tests saved: an older one goes further
    // down at each run, until the picker no longer lays it out
    private func pickTheNewestPhoto(in app: XCUIApplication) {
        let photo = app.images.matching(identifier: "PXGGridLayout-Info").firstMatch
        XCTAssertTrue(photo.waitForExistence(timeout: 10))
        waitUntilStill(photo)
        // Touched at its place: the picker runs in another process, where XCUITest cannot always tell
        // whether a photo can be touched
        photo.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5)).tap()
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