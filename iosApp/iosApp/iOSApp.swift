import SwiftUI
import ComposeApp


@main struct IosApp: App {
    init() {
        KoinKt.initKoinIos()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
