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

        goToTheCustomization(in: app)
        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        XCTAssertTrue(createButton.isEnabled)
        createButton.tap()

        // The creation takes the place of the editor, its code shown in a sheet
        XCTAssertTrue(app.staticTexts["Link"].waitForExistence(timeout: 5))
        XCTAssertTrue(element(labelled: "https://ludoskyle.github.io/scanny.github.io/", in: app).exists)
        showTheCode(in: app)
        keepScreenshot(named: "created-link", of: app)
        closeTheSheet(of: app)

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
        goToTheCustomization(in: app)
        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        createButton.tap()
        showTheCode(in: app)

        app.buttons["Share the image"].tap()
        let shareSheet = app.otherElements["ActivityListView"]
        XCTAssertTrue(shareSheet.waitForExistence(timeout: 5))
        keepScreenshot(named: "created-code-share", of: app)
        // The share sheet has no button to close it: swiped down, it leaves the sheet of the code open,
        // where a touch above it would close both
        shareSheet.swipeDown(velocity: .fast)
        XCTAssertTrue(shareSheet.waitForNonExistence(timeout: 5))

        app.buttons["Save the image"].tap()
        // The photos are asked for at the first save only. Saved, the button of the sheet says so
        let savedButton = app.buttons["Image saved"]
        if !savedButton.waitForExistence(timeout: 3) {
            allowTheSystemAlertIfAsked(timeout: 2)
            XCTAssertTrue(savedButton.waitForExistence(timeout: 10))
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
        goToTheCustomization(in: app)
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

        // The sheet of the creation shows its code in the colors chosen: indigo on cream, its margin
        // cream too. The frame of the image is the code itself, inside its margin
        showTheCode(in: app)
        let code = app.images["The code"]
        let margin = screenColor(at: CGPoint(x: code.frame.minX - 8, y: code.frame.minY - 8), in: app)
        let topLeftCorner = screenColor(at: CGPoint(x: code.frame.minX + 3, y: code.frame.minY + 3), in: app)
        XCTAssertTrue(margin.isClose(to: 0xFFF8E1), "The margin is \(margin)")
        XCTAssertTrue(topLeftCorner.isClose(to: 0x3B43BE), "The corner is \(topLeftCorner)")
        keepScreenshot(named: "created-code-with-a-style", of: app)
    }

    func testAFreeColorThatReadsPoorlyIsToldWithoutStoppingTheCreation() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()

        let textField = textInput(labelled: "Text", in: app)
        XCTAssertTrue(textField.waitForExistence(timeout: 5))
        waitUntilStill(textField)
        textField.tap()
        textField.typeText("A pale code")
        // A touch out of the fields closes the keyboard, over the choices
        let preview = app.images["The code"]
        XCTAssertTrue(preview.waitForExistence(timeout: 5))
        preview.tap()

        // The first "Other color" is the one of the dots
        goToTheCustomization(in: app)
        let otherDotColor = element(labelled: "Other color", in: app)
        scroll(to: otherDotColor, in: app)
        otherDotColor.tap()
        let area = element(labelled: "Saturation and brightness", in: app)
        XCTAssertTrue(area.waitForExistence(timeout: 5))
        waitUntilStill(area)
        // The top left corner of the square: almost white, on the white background
        area.coordinate(withNormalizedOffset: CGVector(dx: 0.02, dy: 0.02)).tap()
        keepScreenshot(named: "custom-color-picker", of: app)
        app.buttons["Choose"].tap()

        let warning = app.staticTexts["Its dots and its background stand out too little: some scanners may not read this code."]
        XCTAssertTrue(warning.waitForExistence(timeout: 5))
        XCTAssertTrue(otherDotColor.isSelected)
        keepScreenshot(named: "custom-color-warning", of: app)

        // Told, not stopped
        let createButton = app.buttons["Create"]
        scroll(to: createButton, in: app)
        XCTAssertTrue(createButton.isEnabled)
        createButton.tap()
        XCTAssertTrue(app.buttons["Show the code"].waitForExistence(timeout: 5))
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

        goToTheCustomization(in: app)
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
        showTheCode(in: app)
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
        let textField = textInput(labelled: "Text", in: app)
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

    func testTheCustomizationIsKeptBackAndForthBetweenTheSteps() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()
        typeATextAndCloseTheKeyboard("Back and forth", in: app)
        XCTAssertTrue(app.staticTexts["Step 1 of 2"].exists)

        goToTheCustomization(in: app)
        XCTAssertTrue(app.staticTexts["Step 2 of 2"].exists)
        let green = element(labelled: "Green", in: app)
        scroll(to: green, in: app)
        green.tap()
        XCTAssertTrue(green.isSelected)

        // Back from the top bar returns to the content, the customization kept
        app.buttons["Back"].tap()
        XCTAssertTrue(app.staticTexts["Step 1 of 2"].waitForExistence(timeout: 5))
        goToTheCustomization(in: app)
        XCTAssertTrue(element(labelled: "Green", in: app).isSelected)

        // The back gesture from the edge as well
        let edge = app.coordinate(withNormalizedOffset: CGVector(dx: 0.01, dy: 0.5))
        edge.press(forDuration: 0.05, thenDragTo: app.coordinate(withNormalizedOffset: CGVector(dx: 0.8, dy: 0.5)))
        XCTAssertTrue(app.staticTexts["Step 1 of 2"].waitForExistence(timeout: 5))
        keepScreenshot(named: "editor-back-on-the-content", of: app)

        // Leaving the editor from the content leaves its customization with it
        app.buttons["Back"].tap()
        XCTAssertTrue(app.buttons["Create a code"].waitForExistence(timeout: 5))
        app.buttons["Create a code"].tap()
        typeATextAndCloseTheKeyboard("Started again", in: app)
        goToTheCustomization(in: app)
        let black = element(labelled: "Black", in: app)
        scroll(to: black, in: app)
        XCTAssertTrue(black.isSelected)
    }

    private func typeATextAndCloseTheKeyboard(
        _ text: String,
        in app: XCUIApplication,
    ) {
        let textField = textInput(labelled: "Text", in: app)
        XCTAssertTrue(textField.waitForExistence(timeout: 5))
        waitUntilStill(textField)
        textField.tap()
        textField.typeText(text)
        // A touch out of the fields closes the keyboard, over the button
        let preview = app.images["The code"]
        XCTAssertTrue(preview.waitForExistence(timeout: 5))
        preview.tap()
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