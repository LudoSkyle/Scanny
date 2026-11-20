package fr.skyle.scanny.fake

import fr.skyle.scanny.platform.app.AppVersion

internal class FakeAppVersion(
    override val name: String = "2.0",
    override val build: String = "20",
) : AppVersion