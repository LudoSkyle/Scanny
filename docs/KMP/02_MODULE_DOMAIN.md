## Module `domain`

Le module `domain` porte ce que Scanny sait faire indépendamment de l'écran et de la plateforme :

- les modèles métier : le contenu typé d'un code (`BarcodeContent`), son format (`BarcodeFormat`),
  un scan, une création ;
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
├── mapper/                        (modèle de data -> modèle métier)
├── model/
│   ├── BarcodeContent.kt
│   ├── BarcodeFormat.kt
│   ├── Scan.kt                    (lot 5)
│   └── WifiEncryption.kt
├── payload/                       (le texte d'un code, dans les deux sens)
│   ├── BarcodeContentEncoder.kt
│   ├── BarcodeContentParser.kt
│   └── PayloadSyntax.kt           (préfixes et caractères échappés, communs aux deux)
└── usecase/                       (lot 5)
    └── scan/
        ├── DeleteScanUseCase.kt
        ├── RecordScanUseCase.kt
        ├── WatchScanUseCase.kt
        └── WatchScansUseCase.kt
```

### Contenu d'un code

`BarcodeContent` est une `sealed interface` dont chaque type porte les champs utiles à l'affichage
et aux actions : `Text`, `Url`, `Email` (destinataire, sujet, corps), `Sms` (numéro, message),
`Wifi` (SSID, chiffrement `NONE`/`WEP`/`WPA`/`WPA3`, mot de passe, réseau masqué), `Contact`. D'autres
types (téléphone, position, événement) s'ajoutent avec l'écran qui les affiche. Le texte brut n'en
fait pas partie : il est porté par le `Scan`.

`Contact` porte un nom affiché (`formattedName`) **et** un prénom et un nom séparés : l'ajout d'un
contact sur Android prend un nom affiché, iOS (`CNMutableContact`) veut le prénom et le nom. S'y
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
- Le parseur et l'encodeur sont `internal` : `shared` les utilise à travers les UseCases.
- Chaque format a ses tests, dans `commonTest` (voir [Common/03_TESTS.md](../Common/03_TESTS.md)).

`BarcodeFormat` liste les formats reconnus par les deux moteurs de détection (QR Code, Data Matrix,
Aztec, PDF417, EAN-13, EAN-8, UPC-A, UPC-E, Code 39, Code 93, Code 128, ITF, Codabar). La conversion
depuis le format de chaque moteur se fait dans le code de plateforme de `shared`. AVFoundation n'a
pas de type UPC-A : il rend un UPC-A comme un EAN-13 commençant par `0`.

### UseCases

- Un UseCase est **une interface et son implémentation, dans le même fichier**, nommé d'après
  l'interface (`RecordScanUseCase.kt`). L'implémentation est `internal`.
- Il expose une seule méthode, `operator fun invoke(...)`, pour s'appeler comme une fonction :
  `recordScanUseCase(rawValue = rawValue, format = format)`.
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

    suspend operator fun invoke(
        rawValue: String,
        format: BarcodeFormat,
    ): Scan
}

internal class RecordScanUseCaseImpl(
    private val scanRepository: ScanRepository,
    private val clock: Clock,
) : RecordScanUseCase {

    override suspend fun invoke(
        rawValue: String,
        format: BarcodeFormat,
    ): Scan {
        val scannedAt = clock.now()

        val id = scanRepository.saveScan(
            rawValue = rawValue,
            format = format.name,
            scannedAt = scannedAt,
        )

        return Scan(
            id = id,
            rawValue = rawValue,
            content = BarcodeContentParser.parse(rawValue = rawValue),
            format = format,
            scannedAt = scannedAt,
        )
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