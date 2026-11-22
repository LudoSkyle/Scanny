package fr.skyle.scanny.ui.test

import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.GraphicsMode

// Inherited by the subclasses: they draw for real, with the native graphics of Robolectric
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
internal actual abstract class ComposeTest actual constructor()
