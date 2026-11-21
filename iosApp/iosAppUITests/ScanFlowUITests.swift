import XCTest

/// The screens that follow a scan, on the simulator. It has no camera: the debug build of the app reads
/// the code given in `SCANNY_SIMULATED_CODE` as if its camera saw it (see `SimulatedScan` in `shared`).
/// Each test launches the app with its own code, in English unless it checks the French texts.
final class ScanFlowUITests: XCTestCase {

    override func setUpWithError() throws {
        continueAfterFailure = false
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

    func testTheDetailCopiesSharesAndGoesBackToTheScan() throws {
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
        XCTAssertTrue(shareSheet.waitForNonExistence(timeout: 5))

        app.buttons["Back"].tap()

        // The code was read once: back on the scan, nothing more is shown
        XCTAssertTrue(app.staticTexts["Place a code in the frame"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["See the detail"].exists)
    }

    func testAnSmsIsWrittenInMessages() throws {
        let app = launchScanning(code: "SMSTO:+33600000000:See you at 10:30 & bring été")

        XCTAssertTrue(app.buttons["Write an SMS"].waitForExistence(timeout: 10))
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

    // The simulator has no Mail app: the e-mail is checked up to the button that writes it
    func testAnEmailShowsItsRecipientAndItsSubject() throws {
        let app = launchScanning(code: "mailto:john@doe.fr?subject=Hello%20Scanny")

        openTheDetail(in: app)

        XCTAssertTrue(app.staticTexts["Recipient"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["john@doe.fr"].exists)
        XCTAssertTrue(app.staticTexts["Hello Scanny"].exists)
        XCTAssertTrue(app.buttons["Write an e-mail"].exists)
        keepScreenshot(named: "email-detail", of: app)
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

    func testAWifiNetworkIsNotOfferedToJoin() throws {
        let app = launchScanning(code: "WIFI:T:WPA;S:ScannyNet;P:secret123;;")

        XCTAssertTrue(app.buttons["See the detail"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.staticTexts["ScannyNet"].exists)
        XCTAssertFalse(app.buttons["Join the network"].exists)
        keepScreenshot(named: "wifi-sheet", of: app)

        openTheDetail(in: app)
        XCTAssertTrue(app.staticTexts["Password"].waitForExistence(timeout: 5))
        XCTAssertTrue(app.staticTexts["secret123"].exists)
        keepScreenshot(named: "wifi-detail", of: app)
    }

    func testAUpcAReadAsAnEan13IsShownAsAUpcA() throws {
        let app = launchScanning(
            code: "0123456789012",
            format: "EAN_13",
        )

        XCTAssertTrue(app.staticTexts["UPC-A"].waitForExistence(timeout: 10))
        XCTAssertTrue(app.staticTexts["123456789012"].exists)
        keepScreenshot(named: "upc-a-sheet", of: app)
    }

    func testTheDetailShowsTheQrCodeDrawnAgain() throws {
        let app = launchScanning(code: "https://ludoskyle.github.io/scanny.github.io/")
        openTheDetail(in: app)

        let showCodeButton = app.buttons["Show the code"]
        XCTAssertTrue(showCodeButton.waitForExistence(timeout: 5))
        showCodeButton.tap()
        XCTAssertTrue(app.images["The code"].waitForExistence(timeout: 5))
        keepScreenshot(named: "qr-code-drawn-again", of: app)

        app.buttons["OK"].tap()
        XCTAssertTrue(app.images["The code"].waitForNonExistence(timeout: 5))
    }

    func testTheDetailShowsABarcodeDrawnAgain() throws {
        let app = launchScanning(
            code: "4006381333931",
            format: "EAN_13",
        )
        openTheDetail(in: app)

        let showCodeButton = app.buttons["Show the code"]
        XCTAssertTrue(showCodeButton.waitForExistence(timeout: 5))
        showCodeButton.tap()
        XCTAssertTrue(app.images["The code"].waitForExistence(timeout: 5))
        keepScreenshot(named: "barcode-drawn-again", of: app)
        app.buttons["OK"].tap()
    }

    func testACodeThatCannotBeDrawnAgainHasNoButtonToShowIt() throws {
        let app = launchScanning(
            code: "In a matrix",
            format: "DATA_MATRIX",
        )
        openTheDetail(in: app)

        XCTAssertTrue(app.buttons["Share"].waitForExistence(timeout: 5))
        XCTAssertFalse(app.buttons["Show the code"].exists)
    }

    func testADeletedCodeGoesBackToTheScan() throws {
        let app = launchScanning(code: "A code to delete")

        openTheDetail(in: app)
        XCTAssertTrue(app.buttons["Delete"].waitForExistence(timeout: 5))

        app.buttons["Delete"].tap()
        XCTAssertTrue(app.staticTexts["Delete this code?"].waitForExistence(timeout: 5))
        keepScreenshot(named: "delete-dialog", of: app)
        // The confirmation of the dialog, the last of the buttons named so
        app.buttons.matching(identifier: "Delete").allElementsBoundByIndex.last?.tap()

        XCTAssertTrue(app.staticTexts["Place a code in the frame"].waitForExistence(timeout: 5))
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