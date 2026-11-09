# Historique

## 0.2.0

- Rapproche `dialoguer/dialogue`, `cataloguer/catalogue`, `épiloguer/épilogue`
  tout en conservant `géologue/géologie` : la nouvelle règle `-loguer` rejoint `-logue`.
- Rapproche `docteure/docteur` et leurs pluriels via le traitement de `-teure`.
- Reconnaît les suffixes inclusifs à points et points médians : `·e`, `·se`,
  `·euse`, `·rice`, `·trice`, `·ne`, `·le`, `·te`, `·ve`, `·ère` / `·ere`.
  Les suffixes de remplacement sont acceptés seulement après une terminaison
  masculine compatible ; les pluriels accolés et détachés sont reconnus.
- Préserve les règles existantes `-isme` et `-able`.
- API stable ES 9.x et nom `custom_french_stemmer` conservés.
- Réindexation nécessaire pour appliquer les corrections aux documents existants.

La reconnaissance inclusive s'applique à un token déjà constitué : les points
doivent parvenir au filtre. Elle ne reconstitue pas les écritures que le tokenizer
sépare, notamment `étudiant(e)`, `acteur/rice` ou `étudiant-e`. Elle ne constitue
pas une lemmatisation générale et n'ajoute pas de traitement des mots épicènes.

## 0.1.0

- Migration à l'API stable, sans modification de l'algorithme historique.
