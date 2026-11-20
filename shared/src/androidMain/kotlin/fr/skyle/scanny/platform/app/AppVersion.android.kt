package fr.skyle.scanny.platform.app

import android.content.Context
import androidx.core.content.pm.PackageInfoCompat

internal class AndroidAppVersion(
    context: Context,
) : AppVersion {

    private val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)

    override val name: String =
        packageInfo.versionName.orEmpty()

    override val build: String =
        PackageInfoCompat.getLongVersionCode(packageInfo).toString()
}