package fr.skyle.scanny.data.repository.scan

import fr.skyle.scanny.data.db.dao.ScanDao
import fr.skyle.scanny.data.db.entity.ScanEntity
import fr.skyle.scanny.data.db.entity.ScannedCodeEntity
import fr.skyle.scanny.data.mapper.toStoredCode
import fr.skyle.scanny.data.mapper.toStoredScan
import fr.skyle.scanny.data.model.CodeToSave
import fr.skyle.scanny.data.model.StoredCode
import fr.skyle.scanny.data.model.StoredScan
import fr.skyle.scanny.data.repository.scan.api.ScanRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

internal class ScanRepositoryImpl(
    private val scanDao: ScanDao,
) : ScanRepository {

    override suspend fun saveScan(
        codes: List<CodeToSave>,
        scannedAt: Instant,
    ): Long {
        require(codes.isNotEmpty()) { "A scan holds at least one code" }

        return scanDao.insertWithCodes(
            scan = ScanEntity(scannedAt = scannedAt.toEpochMilliseconds()),
            codes = codes.mapIndexed { index, code ->
                ScannedCodeEntity(
                    // Set by the DAO, once the scan is inserted
                    scanId = 0,
                    position = index,
                    rawValue = code.rawValue,
                    format = code.format,
                )
            },
        )
    }

    override fun watchScans(): Flow<List<StoredScan>> =
        scanDao.watchAll()
            .map { scans ->
                scans.map { scan ->
                    scan.toStoredScan()
                }
            }
            .flowOn(Dispatchers.Default)

    override fun watchScan(id: Long): Flow<StoredScan?> =
        scanDao.watchById(id = id)
            .map { scan ->
                scan?.toStoredScan()
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    override fun watchCode(id: Long): Flow<StoredCode?> =
        scanDao.watchCodeById(id = id)
            .map { code ->
                code?.toStoredCode()
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    override suspend fun deleteScan(id: Long) {
        scanDao.deleteById(id = id)
    }

    override suspend fun deleteCode(id: Long) {
        scanDao.deleteCodeAndEmptyScan(id = id)
    }
}