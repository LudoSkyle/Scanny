## Module `domain`

Le module `domain` porte ce que Scanny sait faire indépendamment de l'écran et de la plateforme :

- les modèles métier : le contenu typé d'un code (`BarcodeContent`), son format (`BarcodeFormat`),
  un code détecté, un scan (une capture de codes), une création ;
- le **parseur** (texte brut d'un code → `BarcodeContent`) et l'**encodeur** (`BarcodeContent` →
  texte à mettre dans un QR code) ;
- les UseCases, seul point d'entrée de `shared` vers les données.

Il est en Kotlin pur : aucune API de plateforme, aucun `expect`/`actual`, aucune dépendance UI. Il
dépend de `data` en `implementation`, et seul son package `di/` importe Koin (voir
[ADR-0001](../adr/0001-le-domaine-depend-de-la-donnee.md)).

```
domain/src/commonMain/kotlin/fr/skyle/scanny/domain/
├── di/
│   └── DomainModule.kt
├── exception/
├── mapper/                        (modèle de data -> modèle métier : ScanMapper)
├── model/
│   ├── BarcodeContent.kt
│   ├── BarcodeFormat.kt
│   ├── DetectedBarcode.kt         (un code tel que la détection le rend, et sa normalisation)
│   ├── Scan.kt                    (une capture : sa date et ses codes)
│   ├── ScannedCode.kt             (un code d'une capture, son contenu interprété par le parseur)
│   ├── ScanSetting.kt             (un réglage du scan)
│   ├── ScanSettings.kt            (les réglages du scan, ensemble)
│   └── WifiEncryption.kt
├── payload/                       (le texte d'un code, dans les deux sens)
│   ├── BarcodeContentEncoder.kt
│   ├── BarcodeContentParser.kt
│   ├── ContentLinks.kt            (les liens qui ouvrent l'app d'un contenu : mailto:, sms:)
│   └── PayloadSyntax.kt           (préfixes et caractères échappés, communs aux deux)
└── usecase/
    ├── scan/
    │   ├── DeleteAllScansUseCase.kt   (effacer l'historique)
    │   ├── DeleteScannedCodeUseCase.kt
    │   ├── DeleteScanUseCase.kt
    │   ├── RecordScanUseCase.kt
    │   ├── WatchScannedCodeUseCase.kt
    │   ├── WatchScanUseCase.kt
    │   └── WatchScansUseCase.kt
    └── settings/
        ├── SetScanSettingUseCase.kt
        └── WatchScanSettingsUseCase.kt
```

### Contenu d'un code

`BarcodeContent` est une `sealed interface` dont chaque type porte les champs utiles à l'affichage
et aux actions : `Text`, `Url`, `Email` (destinataire, sujet, corps), `Sms` (numéro, message),
`Wifi` (SSID, chiffrement `NONE`/`WEP`/`WPA`/`WPA3`, mot de passe, réseau masqué), `Contact`. D'autres
types (téléphone, position, événement) s'ajoutent avec l'écran qui les affiche. Le texte brut n'en
fait pas partie : il est porté par le `ScannedCode`.

`Contact` porte un nom affiché (`formattedName`) **et** un prénom et un nom séparés : l'ajout d'un
contact sur Android prend un nom affiché, iOS (`CNMutableContact`) veut le prénom et le nom.
`displayName` donne le nom à montrer : le nom affiché, ou à défaut le prénom et le nom. S'y
ajoutent l'organisation, la fonction, et des listes de téléphones, d'emails, d'adresses (une adresse
est une ligne, ses composants séparés par des virgules) et de sites.

Le parseur est **le cœur de la parité entre les plateformes**. ML Kit sait typer un code sur
Android, mais AVFoundation et Vision ne rendent qu'un texte brut sur iOS. Scanny n'utilise donc
jamais les types structurés de ML Kit : les deux plateformes passent le texte brut au même
parseur, et obtiennent le même résultat.

- Formats reconnus : `WIFI:`, `MATMSG:`, `mailto:`, `SMSTO:` / `sms:`, `MECARD:`, `BEGIN:VCARD`,
  et les URL (`http://`, `https://`). Les préfixes et les clés sont insensibles à la casse, les
  valeurs non.
