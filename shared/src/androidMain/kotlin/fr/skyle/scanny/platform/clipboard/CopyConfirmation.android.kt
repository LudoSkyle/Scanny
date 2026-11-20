package fr.skyle.scanny.platform.clipboard

import android.os.Build

// From Android 13, the system shows what was copied at the bottom of the screen
internal actual val isCopyConfirmedBySystem: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU