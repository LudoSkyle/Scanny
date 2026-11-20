package fr.skyle.scanny.platform.link

// Messages on iOS separates the number from the message with "&", where RFC 5724 writes "?"
internal actual fun platformSmsLink(smsLink: String): String =
    smsLink.replaceFirst(
        oldValue = "?body=",
        newValue = "&body=",
    )