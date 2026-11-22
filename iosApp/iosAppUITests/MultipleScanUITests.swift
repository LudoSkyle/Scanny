import XCTest

/// The multiple scan, on the simulator: the simulated camera reads its codes a few seconds after the
/// launch, once the test has started the mode (see `launchScanning(codes:)`).
final class MultipleScanUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        allowTheSystemAlertsInTheWay()
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
        finishTheMultipleScan(in: app)

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
        // A group is told by its number of codes and their types, not by its first code. The newest,
        // first of the list
        let groupRows = app.buttons.matching(NSPredicate(format: "label CONTAINS %@", "Group of 3 codes"))
        let groupRow = groupRows.firstMatch
        XCTAssertTrue(groupRow.waitForExistence(timeout: 5))
        XCTAssertTrue(groupRow.label.contains("Wi-Fi"))
        // Told apart from the groups of the earlier runs by its time, its last text. The label of the
        // row joins its first five texts only: the time is found among its children
        let groupTime = try XCTUnwrap(groupRow.staticTexts.allElementsBoundByIndex.last?.label)
        let thisGroupRow = groupRows.containing(NSPredicate(format: "label == %@", groupTime)).firstMatch
        keepScreenshot(named: "history-with-group", of: app)
        groupRow.tap()
        XCTAssertTrue(app.staticTexts["Group"].waitForExistence(timeout: 5))

        // Deleted, the group leaves the history it was opened from
        tap(app.buttons["Delete"], until: app.staticTexts["Delete this group?"])
        // The confirmation of the dialog, the last of the buttons named so
        app.buttons.matching(identifier: "Delete").allElementsBoundByIndex.last?.tap()
        XCTAssertTrue(app.staticTexts["History"].waitForExistence(timeout: 5))
        XCTAssertTrue(thisGroupRow.waitForNonExistence(timeout: 5))
    }

    // Touched once enabled and still: a touch as it turns on, while the bar settles, is lost
    private func finishTheMultipleScan(in app: XCUIApplication) {
        let finishButton = app.buttons["Finish"]
        let enabled = XCTNSPredicateExpectation(predicate: NSPredicate(format: "enabled == true"), object: finishButton)
        XCTAssertEqual(XCTWaiter.wait(for: [enabled], timeout: 5), .completed)
        waitUntilStill(finishButton)
        finishButton.tap()
    }
}
