package fr.skyle.scanny.ui.core.code.model

/** A piece of the content of a code: a network, a password, a recipient… */
data class CodeField(
    // Null for the content itself, a text or a link, which needs no name
    val label: CodeFieldLabel?,
    val value: String,
)

enum class CodeFieldLabel {
    NETWORK,
    SECURITY,
    PASSWORD,
    RECIPIENT,
    SUBJECT,
    MESSAGE,
    PHONE_NUMBER,
    NAME,
    ORGANIZATION,
    JOB_TITLE,
    PHONE,
    EMAIL,
    ADDRESS,
    WEBSITE,
}