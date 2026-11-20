package fr.skyle.scanny.platform.link

/** The sms: link written by the domain, as the messages app of the platform reads it. */
internal expect fun platformSmsLink(smsLink: String): String