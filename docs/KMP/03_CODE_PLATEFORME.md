## Code propre à chaque plateforme

Toute l'UI est commune (voir [ADR-0002](../adr/0002-ui-commune-en-compose-multiplatform.md)). Ce
document dit où et comment écrire ce qui ne peut pas l'être : la caméra et la détection, le choix
d'une image, les permissions, les actions système.

### Avant d'écrire du code de plateforme

Chercher d'abord l'API commune :

- l'heure courante passe par `kotlin.time.Clock`, les dates et leurs formats par kotlinx-datetime,
  jamais par `java.util` ou `NSDate` ;
- les fichiers passent par kotlinx-io ;
- ouvrir une URL (`https:`, `mailto:`, `sms:`, `tel:`) passe par `LocalUriHandler`, faire vibrer par
  `LocalHapticFeedback`, copier par `LocalClipboard` : ce sont des API de Compose, communes. Pour le
  presse-papiers, la création du `ClipEntry` texte passe par une fonction `expect` si l'API commune
  ne la fournit pas.

### Trois façons d'écrire du natif

| Le besoin… | Se déclare… | Utilisé par |
|---|---|---|
| passe par une API de Compose (lien, vibration, presse-papiers) | rien à déclarer : `LocalUriHandler`, `LocalHapticFeedback`, `LocalClipboard` | la `Route`, en réaction à un clic ou à un event du ViewModel |
| est une action système sans lien avec la composition (partager, ajouter un contact, rejoindre un WiFi, ouvrir les réglages de l'app, noter l'app) | une **interface** en `commonMain`, une implémentation par plateforme, fournies par le module Koin de plateforme | le ViewModel, qui la reçoit par constructeur (et un fake dans ses tests) |
| a besoin de la composition (aperçu caméra, demande de permission, sélecteur d'image) | une fonction `@Composable` `expect`, avec un `actual` par plateforme | l'écran, via ses composants |

- Un `expect` porte sur une fonction ou une propriété, jamais sur une classe : les classes
  `expect`/`actual` sont encore en bêta. Un état natif à exposer se décrit par une interface
  commune, rendue par une fonction `expect` (`rememberCameraPermissionState(): CameraPermissionState`).
- Le code de plateforme vit dans `shared`, sous `platform/<capacité>/` (`platform/camera/`,
  `platform/share/`…), dans le source set de sa plateforme. Le fichier d'un `actual` est suffixé par
  sa plateforme : `ScanCamera.android.kt`, `ScanCamera.ios.kt`.
- Le module Koin de plateforme (`internal expect val platformSharedModule: Module`) déclare les
  implémentations natives ; `sharedModule` l'inclut.

Exemple d'action système :

`commonMain/.../platform/share/TextSharer.kt`

```kotlin
interface TextSharer {

    fun shareText(text: String)
}
```

`androidMain/.../platform/share/TextSharer.android.kt`

```kotlin
internal class AndroidTextSharer(
    private val context: Context,
) : TextSharer {

    override fun shareText(text: String) {
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        context.startActivity(
            Intent.createChooser(sendIntent, null).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
        )
    }
}
```

`iosMain/.../platform/share/TextSharer.ios.kt`

```kotlin
internal class IosTextSharer : TextSharer {

    override fun shareText(text: String) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(text),
            applicationActivities = null,
        )

        topViewController()?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null,
        )
    }
}
```

### Règles par plateforme

Android :

- Une action système part du `Context` de l'application, avec `FLAG_ACTIVITY_NEW_TASK` : une
  implémentation ne garde jamais de référence à une `Activity`.
- Ce qui attend un résultat d'activité (permission, sélecteur d'image) passe par
  `rememberLauncherForActivityResult`, donc par un `expect` composable.

iOS :

- Le contrôleur depuis lequel présenter une feuille (partage, contact, événement) se cherche **au
  moment de l'action** (`topViewController()`), jamais pendant la composition : la composition ne
  dépend pas de son hôte.
