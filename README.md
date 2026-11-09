# Stemmer français Proaxian pour Elasticsearch

Le filtre `custom_french_stemmer` conserve les règles historiques du projet.
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
es-custom-french-stemmer/target/releases/px-custom-french-stemmer-0.1.0-es9.zip
```

Elle contient uniquement les deux JAR du projet,
`stable-plugin-descriptor.properties` et `named_components.json` à sa racine.
Ne pas simplement renommer le descripteur d'une ancienne archive classique.

Les tests figent les sorties de 1 372 termes capturées avant la migration et
vérifient le respect des mots protégés, la fabrique et les métadonnées stables.
Le corpus contient volontairement des contre-exemples : il garantit la
continuité des résultats, pas leur pertinence linguistique parfaite.

Pour tester la même archive dans de vrais Elasticsearch 9.0.0 et 9.5.6 :

```powershell
pwsh -File ./tools/Test-StablePlugin.ps1
```

Ce test nécessite Docker, télécharge les images officielles si elles manquent,
et lance des conteneurs jetables sans réseau ni port publié. Il analyse les
1 372 termes via `_analyze` sur chaque serveur, conserve les diagnostics dans
`es-custom-french-stemmer/target/integration/`, puis supprime ses conteneurs.
Il ne touche à aucun conteneur applicatif existant.

## Remplacer le plugin classique sur un serveur

Planifier l'opération et procéder nœud par nœud pour un cluster. Le serveur
doit être arrêté pendant le remplacement ; il ne faut pas laisser démarrer
les index dépendants entre la suppression et la réinstallation.

```sh
sudo systemctl stop elasticsearch
sudo /usr/share/elasticsearch/bin/elasticsearch-plugin remove px-custom-french-stemmer
sudo /usr/share/elasticsearch/bin/elasticsearch-plugin install --batch file:///chemin/px-custom-french-stemmer-0.1.0-es9.zip
sudo systemctl start elasticsearch
```

Conserver l'ancienne archive pour un éventuel retour arrière compatible avec
la version Elasticsearch du nœud. Installer le plugin sur tous les nœuds concernés.
Le nom du plugin et le type `custom_french_stemmer` restent identiques :
aucun changement de définition d'analyseur n'est nécessaire. La migration ne
modifie pas les radicaux et ne demande pas de réindexation pour ce seul motif.

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
Le passage à l'API stable constitue une nouvelle version du plugin (0.1.0),
sans remplacement de l'algorithme. Conserver les anciennes branches ES 7/8 et
publier des tags/archives distincts pour identifier chaque génération.

