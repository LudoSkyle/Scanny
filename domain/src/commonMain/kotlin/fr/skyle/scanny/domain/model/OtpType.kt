package fr.skyle.scanny.domain.model

/** How a one-time password changes: with the time, or with a counter at each use. */
enum class OtpType {
    TOTP,
    HOTP,
}