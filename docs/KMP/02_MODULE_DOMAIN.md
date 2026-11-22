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
et aux actions : `Text`, `Url`, `Email` (destinataires, copies, copies cachées, sujet, corps, de la RFC 6068), `Sms` (numéro, message),
`Wifi` (SSID, chiffrement `NONE`/`WEP`/`WPA`/`WPA_ENTERPRISE`, mot de passe, réseau masqué, modes sans
repli `WifiTransitionMode`, identifiant du mot de passe SAE, clé publique SAE-PK ; pour un réseau
d'entreprise, méthode EAP, identité, identité anonyme, phase 2), `Contact`, `Phone`
(numéro), `Location` (latitude et longitude en degrés décimaux, altitude et incertitude en mètres, nom du lieu), `Bookmark` (lien et
titre), `Otp` (clé secrète, type `TOTP`/`HOTP`, émetteur, compte, et algorithme `OtpAlgorithm`, nombre
de chiffres, période et compteur tels que lus, nuls quand le code les laisse à leur défaut : SHA1, 6
chiffres, 30 s, les constantes `DEFAULT_…` de `Otp`), `Bitcoin` (adresse,
montant en bitcoins tel qu'écrit, nom du destinataire, message). Le texte brut n'en fait pas partie : il est porté par le `ScannedCode`, et le contenu en est
relu à chaque lecture de la base : un type nouveau s'applique aussi aux codes déjà enregistrés.

`Contact` porte les champs d'une vCard : un nom affiché (`formattedName`) **et** les cinq parties du
nom (civilité, prénom, deuxième prénom, nom, suffixe), un surnom, l'organisation et son service, la
fonction, des listes de téléphones (`ContactPhone`, son type `ContactPhoneType` : mobile, domicile,
travail, fax, autre), d'e-mails et de sites (`ContactEmail`, `ContactWebsite`, leur type
`ContactEntryType` : domicile, travail, autre) et d'adresses (`ContactAddress`, en parties : boîte
postale, complément, rue, ville, région, code postal, pays, et son type), un anniversaire en ISO 8601
et une note. `displayName` donne le nom à montrer : le nom affiché, ou à défaut les parties du nom.
Son `format` (`VCARD`, `MECARD`) est celui dans lequel il a été lu : un contact modifié s'écrit comme
il était écrit.

`Mms` est un SMS qui a un objet : numéro, objet, message.

Le parseur est **le cœur de la parité entre les plateformes**. ML Kit sait typer un code sur
Android, mais AVFoundation et Vision ne rendent qu'un texte brut sur iOS. Scanny n'utilise donc
jamais les types structurés de ML Kit : les deux plateformes passent le texte brut au même
parseur, et obtiennent le même résultat.

- Une vCard donne le type d'une entrée par ses paramètres (`TYPE=CELL,VOICE`, `TYPE="cell,voice"` de
  la 4.0, `CELL` seul de la 2.1) : un fax est un fax avant d'être du travail. Un anniversaire est
  ramené à `AAAA-MM-JJ` (`19900512`, `1990-05-12T08:30:00Z`) ; un autre format est laissé de côté.
- Formats reconnus : `WIFI:`, `MATMSG:`, `mailto:`, `SMSTO:` / `sms:`, `MMSTO:` / `mms:`, `MECARD:`, `BEGIN:VCARD`,
  `tel:`, `geo:`, `MEBKM:`, `otpauth://`, `bitcoin:`, et les URL (`http://`, `https://`). Les préfixes et les clés sont
  insensibles à la casse, les valeurs non.
- Un `geo:` (RFC 5870) donne sa latitude, sa longitude, son altitude et son incertitude (`;u=…`). Un
  autre système de référence que WGS84 (`;crs=…`) en fait un texte : ses coordonnées ne se placeraient
  pas sur les cartes. Android met le lieu dans la requête : `?q=lat,lon(nom)` donne le nom, et
  ses coordonnées quand le lien est à `0,0` ; `?q=nom` autour d'une position la nomme. Une recherche
  autour de `0,0` (une adresse) n'a pas de position : c'est un texte, comme des coordonnées hors de
  la carte.
