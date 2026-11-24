package fr.skyle.scanny.ui.core.code.component

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButton
import fr.skyle.scanny.designsystem.core.iconbutton.SCIconButtonDefaults
import fr.skyle.scanny.designsystem.theme.SCTheme
import fr.skyle.scanny.resources.Res
import fr.skyle.scanny.resources.add_to_favorites
import fr.skyle.scanny.resources.ic_star
import fr.skyle.scanny.resources.ic_star_filled
import fr.skyle.scanny.resources.remove_from_favorites
import fr.skyle.scanny.ui.core.preview.AppPreviewWithBackground
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

/** The star that puts a code among the favorites, filled once it is one, and takes it out. */
@Composable
fun FavoriteButton(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: SCIconButtonDefaults.Style = SCIconButtonDefaults.Style.DARK,
) {
    SCIconButton(
        modifier = modifier,
        icon = painterResource(if (isFavorite) Res.drawable.ic_star_filled else Res.drawable.ic_star),
        contentDescription = stringResource(if (isFavorite) Res.string.remove_from_favorites else Res.string.add_to_favorites),
        onClick = onClick,
        style = style,
        size = SCIconButtonDefaults.Size.MEDIUM,
    )
}

@AppPreviewWithBackground
@Composable
private fun PreviewFavoriteButton() {
    SCTheme {
        FavoriteButton(
            modifier = Modifier.wrapContentSize(),
            isFavorite = true,
            onClick = {},
        )
    }
}
