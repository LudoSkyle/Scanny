# ADR-0001 — Le domaine dépend de la donnée

Status: Accepted — 2026-11-20

> ADR **de projet**. Il prime sur l'ADR Kotlin 0001 du référentiel partagé (« les couches métier ne
> dépendent d'aucun framework de DI ») pour les points listés dans la décision, et seulement
> ceux-là.

## Contexte

L'ADR Kotlin 0001 du référentiel partagé place les interfaces de Repository dans le domaine, que la
donnée implémente (`data` → `domain`), et fait renvoyer aux UseCases un type `sealed` plutôt que de
laisser remonter une exception.

Scanny a peu de métier : interpréter le texte brut d'un code (WiFi, vCard, `mailto:`…), produire le
texte d'un code à créer, tenir deux historiques et trois réglages. Sa donnée se limite à une base
locale et à des préférences, sans réseau. Le domaine existe pour **cacher la donnée** au reste de
l'app, pas pour qu'elle s'y conforme.

## Décision

- Les dépendances vont dans un seul sens : `shared` → `domain` → `data`. `data` ne connaît pas
  `domain`.
- Les interfaces de Repository **et** leurs implémentations vivent dans `data`.
- `domain` dépend de `data` en `implementation` : `shared` ne voit ni les Repositories ni les
  modèles de `data`. Le compilateur garantit que l'UI passe par les UseCases.
- `domain` porte les modèles métier (le contenu typé d'un code), la logique pure (parseur,
  encodeur) et les UseCases.
- Un UseCase qui échoue lève une **exception typée**. C'est l'appelant, en pratique le ViewModel, qui
  l'attrape (`try`/`catch`, `CancellationException` relancée). Pas de type `Response` sealed.

Ce que cet ADR ne remplace pas dans l'ADR Kotlin 0001 continue de s'appliquer : domaine en Kotlin
pur, sans API de plateforme ; UseCase = une interface + une implémentation ; l'appelant reçoit
toujours l'interface.

## Conséquences

- Changer de stockage ne touche que `data`, et le mapping vers les modèles métier dans `domain`.
- Un test de UseCase double les Repositories de `data` par des fakes : leurs interfaces sont
  publiques.
- Aucune annotation `@Throws` : l'UI est en Kotlin sur les deux plateformes (ADR-0002), aucune
  exception ne traverse vers du Swift.
- Le détail de mise en œuvre est dans [KMP/01_MODULE_DATA.md](../KMP/01_MODULE_DATA.md) et
  [KMP/02_MODULE_DOMAIN.md](../KMP/02_MODULE_DOMAIN.md).