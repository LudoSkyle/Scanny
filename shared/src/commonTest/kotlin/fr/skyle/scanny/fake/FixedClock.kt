package fr.skyle.scanny.fake

import kotlin.time.Clock
import kotlin.time.Instant

internal class FixedClock(
    private val instant: Instant,
) : Clock {

    override fun now(): Instant =
        instant
}