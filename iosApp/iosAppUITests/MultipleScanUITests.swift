import XCTest

/// The multiple scan, on the simulator: the simulated camera reads its codes a few seconds after the
/// launch, once the test has started the mode (see `launchScanning(codes:)`).
final class MultipleScanUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
    }

    func testAMultipleScanRecordsItsCodesOnceAsAGroup() throws {
        let link = "https://scanny.app/multiple/\(Date().timeIntervalSince1970)"
        let app = launchScanning(
            codes: [
                link,
                "WIFI:T:WPA;S:ScannyNet;P:secret123;;",
                link,
                "A text of the group",
            ],
        )

        app.buttons["Multiple scan"].tap()
        XCTAssertTrue(app.staticTexts["Pass the codes in front of the camera"].waitForExistence(timeout: 2))
        XCTAssertFalse(app.buttons["Finish"].isEnabled)

        // The link read twice counts once
        XCTAssertTrue(app.staticTexts["3 codes found"].waitForExistence(timeout: 15))
        keepScreenshot(named: "multiple-scan-session", of: app)
        app.buttons["Finish"].tap()

        XCTAssertTrue(app.buttons["See the group"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["3 codes"].exists)
        XCTAssertTrue(element(labelled: link, in: app).exists)
        keepScreenshot(named: "multiple-scan-sheet", of: app)

        app.buttons["See the group"].tap()

        XCTAssertTrue(app.staticTexts["Group"].waitForExistence(timeout: 5))
        XCTAssertTrue(element(labelled: "Today at ", in: app).exists)
        XCTAssertTrue(element(labelled: "ScannyNet", in: app).exists)
        keepScreenshot(named: "group", of: app)

        element(labelled: "A text of the group", in: app).tap()
        XCTAssertTrue(app.otherElements["Content"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["A text of the group"].exists)

        app.buttons["Back"].tap()
        XCTAssertTrue(app.staticTexts["Group"].waitForExistence(timeout: 5))
        app.buttons["Back"].tap()

        // The group is in the history, and opens from there
        XCTAssertTrue(app.buttons["History"].waitForExistence(timeout: 5))
        app.buttons["History"].tap()
        let groupRow = element(labelled: link, in: app)
        XCTAssertTrue(groupRow.waitForExistence(timeout: 5))
        XCTAssertTrue(groupRow.label.contains("3 codes"))
        keepScreenshot(named: "history-with-group", of: app)
        groupRow.tap()
        XCTAssertTrue(app.staticTexts["Group"].waitForExistence(timeout: 5))
    }

    func testACodeOfTheResultOpensItsDetail() throws {
        let app = launchScanning(
            codes: [
                "First of two",
                "Second of two",
            ],
        )
        app.buttons["Multiple scan"].tap()
        XCTAssertTrue(app.staticTexts["2 codes found"].waitForExistence(timeout: 15))
        app.buttons["Finish"].tap()
        XCTAssertTrue(app.buttons["See the group"].waitForExistence(timeout: 5))

        element(labelled: "Second of two", in: app).tap()

        XCTAssertTrue(app.otherElements["Content"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Second of two"].exists)
    }

    func testADeletedGroupGoesBackToTheScan() throws {
        let app = launchScanning(
            codes: [
                "A group to delete",
                "Its second code",
            ],
        )
        app.buttons["Multiple scan"].tap()
        XCTAssertTrue(app.staticTexts["2 codes found"].waitForExistence(timeout: 15))
        app.buttons["Finish"].tap()
        XCTAssertTrue(app.buttons["See the group"].waitForExistence(timeout: 5))
        app.buttons["See the group"].tap()
        XCTAssertTrue(app.buttons["Delete"].waitForExistence(timeout: 5))

        app.buttons["Delete"].tap()
        XCTAssertTrue(app.staticTexts["Delete this group?"].waitForExistence(timeout: 5))
        keepScreenshot(named: "group-delete-dialog", of: app)
        // The confirmation of the dialog, the last of the buttons named so
        app.buttons.matching(identifier: "Delete").allElementsBoundByIndex.last?.tap()

        XCTAssertTrue(app.staticTexts["Place a code in the frame"].waitForExistence(timeout: 5))
    }

    func testStoppingAMultipleScanWithCodesAsksToDiscardThem() throws {
        let app = launchScanning(
            codes: [
                "To discard",
                "Also to discard",
            ],
        )
        app.buttons["Multiple scan"].tap()
        XCTAssertTrue(app.staticTexts["2 codes found"].waitForExistence(timeout: 15))

        app.buttons["Multiple scan"].tap()

        XCTAssertTrue(app.staticTexts["Discard the codes found?"].waitForExistence(timeout: 5))
        keepScreenshot(named: "discard-dialog", of: app)
        app.buttons["Discard"].tap()
        XCTAssertTrue(app.staticTexts["Place a code in the frame"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["Finish"].exists)
    }

    func testTheMultipleScanIsInFrench() throws {
        let app = launchScanning(
            codes: [
                "Premier code",
                "Second code",
            ],
            language: "fr",
        )

        app.buttons["Scan multiple"].tap()
        XCTAssertTrue(app.staticTexts["Passez les codes devant la caméra"].waitForExistence(timeout: 2))
        XCTAssertTrue(app.staticTexts["2 codes trouvés"].waitForExistence(timeout: 15))
        keepScreenshot(named: "french-multiple-scan-session", of: app)
        app.buttons["Terminer"].tap()

        XCTAssertTrue(app.buttons["Voir le groupe"].waitForExistence(timeout: 5))
        app.buttons["Voir le groupe"].tap()
        XCTAssertTrue(app.staticTexts["Groupe"].waitForExistence(timeout: 5))
        keepScreenshot(named: "french-group", of: app)
    }
}