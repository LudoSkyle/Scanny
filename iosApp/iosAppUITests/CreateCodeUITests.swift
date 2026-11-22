import XCTest

/// The creation of a code, from the scan, on the simulator.
final class CreateCodeUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        allowTheSystemAlertsInTheWay()
    }

    func testACreatedCodeIsSharedAndSavedAsAnImage() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()

        // A text, the kind the editor opens on
        typeATextAndCloseTheKeyboard("Shared from Scanny", in: app)
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

    func testALogoPickedInThePhotosIsPutAtTheCenterOfTheCode() throws {
        let app = launchApp()
        app.buttons["Create a code"].tap()

        typeATextAndCloseTheKeyboard("Logo by Scanny", in: app)

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
        goBackByTheEdge(in: app)
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
}