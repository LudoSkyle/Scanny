## Conventions Compose

Ces conventions s'appliquent à `shared` et `designSystem`, en complément de
[Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md).

### Signature et appel d'un composable

- Un composable qui dessine quelque chose prend un `modifier: Modifier = Modifier`, **premier
  paramètre optionnel**, placé après les paramètres obligatoires, et l'applique à son conteneur
  racine.
- Il ne force pas sa taille : un `fillMaxSize()` dans le corps d'un composant réutilisable retire à
  l'appelant le contrôle de sa mise en page. C'est le rôle du `modifier` reçu.
- Les `Route` et les `Screen` n'exposent pas de `modifier` : ils occupent tout l'écran.
- À l'appel, tout composant reçoit un `modifier` qui dit sa stratégie de taille
  (`fillMaxWidth()`, `fillMaxSize()`, `wrapContentWidth()`, `size(...)`, le `modifier` reçu…).
    - Exemple à éviter : `Text(text = title)`, `Box { … }`.
    - Exemple attendu : `Box(modifier = Modifier.fillMaxSize()) { … }`.
- `modifier = ...` est le **premier argument nommé** de l'appel :
  ```kotlin
  Text(
      modifier = Modifier.wrapContentWidth(),
      text = title,
  )
  ```
- Dans un composable, une ligne vide sépare chaque appel frère d'un même bloc (deux `Text`
  successifs, un `Spacer` et un `Button`…).
- Une lambda composable (un slot comme `title`, `icon`, ou le contenu d'un conteneur) s'écrit sur
  plusieurs lignes, son contenu sur sa propre ligne. Un callback court
  (`onClick = { onScanClicked(scan.id) }`) reste sur une ligne.
    - Exemple à éviter : `title = { Text(text = title) },`
    - Exemple attendu :
      ```kotlin
      title = {
          Text(text = title)
      },
      ```

### Ressources

- Textes, polices et icônes passent par **Compose Resources** : `src/commonMain/composeResources/`
  (dossiers `values`, `values-fr`, `drawable`, `font`), lus avec `stringResource(Res.string.x)`,
  `painterResource(Res.drawable.x)` et `Font(Res.font.x)`.
- Hors d'un composable, une chaîne se lit avec la fonction suspendue `getString(Res.string.x)`.
- Les icônes sont des **Material Symbols** (style Rounded), téléchargées en vectoriel XML dans
  `drawable`, préfixées `ic_` (`ic_flashlight_on`).
- Une icône a toujours un `contentDescription` non nul, même à côté d'un texte qui dit la même
  chose.
- Voir [Common/02_CONVENTIONS.md](../Common/02_CONVENTIONS.md) pour les règles des `strings.xml`.

### État et recomposition

- Un état que l'utilisateur ouvre ou saisit se garde avec `rememberSaveable`, pas `remember` : une
  rotation ne doit ni fermer une dialog ni perdre une saisie.
- **`derivedStateOf` ne sert qu'à dériver d'un autre `State`** (un `LazyListState`, un
  `mutableStateOf`). Dériver d'un paramètre se fait avec `remember(parametre) { … }`.
- **Un `remember` qui lit un paramètre prend ce paramètre en clé.** Sans clé, la valeur n'est
  jamais recalculée : c'est un bug d'affichage silencieux.
- Pas de `mutableStateOf` enveloppant une valeur déjà dérivable : `val isEmpty = scans.isEmpty()`
  suffit.
- Le fichier `compose-stability.conf` à la racine déclare stables les modèles immuables venus d'un
  module sans compilateur Compose (`fr.skyle.scanny.domain.model.**`, `kotlinx.datetime.**`). Un
  composable qui les reçoit reste ainsi *skippable*.

### Performance

- Une `LazyColumn` (historiques) fournit une `key` **et** un `contentType` stables pour chaque
  élément.
- Aucune allocation coûteuse (formatage de date, `Regex`, parsing) dans le corps d'un composable
  appelé par élément de liste : ce qu'affiche une cellule se prépare dans le ViewModel, dans un
  modèle propre à la liste.
- Le tri, le regroupement et le filtrage d'une liste appartiennent au ViewModel, pas à la
  composition.
- Une valeur animée (zoom, cadre de visée) se lit de préférence dans `Modifier.graphicsLayer` ou
  `Modifier.layout` plutôt que dans le corps du composable : la lecture reste en phase de layout ou
  de dessin, sans recomposition à chaque frame.

### Mise en page

- Ne pas utiliser `Arrangement.spacedBy(X.dp)` dès lors que l'espacement entre les enfants n'obéit
  pas à une vraie règle unique et volontaire.
    - Acceptable dans un composant dont le design impose le même espacement quel que soit le contenu.
    - À éviter sur un écran composé de blocs distincts : que des blocs sans lien partagent la même
      valeur est une coïncidence du moment. Un `Spacer` explicite entre chaque bloc dit mieux
      l'intention.
      ```kotlin
      Column(modifier = Modifier.fillMaxWidth()) {
          ScanDetailHeader(modifier = Modifier.fillMaxWidth())

          Spacer(modifier = Modifier.height(24.dp))

          ScanDetailContent(modifier = Modifier.fillMaxWidth())
      }
      ```
- Un modificateur conditionnel s'exprime par une concaténation, pas par un `let` :
    - Exemple à éviter : `.let { if (isSelected) it.border(...) else it }`
    - Exemple attendu : `.then(if (isSelected) Modifier.border(...) else Modifier)`
- Une surface cliquable ne porte aucun autre élément cliquable.
- Les insets système (barres d'état et de navigation, encoche) sont gérés par la structure d'écran
  du design system, pas écran par écran : Compose dessine sous les barres sur les deux plateformes.

### Previews

- Chaque composable non trivial a au moins une preview, `private`, nommée `Preview` + nom du
  composable (`PreviewScanDetailScreen`, `PreviewScanDetailScreen_Empty`), et enveloppée dans
  `SCTheme`.
- Les previews couvrent les états limites : vide, chargement, contenu long, valeur absente.
- Une preview a plusieurs variantes « personnalisables » : elle passe par un
  `PreviewParameterProvider` (existant ou à créer).
- Dans `shared`, une preview n'utilise pas `@Preview` directement mais une annotation de
  `ui/core/preview/Previews.kt`, qui la rend en français et en anglais :
    - `@AppScreenPreview` pour un écran entier, et pour ce qui s'affiche par-dessus (dialog,
      bottom sheet) ;
    - `@AppPreview` pour un composant ;
    - `@AppPreviewWithBackground` pour un composant qui ne dessine pas son propre fond.