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

    override suspend fun saveScan(
        codes: List<CodeToSave>,
        scannedAt: Instant,
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
                    )
                },
            )
        }
        return scanId
    }

    override fun watchScans(): Flow<List<StoredScan>> =
        scans

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

    override suspend fun deleteScan(id: Long) {
        scans.update { storedScans ->
            storedScans.filterNot { it.id == id }
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