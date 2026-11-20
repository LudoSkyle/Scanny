package fr.skyle.scanny.fake

import fr.skyle.scanny.platform.share.TextSharer

internal class FakeTextSharer : TextSharer {

    val sharedTexts = mutableListOf<String>()

    override fun shareText(text: String) {
        sharedTexts += text
    }
}