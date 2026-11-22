import XCTest

/// The screens that follow a scan, on the simulator. It has no camera: the debug build of the app reads
/// the code given in `SCANNY_SIMULATED_CODE` as if its camera saw it (see `SimulatedScan` in `shared`).
/// Each test launches the app with its own code, in English unless it checks the French texts.
final class ScanFlowUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
        allowTheSystemAlertsInTheWay()
    }

    func testALinkOpensInSafari() throws {
        let app = launchScanning(code: "https://scanny.app")

        XCTAssertTrue(app.buttons["Open the link"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.staticTexts["https://scanny.app"].exists)
        XCTAssertTrue(app.staticTexts["QR Code"].exists)
        keepScreenshot(named: "link-sheet", of: app)

        app.buttons["Open the link"].tap()

        let safari = XCUIApplication(bundleIdentifier: "com.apple.mobilesafari")
        XCTAssertTrue(safari.wait(for: .runningForeground, timeout: 10))
        keepScreenshot(named: "link-safari", of: safari)
    }

    func testTheDetailCopiesSharesAndDeletesTheCode() throws {
        let app = launchScanning(code: "https://scanny.app/detail")

        openTheDetail(in: app)

        XCTAssertTrue(app.otherElements["Content"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["https://scanny.app/detail"].exists)
        keepScreenshot(named: "detail", of: app)

        app.buttons["Copy"].tap()
        // iOS shows nothing when a text is copied: the app tells it
        XCTAssertTrue(app.staticTexts["Copied to the clipboard"].waitForExistence(timeout: 3))
        keepScreenshot(named: "detail-copied", of: app)

        app.buttons["Share"].tap()
        let shareSheet = app.otherElements["ActivityListView"]
        XCTAssertTrue(shareSheet.waitForExistence(timeout: 5))
        keepScreenshot(named: "detail-share", of: app)
        // The share sheet has no button to close it: a touch above it does
        app.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.4)).tap()
        waitForTheShareSheetToLeave(shareSheet)

        // Deleted, the code leaves its detail for the scan
        tap(app.buttons["Delete"], until: app.staticTexts["Delete this code?"])
        // The confirmation of the dialog, the last of the buttons named so
        app.buttons.matching(identifier: "Delete").allElementsBoundByIndex.last?.tap()

        // The code was read once: back on the scan, nothing more is shown
        XCTAssertTrue(app.staticTexts["Place a code in the frame"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["See the detail"].exists)
    }

    func testAnSmsIsWrittenInMessages() throws {
        let app = launchScanning(code: "SMSTO:+33600000000:See you at 10:30 & bring été")

        XCTAssertTrue(app.buttons["Write an SMS"].waitForExistence(timeout: 10))
        // The sheet shows the whole content, the message with the number
        XCTAssertTrue(app.staticTexts["+33600000000"].exists)
        XCTAssertTrue(app.staticTexts["See you at 10:30 & bring été"].exists)
        keepScreenshot(named: "sms-sheet", of: app)
        app.buttons["Write an SMS"].tap()

        let messages = XCUIApplication(bundleIdentifier: "com.apple.MobileSMS")
        XCTAssertTrue(messages.wait(for: .runningForeground, timeout: 10))
        // At its first launch on a simulator, Messages presents its sharing before the message
        let sharingIntroduction = messages.staticTexts["Shared with You"]
        if sharingIntroduction.waitForExistence(timeout: 3) {
            messages.staticTexts["OK"].tap()
        }
        let messageField = messages.textFields["messageBodyField"]
        XCTAssertTrue(messageField.waitForExistence(timeout: 10))
        // Time for Messages to show the number it was given as the recipient
        sleep(2)
        keepScreenshot(named: "sms-messages", of: messages)
        XCTAssertEqual(messageField.value as? String, "See you at 10:30 & bring été")
    }

    func testAContactIsAddedToTheContacts() throws {
        let vCard = [
            "BEGIN:VCARD",
            "VERSION:3.0",
            "N:Doe;John;;;",
            "FN:John Doe",
            "ORG:Doe & Co",
            "TITLE:Lead",
            "TEL:+33600000000",
            "TEL:+33100000000",
            "EMAIL:john@doe.fr",
            "ADR:;;12 rue de la Paix\\, Paris;;;;",
            "URL:https://doe.fr",
            "END:VCARD",
        ].joined(separator: "\n")
        let app = launchScanning(code: vCard)

        XCTAssertTrue(app.buttons["Add the contact"].waitForExistence(timeout: 10))
        app.buttons["Add the contact"].tap()

        let doneButton = app.navigationBars.buttons["Done"]
        XCTAssertTrue(doneButton.waitForExistence(timeout: 10))
        keepScreenshot(named: "contact-editor", of: app)
        doneButton.tap()

        // Back on the sheet once the contact is saved
        XCTAssertTrue(app.buttons["Add the contact"].waitForExistence(timeout: 10))

        let contacts = XCUIApplication(bundleIdentifier: "com.apple.MobileAddressBook")
        contacts.launch()
        XCTAssertTrue(contacts.staticTexts["John Doe"].firstMatch.waitForExistence(timeout: 10))
        contacts.staticTexts["John Doe"].firstMatch.tap()
        for value in ["Doe & Co", "Lead", "+33 6 00 00 00 00", "+33 1 00 00 00 00", "john@doe.fr"] {
            XCTAssertTrue(element(labelled: value, in: contacts).waitForExistence(timeout: 5), value)
        }
        keepScreenshot(named: "contact-saved", of: contacts)
        // The address and the website are lower on the card
        contacts.swipeUp()
        for value in ["https://doe.fr", "12 rue de la Paix, Paris"] {
            XCTAssertTrue(element(labelled: value, in: contacts).waitForExistence(timeout: 5), value)
        }
        keepScreenshot(named: "contact-saved-bottom", of: contacts)
    }

    func testTheTextsAreInFrench() throws {
        let app = launchScanning(
            code: "https://scanny.app/fr",
            language: "fr",
        )

        XCTAssertTrue(app.buttons["Ouvrir le lien"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.staticTexts["Lien"].exists)
        XCTAssertTrue(app.buttons["Copier"].exists)
        keepScreenshot(named: "french-sheet", of: app)

        openTheDetail(in: app, labelled: "Voir le détail")
        XCTAssertTrue(app.otherElements["Contenu"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.buttons["Partager"].exists)
        keepScreenshot(named: "french-detail", of: app)

        app.buttons["Supprimer"].tap()
        XCTAssertTrue(app.staticTexts["Supprimer ce code\u{00A0}?"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["Il sera retiré de l'historique."].exists)
        keepScreenshot(named: "french-delete-dialog", of: app)
    }
}