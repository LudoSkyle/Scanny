# ADR-0004 — Les encodeurs Data Matrix et Aztec sont une copie corrigée de qrose

Status: Accepted — 2026-11-22

> ADR **de projet**. Vaut pour tout le dépôt.

## Contexte

Scanny dessine ses codes avec qrose. Pour le QR code et les codes-barres, qrose encode et dessine.
Pour Data Matrix, Aztec et PDF417, il n'a que des encodeurs (`qrose-encoder-matrix`), qui rendent
une matrice de modules à dessiner soi-même.

Les codes produits par ces encodeurs ont été relus avec ZXing, sur des milliers de textes
aléatoires. PDF417 ne s'est jamais trompé, accents compris. Data Matrix et Aztec, en version 1.3.1,
la dernière, écrivent des codes faux, **sans aucune erreur** : le lecteur relit un autre texte que
celui encodé.

- **Data Matrix** se trompe sur 6 à 20 % des textes qui mêlent majuscules, minuscules et
  ponctuation. Dans le mode Text, une majuscule est écrite comme la lettre qui la précède (« Dupont »
  relu « Cupont »). Les codes de 144 × 144 modules ne se relisent pas : leur correction d'erreur
  n'est pas entrelacée comme le veut la norme. Un caractère au-delà du latin-1 (« € ») fait planter
  l'encodeur.
- **Aztec** se trompe sur 11 à 14 % des textes en ASCII, à cause de deux changements de mode faux
  dans sa table, et d'un décalage absent qui écrit un mot de code « -1 ». Il écrit les accents en
  UTF-8 sans le signaler, et les lecteurs, qui supposent le latin-1, relisent « Ã© » pour « é ».

## Décision

- **Les encodeurs Data Matrix et Aztec de qrose sont copiés dans `shared`**, sous
  `ui/core/code/encoder`, avec leur Reed-Solomon, et corrigés. qrose est sous licence MIT : sa
  licence est gardée à côté des fichiers copiés, et chaque correction est marquée « Fixed from qrose ».
- **Data Matrix n'écrit qu'en mode ASCII**, celui que tout lecteur comprend, au lieu de passer d'un
  mode à l'autre (C40, Text, X12, EDIFACT, Base256). Les deux chiffres d'une paire tiennent dans un
  mot de code, et un caractère au-delà de 127 dans deux.
- **Aztec garde ses modes**, avec la table de ZXing, et écrit en latin-1.
- **Data Matrix et Aztec ne prennent que le latin-1** : une norme sans ECI n'en lit pas d'autre.
  L'éditeur refuse un contenu qui en sort, et propose un QR code ou un PDF417.
- **PDF417 et le QR code restent ceux de qrose.**
- Un test relit les trois encodeurs avec ZXing, sur des textes aléatoires toujours les mêmes, de
  chaque alphabet, et sur les plus grands symboles.

## Conséquences

- Scanny maintient environ 1 400 lignes d'encodeurs. Une mise à jour de qrose n'y touche pas.
- Un texte long tout en majuscules donne un Data Matrix un peu plus grand qu'avec le mode C40 ; la
  limite reste de 1 556 caractères, 3 116 chiffres.
- « € », « œ » ou un emoji ne vont ni dans un Data Matrix ni dans un code Aztec.
- Le jour où qrose corrige ses encodeurs, la copie peut disparaître : le test de relecture dira si
  les siens suffisent.
