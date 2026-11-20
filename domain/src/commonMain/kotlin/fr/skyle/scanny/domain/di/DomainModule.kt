package fr.skyle.scanny.domain.di

import fr.skyle.scanny.data.di.dataModule
import fr.skyle.scanny.domain.usecase.scan.DeleteScanUseCase
import fr.skyle.scanny.domain.usecase.scan.DeleteScanUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.RecordScanUseCase
import fr.skyle.scanny.domain.usecase.scan.RecordScanUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.WatchScanUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScanUseCaseImpl
import fr.skyle.scanny.domain.usecase.scan.WatchScansUseCase
import fr.skyle.scanny.domain.usecase.scan.WatchScansUseCaseImpl
import fr.skyle.scanny.domain.usecase.settings.SetScanSettingUseCase
import fr.skyle.scanny.domain.usecase.settings.SetScanSettingUseCaseImpl
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCase
import fr.skyle.scanny.domain.usecase.settings.WatchScanSettingsUseCaseImpl
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module
import kotlin.time.Clock

val domainModule = module {
    includes(dataModule)

    single<Clock> { Clock.System }

    factoryOf(::RecordScanUseCaseImpl) bind RecordScanUseCase::class
    factoryOf(::WatchScansUseCaseImpl) bind WatchScansUseCase::class
    factoryOf(::WatchScanUseCaseImpl) bind WatchScanUseCase::class
    factoryOf(::DeleteScanUseCaseImpl) bind DeleteScanUseCase::class
    factoryOf(::WatchScanSettingsUseCaseImpl) bind WatchScanSettingsUseCase::class
    factoryOf(::SetScanSettingUseCaseImpl) bind SetScanSettingUseCase::class
}