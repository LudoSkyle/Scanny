## Design system

Le thème et les composants génériques vivent dans le module `designSystem`, à part des écrans. Le
module ne dépend que de Compose et de Material 3 : un composant ne connaît ni un écran, ni un modèle
métier. Un écran choisit un composant et l'une de ses variantes ; il ne décide ni d'une couleur, ni
d'une forme, ni d'une taille de texte.

Le design n'a pas à reproduire celui de la v1 : les composants peuvent le moderniser. Seuls la
palette, la police Lato et l'identité de l'app (icône, ton des textes) servent de point de départ.

On y trouve :

- le thème : `SCColors`, `SCTypography`, `SCShapes`, `SCTheme` ;
- les composants génériques : `SCScreenStructure` (fond, insets, barre du haut, messages, couleur
  des icônes système), `SCTopAppBar`, `SCIconButton`, `SCIconBadge`, `SCButton`, `SCText`, `SCCard`,
  `SCDialog`, `SCBottomSheet` et `SCSnackbarState`. Les autres (champ de texte, interrupteur…)
  arrivent avec le premier écran qui en a besoin ;
- les polices (Lato), les icônes et les textes des composants, dans Compose Resources.

On n'y trouve pas :

- les composants « produit », propres à Scanny même s'ils servent dans plusieurs écrans (la cellule
  d'un scan dans l'historique, l'icône d'un type de contenu) : ils vivent dans `shared`, sous
  `ui/core/`.

```
designSystem/src/commonMain/
├── kotlin/fr/skyle/scanny/designsystem/
│   ├── theme/
│   │   ├── SCColors.kt        (SCColors, LocalColors, valeurs des couleurs)
│   │   ├── SCShapes.kt
│   │   ├── SCTheme.kt         (composable SCTheme et objet SCTheme)
│   │   └── SCTypography.kt    (SCTypography, LocalTypography, famille Lato)
│   └── core/
│       ├── bottomsheet/       (SCBottomSheet : ouverte en entier ou fermée)
│       ├── button/
│       │   ├── SCButton.kt
│       │   └── SCButtonDefaults.kt
│       ├── card/              (SCCard : un bloc de contenu détaché du fond)
│       ├── dialog/            (SCDialog : une question avant une action, confirmer ou annuler)
│       ├── iconbadge/         (SCIconBadge : une icône dans un rond, non cliquable)
│       ├── iconbutton/
│       │   ├── SCIconButton.kt
│       │   └── SCIconButtonDefaults.kt
│       ├── screenstructure/   (SCScreenStructure, et la couleur des icônes système par plateforme)
│       ├── snackbar/          (SCSnackbarState : les messages courts d'un écran)
│       ├── text/              (SCText : un texte dans un style du thème)
│       └── topappbar/         (SCTopAppBar : retour, titre, actions)
└── composeResources/
    ├── drawable/              (icônes des composants)
    ├── font/                  (lato_*.ttf)
    ├── values/                (textes des composants, en anglais)
    └── values-fr/
```

### Thème

Les composants génériques et le thème portent le préfixe `SC`. Le composable `SCTheme` fournit les
valeurs ; l'objet `SCTheme` les lit :

`theme/SCTheme.kt`

```kotlin
@Composable
fun SCTheme(
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalColors provides scannyColors,
        LocalTypography provides scannyTypography(),
        LocalShapes provides scannyShapes,
        LocalIndication provides ripple(),
    ) {
        ProvideTextStyle(
            value = SCTheme.typography.body1,
            content = content,
        )
    }
}

object SCTheme {
    val colors: SCColors
        @Composable
        @ReadOnlyComposable
        get() = LocalColors.current

    val typography: SCTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalTypography.current

    val shapes: SCShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalShapes.current
}
```

- Les couleurs sont déclarées en Kotlin dans `SCColors.kt`, regroupées dans la data class
  `@Immutable SCColors`, et fournies par un `staticCompositionLocalOf` initialisé à
  `Color.Unspecified`.
- La typographie lit les polices de Compose Resources (`Font(Res.font.lato_regular, ...)`), ce qui
  ne se fait que dans un composable : `scannyTypography()` est donc `@Composable`.
- `SCShapes` ne porte que les formes utilisées : `medium` (coins arrondis de 10 dp, ceux des
  boutons, des cartes et des cellules de la v1), `dialog` (24 dp) et `sheet` (le haut arrondi d'une
  bottom sheet). Une forme
  s'ajoute avec le composant qui en a besoin.
