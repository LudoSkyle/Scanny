package fr.skyle.scanny.platform.share

import android.content.Context
import android.content.Intent

internal class AndroidTextSharer(
    private val context: Context,
) : TextSharer {

    override fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        context.startActivity(
            Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}