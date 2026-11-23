package fr.skyle.scanny.ui.screen.feedback.ui.mapper

import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.payload.toMailtoLink
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.feedback_about_other
import fr.skyle.scanny.resources.feedback_about_scan
import fr.skyle.scanny.resources.feedback_about_type_detection
import fr.skyle.scanny.ui.core.link.ScannyLinks
import fr.skyle.scanny.ui.screen.feedback.ui.model.FeedbackSubject
import org.jetbrains.compose.resources.StringResource

internal fun FeedbackSubject.toText(): StringResource =
    when (this) {
        FeedbackSubject.SCAN -> Res.string.feedback_about_scan
        FeedbackSubject.CODE_DETECTION -> Res.string.feedback_about_type_detection
        FeedbackSubject.OTHER -> Res.string.feedback_about_other
    }

/** The e-mail of a feedback, to Scanny, its tag as its subject: the mail app of the device sends it. */
internal fun FeedbackSubject.toFeedbackMailtoLink(message: String): String =
    BarcodeContent.Email(
        to = listOf(ScannyLinks.CONTACT_EMAIL),
        subject = tag,
        body = message,
    ).toMailtoLink()