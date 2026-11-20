## Design system

Le thème et les composants génériques vivent dans le module `designSystem`, à part des écrans. Le
module ne dépend que de Compose : un composant ne connaît ni un écran, ni un modèle métier. Un écran
choisit un composant et l'une de ses variantes ; il ne décide ni d'une couleur, ni d'une forme, ni
d'une taille de texte.

On y trouve :

- le thème : `SCColors`, `SCTypography`, `SCShapes`, `SCTheme` ;
- les composants génériques (`SCButton`, `SCIconButton`, `SCDialog`, `SCTextField`, `SCSwitch`,
  `SCScreenStructure`…) ;
- les polices (Lato) et les icônes des composants, dans Compose Resources.

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
│       ├── button/
│       │   ├── SCButton.kt
│       │   └── SCButtonDefaults.kt
│       ├── dialog/
│       ├── iconbutton/
│       ├── screenstructure/
│       ├── switch/
│       └── textfield/
└── composeResources/
    ├── drawable/              (icônes des composants)
    └── font/                  (lato_*.ttf)
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
        content = content,
    )
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
- Tant que l'app n'a qu'un thème, on n'en prévoit qu'un. Le second viendra avec son besoin.
- Les écrans ne lisent jamais `MaterialTheme` : un composant Material 3 (switch, slider, chip) est
  enveloppé dans le design system, qui lui passe les couleurs de `SCTheme`. Sans cela, il prendrait
  les couleurs par défaut de Material.

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
  `PreviewParameterProvider`. Elles n'utilisent que des ressources du module.
- La classe `Res` générée est `internal` au module : une ressource du design system qu'un écran
  affiche aussi passe par un accesseur public dédié.