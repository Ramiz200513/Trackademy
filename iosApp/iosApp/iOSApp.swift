import SwiftUI
import ComposeApp

@main
struct iOSApp: App {
    init() {
        KoinIOSKt.initKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
