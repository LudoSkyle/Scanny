## Conventions de code

Conventions transverses à tous les modules. Pour l'UI, voir en complément
[UI/04_CONVENTIONS_COMPOSE.md](../UI/04_CONVENTIONS_COMPOSE.md).

### Langue et commentaires

- Le code, les noms et les commentaires sont **en anglais**. La documentation du projet (`docs/`,
  `CLAUDE.md`) est en français.
- **On commente peu.** Un commentaire explique **pourquoi**, jamais **quoi**.
    - Exemple à éviter : `// Parse the content` au-dessus de `parser.parse(rawValue)`.
    - Exemple attendu : `// MeCard before vCard: a MeCard payload may contain "BEGIN:" in a field.`
- Sont du bruit, donc à ne pas écrire :
    - le KDoc qui ne fait que reformuler une signature ;
    - les étiquettes de section (`// Insert`, `// Read`, `// View`) ;
    - les commentaires accolés aux dépendances d'un `build.gradle.kts`, sauf pour justifier un choix
      qui ne se devine pas ;
    - tout commentaire qui paraphrase la ligne suivante.
- Les champs d'une entité Room se commentent quand leur sens, leur unité ou leur format ne se lit
  pas dans leur nom.

### Nommage

- `UI` s'écrit toujours en majuscules, jamais `Ui` : `ScanUIState`, `UIModel`.
- Les acronymes de 2 lettres restent en majuscules (`UI`, `ID`, `QR`), ceux de 3 lettres ou plus
  s'écrivent comme un mot (`Url`, `Sms`, `Wifi`, `Json`, `Ean`).
    - Exemple à éviter : `SMSContent`, `parseURL`, `WIFIEncryption`.
    - Exemple attendu : `SmsContent`, `parseUrl`, `WifiEncryption`, `QRCodeGenerator`.
- Un `val` ne se nomme jamais `getXxx` : c'est déjà un accesseur.
- Le nom dit **ce que fait** la fonction : une méthode qui supprime tout l'historique s'appelle
  `deleteScans`, pas `deleteScan`.
- Pas de redondance : dans un `ScanDao`, la méthode s'appelle `insert`, pas `insertScanEntity`.
- Pas de suffixe `Helper` / `Utils` / `Manager` sur une classe qui a un rôle précis : le nom dit le
  rôle (`BarcodeContentParser`, pas `QRCodeHelper`).
- Une exception se nomme `<Problème>Exception` (`ScanNotFoundException`).

### Style

- Ne jamais laisser de ligne vide en fin de fichier.
- Jamais deux lignes vides consécutives. Aérer son code pour gagner en lisibilité.
- Si un appel comporte 2 paramètres ou plus, **tous** les paramètres sont nommés (sauf là où Kotlin
  l'interdit : méthode Java, lambda reçue en paramètre, `vararg`), et chacun va sur sa propre ligne,
  même quand l'appel tiendrait sur une seule.
    - Exemple à éviter : `scanDao.insert(entity, true)`, ou
      `scanDao.insert(entity = entity, replace = true)` sur une ligne.
    - Exemple attendu :
      ```kotlin
      scanDao.insert(
          entity = entity,
          replace = true,
      )
      ```
- `combine` (comme `zip`) reçoit ses flows sans les nommer : c'est sa lambda, aux paramètres nommés,
  qui dit ce qu'est chaque valeur. La lambda construit l'objet avec des arguments nommés, sans
  référence de constructeur.
    - Exemple attendu :
      ```kotlin
      combine(
          settingsRepository.watchIsVibrationEnabled(),
          settingsRepository.watchIsRawContentShown(),
      ) { isVibrationEnabled, isRawContentShown ->
          ScanSettings(
              isVibrationEnabled = isVibrationEnabled,
              isRawContentShown = isRawContentShown,
          )
      }
      ```
- Pour une fonction déclarée avec `=` (expression body), rien sur la ligne du `=` : le corps
  commence à la ligne suivante. Ne s'applique qu'aux fonctions, pas aux propriétés.
    - Exemple à éviter :
      ```kotlin
      internal fun ScannedCodeEntity.toStoredCode(): StoredCode = StoredCode(
          id = id,
          rawValue = rawValue,
      )
      ```
    - Exemple attendu :
      ```kotlin
      internal fun ScannedCodeEntity.toStoredCode(): StoredCode =
          StoredCode(
              id = id,
              rawValue = rawValue,
          )
      ```
- Une fonction qui retourne `Unit` utilise un corps en bloc, pas une expression body.
- Virgule finale (trailing comma) sur les listes de paramètres et d'arguments multi-lignes.
- Les `enum class` ne portent pas de `;` final inutile ni de `companion object` vide.

### Gestion d'erreur

- Un `catch (exception: Exception)` relance toujours `CancellationException` :
  ```kotlin
  try {
      deleteScanUseCase(scanId = scanId)
  } catch (exception: CancellationException) {
      throw exception
  } catch (exception: Exception) {
      Logger.e(throwable = exception) { "Failed to delete scan $scanId" }
  }
  ```
- Le `try`/`catch` s'écrit sur place, là où l'erreur se traite (le ViewModel), et n'est pas caché
  derrière une fonction utilitaire.
- `throw Exception("...")` est interdit : toujours un type dédié.
- Les observations de la base (`watch…`) n'attrapent **pas** leurs erreurs : une lecture qui échoue
  est un bug, il doit se voir.
- Une erreur de programmation (argument invalide, état impossible) se signale par `require` /
  `check`, pas par un silence.
- Un log d'erreur passe par Kermit (`Logger.e`), jamais par `println`.

### Textes et traductions

- Tout texte affiché vient des ressources, même un sigle qui ne se traduit pas : il est alors
  déclaré dans `values/strings.xml` seulement, avec `translatable="false"`.
- Les textes vivent dans Compose Resources : `src/commonMain/composeResources/values/strings.xml`
  (anglais, langue par défaut) et `values-fr/strings.xml`, dans le module qui les affiche.
- Les fichiers `strings.xml` restent triés par ordre alphabétique des clés.
- Les clés sont en anglais, même si la valeur est en français.
- La clé décrit le contenu, pas l'usage à l'écran.
    - Exemple attendu : `settings -> Settings`.
    - Exemple à éviter : `settings_title -> Settings`.
- Dans un `strings.xml` de Compose Resources, l'apostrophe s'écrit telle quelle (`L'historique`, et
  non `L\'historique`, dont l'antislash s'afficherait). Les variables restent positionnelles
  (`%1$s`).