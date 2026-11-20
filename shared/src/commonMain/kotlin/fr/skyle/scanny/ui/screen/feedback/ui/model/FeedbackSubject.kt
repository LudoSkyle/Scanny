package fr.skyle.scanny.ui.screen.feedback.ui.model

/** What a feedback is about, and the tag that sorts it in the mailbox of Scanny. */
enum class FeedbackSubject(val tag: String) {
    SCAN(tag = "[SCAN]"),
    CODE_DETECTION(tag = "[CODE_DETECTION]"),
    OTHER(tag = "[OTHER]"),
}