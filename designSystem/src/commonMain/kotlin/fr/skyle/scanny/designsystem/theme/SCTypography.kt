package fr.skyle.scanny.designsystem.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import fr.skyle.scanny.designsystem.resources.Res
import fr.skyle.scanny.designsystem.resources.lato_black
import fr.skyle.scanny.designsystem.resources.lato_bold
import fr.skyle.scanny.designsystem.resources.lato_light
import fr.skyle.scanny.designsystem.resources.lato_regular
import fr.skyle.scanny.designsystem.resources.lato_thin
import org.jetbrains.compose.resources.Font

@Immutable
data class SCTypography(
    val h1: TextStyle,
    val h2: TextStyle,
    val h3: TextStyle,
    val body1: TextStyle,
    val body2: TextStyle,
    val body3: TextStyle,
    val subtitle1: TextStyle,
    val subtitle2: TextStyle,
    val button: TextStyle,
    val caption: TextStyle,
)

// Composable: a font of Compose Resources is only read during the composition
@Composable
internal fun scannyTypography(): SCTypography {
    val lato = FontFamily(
        Font(
            resource = Res.font.lato_thin,
            weight = FontWeight.Thin,
        ),
        Font(
            resource = Res.font.lato_light,
            weight = FontWeight.Light,
        ),
        Font(
            resource = Res.font.lato_regular,
            weight = FontWeight.Normal,
        ),
        Font(
            resource = Res.font.lato_bold,
            weight = FontWeight.Bold,
        ),
        Font(
            resource = Res.font.lato_black,
            weight = FontWeight.Black,
        ),
    )

    return remember(lato) {
        SCTypography(
            h1 = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
            ),
            h2 = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
            ),
            h3 = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
            ),
            body1 = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            ),
            body2 = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.Normal,
                fontSize = 15.sp,
            ),
            body3 = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
            ),
            subtitle1 = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            ),
            subtitle2 = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.Normal,
                fontSize = 13.sp,
            ),
            button = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
            ),
            caption = TextStyle(
                fontFamily = lato,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp,
            ),
        )
    }
}

internal val LocalTypography = staticCompositionLocalOf {
    SCTypography(
        h1 = TextStyle.Default,
        h2 = TextStyle.Default,
        h3 = TextStyle.Default,
        body1 = TextStyle.Default,
        body2 = TextStyle.Default,
        body3 = TextStyle.Default,
        subtitle1 = TextStyle.Default,
        subtitle2 = TextStyle.Default,
        button = TextStyle.Default,
        caption = TextStyle.Default,
    )
}