- Un `otpauth://` (le format des applis d'authentification) a besoin de sa clé (`secret`) et d'un type
  connu (`totp`, `hotp`). Son libellé est `émetteur:compte` ou le compte seul : il est coupé au premier
  `:` littéral, sinon à un `:` encodé en `%3A`, que le format autorise aussi entre les deux. Le
  paramètre `issuer` l'emporte sur l'émetteur du libellé. Un algorithme inconnu fait du code un
  texte : pris pour du SHA1, il donnerait d'autres codes que ceux de la clé.
- Un `bitcoin:` (BIP 21) a besoin de son adresse ; un montant qui n'est pas un nombre décimal est
  laissé de côté.
- Les caractères échappés sont décodés (`\;`, `\,`, `\:`, `\\`, `\"` dans un `WIFI:` ou un
  `MECARD:`, et une valeur entre guillemets non échappés perd ses guillemets), les lignes repliées
  d'une vCard sont dépliées (CRLF ou LF), ses groupes (`item1.`) et paramètres (`;TYPE=…`) ignorés,
  et l'encodage `%` d'un `mailto:` ou d'un `sms:` est décodé en UTF-8. Un `+` reste un `+`.
- Un `WIFI:` suit la section « WIFI URI » de la spécification WPA3 de la Wi-Fi Alliance, et pour un
  réseau d'entreprise le format de ZXing. Sans type, c'est un réseau ouvert, sauf s'il donne un mot de
  passe (alors WPA) ; un type en `…EAP` est un réseau d'entreprise. `R:` donne en hexadécimal les modes
  dont le réseau ne revient pas (bit 0 WPA3-Personal seul, 1 SAE-PK seul, 2 WPA3-Entreprise seul, 3
  Wi-Fi Enhanced Open seul) ; `SAE` et `WPA3`, types d'anciens générateurs, sont le WPA3-Personal seul
  que la spécification écrit `T:WPA;R:1`. `I:` est l'identité d'un réseau d'entreprise (ZXing),
  l'identifiant du mot de passe SAE sinon (Wi-Fi Alliance) ; `K:` la clé publique SAE-PK ; `E:`, `A:`,
  `PH2:` la méthode EAP, l'identité anonyme et la phase 2.
- Les champs d'un `WIFI:` sont échappés par une barre oblique inverse, comme ZXing et les appareils
  photo d'Android et d'iOS les lisent, et non encodés en `%` comme le veut la spécification : décoder
  les deux ferait d'un mot de passe ZXing qui contient `%41` un autre mot de passe.
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
| Email | `mailto:adresse,adresse?cc=…&bcc=…&subject=…&body=…`, encodé en `%`, les adresses séparées par des virgules ; le paramètre `to=` est lu aussi (`MATMSG:`, un seul destinataire, n'est que lu) |
| SMS | `SMSTO:numéro:message` (`sms:` n'est que lu) |
| WiFi | dans l'ordre de la spécification WPA3 : `WIFI:T:…;R:…;S:…;H:true;I:…;P:…;K:…;`, puis `E:…;A:…;PH2:…;` d'un réseau d'entreprise (`T:WPA2-EAP`) et `;` ; `T:nopass` et pas de `P:` pour un réseau ouvert, le WPA3 seul en `T:WPA;R:1`, `R:` et `H:true` seulement quand ils servent |
| Contact | vCard 3.0 : `N` en cinq parties, `FN`, `NICKNAME`, `ORG` (organisation;service), `TITLE`, `TEL`, `EMAIL`, `ADR` (sept parties), `URL` avec `;TYPE=` (aucun pour « autre »), `BDAY`, `NOTE` ; seules les propriétés texte sont échappées. En MeCard : `MECARD:N:nom,prénom;…;;` (ou le nom affiché seul), `NICKNAME`, `TEL`, `EMAIL`, `ADR` (ses parties séparées par des virgules, une rue seule écrite seule), `URL`, `BDAY` (AAAAMMJJ), `NOTE`, et `ORG`, `TITLE` hors du format mais lus par les lecteurs ; champs échappés comme ceux d'un `WIFI:`, une virgule échappée dans `N` ou `ADR` appartient à la partie. Un MeCard n'a ni les autres parties du nom, ni service, ni types |
| MMS | `mms:numéro?subject=…&body=…`, encodé en `%` (`MMSTO:numéro:message`, sans objet, n'est que lu) |
| Téléphone | `tel:numéro`, encodé en `%` |
| Position | `geo:lat,lon[,alt][;u=…]`, et `?q=lat,lon(nom)` s'il a un nom ; coordonnées en décimales, sept au plus (`toCoordinateText()`, jamais en exposant : le texte d'un `Double` diffère entre les plateformes) |
| Marque-page | `MEBKM:TITLE:…;URL:…;;`, champs échappés comme ceux d'un `WIFI:` |
| Clé à usage unique | `otpauth://totp/émetteur:compte?secret=…&issuer=…`, puis les seuls paramètres lus ; l'émetteur dans le libellé et dans `issuer`, que les applis lisent l'un ou l'autre ; un émetteur sans compte garde son `:` |
| Bitcoin | `bitcoin:adresse?amount=…&label=…&message=…`, nom et message encodés en `%` |
- Le parseur et l'encodeur sont `internal` : `shared` les utilise à travers les UseCases. Seuls sont
  publics les liens qui ouvrent l'app d'un contenu (`toMailtoLink()`, `toSmsLink()`, `toMmsLink()`, `toTelLink()`,
  `toGeoLink()`, `toAppleMapsLink()`, `toOtpauthLink()`, `toBitcoinLink()`, dans `ContentLinks.kt`) et l'écriture d'une coordonnée
  (`toCoordinateText()`) : `shared` les ouvre et les affiche tels quels, et leur encodage `%` est celui
  de l'encodeur. Le lien `geo:` d'une position y porte toujours son lieu (`?q=lat,lon`) : sans lui,
  Maps centre la carte sans l'épingler.
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
  (ARGB, noir sur blanc par défaut), un dégradé des points vers une seconde couleur (`CodeGradient` :
  aucun, linéaire d'un coin à l'autre, radial du centre aux bords), celle des trois coins (celle des
  points, dégradé compris, par défaut), la forme des points et celle des trois coins (`CodeShape` :
  carré, arrondi, rond), le logo posé en son centre (un PNG, ou aucun) avec sa forme (`CodeShape`,
  carré par défaut) et sa marge (`CodeLogoMargin` : aucune, normale par défaut, large), et sa
  correction d'erreur (`CodeErrorCorrection` : automatique, faible, moyenne, élevée, maximale). Un code-barres garde le
  style par défaut. Une forme inconnue, écrite par une version plus récente, se lit carrée, un
  dégradé inconnu se lit sans dégradé, une marge inconnue normale, et une correction inconnue se
  lit automatique.
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
- L'éditeur offre **chaque champ de la spec du format** d'un contenu, pas un sous-ensemble : pour une
  clé à usage unique, le type, l'émetteur, le compte, la clé, l'algorithme, le nombre de chiffres, et
  la période (TOTP) ou le compteur (HOTP). Un champ sans lequel le code n'existe pas porte un
  astérisque (`isRequired` de `CreationTextField`). Un paramètre laissé à son défaut n'est pas écrit :
  le code est plus petit et se lit pareil.
- Le formulaire de l'éditeur (`CreationFormMapper`, dans `shared`) rend un contenu seulement quand ce
  qu'il exige est là : un texte, un lien (sans schéma, il reçoit `https://`, sans quoi un lecteur le
  lirait comme un texte), un champ au moins d'un e-mail (la RFC 6068 n'en exige aucun), une altitude et
  une incertitude (positive) qui sont des nombres si elles sont données, le numéro d'un SMS, pour un contact en vCard un nom à montrer (tapé, fait de ses parties ou de son
  organisation) et en MeCard un prénom ou un nom (les champs qu'un MeCard ne tient pas restent dans le
  formulaire, cachés, mais ne sont pas écrits), le
  nom d'un réseau et, pour un réseau WPA, un mot de passe de 8 à 63 caractères (ceux qu'Android
  accepte) s'il peut se replier sur le WPA2, d'une longueur quelconque en WPA3 seul (une clé publique
  SAE-PK le rend SAE-PK seul, donc WPA3 seul), pour un réseau d'entreprise une identité et le mot de
  passe des méthodes qui en prennent un (PEAP, TTLS, PWD), la phase 2 n'étant offerte qu'à PEAP et TTLS, le compte et la clé d'une clé à usage unique (en base32, 16 caractères au moins, tapée en
  groupes et en minuscules comme les services la montrent), sa période (TOTP) ou son compteur (HOTP). Le format juge ensuite le texte (`canHold`), avec l'encodeur qui le dessine : un
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
- Les options avancées de la seconde étape, repliées sous le logo, donnent aux coins une couleur à
  eux, de la palette des points ou libre (la couleur des points n'y est offerte qu'une fois, comme la
  leur), aux points un dégradé vers une seconde couleur, et, une fois un logo posé, sa forme et sa
  marge. qrose ne donne de forme qu'à l'espace dégagé autour du logo : le logo est découpé à sa
  forme par `ShapedLogoPainter`. Sans marge, il est posé sur les points.
