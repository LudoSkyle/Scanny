package fr.skyle.scanny.domain.fake

import fr.skyle.scanny.data.model.CodeToSave
import fr.skyle.scanny.data.model.StoredCode
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

    // The ids of the captures recorded out of the history
    val outOfHistoryIds = mutableSetOf<Long>()

    // The time before which each purge deleted the captures, null for none
    val purges = mutableListOf<Instant?>()

    override suspend fun saveScan(
        codes: List<CodeToSave>,
        scannedAt: Instant,
        isInHistory: Boolean,
    ): Long {
        val scanId = (scans.value.maxOfOrNull { it.id } ?: 0) + 1
        val firstCodeId = (scans.value.flatMap { it.codes }.maxOfOrNull { it.id } ?: 0) + 1
        scans.update { storedScans ->
            storedScans + StoredScan(
                id = scanId,
                scannedAt = scannedAt,
                codes = codes.mapIndexed { index, code ->
                    StoredCode(
                        id = firstCodeId + index,
                        scanId = scanId,
                        rawValue = code.rawValue,
                        format = code.format,
                        scannedAt = scannedAt,
                        isScanFavorite = false,
                        scanCodeCount = codes.size,
                    )
                },
                isFavorite = false,
            )
        }
        if (!isInHistory) {
            outOfHistoryIds += scanId
        }
        return scanId
    }

    override fun watchScans(): Flow<List<StoredScan>> =
        scans.map { storedScans ->
            storedScans.filterNot { it.id in outOfHistoryIds }
        }

    override suspend fun findLatestScan(): StoredScan? =
        scans.value
            .filterNot { it.id in outOfHistoryIds }
            .maxWithOrNull(compareBy<StoredScan> { it.scannedAt }.thenBy { it.id })

    override suspend fun updateScanTime(
        id: Long,
        scannedAt: Instant,
    ) {
        scans.update { storedScans ->
            storedScans.map { storedScan ->
                if (storedScan.id == id) {
                    storedScan.copy(
                        scannedAt = scannedAt,
                        codes = storedScan.codes.map { it.copy(scannedAt = scannedAt) },
                    )
                } else {
                    storedScan
                }
            }
        }
    }

    override fun watchScan(id: Long): Flow<StoredScan?> =
        scans.map { storedScans ->
            storedScans.firstOrNull { it.id == id }
        }

    override fun watchCode(id: Long): Flow<StoredCode?> =
        scans.map { storedScans ->
            storedScans
                .flatMap { it.codes }
                .firstOrNull { it.id == id }
        }

    override suspend fun setScanFavorite(
        id: Long,
        isFavorite: Boolean,
    ) {
        scans.update { storedScans ->
            storedScans.map { scan ->
                if (scan.id == id) {
                    scan.copy(
                        isFavorite = isFavorite,
                        codes = scan.codes.map { code -> code.copy(isScanFavorite = isFavorite) },
                    )
                } else {
                    scan
                }
            }
        }
    }

    override suspend fun deleteScan(id: Long) {
        scans.update { storedScans ->
            storedScans.filterNot { it.id == id }
        }
    }

    override suspend fun deleteAllScans() {
        scans.value = emptyList()
    }

    override suspend fun purgeScans(scannedBefore: Instant?) {
        purges += scannedBefore
        scans.update { storedScans ->
            storedScans.filterNot { storedScan ->
                storedScan.id in outOfHistoryIds || (scannedBefore != null && storedScan.scannedAt < scannedBefore)
            }
        }
    }

    override suspend fun deleteCode(id: Long) {
        scans.update { storedScans ->
            storedScans
                .map { storedScan ->
                    storedScan.copy(codes = storedScan.codes.filterNot { it.id == id })
                }
                .filter { it.codes.isNotEmpty() }
        }
    }
}