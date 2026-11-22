package fr.skyle.scanny.data.repository.creation

import fr.skyle.scanny.data.db.dao.CreationDao
import fr.skyle.scanny.data.db.entity.CreationEntity
import fr.skyle.scanny.data.mapper.toStoredCreation
import fr.skyle.scanny.data.model.StoredCodeStyle
import fr.skyle.scanny.data.model.StoredCreation
import fr.skyle.scanny.data.repository.creation.api.CreationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.time.Instant

internal class CreationRepositoryImpl(
    private val creationDao: CreationDao,
) : CreationRepository {

    override suspend fun saveCreation(
        rawValue: String,
        format: String,
        style: StoredCodeStyle,
        createdAt: Instant,
        isInHistory: Boolean,
    ): Long =
        creationDao.insert(
            entity = CreationEntity(
                createdAt = createdAt.toEpochMilliseconds(),
                rawValue = rawValue,
                format = format,
                darkColor = style.darkColor,
                lightColor = style.lightColor,
                cornerColor = style.cornerColor,
                gradient = style.gradient,
                gradientColor = style.gradientColor,
                dotShape = style.dotShape,
                cornerShape = style.cornerShape,
                logo = style.logo,
                logoShape = style.logoShape,
                logoMargin = style.logoMargin,
                errorCorrection = style.errorCorrection,
                isInHistory = isInHistory,
            ),
        )

    override fun watchCreations(): Flow<List<StoredCreation>> =
        creationDao.watchHistory()
            .map { creations ->
                creations.map { creation ->
                    creation.toStoredCreation()
                }
            }
            .flowOn(Dispatchers.Default)

    override fun watchCreation(id: Long): Flow<StoredCreation?> =
        creationDao.watchById(id = id)
            .map { creation ->
                creation?.toStoredCreation()
            }
            .distinctUntilChanged()

    override suspend fun deleteCreation(id: Long) {
        creationDao.deleteById(id = id)
    }

    override suspend fun deleteAllCreations() {
        creationDao.deleteAll()
    }

    override suspend fun purgeCreations(createdBefore: Instant?) {
        creationDao.deleteOutOfHistory()
        createdBefore?.let { before ->
            creationDao.deleteCreatedBefore(before = before.toEpochMilliseconds())
        }
    }
}