- Les caractères échappés sont décodés (`\;`, `\,`, `\:`, `\\`, `\"` dans un `WIFI:` ou un
  `MECARD:`, et une valeur entre guillemets non échappés perd ses guillemets), les lignes repliées
  d'une vCard sont dépliées (CRLF ou LF), ses groupes (`item1.`) et paramètres (`;TYPE=…`) ignorés,
  et l'encodage `%` d'un `mailto:` ou d'un `sms:` est décodé en UTF-8. Un `+` reste un `+`.
- Un `WIFI:` sans type est un réseau ouvert, sauf s'il donne un mot de passe (alors WPA) ; `SAE` est
  du WPA3.
- Un contenu structuré sans aucun champ utile (un `WIFI:` sans SSID, une vCard vide) est un texte.
  Un lien sans schéma (`www.…`) reste un texte.
- **Le parseur ne lève jamais d'exception** : un contenu qu'il ne reconnaît pas, ou mal formé (un `%`
  isolé, un champ tronqué), devient un `BarcodeContent.Text`. Le code est écrit pour ne pas pouvoir
  échouer, sans `catch` général : ce sont les tests d'entrées malformées qui le garantissent.
- L'encodeur est l'inverse exact du parseur : il échappe ce que le parseur décode, et un
  aller-retour encodage → parsing redonne le même contenu. Les formats qu'il écrit sont ceux que lisent
  les appareils photo d'Android et d'iOS :

