package fr.skyle.scanny.domain.fake

import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlin.time.Instant

internal class FakeScanRepository(
    storedScans: List<StoredScan> = emptyList(),
) : ScanRepository {

    private val scans = MutableStateFlow(storedScans)

    val storedScans: List<StoredScan>
        get() = scans.value

    override suspend fun saveScan(
        rawValue: String,
        format: String,
        scannedAt: Instant,
    ): Long {
        val id = (scans.value.maxOfOrNull { it.id } ?: 0) + 1
        scans.update { storedScans ->
            storedScans + StoredScan(
                id = id,
                rawValue = rawValue,
                format = format,
                scannedAt = scannedAt,
            )
        }
        return id
    }

    override fun watchScans(): Flow<List<StoredScan>> =
        scans

    override fun watchScan(id: Long): Flow<StoredScan?> =
        scans.map { storedScans ->
            storedScans.firstOrNull { it.id == id }
        }

    override suspend fun deleteScan(id: Long) {
        scans.update { storedScans ->
            storedScans.filterNot { it.id == id }
        }
    }
}