package fr.skyle.scanny.domain.di

import fr.skyle.scanny.data.di.dataModule
import fr.skyle.scanny.domain.usecase.creation.DeleteAllCreationsUseCase
import fr.skyle.scanny.domain.usecase.creation.DeleteAllCreationsUseCaseImpl
import fr.skyle.scanny.domain.usecase.creation.DeleteCreationUseCase
import fr.skyle.scanny.domain.usecase.creation.DeleteCreationUseCaseImpl
import fr.skyle.scanny.domain.usecase.creation.RecordCreationUseCase
import fr.skyle.scanny.domain.usecase.creation.RecordCreationUseCaseImpl
import fr.skyle.scanny.domain.usecase.creation.SetCreationFavoriteUseCase
import fr.skyle.scanny.domain.usecase.creation.SetCreationFavoriteUseCaseImpl
import fr.skyle.scanny.domain.usecase.creation.WatchCreationUseCase
import fr.skyle.scanny.domain.usecase.creation.WatchCreationUseCaseImpl
import fr.skyle.scanny.domain.usecase.creation.WatchCreationsUseCase
import fr.skyle.scanny.domain.usecase.creation.WatchCreationsUseCaseImpl
import fr.skyle.scanny.domain.usecase.history.PurgeHistoryUseCase
import fr.skyle.scanny.domain.usecase.history.PurgeHistoryUseCaseImpl
import fr.skyle.scanny.domain.usecase.history.SetHistoryRetentionUseCase
import fr.skyle.scanny.domain.usecase.history.SetHistoryRetentionUseCaseImpl
import fr.skyle.scanny.domain.usecase.history.SetHistorySettingUseCase
import fr.skyle.scanny.domain.usecase.history.SetHistorySettingUseCaseImpl
import fr.skyle.scanny.domain.usecase.history.WatchHistorySettingsUseCase
import fr.skyle.scanny.domain.usecase.history.WatchHistorySettingsUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.DeleteAllScansUseCase
import fr.skyle.scanny.domain.usecase.scan.DeleteAllScansUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.DeleteScanUseCase
import fr.skyle.scanny.domain.usecase.scan.DeleteScanUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.DeleteScannedCodeUseCase
import fr.skyle.scanny.domain.usecase.scan.DeleteScannedCodeUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.RecordScanUseCase
import fr.skyle.scanny.domain.usecase.scan.RecordScanUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.SetScanFavoriteUseCase
import fr.skyle.scanny.domain.usecase.scan.SetScanFavoriteUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.WatchScanUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScanUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.WatchScannedCodeUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScannedCodeUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.WatchScansUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScansUseCaseImpl
import fr.skyle.scanny.domain.usecase.settings.SetScanSettingUseCase
import fr.skyle.scanny.domain.usecase.settings.SetScanSettingUseCaseImpl
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCase
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCaseImpl
import kotlin.time.Clock
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val domainModule = module {
    includes(dataModule)

    single<Clock> { Clock.System }

    factoryOf(::RecordScanUseCaseImpl) bind RecordScanUseCase::class
    factoryOf(::WatchScansUseCaseImpl) bind WatchScansUseCase::class
    factoryOf(::WatchScanUseCaseImpl) bind WatchScanUseCase::class
    factoryOf(::WatchScannedCodeUseCaseImpl) bind WatchScannedCodeUseCase::class
    factoryOf(::DeleteScanUseCaseImpl) bind DeleteScanUseCase::class
    factoryOf(::DeleteAllScansUseCaseImpl) bind DeleteAllScansUseCase::class
    factoryOf(::DeleteScannedCodeUseCaseImpl) bind DeleteScannedCodeUseCase::class
    factoryOf(::SetScanFavoriteUseCaseImpl) bind SetScanFavoriteUseCase::class
    factoryOf(::RecordCreationUseCaseImpl) bind RecordCreationUseCase::class
    factoryOf(::WatchCreationsUseCaseImpl) bind WatchCreationsUseCase::class
    factoryOf(::WatchCreationUseCaseImpl) bind WatchCreationUseCase::class
    factoryOf(::DeleteCreationUseCaseImpl) bind DeleteCreationUseCase::class
    factoryOf(::DeleteAllCreationsUseCaseImpl) bind DeleteAllCreationsUseCase::class
    factoryOf(::SetCreationFavoriteUseCaseImpl) bind SetCreationFavoriteUseCase::class
    factoryOf(::WatchScanSettingsUseCaseImpl) bind WatchScanSettingsUseCase::class
    factoryOf(::SetScanSettingUseCaseImpl) bind SetScanSettingUseCase::class
    factoryOf(::WatchHistorySettingsUseCaseImpl) bind WatchHistorySettingsUseCase::class
    factoryOf(::SetHistorySettingUseCaseImpl) bind SetHistorySettingUseCase::class
    factoryOf(::SetHistoryRetentionUseCaseImpl) bind SetHistoryRetentionUseCase::class
    factoryOf(::PurgeHistoryUseCaseImpl) bind PurgeHistoryUseCase::class
}