| Contenu | Texte écrit |
|---|---|
| Texte, URL | tel quel |
| Email | `mailto:adresse?subject=…&body=…`, encodé en `%` (`MATMSG:` n'est que lu) |
| SMS | `SMSTO:numéro:message` (`sms:` n'est que lu) |
| WiFi | `WIFI:T:…;S:…;P:…;H:true;;`, `T:nopass` et pas de `P:` pour un réseau ouvert, `SAE` pour le WPA3, `H:true` seulement pour un réseau masqué |
| Contact | vCard 3.0 ; seules les propriétés texte (`N`, `FN`, `ORG`, `TITLE`, `ADR`) sont échappées |
- Le parseur et l'encodeur sont `internal` : `shared` les utilise à travers les UseCases. Seuls sont
  publics les liens qui ouvrent l'app d'un contenu (`toMailtoLink()`, `toSmsLink()`, dans
  `ContentLinks.kt`) : `shared` les ouvre tels quels, et leur encodage `%` est celui de l'encodeur.
  Le lien `sms:` suit la RFC 5724 (`sms:numéro?body=…`) ; ce qu'en fait chaque plateforme est dit dans
  [KMP/03_CODE_PLATEFORME.md](./03_CODE_PLATEFORME.md).
- Chaque format a ses tests, dans `commonTest` (voir [Common/03_TESTS.md](../Common/03_TESTS.md)).

`BarcodeFormat` liste les formats reconnus par les deux moteurs de détection (QR Code, Data Matrix,
Aztec, PDF417, EAN-13, EAN-8, UPC-A, UPC-E, Code 39, Code 93, Code 128, ITF, Codabar). La conversion
depuis le format de chaque moteur se fait dans le code de plateforme de `shared`. AVFoundation n'a
pas de type UPC-A : il rend un UPC-A comme un EAN-13 commençant par `0`. `DetectedBarcode.normalized()`
le ramène à un UPC-A, pour que le même code soit enregistré pareil sur les deux plateformes.

### Captures

Un `Scan` est une capture : sa date et la liste de ses `ScannedCode`, dans l'ordre de lecture. Un
scan simple est une capture d'un code, un scan multiple celle de tous les codes trouvés pendant la
session.

- `RecordScanUseCase` reçoit les `DetectedBarcode` d'une capture, au moins un. Il les normalise, puis
  ne garde qu'une fois un code lu plusieurs fois (même texte et même format, après normalisation), à
  sa première lecture, sans changer l'ordre des autres. Le même texte dans deux formats reste deux
  codes.
- Une liste vide est une erreur de l'appelant, pas un cas métier : `require`, qui lève une
  `IllegalArgumentException`.
- Il rend la capture relue depuis la base, avec les identifiants qu'elle a donnés aux codes : c'est
  par l'identifiant d'un code que s'ouvre son écran de détail.
- Quand l'utilisateur ne garde pas d'historique, la capture s'enregistre hors de lui : la feuille et
  le détail l'ouvrent par son identifiant, puis la purge du démarrage suivant la supprime.
- Quand il ignore les doublons consécutifs, les mêmes codes, dans n'importe quel ordre, que la
  dernière capture de l'historique enregistrée il y a 5 minutes au plus
  (`CONSECUTIVE_DUPLICATE_WINDOW`) ne font pas une nouvelle capture : celle-là est remise à l'heure,
  et rendue.
- `PurgeHistoryUseCase` supprime les captures hors de l'historique, et celles plus anciennes que la
  durée de conservation choisie (`HistoryRetention` : 7 jours, 30 par défaut, 90, un an, ou
  toujours). Il tourne au démarrage de l'app (`MainViewModel`), et quand la durée change
  (`SetHistoryRetentionUseCase`), pour qu'une durée plus courte s'applique tout de suite.
- Supprimer le dernier code d'une capture supprime la capture (règle portée par `data`).

### Créations

Une `Creation` est un code créé par l'utilisateur : son contenu, le texte mis dans le code, son
format, son style, sa date.

- Le style (`CodeStyle`) dit comment dessiner un QR code : la couleur des points et celle du fond
  (ARGB, noir sur blanc par défaut), la forme des points et celle des trois coins (`CodeShape` :
  carré, arrondi, rond), le logo posé en son centre (un PNG, ou aucun), et sa correction d'erreur
  (`CodeErrorCorrection` : automatique, faible, moyenne, élevée, maximale). Un code-barres garde le
  style par défaut. Une forme inconnue, écrite par une version plus récente, se lit carrée, et une
  correction inconnue se lit automatique.
- La correction automatique est la plus faible, et la plus forte sous un logo. Un logo est toujours
  encodé à la plus forte, quelle que soit la correction gardée : l'éditeur ne permet un logo qu'en
  correction automatique ou maximale, et ne propose plus que celles-là quand un logo est posé.
- Un logo est un `ByteArray`, qu'une classe de données comparerait par référence : `CodeStyle` (comme
  `StoredCodeStyle` et `CreationEntity` dans `data`) compare le sien par son contenu.
- `RecordCreationUseCase` reçoit un contenu, un format et un style. Il écrit le texte du contenu
  (`toRawValue`, l'encodeur, l'exact inverse du parseur), l'enregistre, hors de l'historique quand
  l'utilisateur n'en garde pas des créations, et rend la création relue.
- `toRawValue` est publique : l'éditeur en tire l'aperçu du code avant tout enregistrement.
- Pour un code-barres, le contenu est un `Text` de ses chiffres ou de ses caractères.
- Le formulaire de l'éditeur (`CreationFormMapper`, dans `shared`) rend un contenu seulement quand ce
  qu'il exige est là : un texte, un lien (sans schéma, il reçoit `https://`, sans quoi un lecteur le
  lirait comme un texte), le destinataire d'un e-mail, le numéro d'un SMS, le nom d'un contact, le
  nom d'un réseau et, pour un réseau WPA, un mot de passe de 8 à 63 caractères (ceux qu'Android
  accepte). Le format juge ensuite le texte (`canHold`), avec l'encodeur qui le dessine : un
  code-barres refuse une mauvaise longueur ou un mauvais chiffre de contrôle, un QR code un texte
  trop long pour la correction avec laquelle il sera encodé (`qrErrorCorrection`). qrose encode un QR code
  en le dessinant, sans moyen d'en rattraper l'échec : un texte n'est dessiné qu'une fois accepté.
- Sous les champs d'un QR code, un compteur (`qrContentSize`) dit ce que le contenu entier prend du
  code, contre ce que le code tient à cette même correction. qrose écrit tout le texte dans un seul
  mode, compté dans son unité : des chiffres seuls, des majuscules et chiffres (avec ` $%*+-./:`), ou
  sinon les octets de son UTF-8, où un accent ou un emoji en prend plusieurs (le compteur le dit).
  Ses maxima, ceux de la version 40 du QR code, sont vérifiés contre l'encodeur pour chaque mode et
  chaque correction (`ContentSizeMapperTest`) : le compteur passe au rouge exactement quand l'aperçu
  refuse le contenu.
