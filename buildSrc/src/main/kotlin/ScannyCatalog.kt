import org.gradle.api.Project
import org.gradle.api.artifacts.MinimalExternalModuleDependency
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.api.provider.Provider
import org.gradle.kotlin.dsl.getByType

// The type-safe accessors of the version catalog are not generated for the convention plugins
internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

internal fun Project.catalogVersion(alias: String): String =
    libs.findVersion(alias).get().requiredVersion

internal fun Project.catalogLibrary(alias: String): Provider<MinimalExternalModuleDependency> =
    libs.findLibrary(alias).get()