- Les vues natives (aperçu caméra `AVCaptureVideoPreviewLayer`) s'intègrent par `UIKitView`.
- Les appels UIKit se font sur le thread principal ; le démarrage et l'arrêt d'une
  `AVCaptureSession` se font hors du thread principal.
- Un `@OptIn(ExperimentalForeignApi::class)` se place au plus près de l'appel qui l'exige, pas sur
  un fichier entier.
- Toute API qui demande une autorisation déclare sa clé d'usage dans l'`Info.plist` de `iosApp`
  (`NSCameraUsageDescription` pour la caméra, `NSPhotoLibraryAddUsageDescription` pour enregistrer un
  QR code dans Photos…), traduite en anglais et en français.

### Caméra et détection

- `ScanCamera` (`expect` composable) affiche l'aperçu et remonte chaque code détecté sous une forme
  commune : son texte brut et son `BarcodeFormat`. Jamais un type de ML Kit ou d'AVFoundation au-delà
  de l'`actual`.
- Il reçoit de l'écran l'état à appliquer (détection active ou en pause, torche, niveau de zoom) et
  remonte ce que seul l'appareil connaît (plage de zoom disponible, présence d'une torche).
- La détection est en pause tant qu'un résultat est affiché : un même code n'est pas traité deux
  fois.
- Le geste de zoom (pincement) et le geste de mise au point (appui) sont détectés en commun ; seule
  leur application à la caméra est native.

### Matrice des capacités

Mécanisme visé pour chaque capacité. Une ligne est confirmée (✅) quand son implémentation est
livrée ; le tableau est mis à jour dans le même commit.

| Capacité | Android | iOS | Forme |
|---|---|---|---|
| Aperçu et détection en direct | CameraX + ML Kit Barcode Scanning | `AVCaptureSession` + `AVCaptureMetadataOutput` | `expect` composable |
| Zoom, mise au point | `CameraControl` (`setZoomRatio`, `startFocusAndMetering`) | `AVCaptureDevice` (`videoZoomFactor`, `focusPointOfInterest`) | `expect` composable |
| Torche | `CameraControl.enableTorch` | `AVCaptureDevice.torchMode` | `expect` composable |
| Permission caméra | `RequestPermission` | `AVCaptureDevice.requestAccess` | `expect` composable |
| Détection dans une image | Photo Picker + ML Kit (`InputImage`) | `PHPickerViewController` + Vision (`VNDetectBarcodesRequest`) | `expect` composable |
| Ouvrir un lien, un mail, un SMS | `LocalUriHandler` | `LocalUriHandler` | Compose |
| Vibration | `LocalHapticFeedback` | `LocalHapticFeedback` | Compose |
| Copier | `LocalClipboard` | `LocalClipboard` | Compose (+ `expect` pour créer le `ClipEntry` si besoin) |
| Partager un texte | `ACTION_SEND` | `UIActivityViewController` | interface |
| Partager, enregistrer un QR code | `FileProvider` + `ACTION_SEND`, `MediaStore` | `UIActivityViewController`, Photos | interface |
| Ajouter un contact | `ACTION_INSERT_OR_EDIT` | `CNContactViewController` | interface |
| Rejoindre un réseau WiFi | `Settings.ACTION_WIFI_ADD_NETWORKS` | Reporté (`NEHotspotConfigurationManager` exige l'entitlement *Hotspot Configuration*, donc un compte payant) : action non proposée | interface |
| Ouvrir les réglages de l'app | `ACTION_APPLICATION_DETAILS_SETTINGS` | `UIApplicationOpenSettingsURLString` | interface |
| Noter l'app | Lien Play Store | Lien App Store | interface |
| Version de l'app | `PackageManager` (`versionName`, `longVersionCode`) | `CFBundleShortVersionString`, `CFBundleVersion` | interface |