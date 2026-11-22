import XCTest

/// The history, reached from the scan, on the simulator: the test first scans its own code (see
/// `launchScanning`).
final class ScanHistoryUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        allowTheSystemAlertsInTheWay()
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
        let title = app.staticTexts["History"]
        XCTAssertTrue(title.waitForExistence(timeout: 5))
        waitUntilStill(title)
        // Its views swipe sideways: on the first one, the back gesture from the edge still leaves it
        goBackByTheEdge(in: app)
        XCTAssertTrue(app.staticTexts["Place a code in the frame"].waitForExistence(timeout: 5))
    }
}