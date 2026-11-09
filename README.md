# Stemmer français Proaxian pour Elasticsearch

Le filtre `custom_french_stemmer` adapte le stemming léger français aux métiers.
La version **0.2.0** corrige certaines familles et les suffixes inclusifs explicites.
À partir de la version **0.1.0**, il utilise l'API stable des plugins d'analyse
d'Elasticsearch au lieu de l'API classique.

## Compatibilité

- Elasticsearch **9.0.0 et versions 9.x ultérieures** : même archive compilée
  contre l'API stable 9.0.0 et son Lucene 10.1.0.
- Elasticsearch 8.x ou 7.x : utiliser les anciennes branches/versions du dépôt.
- Elasticsearch 10.x : adaptation et nouvelle compilation à prévoir.
- Compilation : **JDK 21 minimum**, Maven 3.9.x recommandé.

La version Elasticsearch du POM est la **version minimale de l'API**, pas la
version du serveur cible à modifier à chaque mise à jour. La version Lucene
doit correspondre à cette version minimale. Les API Elasticsearch et Lucene
sont fournies par le serveur et ne sont pas embarquées dans le ZIP.

Documentation officielle :
[API stable](https://www.elastic.co/docs/extend/elasticsearch/creating-stable-plugins).

## Compiler et tester

À la racine du dépôt, avec `JAVA_HOME` pointant sur le JDK :

```sh
mvn clean verify
```

L'archive installable se trouve dans :

```text
es-custom-french-stemmer/target/releases/px-custom-french-stemmer-0.2.0-es9.zip
```

Elle contient uniquement les deux JAR du projet,
`stable-plugin-descriptor.properties` et `named_components.json` à sa racine.
Ne pas simplement renommer le descripteur d'une ancienne archive classique.

Les tests figent les sorties de 1 372 termes capturées avant la migration et
vérifient le respect des mots protégés, la fabrique et les métadonnées stables.
Le corpus historique reste inchangé ; `stemmer-corrections.tsv` énumère les
seules modifications de résultats autorisées. Les tests linguistiques contrôlent
les nouvelles équivalences, les contre-exemples et la tokenisation standard.

Pour tester la même archive dans de vrais Elasticsearch 9.0.0 et 9.5.6 :

```powershell
pwsh -File ./tools/Test-StablePlugin.ps1
```

Ce test nécessite Docker, télécharge les images officielles si elles manquent,
et lance des conteneurs jetables sans réseau ni port publié. Il analyse les
termes historiques, corrections et formes inclusives via `_analyze` sur chaque serveur.
Il conserve les diagnostics dans
`es-custom-french-stemmer/target/integration/`, puis supprime ses conteneurs.
Il ne touche à aucun conteneur applicatif existant.

## GitHub Actions

Le workflow **Plugin stable - compilation et tests** s'exécute après un push
sur `master`, sur les tags `v*`, sur les pull requests vers `master` et manuellement avec
**Actions → Plugin stable - compilation et tests → Run workflow**.

Il compile une seule fois avec Java 21 et Maven, exécute les tests unitaires,
puis utilise `tools/Test-StablePlugin.ps1` pour tester exactement la même
archive sur Elasticsearch 9.0.0 et 9.5.6. Ces versions de test sont centralisées
dans ce script ; ajouter une version au tableau `$ESVersions` permet d'étendre
les vérifications sans changer la version minimale de l'API dans le POM.

Une fois tous les contrôles réussis, ouvrir l'exécution dans l'onglet **Actions**
et télécharger l'artefact **px-custom-french-stemmer-VERSION-es9**. Dézipper
l'artefact GitHub : il contient le ZIP installable du plugin et `SHA256SUMS`.
Depuis le dossier dézippé, on peut vérifier le fichier avant installation :

```sh
sha256sum --check SHA256SUMS
```

L'artefact validé est conservé 90 jours, dans la limite du paramétrage du dépôt.
Les rapports Maven et journaux Elasticsearch sont conservés 14 jours dans
**rapports-tests**, y compris lorsqu'un test échoue. Si un contrôle échoue,
aucune archive validée n'est proposée.

Le workflow utilise le jeton GitHub automatique : aucun secret supplémentaire,
aucun commit généré ni déploiement sur les serveurs. Les tests restent en lecture
seule ; seul le job de publication d'un tag peut écrire une GitHub Release.

### Publier une version installable depuis une URL

Après avoir mis à jour la version dans les POM et committé les sources :

```sh
git tag -a v0.2.0 -m "Version 0.2.0"
git push origin master v0.2.0
```

Le tag doit correspondre exactement à `v` suivi de la version du POM, sinon
le workflow échoue. Une fois tous les tests réussis, un second job récupère
exactement l'archive testée, revérifie son empreinte et publie une GitHub Release
contenant le ZIP et `SHA256SUMS`. Les simples push de branche et lancements
manuels produisent seulement les artefacts Actions.

Après publication de `v0.2.0`, une première installation peut utiliser :

```sh
sudo /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch \
  'https://github.com/scoueille/px-custom-french-stemmer-for-es/releases/download/v0.2.0/px-custom-french-stemmer-0.2.0-es9.zip'
```

Pour remplacer un plugin existant, suivre la procédure d'arrêt et remplacement
ci-dessous. Les archives des Releases restent disponibles tant que la Release
est conservée ; elles ne dépendent pas de la rétention des artefacts Actions.

Le premier lancement distant nécessite que le fichier
`.github/workflows/stable-plugin.yml` ait été committé puis poussé sur GitHub.

## Remplacer le plugin classique sur un serveur

Planifier l'opération et procéder nœud par nœud pour un cluster. Le serveur
doit être arrêté pendant le remplacement ; il ne faut pas laisser démarrer
les index dépendants entre la suppression et la réinstallation.

```sh
sudo systemctl stop elasticsearch
sudo /usr/share/elasticsearch/bin/elasticsearch-plugin remove px-custom-french-stemmer
sudo /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch file:///chemin/px-custom-french-stemmer-0.2.0-es9.zip
sudo systemctl start elasticsearch
```

Conserver l'ancienne archive pour un éventuel retour arrière compatible avec
la version Elasticsearch du nœud. Installer le plugin sur tous les nœuds concernés.
Le nom du plugin et le type `custom_french_stemmer` restent identiques :
aucun changement de définition d'analyseur n'est nécessaire.

**La version 0.2.0 change des radicaux : reconstruire les index concernés.**
Mettre le plugin à jour sur les nœuds puis réindexer avant de basculer les recherches.
Un index créé avec 0.1.0 et interrogé avec 0.2.0 peut manquer les mots corrigés.
La migration technique 0.0.1 → 0.1.0 seule ne nécessitait pas de réindexation.

```json
POST /_analyze
{
  "tokenizer": "standard",
  "filter": ["lowercase", "custom_french_stemmer"],
  "text": ["ingénieur", "ingénieure", "directeur", "directrice"]
}
```

Sorties attendues : `ingenieu`, `ingenieu`, `direct`, `direct`.

Le kit d'installation serveur doit aussi reconnaître les métadonnées stables
et leur compatibilité par version majeure avant de déployer cette archive.
Sa logique destinée à reconstruire un plugin classique pour une version exacte
ne convient plus à cette version.

## Versions du dépôt

Le même dépôt Git peut conserver l'historique classique et cette migration.
Le passage à l'API stable constitue la version 0.1.0. La version 0.2.0
apporte les corrections linguistiques détaillées dans CHANGELOG.md.
Conserver les anciennes branches ES 7/8 et
publier des tags/archives distincts pour identifier chaque génération.