- La barre du haut de la personnalisation la réinitialise : retour au style par défaut, la correction
  choisie avec le contenu gardée. Un logo s'y perdrait : un message propose d'annuler, tant que la
  personnalisation n'a pas changé depuis.
- L'avertissement de lisibilité juge les points, les coins et la seconde couleur du dégradé : plus
  sombres que le fond, et moins de moitié aussi lumineux que lui, dans la luminance que ZXing lit
  (`scannerLuminance` : rouge, deux fois le vert et bleu, sur quatre, en nombres entiers). Le
  binariseur de ZXing, le lecteur de la plupart des apps de scan sur Android, prend une zone claire
  uniforme pour le fond et place son seuil à la moitié de sa luminance : un point aussi clair que
  cette moitié se lit comme du fond. La règle dit exactement les couleurs que ZXing lit, quelles que
  soient la teinte, la forme des points et le fond (`CodeImageReadabilityTest`). Le contraste de
  4,5:1 que les règles d'accessibilité demandent d'un texte, d'abord retenu, avertissait de couleurs
  que ZXing lit (un rouge, un vert, un turquoise vifs sur blanc) ; le contraste de symbole de la
  norme ISO/IEC 15415, mesuré en réflectance, en laissait passer qu'il ne lit pas (un gris moyen, un
  orange, un magenta).
- Une couleur libre se choisit sur la palette des teintes, ou se saisit en hexadécimal (six chiffres,
  avec ou sans `#`) ou en rouge, vert et bleu de 0 à 255 (`hexToArgb`, `rgbToArgb`). Les champs et la
  palette se suivent ; une saisie ne change la couleur qu'une fois complète et valide. La couleur
  saisie est gardée exactement : repassée par la teinte, la saturation et la luminosité, elle pourrait
  s'écarter d'une unité.
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