package fr.skyle.scanny.data.repository.scan

import fr.skyle.scanny.data.db.dao.ScanDao
import fr.skyle.scanny.data.db.entity.ScanEntity
import fr.skyle.scanny.data.mapper.toStoredScan
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
        rawValue: String,
        format: String,
        scannedAt: Instant,
    ): Long =
        scanDao.insert(
            entity = ScanEntity(
                rawValue = rawValue,
                format = format,
                scannedAt = scannedAt.toEpochMilliseconds(),
            ),
        )

    override fun watchScans(): Flow<List<StoredScan>> =
        scanDao.watchAll()
            .map { entities ->
                entities.map { entity ->
                    entity.toStoredScan()
                }
            }
            .flowOn(Dispatchers.Default)

    override fun watchScan(id: Long): Flow<StoredScan?> =
        scanDao.watchById(id = id)
            .map { entity ->
                entity?.toStoredScan()
            }
            .distinctUntilChanged()
            .flowOn(Dispatchers.Default)

    override suspend fun deleteScan(id: Long) {
        scanDao.deleteById(id = id)
    }
}