- Un écran affiche un texte avec `SCText` et un message court par un `SCSnackbarState` donné à
  `SCScreenStructure` : il ne voit jamais `Text` ni `SnackbarHostState` de Material.
- `SCTheme` fournit aussi le style de texte par défaut (`body1`) et le ripple de Material comme
  retour au toucher de tout élément cliquable.
- Tant que l'app n'a qu'un thème, on n'en prévoit qu'un. Le second viendra avec son besoin.

### Material 3

- La version est l'alpha alignée sur notre Compose Multiplatform (voir le catalogue et la
  [feuille de route](../ROADMAP.md)) : la dernière stable est compilée contre un Compose plus ancien.
- **Seul `designSystem` dépend de Material 3.** `shared` ne l'a pas sur son classpath : un écran
  n'appelle jamais `Text`, `Button` ou `Switch` de Material, il passe par un composant `SC…`. Quand un
  écran a besoin d'un élément qui n'existe pas encore (un texte, un bouton), on crée le composant dans
  le design system, on n'ouvre pas la dépendance.
- **Aucune API Expressive** : `ExperimentalMaterial3ExpressiveApi` n'est jamais activée, ni dans un
  fichier, ni dans un plugin de convention. Les autres API expérimentales de Material 3 (`TopAppBar`)
  sont permises, avec un `@OptIn(ExperimentalMaterial3Api::class)` local au composant.
- On ne déclare pas de `MaterialTheme` : un composant Material est enveloppé dans le design system,
  qui lui passe les couleurs de `SCTheme`. Sans cela, il prendrait les couleurs par défaut de
  Material.

### Composants

- **Un package par composant**, nommé comme lui en minuscules (`core/iconbutton/`).
- **Un objet `<Composant>Defaults` porte les variantes**, sous forme d'`enum class Style` (les
  couleurs) et d'`enum class Size` (dimensions, formes, marges, typographie). Les valeurs sont des
  propriétés de l'enum, `@Composable` quand elles lisent le thème :
  ```kotlin
  object SCButtonDefaults {

      enum class Style {
          PRIMARY,
          SECONDARY;

          val containerColor: Color
              @Composable
              get() = when (this) {
                  PRIMARY -> SCTheme.colors.primary
                  SECONDARY -> SCTheme.colors.backgroundLight
              }
      }

      enum class Size {
          MEDIUM,
          LARGE;

          val height: Dp
              get() = when (this) {
                  MEDIUM -> 44.dp
                  LARGE -> 56.dp
              }
      }
  }
  ```
- Le composant prend `style` et `size` **sans valeur par défaut** : l'appelant choisit
  explicitement, et la relecture voit ce qu'il a choisi.
  ```kotlin
  @Composable
  fun SCButton(
      text: String,
      onClick: () -> Unit,
      style: SCButtonDefaults.Style,
      size: SCButtonDefaults.Size,
      modifier: Modifier = Modifier,
      isEnabled: Boolean = true,
      isLoading: Boolean = false,
      leadingIcon: Painter? = null,
  )
  ```
- Une variante n'est ajoutée que quand un écran en a besoin. Une enum à une seule entrée est
  acceptée : c'est le point d'extension.
- Quand un conteneur a une mise en page interne récurrente (l'en-tête d'une dialog, la structure
  d'un écran avec sa barre du haut), elle est fournie par un `<Composant>Structure` à côté du
  conteneur, plutôt que réécrite par chaque écran.
- Un composant qui affiche une icône la reçoit en **`Painter`** (`icon: Painter`), lu par l'appelant
  avec `painterResource(Res.drawable.x)`.
- Un bouton qui n'affiche qu'une icône prend un `contentDescription` **obligatoire** et non nullable.
- Les previews d'un composant parcourent **toutes** ses combinaisons `Style` × `Size`, avec un
  `PreviewParameterProvider`. Elles n'utilisent que des ressources du module, et l'annotation
  `@Preview` directement (les annotations FR/EN sont propres à `shared`). Un style clair sur fond
  sombre se prévisualise avec `showBackground = true` et `backgroundColor = 0xFF000000`.
- Les icônes viennent des Material Symbols Rounded : le tracé SVG de Google, dans un vectoriel XML
  avec un commentaire qui nomme l'icône, un groupe `translateY="960"` et `fillColor="#000000"` (la
  couleur vient de l'`Icon`). Pas d'attribut propre à Android (`?attr/…`, `@android:color/…`).
- La classe `Res` générée est `internal` au module : une ressource du design system qu'un écran
  affiche aussi passe par un accesseur public dédié.