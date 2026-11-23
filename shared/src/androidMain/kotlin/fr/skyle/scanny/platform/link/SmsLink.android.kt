package fr.skyle.scanny.platform.link

// The messages apps of Android read the link of RFC 5724 as is
internal actual fun platformSmsLink(smsLink: String): String =
    smsLink