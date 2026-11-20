package fr.skyle.scanny.fake

import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Instant

/** A clock that only moves when the test moves it. */
internal class TestClock(
    private var instant: Instant = Instant.fromEpochMilliseconds(0),
) : Clock {

    override fun now(): Instant =
        instant

    fun advanceBy(duration: Duration) {
        instant += duration
    }
}