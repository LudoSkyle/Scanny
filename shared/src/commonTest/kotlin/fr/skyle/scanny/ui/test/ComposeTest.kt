package fr.skyle.scanny.ui.test

/**
 * Base class of the tests that draw a screen in the test itself, with `runComposeUiTest`: on the JVM
 * through Robolectric, which gives Compose the Android framework, and on the iOS simulator as is.
 */
internal expect abstract class ComposeTest()
