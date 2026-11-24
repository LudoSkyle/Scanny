package fr.skyle.scanny.ui.screen.creationdetail.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.fake.FakeCodeImageRenderer
import fr.skyle.scanny.fake.FakeDeleteCreationUseCase
import fr.skyle.scanny.fake.FakeImageSaver
import fr.skyle.scanny.fake.FakeImageSharer
import fr.skyle.scanny.fake.FakeSetCreationFavoriteUseCase
import fr.skyle.scanny.fake.FakeTextSharer
import fr.skyle.scanny.fake.FakeWatchCreationUseCase
import fr.skyle.scanny.fake.FixedClock
import fr.skyle.scanny.platform.image.SavedImage
import fr.skyle.scanny.ui.core.code.image.CodeImageExporter
import fr.skyle.scanny.ui.core.code.model.CodeField
import fr.skyle.scanny.ui.core.code.model.CodeImageFormat
import fr.skyle.scanny.ui.core.code.model.CodeType
import fr.skyle.scanny.ui.core.date.model.CalendarDay
import fr.skyle.scanny.ui.screen.creationdetail.ui.model.CreationDetailItem
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.TimeZone

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class CreationDetailViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setMainDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun resetMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `the creation is shown with its type its format its time its content and its style`() = runTest(testDispatcher) {
        // When
        val viewModel = creationDetailViewModel()

        // Then
        assertEquals(
            expected = CreationDetailItem(
                type = CodeType.LINK,
                format = BarcodeFormat.QR_CODE,
                createdDay = CalendarDay.Today,
                createdTime = "00:00",
                fields = listOf(
                    CodeField(
                        label = null,
                        value = "https://scanny.app",
                    ),
                ),
                rawValue = "https://scanny.app",
                isRawValueShown = false,
                style = STYLE,
                isFavorite = false,
            ),
            actual = viewModel.uiState.value.creation,
        )
    }

    @Test
    fun `a creation that does not exist shows nothing`() = runTest(testDispatcher) {
        // When
        val viewModel = creationDetailViewModel(watchCreationUseCase = FakeWatchCreationUseCase(creation = null))

        // Then
        assertNull(viewModel.uiState.value.creation)
    }

    @Test
    fun `a deleted creation is gone and its screen closes`() = runTest(testDispatcher) {
        // Given
        val deleteCreationUseCase = FakeDeleteCreationUseCase()
        val viewModel = creationDetailViewModel(deleteCreationUseCase = deleteCreationUseCase)
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onDeleteConfirmed()

        // Then
        assertEquals(
            expected = listOf(CREATION_ID),
            actual = deleteCreationUseCase.deletedCreationIds,
        )
        assertEquals(
            expected = listOf(CreationDetailViewModel.CreationDetailEvent.CreationDeleted),
            actual = events,
        )
    }

    @Test
    fun `a creation that cannot be deleted is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = creationDetailViewModel(deleteCreationUseCase = FakeDeleteCreationUseCase(error = IllegalStateException("Disk full")))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onDeleteConfirmed()

        // Then
        assertEquals(
            expected = listOf(CreationDetailViewModel.CreationDetailEvent.DeleteFailed),
            actual = events,
        )
    }

    @Test
    fun `sharing draws the code in its style and shares its image named after the creation`() = runTest(testDispatcher) {
        // Given
        val codeImageRenderer = FakeCodeImageRenderer()
        val imageSharer = FakeImageSharer()
        val viewModel = creationDetailViewModel(
            codeImageRenderer = codeImageRenderer,
            imageSharer = imageSharer,
        )

        // When
        viewModel.onShareImageClicked(imageFormat = CodeImageFormat.PNG)

        // Then
        assertEquals(
            expected = listOf(
                FakeCodeImageRenderer.RenderedCode(
                    rawValue = "https://scanny.app",
                    format = BarcodeFormat.QR_CODE,
                    style = STYLE,
                ),
            ),
            actual = codeImageRenderer.renderedCodes,
        )
        assertEquals(
            expected = listOf(listOf<Byte>(1, 2, 3) to "scanny-3.png"),
            actual = imageSharer.sharedImages,
        )
    }

    @Test
    fun `exporting draws the code in its style and shares its svg image named after the creation`() = runTest(testDispatcher) {
        // Given
        val codeImageRenderer = FakeCodeImageRenderer(svg = "<svg>code</svg>")
        val imageSharer = FakeImageSharer()
        val viewModel = creationDetailViewModel(
            codeImageRenderer = codeImageRenderer,
            imageSharer = imageSharer,
        )

        // When
        viewModel.onShareImageClicked(imageFormat = CodeImageFormat.SVG)

        // Then
        assertEquals(
            expected = listOf(
                FakeCodeImageRenderer.RenderedCode(
                    rawValue = "https://scanny.app",
                    format = BarcodeFormat.QR_CODE,
                    style = STYLE,
                ),
            ),
            actual = codeImageRenderer.renderedCodes,
        )
        assertEquals(
            expected = listOf("<svg>code</svg>" to "scanny-3.svg"),
            actual = imageSharer.sharedSvgs,
        )
    }

    @Test
    fun `saving an svg image keeps it in the files and says it`() = runTest(testDispatcher) {
        // Given
        val imageSaver = FakeImageSaver()
        val viewModel = creationDetailViewModel(
            codeImageRenderer = FakeCodeImageRenderer(svg = "<svg>code</svg>"),
            imageSaver = imageSaver,
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onSaveImageClicked(imageFormat = CodeImageFormat.SVG)

        // Then
        assertEquals(
            expected = listOf("<svg>code</svg>" to "scanny-3.svg") to listOf<CreationDetailViewModel.CreationDetailEvent>(
                CreationDetailViewModel.CreationDetailEvent.ImageSaved(
                    savedImage = SavedImage(id = "saved/scanny-3.svg", mimeType = SavedImage.SVG_MIME_TYPE),
                ),
            ),
            actual = imageSaver.savedSvgs to events,
        )
    }

    @Test
    fun `an svg image the user chose not to keep is not reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = creationDetailViewModel(imageSaver = FakeImageSaver(isSvgCancelled = true))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onSaveImageClicked(imageFormat = CodeImageFormat.SVG)

        // Then
        assertEquals(
            expected = emptyList(),
            actual = events,
        )
    }

    @Test
    fun `an image that cannot be drawn is reported instead of shared`() = runTest(testDispatcher) {
        // Given
        val imageSharer = FakeImageSharer()
        val viewModel = creationDetailViewModel(
            codeImageRenderer = FakeCodeImageRenderer(error = IllegalArgumentException("Code length overflow")),
            imageSharer = imageSharer,
        )
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onShareImageClicked(imageFormat = CodeImageFormat.PNG)

        // Then
        assertEquals(
            expected = listOf(CreationDetailViewModel.CreationDetailEvent.ImageNotShared),
            actual = events,
        )
        assertEquals(
            expected = emptyList(),
            actual = imageSharer.sharedImages,
        )
    }

    @Test
    fun `saving keeps the image in the photos and says it`() = runTest(testDispatcher) {
        // Given
        val imageSaver = FakeImageSaver()
        val viewModel = creationDetailViewModel(imageSaver = imageSaver)
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onSaveImageClicked(imageFormat = CodeImageFormat.PNG)

        // Then
        assertEquals(
            expected = listOf(listOf<Byte>(1, 2, 3) to "scanny-3.png"),
            actual = imageSaver.savedImages,
        )
        assertEquals(
            expected = listOf(CreationDetailViewModel.CreationDetailEvent.ImageSaved(savedImage = SavedImage(id = "saved/scanny-3.png"))),
            actual = events,
        )
    }

    @Test
    fun `an image the photos refuse is reported`() = runTest(testDispatcher) {
        // Given
        val viewModel = creationDetailViewModel(imageSaver = FakeImageSaver(error = IllegalStateException("No access to the photos")))
        val events = collectEvents(viewModel = viewModel)

        // When
        viewModel.onSaveImageClicked(imageFormat = CodeImageFormat.PNG)

        // Then
        assertEquals(
            expected = listOf(CreationDetailViewModel.CreationDetailEvent.ImageNotSaved),
            actual = events,
        )
    }

    @Test
    fun `saving is not offered on a device that does not let the app keep an image`() = runTest(testDispatcher) {
        // When
        val viewModel = creationDetailViewModel(imageSaver = FakeImageSaver(canSave = false))

        // Then
        assertFalse(viewModel.uiState.value.canSaveImage)
    }

    @Test
    fun `the star puts the creation among the favorites and takes it out`() = runTest(testDispatcher) {
        // Given
        val setCreationFavoriteUseCase = FakeSetCreationFavoriteUseCase()
        val notFavorite = creationDetailViewModel(setCreationFavoriteUseCase = setCreationFavoriteUseCase)
        val favorite = creationDetailViewModel(
            watchCreationUseCase = FakeWatchCreationUseCase(creation = linkCreation().copy(isFavorite = true)),
            setCreationFavoriteUseCase = setCreationFavoriteUseCase,
        )

        // When
        notFavorite.onFavoriteClicked()
        favorite.onFavoriteClicked()

        // Then
        assertEquals(
            expected = listOf(CREATION_ID to true, CREATION_ID to false),
            actual = setCreationFavoriteUseCase.setFavorites,
        )
    }

    @Test
    fun `sharing the creation shares the text put in its code`() = runTest(testDispatcher) {
        // Given
        val textSharer = FakeTextSharer()
        val viewModel = creationDetailViewModel(textSharer = textSharer)

        // When
        viewModel.onShareClicked()

        // Then
        assertEquals(
            expected = listOf("https://scanny.app"),
            actual = textSharer.sharedTexts,
        )
    }

    private fun creationDetailViewModel(
        watchCreationUseCase: FakeWatchCreationUseCase = FakeWatchCreationUseCase(creation = linkCreation()),
        deleteCreationUseCase: FakeDeleteCreationUseCase = FakeDeleteCreationUseCase(),
        setCreationFavoriteUseCase: FakeSetCreationFavoriteUseCase = FakeSetCreationFavoriteUseCase(),
        textSharer: FakeTextSharer = FakeTextSharer(),
        codeImageRenderer: FakeCodeImageRenderer = FakeCodeImageRenderer(),
        imageSharer: FakeImageSharer = FakeImageSharer(),
        imageSaver: FakeImageSaver = FakeImageSaver(),
    ): CreationDetailViewModel =
        CreationDetailViewModel(
            creationId = CREATION_ID,
            watchCreationUseCase = watchCreationUseCase,
            deleteCreationUseCase = deleteCreationUseCase,
            setCreationFavoriteUseCase = setCreationFavoriteUseCase,
            textSharer = textSharer,
            codeImageExporter = CodeImageExporter(
                codeImageRenderer = codeImageRenderer,
                imageSharer = imageSharer,
                imageSaver = imageSaver,
            ),
            // Created at the very moment of the test, in UTC
            clock = FixedClock(instant = Instant.fromEpochMilliseconds(0)),
            timeZone = TimeZone.UTC,
        )

    private fun linkCreation(): Creation =
        Creation(
            id = CREATION_ID,
            content = BarcodeContent.Url(url = "https://scanny.app"),
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            style = STYLE,
            createdAt = Instant.fromEpochMilliseconds(0),
            isFavorite = false,
        )

    private fun TestScope.collectEvents(viewModel: CreationDetailViewModel): List<CreationDetailViewModel.CreationDetailEvent> {
        val events = mutableListOf<CreationDetailViewModel.CreationDetailEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private companion object {
        const val CREATION_ID = 3L

        val STYLE = CodeStyle(
            darkColor = 0xFF283593,
            lightColor = 0xFFFFF8E1,
            dotShape = CodeShape.CIRCLE,
            cornerShape = CodeShape.ROUNDED,
        )
    }
}