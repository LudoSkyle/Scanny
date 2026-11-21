package fr.skyle.scanny.domain.usecase.creation

import fr.skyle.scanny.data.model.StoredCreation
import fr.skyle.scanny.domain.fake.FakeCreationRepository
import fr.skyle.scanny.domain.fake.FakeSettingsRepository
import fr.skyle.scanny.domain.fake.FixedClock
import fr.skyle.scanny.domain.model.BarcodeContent
import fr.skyle.scanny.domain.model.BarcodeFormat
import fr.skyle.scanny.domain.model.Creation
import fr.skyle.scanny.domain.model.WifiEncryption
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

class RecordCreationUseCaseTest {

    private val creationRepository = FakeCreationRepository()

    @Test
    fun `a creation is saved with the text of its content its format and the current time`() = runTest {
        // Given
        val recordCreationUseCase = recordCreationUseCase()

        // When
        recordCreationUseCase(
            content = BarcodeContent.Wifi(
                ssid = "Scanny",
                encryption = WifiEncryption.WPA,
                password = "secret12",
            ),
            format = BarcodeFormat.QR_CODE,
        )

        // Then
        assertEquals(
            expected = listOf(
                StoredCreation(
                    id = 1,
                    rawValue = "WIFI:T:WPA;S:Scanny;P:secret12;;",
                    format = "QR_CODE",
                    createdAt = NOW,
                ),
            ),
            actual = creationRepository.storedCreations,
        )
    }

    @Test
    fun `the recorded creation is returned with its id and its content read back`() = runTest {
        // Given
        val recordCreationUseCase = recordCreationUseCase()

        // When
        val creation = recordCreationUseCase(
            content = BarcodeContent.Text(text = "4006381333931"),
            format = BarcodeFormat.EAN_13,
        )

        // Then
        assertEquals(
            expected = Creation(
                id = 1,
                content = BarcodeContent.Text(text = "4006381333931"),
                rawValue = "4006381333931",
                format = BarcodeFormat.EAN_13,
                createdAt = NOW,
            ),
            actual = creation,
        )
    }

    @Test
    fun `a creation is recorded out of the history when the user keeps none`() = runTest {
        // Given
        val recordCreationUseCase = recordCreationUseCase(settingsRepository = FakeSettingsRepository(isCreationHistoryEnabled = false))

        // When
        val creation = recordCreationUseCase(
            content = BarcodeContent.Url(url = "https://scanny.app"),
            format = BarcodeFormat.QR_CODE,
        )

        // Then
        assertEquals(
            expected = setOf(creation.id),
            actual = creationRepository.outOfHistoryIds,
        )
    }

    private fun recordCreationUseCase(settingsRepository: FakeSettingsRepository = FakeSettingsRepository()): RecordCreationUseCaseImpl =
        RecordCreationUseCaseImpl(
            creationRepository = creationRepository,
            settingsRepository = settingsRepository,
            clock = FixedClock(instant = NOW),
        )

    private companion object {
        val NOW = Instant.fromEpochMilliseconds(1_700_000_000_000)
    }
}