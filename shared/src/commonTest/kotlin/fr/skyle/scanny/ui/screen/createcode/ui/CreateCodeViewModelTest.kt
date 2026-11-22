package fr.skyle.scanny.ui.screen.createcode.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.model.ScannedCode
import fr.skyle.scanny.fake.FakeRecordCreationUseCase
import fr.skyle.scanny.fake.FakeWatchCreationUseCase
import fr.skyle.scanny.fake.FakeWatchScannedCodeUseCase
import fr.skyle.scanny.platform.gallery.LogoPickResult
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodeRefusal
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationFamily
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationSource
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationStep
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionBlocker
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.time.Instant

// The test dispatchers of kotlinx-coroutines-test are still experimental
@OptIn(ExperimentalCoroutinesApi::class)
class CreateCodeViewModelTest {

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
    fun `the code shows once its content is filled in`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        val initialPreview = viewModel.uiState.value.preview

        // When
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))

        // Then
        assertEquals(
            expected = CodePreview.Incomplete to CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
            actual = initialPreview to viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `the code of a link holds its scheme`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())

        // When
        viewModel.onFormChanged(
            form = CreationForm(
                kind = CreationKind.LINK,
                url = "scanny.app",
            ),
        )

        // Then
        assertEquals(
            expected = CodePreview.Code(rawValue = "https://scanny.app", format = BarcodeFormat.QR_CODE),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `a barcode its format refuses is told as refused`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())

        // When
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.BARCODE,
                barcodeFormat = BarcodeFormat.EAN_13,
                barcodeValue = "4006381333932",
            ),
        )

        // Then
        assertEquals(
            expected = CodePreview.Refused(
                format = BarcodeFormat.EAN_13,
                reason = CodeRefusal.VALUE_NOT_FOR_FORMAT,
            ),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `the content of a qr code left to scanny is counted against the most the lowest correction holds`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())

        // When
        viewModel.onFormChanged(
            form = CreationForm(
                kind = CreationKind.LINK,
                url = "scanny.app",
            ),
        )

        // Then
        // "https://scanny.app", counted with the scheme the code holds
        assertEquals(
            expected = ContentSize(
                used = 18,
                max = 2953,
                hasWideCharacters = false,
            ),
            actual = viewModel.uiState.value.contentSize,
        )
    }

    @Test
    fun `a content over the limit is counted over it and refused`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())

        // When
        viewModel.onFormChanged(form = CreationForm(text = "a".repeat(2954)))

        // Then
        assertEquals(
            expected = true,
            actual = viewModel.uiState.value.contentSize?.isOverLimit,
        )
        assertEquals(
            expected = CodePreview.Refused(
                format = BarcodeFormat.QR_CODE,
                reason = CodeRefusal.TOO_LONG,
            ),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `the limit follows the correction chosen`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        viewModel.onFormChanged(form = CreationForm(text = "a".repeat(2000)))

        // When
        viewModel.onStyleChanged(style = CodeStyle(errorCorrection = CodeErrorCorrection.QUARTILE))

        // Then
        assertEquals(
            expected = ContentSize(
                used = 2000,
                max = 1663,
                hasWideCharacters = false,
            ),
            actual = viewModel.uiState.value.contentSize,
        )
        assertEquals(
            expected = CodePreview.Refused(
                format = BarcodeFormat.QR_CODE,
                reason = CodeRefusal.TOO_LONG,
            ),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `a logo raises the correction to the highest and lowers the limit with it`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))

        // When
        viewModel.onLogoPicked(result = LogoPickResult.Picked(png = byteArrayOf(1, 2, 3)))

        // Then
        assertEquals(
            expected = 1273,
            actual = viewModel.uiState.value.contentSize?.max,
        )
        assertEquals(
            expected = ErrorCorrectionBlocker.TOO_LOW_FOR_LOGO,
            actual = viewModel.uiState.value.errorCorrectionChoices
                .single { choice -> choice.errorCorrection == CodeErrorCorrection.LOW }
                .blocker,
        )
    }

    @Test
    fun `a content too long for a logo keeps the logo out`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())

        // When
        viewModel.onFormChanged(form = CreationForm(text = "a".repeat(2000)))

        // Then
        assertEquals(
            expected = CodePreview.Code(rawValue = "a".repeat(2000), format = BarcodeFormat.QR_CODE),
            actual = viewModel.uiState.value.preview,
        )
        assertEquals(
            expected = LogoBlocker.CONTENT_TOO_LONG,
            actual = viewModel.uiState.value.logoBlocker,
        )
    }

    @Test
    fun `a qr code is created with the correction chosen`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = createCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))

        // When
        viewModel.onStyleChanged(style = CodeStyle(errorCorrection = CodeErrorCorrection.MEDIUM))
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = listOf(CodeStyle(errorCorrection = CodeErrorCorrection.MEDIUM)),
            actual = recordCreationUseCase.recordedCreations.map(FakeRecordCreationUseCase.RecordedCreation::style),
        )
    }

    @Test
    fun `resetting the customization keeps the correction chosen with the content`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        val events = mutableListOf<CreateCodeViewModel.CreateCodeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        viewModel.onStyleChanged(style = CUSTOMIZED_STYLE)

        // When
        viewModel.onResetStyleClicked()

        // Then
        assertEquals(
            expected = Triple(CodeStyle(errorCorrection = CodeErrorCorrection.HIGH), false, listOf<CreateCodeViewModel.CreateCodeEvent>(CreateCodeViewModel.CreateCodeEvent.StyleReset)),
            actual = Triple(viewModel.uiState.value.style, viewModel.uiState.value.canResetStyle, events.toList()),
        )
    }

    @Test
    fun `a reset customization comes back when the reset is undone`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        viewModel.onStyleChanged(style = CUSTOMIZED_STYLE)
        viewModel.onResetStyleClicked()

        // When
        viewModel.onUndoStyleResetClicked()

        // Then
        assertEquals(
            expected = CUSTOMIZED_STYLE to true,
            actual = viewModel.uiState.value.style to viewModel.uiState.value.canResetStyle,
        )
    }

    @Test
    fun `a customization changed after its reset is not undone by it`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        viewModel.onStyleChanged(style = CUSTOMIZED_STYLE)
        viewModel.onResetStyleClicked()
        viewModel.onStyleChanged(style = CodeStyle(darkColor = 0xFF1B5E20))

        // When
        viewModel.onUndoStyleResetClicked()

        // Then
        assertEquals(
            expected = CodeStyle(darkColor = 0xFF1B5E20),
            actual = viewModel.uiState.value.style,
        )
    }

    @Test
    fun `a default customization is not reset`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        val events = mutableListOf<CreateCodeViewModel.CreateCodeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }

        // When
        viewModel.onResetStyleClicked()

        // Then
        assertEquals(
            expected = false to emptyList<CreateCodeViewModel.CreateCodeEvent>(),
            actual = viewModel.uiState.value.canResetStyle to events.toList(),
        )
    }

    @Test
    fun `an empty content counts nothing and a barcode is not counted`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        assertEquals(
            expected = 0,
            actual = viewModel.uiState.value.contentSize?.used,
        )

        // When
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.BARCODE,
                barcodeFormat = BarcodeFormat.EAN_13,
                barcodeValue = "4006381333931",
            ),
        )

        // Then
        assertNull(viewModel.uiState.value.contentSize)
    }

    @Test
    fun `creating records the content in its format and opens the creation`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = createCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        val events = collectEvents(viewModel = viewModel)
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.BARCODE,
                barcodeFormat = BarcodeFormat.EAN_13,
                barcodeValue = "4006381333931",
            ),
        )

        // When
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = listOf(
                FakeRecordCreationUseCase.RecordedCreation(
                    content = BarcodeContent.Text(text = "4006381333931"),
                    format = BarcodeFormat.EAN_13,
                    style = CodeStyle(),
                ),
            ),
            actual = recordCreationUseCase.recordedCreations,
        )
        assertEquals(
            expected = listOf(CreateCodeViewModel.CreateCodeEvent.OpenCreationDetail(creationId = 1)),
            actual = events,
        )
    }

    @Test
    fun `a qr code is created in the style chosen`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = createCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))

        // When
        viewModel.onStyleChanged(style = STYLE)
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = STYLE to listOf(
                FakeRecordCreationUseCase.RecordedCreation(
                    content = BarcodeContent.Text(text = "Hello"),
                    format = BarcodeFormat.QR_CODE,
                    style = STYLE,
                ),
            ),
            actual = viewModel.uiState.value.style to recordCreationUseCase.recordedCreations,
        )
    }

    @Test
    fun `a barcode is created dark on white whatever the style chosen for a qr code`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = createCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        viewModel.onStyleChanged(style = STYLE)
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.BARCODE,
                barcodeFormat = BarcodeFormat.EAN_13,
                barcodeValue = "4006381333931",
            ),
        )

        // When
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = listOf(CodeStyle()),
            actual = recordCreationUseCase.recordedCreations.map(FakeRecordCreationUseCase.RecordedCreation::style),
        )
    }

    @Test
    fun `a qr code is created with the logo picked`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = createCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))

        // When
        viewModel.onLogoPicked(result = LogoPickResult.Picked(png = byteArrayOf(1, 2, 3)))
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = listOf(CodeStyle(logo = byteArrayOf(1, 2, 3))),
            actual = recordCreationUseCase.recordedCreations.map(FakeRecordCreationUseCase.RecordedCreation::style),
        )
    }

    @Test
    fun `an image that cannot be read is told and leaves the logo as it was`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        val events = collectEvents(viewModel = viewModel)
        viewModel.onLogoPicked(result = LogoPickResult.Picked(png = byteArrayOf(1, 2, 3)))

        // When
        viewModel.onLogoPicked(result = LogoPickResult.Unreadable)

        // Then
        assertEquals(
            expected = CodeStyle(logo = byteArrayOf(1, 2, 3)) to listOf(CreateCodeViewModel.CreateCodeEvent.LogoNotRead),
            actual = viewModel.uiState.value.style to events,
        )
    }

    @Test
    fun `a qr code goes on to its customization once its content holds`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel()

        // When
        viewModel.onNextClicked()
        val stepWhileIncomplete = viewModel.uiState.value.step
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))
        viewModel.onNextClicked()

        // Then
        assertEquals(
            expected = CreationStep.CONTENT to CreationStep.STYLE,
            actual = stepWhileIncomplete to viewModel.uiState.value.step,
        )
    }

    @Test
    fun `a barcode has no customization step`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel()
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.BARCODE,
                barcodeFormat = BarcodeFormat.EAN_13,
                barcodeValue = "4006381333931",
            ),
        )

        // When
        viewModel.onNextClicked()

        // Then
        assertEquals(
            expected = CreationStep.CONTENT,
            actual = viewModel.uiState.value.step,
        )
    }

    @Test
    fun `a code in two dimensions is created from its content in its format dark on white`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = createCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        viewModel.onStyleChanged(style = STYLE)
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.TWO_DIMENSIONAL,
                matrixFormat = BarcodeFormat.AZTEC,
                kind = CreationKind.LINK,
                url = "scanny.app",
            ),
        )

        // When
        viewModel.onNextClicked()
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = CreationStep.CONTENT,
            actual = viewModel.uiState.value.step,
        )
        assertEquals(
            expected = listOf(
                FakeRecordCreationUseCase.RecordedCreation(
                    content = BarcodeContent.Url(url = "https://scanny.app"),
                    format = BarcodeFormat.AZTEC,
                    style = CodeStyle(),
                ),
            ),
            actual = recordCreationUseCase.recordedCreations,
        )
    }

    @Test
    fun `a code in two dimensions is not counted`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel()

        // When
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.TWO_DIMENSIONAL,
                text = "Scanny",
            ),
        )

        // Then
        assertNull(viewModel.uiState.value.contentSize)
    }

    @Test
    fun `a character a data matrix does not hold is told as refused for it`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel()

        // When
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.TWO_DIMENSIONAL,
                matrixFormat = BarcodeFormat.DATA_MATRIX,
                text = "Prix : 5 €",
            ),
        )

        // Then
        assertEquals(
            expected = CodePreview.Refused(
                format = BarcodeFormat.DATA_MATRIX,
                reason = CodeRefusal.CHARACTERS_NOT_HELD,
            ),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `a content too long for a pdf417 is told as too long`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel()

        // When
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.TWO_DIMENSIONAL,
                matrixFormat = BarcodeFormat.PDF417,
                text = "a".repeat(4_000),
            ),
        )

        // Then
        assertEquals(
            expected = CodePreview.Refused(
                format = BarcodeFormat.PDF417,
                reason = CodeRefusal.TOO_LONG,
            ),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `back to the content keeps the customization`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel()
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))
        viewModel.onNextClicked()
        viewModel.onStyleChanged(style = STYLE)

        // When
        viewModel.onBackToContentClicked()
        val stepBack = viewModel.uiState.value.step
        viewModel.onNextClicked()

        // Then
        assertEquals(
            expected = Triple(CreationStep.CONTENT, CreationStep.STYLE, STYLE),
            actual = Triple(stepBack, viewModel.uiState.value.step, viewModel.uiState.value.style),
        )
    }

    @Test
    fun `nothing is created while the content is incomplete or refused`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = createCodeViewModel(recordCreationUseCase = recordCreationUseCase)

        // When
        viewModel.onCreateClicked()
        viewModel.onFormChanged(
            form = CreationForm(
                family = CreationFamily.BARCODE,
                barcodeValue = "letters",
            ),
        )
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = emptyList(),
            actual = recordCreationUseCase.recordedCreations,
        )
    }

    @Test
    fun `a code being created is not created twice`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase(completion = CompletableDeferred())
        val viewModel = createCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))

        // When
        viewModel.onCreateClicked()
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = 1,
            actual = recordCreationUseCase.recordedCreations.size,
        )
    }

    @Test
    fun `a code that cannot be created is reported and can be tried again`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase(error = IllegalStateException("Disk full")))
        val events = collectEvents(viewModel = viewModel)
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))

        // When
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = listOf(CreateCodeViewModel.CreateCodeEvent.CreationNotSaved),
            actual = events,
        )
        assertFalse(viewModel.uiState.value.isCreating)
    }

    @Test
    fun `switching to another kind and back keeps what was typed`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
        viewModel.onFormChanged(form = CreationForm(text = "Hello"))

        // When
        viewModel.onFormChanged(form = viewModel.uiState.value.form.copy(kind = CreationKind.WIFI))
        viewModel.onFormChanged(form = viewModel.uiState.value.form.copy(kind = CreationKind.TEXT))

        // Then
        assertEquals(
            expected = CodePreview.Code(rawValue = "Hello", format = BarcodeFormat.QR_CODE),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `the editor opens filled with the scanned code it is given`() = runTest(testDispatcher) {
        // Given
        val watchScannedCodeUseCase = FakeWatchScannedCodeUseCase(
            scannedCode = ScannedCode(
                id = 4,
                scanId = 2,
                rawValue = "https://scanny.app",
                content = BarcodeContent.Url(url = "https://scanny.app"),
                format = BarcodeFormat.QR_CODE,
                scannedAt = Instant.fromEpochMilliseconds(0),
            ),
        )

        // When
        val viewModel = createCodeViewModel(
            source = CreationSource.ScannedCode(codeId = 4),
            watchScannedCodeUseCase = watchScannedCodeUseCase,
        )

        // Then
        assertEquals(
            expected = listOf(4L) to CreationForm(
                kind = CreationKind.LINK,
                eventTimeZone = DEVICE_ZONE,
                url = "https://scanny.app",
            ),
            actual = watchScannedCodeUseCase.watchedCodeIds to viewModel.uiState.value.form,
        )
        assertEquals(
            expected = CodePreview.Code(rawValue = "https://scanny.app", format = BarcodeFormat.QR_CODE),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `a scanned code deleted meanwhile leaves the editor empty`() = runTest(testDispatcher) {
        // When
        val viewModel = createCodeViewModel(
            source = CreationSource.ScannedCode(codeId = 4),
            watchScannedCodeUseCase = FakeWatchScannedCodeUseCase(scannedCode = null),
        )

        // Then
        assertEquals(
            expected = CreationForm(eventTimeZone = DEVICE_ZONE),
            actual = viewModel.uiState.value.form,
        )
    }

    @Test
    fun `the editor opens filled with the content and the style of a created code`() = runTest(testDispatcher) {
        // Given
        val watchCreationUseCase = FakeWatchCreationUseCase(
            creation = Creation(
                id = 3,
                content = BarcodeContent.Text(text = "Hello"),
                rawValue = "Hello",
                format = BarcodeFormat.QR_CODE,
                style = STYLE,
                createdAt = Instant.fromEpochMilliseconds(0),
            ),
        )

        // When
        val viewModel = createCodeViewModel(
            source = CreationSource.CreatedCode(creationId = 3),
            watchCreationUseCase = watchCreationUseCase,
        )

        // Then
        assertEquals(
            expected = CreationForm(text = "Hello", eventTimeZone = DEVICE_ZONE) to STYLE,
            actual = viewModel.uiState.value.form to viewModel.uiState.value.style,
        )
    }

    @Test
    fun `a created code changed is recorded as a new code`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = createCodeViewModel(
            recordCreationUseCase = recordCreationUseCase,
            source = CreationSource.CreatedCode(creationId = 3),
            watchCreationUseCase = FakeWatchCreationUseCase(
                creation = Creation(
                    id = 3,
                    content = BarcodeContent.Text(text = "Hello"),
                    rawValue = "Hello",
                    format = BarcodeFormat.QR_CODE,
                    style = STYLE,
                    createdAt = Instant.fromEpochMilliseconds(0),
                ),
            ),
        )

        // When
        viewModel.onFormChanged(form = viewModel.uiState.value.form.copy(text = "Hello again"))
        viewModel.onCreateClicked()

        // Then
        assertEquals(
            expected = listOf(
                FakeRecordCreationUseCase.RecordedCreation(
                    content = BarcodeContent.Text(text = "Hello again"),
                    format = BarcodeFormat.QR_CODE,
                    style = STYLE,
                ),
            ),
            actual = recordCreationUseCase.recordedCreations,
        )
    }

    @Test
    fun `a new event is in the zone of the device`() = runTest(testDispatcher) {
        // Given
        val viewModel = createCodeViewModel()

        // When
        viewModel.onFormChanged(form = viewModel.uiState.value.form.copy(kind = CreationKind.EVENT))

        // Then
        assertEquals(
            expected = DEVICE_ZONE,
            actual = viewModel.uiState.value.form.eventTimeZone,
        )
    }

    private fun createCodeViewModel(
        recordCreationUseCase: FakeRecordCreationUseCase = FakeRecordCreationUseCase(),
        source: CreationSource = CreationSource.New,
        watchScannedCodeUseCase: FakeWatchScannedCodeUseCase = FakeWatchScannedCodeUseCase(scannedCode = null),
        watchCreationUseCase: FakeWatchCreationUseCase = FakeWatchCreationUseCase(creation = null),
    ): CreateCodeViewModel =
        CreateCodeViewModel(
            source = source,
            watchScannedCodeUseCase = watchScannedCodeUseCase,
            watchCreationUseCase = watchCreationUseCase,
            recordCreationUseCase = recordCreationUseCase,
            timeZone = TimeZone.of(DEVICE_ZONE),
        )

    private fun TestScope.collectEvents(viewModel: CreateCodeViewModel): List<CreateCodeViewModel.CreateCodeEvent> {
        val events = mutableListOf<CreateCodeViewModel.CreateCodeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private companion object {
        // The zone of the device, an event created is in
        const val DEVICE_ZONE = "Europe/Paris"

        // Every part of the customization changed, and the correction chosen with the content
        val CUSTOMIZED_STYLE = CodeStyle(
            darkColor = 0xFF3B43BE,
            lightColor = 0xFFFFF8E1,
            cornerColor = 0xFFB71C1C,
            dotShape = CodeShape.CIRCLE,
            logo = byteArrayOf(1, 2, 3),
            errorCorrection = CodeErrorCorrection.HIGH,
        )

        val STYLE = CodeStyle(
            darkColor = 0xFF3B43BE,
            lightColor = 0xFFFFF8E1,
            dotShape = CodeShape.CIRCLE,
            cornerShape = CodeShape.ROUNDED,
        )
    }
}
