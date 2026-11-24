package fr.skyle.scanny.ui.core.code.image

import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeShape
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.fake.FakeCodeImageRenderer
import fr.skyle.scanny.fake.FakeImageSaver
import fr.skyle.scanny.fake.FakeImageSharer
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class CodeImageExporterTest {

    private val codeImageRenderer = FakeCodeImageRenderer()
    private val imageSharer = FakeImageSharer()
    private val imageSaver = FakeImageSaver()

    @Test
    fun `sharing draws the code in its style and shares its image`() = runTest {
        // When
        codeImageExporter().share(
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            style = STYLE,
            fileName = "scanny-1.png",
        )

        // Then
        assertEquals(
            expected = listOf(
                FakeCodeImageRenderer.RenderedCode(
                    rawValue = "https://scanny.app",
                    format = BarcodeFormat.QR_CODE,
                    style = STYLE,
                ),
            ) to listOf(listOf<Byte>(1, 2, 3) to "scanny-1.png"),
            actual = codeImageRenderer.renderedCodes to imageSharer.sharedImages,
        )
    }

    @Test
    fun `exporting draws the code in its style and shares its svg image`() = runTest {
        // When
        CodeImageExporter(
            codeImageRenderer = FakeCodeImageRenderer(svg = "<svg>code</svg>"),
            imageSharer = imageSharer,
            imageSaver = imageSaver,
        ).shareSvg(
            rawValue = "https://scanny.app",
            format = BarcodeFormat.QR_CODE,
            style = STYLE,
            fileName = "scanny-1.svg",
        )

        // Then
        assertEquals(
            expected = listOf("<svg>code</svg>" to "scanny-1.svg"),
            actual = imageSharer.sharedSvgs,
        )
    }

    @Test
    fun `saving draws the code and keeps its image in the photos`() = runTest {
        // When
        codeImageExporter().save(
            rawValue = "4006381333931",
            format = BarcodeFormat.EAN_13,
            style = CodeStyle(),
            fileName = "scanny-scan-2.png",
        )

        // Then
        assertEquals(
            expected = listOf(listOf<Byte>(1, 2, 3) to "scanny-scan-2.png"),
            actual = imageSaver.savedImages,
        )
    }

    @Test
    fun `a code that cannot be drawn is neither shared nor saved`() = runTest {
        // Given
        val codeImageExporter = CodeImageExporter(
            codeImageRenderer = FakeCodeImageRenderer(error = IllegalArgumentException("Code length overflow")),
            imageSharer = imageSharer,
            imageSaver = imageSaver,
        )

        // When
        assertFailsWith<IllegalArgumentException> {
            codeImageExporter.share(
                rawValue = "Too long",
                format = BarcodeFormat.QR_CODE,
                style = CodeStyle(),
                fileName = "scanny-1.png",
            )
        }
        assertFailsWith<IllegalArgumentException> {
            codeImageExporter.save(
                rawValue = "Too long",
                format = BarcodeFormat.QR_CODE,
                style = CodeStyle(),
                fileName = "scanny-1.png",
            )
        }

        // Then
        assertEquals(
            expected = emptyList<Pair<List<Byte>, String>>() to emptyList<Pair<List<Byte>, String>>(),
            actual = imageSharer.sharedImages to imageSaver.savedImages,
        )
    }

    @Test
    fun `saving is offered as the photos allow it`() {
        // When
        val canSave = listOf(true, false).map { canSave ->
            CodeImageExporter(
                codeImageRenderer = codeImageRenderer,
                imageSharer = imageSharer,
                imageSaver = FakeImageSaver(canSave = canSave),
            ).canSave
        }

        // Then
        assertEquals(
            expected = listOf(true, false),
            actual = canSave,
        )
    }

    private fun codeImageExporter(): CodeImageExporter =
        CodeImageExporter(
            codeImageRenderer = codeImageRenderer,
            imageSharer = imageSharer,
            imageSaver = imageSaver,
        )

    private companion object {
        val STYLE = CodeStyle(
            darkColor = 0xFF3B43BE,
            dotShape = CodeShape.CIRCLE,
        )
    }
}
