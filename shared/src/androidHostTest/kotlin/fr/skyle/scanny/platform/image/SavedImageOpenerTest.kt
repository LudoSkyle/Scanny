package fr.skyle.scanny.platform.image

import android.app.Activity
import android.app.DownloadManager
import android.content.Context
import android.content.Intent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.v2.runComposeUiTest
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
class SavedImageOpenerTest {

    @Test
    fun `a saved image opens in the gallery which is let read it`() = runComposeUiTest {
        // Given
        lateinit var openSavedImage: (SavedImage) -> Boolean
        lateinit var context: Context
        setContent {
            openSavedImage = rememberSavedImageOpener()
            context = LocalContext.current
        }

        // When
        val isOpened = openSavedImage(SavedImage(id = IMAGE_URI))

        // Then
        val intent = shadowOf(context as Activity).nextStartedActivity
        assertEquals(
            expected = listOf(true, Intent.ACTION_VIEW, IMAGE_URI, "image/png", true),
            actual = listOf(
                isOpened,
                intent.action,
                intent.data.toString(),
                intent.type,
                intent.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION != 0,
            ),
        )
    }

    @Test
    fun `a saved svg image opens the downloads where it is kept`() = runComposeUiTest {
        // Given
        lateinit var openSavedImage: (SavedImage) -> Boolean
        lateinit var context: Context
        setContent {
            openSavedImage = rememberSavedImageOpener()
            context = LocalContext.current
        }

        // When
        val isOpened = openSavedImage(SavedImage(id = SVG_URI, mimeType = SavedImage.SVG_MIME_TYPE))

        // Then
        val intent = shadowOf(context as Activity).nextStartedActivity
        assertEquals(
            expected = true to DownloadManager.ACTION_VIEW_DOWNLOADS,
            actual = isOpened to intent.action,
        )
    }

    private companion object {
        const val IMAGE_URI = "content://media/external/images/media/42"
        const val SVG_URI = "content://media/external/downloads/43"
    }
}
