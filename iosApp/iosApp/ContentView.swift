import Shared
import SwiftUI
import UIKit

/// Shows ScannyApp, the Compose Multiplatform UI of shared.
struct ContentView: View {
    var body: some View {
        ComposeView()
            // Compose draws under the system bars itself, from the insets it receives
            .ignoresSafeArea()
    }
}

private struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}