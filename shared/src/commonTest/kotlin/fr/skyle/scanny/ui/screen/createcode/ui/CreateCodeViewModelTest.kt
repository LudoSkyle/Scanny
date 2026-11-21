package fr.skyle.scanny.ui.screen.createcode.ui

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.fake.FakeRecordCreationUseCase
import fr.skyle.scanny.platform.gallery.LogoPickResult
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())

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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())

        // When
        viewModel.onFormChanged(
            form = CreationForm(
                kind = CreationKind.BARCODE,
                barcodeFormat = BarcodeFormat.EAN_13,
                barcodeValue = "4006381333932",
            ),
        )

        // Then
        assertEquals(
            expected = CodePreview.Refused(format = BarcodeFormat.EAN_13),
            actual = viewModel.uiState.value.preview,
        )
    }

    @Test
    fun `creating records the content in its format and opens the creation`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = CreateCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        val events = collectEvents(viewModel = viewModel)
        viewModel.onFormChanged(
            form = CreationForm(
                kind = CreationKind.BARCODE,
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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = recordCreationUseCase)
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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = recordCreationUseCase)
        viewModel.onStyleChanged(style = STYLE)
        viewModel.onFormChanged(
            form = CreationForm(
                kind = CreationKind.BARCODE,
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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = recordCreationUseCase)
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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
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
    fun `nothing is created while the content is incomplete or refused`() = runTest(testDispatcher) {
        // Given
        val recordCreationUseCase = FakeRecordCreationUseCase()
        val viewModel = CreateCodeViewModel(recordCreationUseCase = recordCreationUseCase)

        // When
        viewModel.onCreateClicked()
        viewModel.onFormChanged(
            form = CreationForm(
                kind = CreationKind.BARCODE,
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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = recordCreationUseCase)
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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase(error = IllegalStateException("Disk full")))
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
        val viewModel = CreateCodeViewModel(recordCreationUseCase = FakeRecordCreationUseCase())
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

    private fun TestScope.collectEvents(viewModel: CreateCodeViewModel): List<CreateCodeViewModel.CreateCodeEvent> {
        val events = mutableListOf<CreateCodeViewModel.CreateCodeEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.event.toList(events)
        }
        return events
    }

    private companion object {
        val STYLE = CodeStyle(
            darkColor = 0xFF3B43BE,
            lightColor = 0xFFFFF8E1,
            dotShape = CodeShape.CIRCLE,
            cornerShape = CodeShape.ROUNDED,
        )
    }
}
