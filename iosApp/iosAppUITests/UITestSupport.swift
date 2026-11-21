import XCTest

/// What the UI tests share: launching the app on a simulated code, finding an element, keeping a
/// screenshot.
extension XCTestCase {

    /// Launches the app, whose debug build reads [code] as if its camera saw it (see `SimulatedScan`).
    func launchScanning(
        code: String,
        format: String = "QR_CODE",
        language: String = "en",
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["SCANNY_SIMULATED_CODE"] = code
        app.launchEnvironment["SCANNY_SIMULATED_FORMAT"] = format
        app.launchArguments += [
            "-AppleLanguages", "(\(language))",
            "-AppleLocale", language == "fr" ? "fr_FR" : "en_US",
        ]
        app.launch()
        allowTheCameraIfAsked()
        return app
    }

    /**
     * Launches the app, whose debug build reads [codes], one after the other, [delayInMilliseconds]
     * after its start: the time for the test to start a multiple scan.
     */
    func launchScanning(
        codes: [String],
        delayInMilliseconds: Int = 5_000,
        language: String = "en",
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["SCANNY_SIMULATED_CODES"] = codes.joined(separator: "\u{1E}")
        app.launchEnvironment["SCANNY_SIMULATED_DELAY_MS"] = String(delayInMilliseconds)
        app.launchArguments += [
            "-AppleLanguages", "(\(language))",
            "-AppleLocale", language == "fr" ? "fr_FR" : "en_US",
        ]
        app.launch()
        allowTheCameraIfAsked()
        return app
    }

    /// Launches the app with no code to read: the simulator then has no camera.
    func launchApp(language: String = "en") -> XCUIApplication {
        let app = XCUIApplication()
        app.launchArguments += [
            "-AppleLanguages", "(\(language))",
            "-AppleLocale", language == "fr" ? "fr_FR" : "en_US",
        ]
        app.launch()
        allowTheCameraIfAsked()
        return app
    }

    func element(
        labelled label: String,
        in app: XCUIApplication,
    ) -> XCUIElement {
        app.descendants(matching: .any)
            .matching(NSPredicate(format: "label CONTAINS[c] %@", label))
            .firstMatch
    }

    func keepScreenshot(
        named name: String,
        of app: XCUIApplication,
    ) {
        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = name
        attachment.lifetime = .keepAlways
        add(attachment)
    }

    /**
     * A screen longer than the screen: an element out of it is not seen at all, and an element cut by
     * its edge is seen cut. Scrolled to until it stands clear of the edges, then until it stops moving:
     * a touch on a list that still moves, or on a screen still coming in, only stops it.
     */
    func scroll(
        to element: XCUIElement,
        in app: XCUIApplication,
        upward: Bool = false,
    ) {
        let window = app.windows.firstMatch
        let clearArea = CGRect(
            x: window.frame.minX,
            y: window.frame.minY + 120,
            width: window.frame.width,
            height: window.frame.height - 220,
        )
        waitUntilStill(element)
        var swipeCount = 0
        while !(element.exists && clearArea.contains(element.frame)) && swipeCount < 5 {
            if upward {
                app.swipeDown(velocity: .slow)
            } else {
                app.swipeUp(velocity: .slow)
            }
            swipeCount += 1
            waitUntilStill(element)
        }
        // A list still glides by less than a point once it looks still, and Compose takes the first touch
        // to stop it: the touch would be lost
        if swipeCount > 0 {
            Thread.sleep(forTimeInterval: 1)
        }
    }

    /// Opens the detail of the code shown in the result sheet, once the sheet stops rising: a touch on
    /// a sheet still moving is lost.
    func openTheDetail(
        in app: XCUIApplication,
        labelled label: String = "See the detail",
    ) {
        let button = app.buttons[label]
        XCTAssertTrue(button.waitForExistence(timeout: 10))
        waitUntilStill(button)
        button.tap()
    }

    func waitUntilStill(_ element: XCUIElement) {
        var frame = element.exists ? element.frame : .zero
        for _ in 0..<10 {
            Thread.sleep(forTimeInterval: 0.3)
            let newFrame = element.exists ? element.frame : .zero
            if newFrame == frame {
                return
            }
            frame = newFrame
        }
    }

    /// A text field of Compose, seen as a text view when it may hold several lines.
    func textInput(
        labelled label: String,
        in app: XCUIApplication,
    ) -> XCUIElement {
        app.textViews[label].exists ? app.textViews[label] : app.textFields[label]
    }

    /// Allows what the system asks for the app, the camera or the photos: asked once only, the simulator
    /// remembering the answer.
    func allowTheSystemAlertIfAsked(timeout: TimeInterval = 2) {
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let alert = springboard.alerts.firstMatch
        if alert.waitForExistence(timeout: timeout) {
            // The button that allows is the last one of the alert
            alert.buttons.element(boundBy: alert.buttons.count - 1).tap()
        }
    }

    /// The color shown at a point of the screen, in points from its top left corner.
    func screenColor(
        at point: CGPoint,
        in app: XCUIApplication,
    ) -> ScreenColor {
        let screenshot = app.screenshot().image
        guard let image = screenshot.cgImage else {
            return ScreenColor(red: 0, green: 0, blue: 0)
        }
        // Drawn into a single pixel in a known format, whatever the format of the screenshot
        var pixel = [UInt8](repeating: 0, count: 4)
        let context = CGContext(
            data: &pixel,
            width: 1,
            height: 1,
            bitsPerComponent: 8,
            bytesPerRow: 4,
            space: CGColorSpace(name: CGColorSpace.sRGB)!,
            bitmapInfo: CGImageAlphaInfo.premultipliedLast.rawValue,
        )
        let x = (point.x * screenshot.scale).rounded(.down)
        let y = (point.y * screenshot.scale).rounded(.down)
        // Core Graphics counts its rows from the bottom
        context?.draw(
            image,
            in: CGRect(
                x: -x,
                y: y - CGFloat(image.height) + 1,
                width: CGFloat(image.width),
                height: CGFloat(image.height),
            ),
        )
        return ScreenColor(red: Int(pixel[0]), green: Int(pixel[1]), blue: Int(pixel[2]))
    }

    private func allowTheCameraIfAsked() {
        allowTheSystemAlertIfAsked()
    }
}

struct ScreenColor: CustomStringConvertible {
    let red: Int
    let green: Int
    let blue: Int

    var description: String {
        String(format: "#%02X%02X%02X", red, green, blue)
    }

    /// Whether this color is [rgb], as 0xRRGGBB, give or take what the screen rounds.
    func isClose(to rgb: Int) -> Bool {
        let tolerance = 8
        return abs(red - (rgb >> 16 & 0xFF)) <= tolerance
            && abs(green - (rgb >> 8 & 0xFF)) <= tolerance
            && abs(blue - (rgb & 0xFF)) <= tolerance
    }
}
