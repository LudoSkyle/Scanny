package fr.skyle.scanny.domain.model

/** The hash a one-time password is computed with: the three the key URI format names. */
enum class OtpAlgorithm {
    SHA1,
    SHA256,
    SHA512,
}