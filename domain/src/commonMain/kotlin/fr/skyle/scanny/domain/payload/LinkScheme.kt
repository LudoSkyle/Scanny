package fr.skyle.scanny.domain.payload

// A scheme of RFC 3986 followed by an authority: "https://", "whatsapp://", "market://"
private val SCHEME_WITH_AUTHORITY = Regex("""[A-Za-z][A-Za-z0-9+.\-]*://.*""")

/**
 * Whether this text starts with a scheme and its "//", as a link of the web or the deep link of an app
 * does. A scheme without "//" ("spotify:track:…") is left out: "Ref:123" would be one too.
 */
fun String.startsWithLinkScheme(): Boolean =
    matches(SCHEME_WITH_AUTHORITY)