- La correction se choisit dans les options avancées de la première étape, repliées sous le
  compteur, puisqu'elle change ce que le code tient (`errorCorrectionChoices`). Une correction trop
  forte pour le contenu, ou trop faible pour le logo posé, le dit et ne se choisit pas. Le logo, à la
  seconde étape, dit de même ce qui l'empêche (`logoBlocker`) : une correction choisie plus faible
  que la maximale, ou un contenu trop long pour elle.
- À l'inverse, un code scanné ou créé remplit le formulaire (`CodeFormMapper`) pour être modifié et
  créé comme un nouveau code ; une création apporte aussi son style : un code-barres d'un format que l'éditeur dessine reste un code-barres de sa
  valeur, tout autre code devient un QR code de son contenu, et un contact garde le premier de ses
  numéros, e-mails, adresses et sites. Un contenu complet, rempli puis relu, revient tel quel.
- La purge de l'historique (`PurgeHistoryUseCase`) vaut pour les créations comme pour les captures,
  avec la même durée de conservation.

### UseCases

- Un UseCase est **une interface et son implémentation, dans le même fichier**, nommé d'après
  l'interface (`RecordScanUseCase.kt`). L'implémentation est `internal`.
- Il expose une seule méthode, `operator fun invoke(...)`, pour s'appeler comme une fonction :
  `recordScanUseCase(barcodes = listOf(barcode))`. Elle est `suspend`, sauf quand elle rend
  un `Flow` (`WatchScansUseCase`) : un `Flow` est déjà froid, il n'a rien à suspendre.
- Un code est interprété à la lecture (`BarcodeContentParser` dans le mapper) : seul le texte brut
  est stocké, et une amélioration du parseur profite à tout l'historique. La conversion d'une liste
  se fait hors du thread principal (`flowOn(Dispatchers.Default)`).
- Il reçoit et rend des modèles métier, jamais des modèles de `data` : il fait la conversion.
- Comme `shared` ne voit pas `data`, toute lecture passe par un UseCase, même quand il ne fait que
  lire et convertir. C'est le prix de la frontière, accepté par l'ADR-0001.
- Il ne contient pas de `try`/`catch` global : c'est l'appelant (le ViewModel) qui gère l'erreur. Un
  `try`/`catch` local est permis pour traiter un cas précis ou relancer une exception plus parlante.
- Une erreur métier est une exception dédiée, dans `exception/`, nommée `<Problème>Exception`
  (`ScanNotFoundException`).
- L'heure courante vient d'un `Clock` reçu par constructeur (`Clock.System` en production, déclaré
  dans `DomainModule`), jamais d'un appel direct : les tests passent une date fixe.

`usecase/scan/RecordScanUseCase.kt`

```kotlin
interface RecordScanUseCase {

    /**
     * Records the [barcodes] read in one capture, at least one, and returns the capture. The same code
     * read several times is recorded once, at its first reading.
     */
    suspend operator fun invoke(barcodes: List<DetectedBarcode>): Scan
}

internal class RecordScanUseCaseImpl(
    private val scanRepository: ScanRepository,
    private val clock: Clock,
) : RecordScanUseCase {

    override suspend fun invoke(barcodes: List<DetectedBarcode>): Scan {
        require(barcodes.isNotEmpty()) { "A scan holds at least one code" }

        val codes = barcodes
            .map { barcode ->
                barcode.normalized()
            }
            .distinct()
            .map { barcode ->
                CodeToSave(
                    rawValue = barcode.rawValue,
                    format = barcode.format.name,
                )
            }

        val scanId = scanRepository.saveScan(
            codes = codes,
            scannedAt = clock.now(),
        )

        // Read back, for the ids of the codes the database just gave
        return checkNotNull(scanRepository.watchScan(id = scanId).first()) {
            "The scan $scanId was not found once recorded"
        }.toScan()
    }
}
```

`di/DomainModule.kt`

```kotlin
val domainModule = module {
    includes(dataModule)

    single<Clock> { Clock.System }

    factoryOf(::RecordScanUseCaseImpl) bind RecordScanUseCase::class
    factoryOf(::WatchScansUseCaseImpl) bind WatchScansUseCase::class
}
```