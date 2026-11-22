package fr.skyle.scanny.ui.screen.createcode.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructure
import fr.skyle.scanny.designsystem.core.screenstructure.SCScreenStructureDefaults
import fr.skyle.scanny.designsystem.core.snackbar.SCSnackbarState
import fr.skyle.scanny.designsystem.core.snackbar.rememberSCSnackbarState
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBar
import fr.skyle.scanny.designsystem.core.topappbar.SCTopAppBarDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.CodeErrorCorrection
import fr.skyle.scanny.domain.model.CodeStyle
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.content
import fr.skyle.scanny.resources.create_code
import fr.skyle.scanny.resources.customization
import fr.skyle.scanny.resources.ic_refresh
import fr.skyle.scanny.resources.reset_customization
import fr.skyle.scanny.resources.step_of
import fr.skyle.scanny.ui.core.preview.AppScreenPreview
import fr.skyle.scanny.ui.screen.createcode.ui.component.CreationContentStep
import fr.skyle.scanny.ui.screen.createcode.ui.component.CreationStyleStep
import fr.skyle.scanny.ui.screen.createcode.ui.mapper.format
import fr.skyle.scanny.ui.screen.createcode.ui.model.CodePreview
import fr.skyle.scanny.ui.screen.createcode.ui.model.ContentSize
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationForm
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationKind
import fr.skyle.scanny.ui.screen.createcode.ui.model.CreationStep
import fr.skyle.scanny.ui.screen.createcode.ui.model.ErrorCorrectionChoice
import fr.skyle.scanny.ui.screen.createcode.ui.model.LogoBlocker
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

// The steps of a QR code; a barcode has the first only
private const val STEP_COUNT = 2

@Composable
fun CreateCodeScreen(
    form: CreationForm,
    preview: CodePreview,
    contentSize: ContentSize?,
    errorCorrectionChoices: List<ErrorCorrectionChoice>,
    logoBlocker: LogoBlocker?,
    style: CodeStyle,
    canResetStyle: Boolean,
    step: CreationStep,
    isCreating: Boolean,
    snackbarState: SCSnackbarState,
    onBackClicked: () -> Unit,
    onFormChanged: (CreationForm) -> Unit,
    onStyleChanged: (CodeStyle) -> Unit,
    onResetStyleClicked: () -> Unit,
    onAddLogoClicked: () -> Unit,
    onNextClicked: () -> Unit,
    onCreateClicked: () -> Unit,
) {
    val isQrCode = form.format == BarcodeFormat.QR_CODE

    SCScreenStructure(
        modifier = Modifier.fillMaxSize(),
        style = SCScreenStructureDefaults.Style.BACKGROUND,
        topBar = {
            SCTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                style = SCTopAppBarDefaults.Style.BACKGROUND,
                title = stringResource(
                    when {
                        step == CreationStep.STYLE -> Res.string.customization
                        isQrCode -> Res.string.content
                        else -> Res.string.create_code
                    },
                ),
                subtitle = if (isQrCode) stringResource(Res.string.step_of, step.ordinal + 1, STEP_COUNT) else null,
                onBackClicked = onBackClicked,
                actions = {
                    if (step == CreationStep.STYLE) {
                        SCIconButton(
                            modifier = Modifier.wrapContentSize(),
                            icon = painterResource(Res.drawable.ic_refresh),
                            contentDescription = stringResource(Res.string.reset_customization),
                            onClick = onResetStyleClicked,
                            style = SCIconButtonDefaults.Style.DARK,
                            size = SCIconButtonDefaults.Size.MEDIUM,
                            isEnabled = canResetStyle,
                        )
                    }
                },
            )
        },
        snackbarState = snackbarState,
    ) { contentPadding ->
        // Forward to the customization, backward to the content, as a screen opened then closed
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = step,
            transitionSpec = {
                val direction = if (targetState.ordinal > initialState.ordinal) 1 else -1
                slideInHorizontally(animationSpec = tween()) { width -> direction * width } togetherWith
                    slideOutHorizontally(animationSpec = tween()) { width -> -direction * width }
            },
        ) { shownStep ->
            when (shownStep) {
                CreationStep.CONTENT -> CreationContentStep(
                    modifier = Modifier.fillMaxSize(),
                    form = form,
                    preview = preview,
                    contentSize = contentSize,
                    errorCorrectionChoices = errorCorrectionChoices,
                    style = style,
                    isCreating = isCreating,
                    contentPadding = contentPadding,
                    onFormChanged = onFormChanged,
                    onStyleChanged = onStyleChanged,
                    onNextClicked = onNextClicked,
                    onCreateClicked = onCreateClicked,
                )

                CreationStep.STYLE -> CreationStyleStep(
                    modifier = Modifier.fillMaxSize(),
                    preview = preview,
                    style = style,
                    logoBlocker = logoBlocker,
                    isCreating = isCreating,
                    contentPadding = contentPadding,
                    onStyleChanged = onStyleChanged,
                    onAddLogoClicked = onAddLogoClicked,
                    onCreateClicked = onCreateClicked,
                )
            }
        }
    }
}

@AppScreenPreview
@Composable
private fun PreviewCreateCodeScreen() {
    SCTheme {
        CreateCodeScreen(
            form = CreationForm(
                kind = CreationKind.LINK,
                url = "scanny.app",
            ),
            preview = CodePreview.Code(
                rawValue = "https://scanny.app",
                format = BarcodeFormat.QR_CODE,
            ),
            contentSize = ContentSize(
                used = 18,
                max = 1273,
                hasWideCharacters = false,
            ),
            errorCorrectionChoices = CodeErrorCorrection.entries.map { errorCorrection ->
                ErrorCorrectionChoice(
                    errorCorrection = errorCorrection,
                    blocker = null,
                )
            },
            logoBlocker = null,
            style = CodeStyle(),
            canResetStyle = false,
            step = CreationStep.CONTENT,
            isCreating = false,
            snackbarState = rememberSCSnackbarState(),
            onBackClicked = {},
            onFormChanged = {},
            onStyleChanged = {},
            onResetStyleClicked = {},
            onAddLogoClicked = {},
            onNextClicked = {},
            onCreateClicked = {},
        )
    }
}
