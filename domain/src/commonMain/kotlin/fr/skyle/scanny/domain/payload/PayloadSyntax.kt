package fr.skyle.scanny.domain.payload

internal object PayloadSyntax {
    const val WIFI = "WIFI:"
    const val MATMSG = "MATMSG:"
    const val MAILTO = "mailto:"
    const val SMSTO = "SMSTO:"
    const val SMS = "sms:"
    const val MECARD = "MECARD:"
    const val VCARD = "BEGIN:VCARD"
    const val TEL = "tel:"
    const val GEO = "geo:"
    const val MEBKM = "MEBKM:"
    const val OTPAUTH = "otpauth://"
    const val BITCOIN = "bitcoin:"
    const val HTTP = "http://"
    const val HTTPS = "https://"

    // Escaped with a backslash in the fields of a WIFI or MECARD payload
    const val FIELD_SPECIAL_CHARACTERS = "\\;,:\""
}