# ADR-0002 — L'UI est commune, en Compose Multiplatform

Status: Accepted — 2026-11-20

> ADR **de projet**. Vaut pour tout le dépôt.

## Contexte

Scanny cible Android et iOS, avec les mêmes écrans et les mêmes fonctionnalités sur les deux. Deux
UI natives (Compose d'un côté, SwiftUI de l'autre) doubleraient chaque écran, chaque correction et
chaque test d'UI, pour une app qui a déjà son propre design system et n'a pas besoin du rendu natif
d'iOS.

Une partie du travail reste propre à chaque plateforme : la caméra et la détection des codes, le
choix d'une image, les permissions, et les actions système (ajouter un contact, rejoindre un
réseau WiFi, partager…).

## Décision

- Tous les écrans, la navigation, les ViewModels et le design system sont écrits en **Compose
  Multiplatform**, dans `commonMain`.
- L'app iOS n'est qu'un **hôte SwiftUI** qui affiche le `ComposeUIViewController` de `shared`. Aucun
  écran n'est écrit en SwiftUI.
- Le natif n'apparaît que derrière une frontière commune, selon les règles de
  [KMP/03_CODE_PLATEFORME.md](../KMP/03_CODE_PLATEFORME.md).
- La détection utilise le moteur natif de chaque plateforme : ML Kit sur Android, AVFoundation et
  Vision sur iOS. Pas de bibliothèque de scan tierce, et jamais ZXing.
- Le code natif iOS s'écrit de préférence en Kotlin, dans `iosMain`, par l'interop Kotlin/Native.
  Du Swift n'est écrit que pour une API inaccessible depuis Kotlin ; il est alors fourni à `shared`
  au démarrage, derrière une interface commune.

## Conséquences

- Une fonctionnalité s'écrit et se teste une fois.
- Le rendu iOS n'est pas celui d'UIKit : c'est accepté, Scanny suit son propre design system.
- Tout le code vit dans le build Gradle, à l'exception de l'hôte SwiftUI et de la configuration du
  projet Xcode.
- Ce qui ne peut être que natif (zoom et autofocus de la caméra, présentation d'un contrôleur iOS)
  reste dans un `actual` ou une implémentation de plateforme, jamais dans un écran.