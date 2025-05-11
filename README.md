# Gomoku-IA

C'est un projet réalisé dans le cadre de l'UE Intelligence Artificielle, à réaliser en binôme. L'objectif est de concevoir et programmer un jeu stratégique dans lequel un joueur humain affronte une Intelligence Artificielle (IA). Le jeu choisi est le Gomoku, dont l'objectif est d'aligner cinq pions consécutifs horizontalement, verticalement ou en diagonale.

## Contexte

Dans ce projet, quatres niveaux d'IA sont implémentés, allant d'une stratégie simple à une stratégie optimale. Pour y parvenir, nous avons utilisé notamment :

- L'algorithme Minimax basique
- L'amélioration de Minimax par élagage αβ

Une fois les IA implémentées, des tournois seront organisés (au moins 50 parties par couple d'IA) pour analyser leurs performances. Les résultats et analyses devront être intégrés dans le rapport final.

## Description du Projet

- **Jeu** : Gomoku  
  Le but du jeu est d'aligner cinq pions sur un plateau (ici, par exemple, un plateau 15x15).

- **Niveaux d'IA** :
  - **Niveau 1** : Stratégie simple (basique)
  - **Niveau 2** : Stratégie intermédiaire utilisant l'algorithme Minimax
  - **Niveau 3** : Stratégie optimale avec élagage αβ
  - **Niveau 4** : Stratégie optimale avec MCTS

## Wiki

Pour en savoir plus sur le jeu, vous pouvez consulter la page Wikipedia :  
[https://en.wikipedia.org/wiki/Gomoku](https://en.wikipedia.org/wiki/Gomoku)

## Fonctionnalités

- Gestion de l'état du jeu (plateau, joueur courant, conditions de victoire)
- Implémentation de trois niveaux d'IA avec différents degrés de complexité
- Saisie des coups via la console (entrée des coordonnées sous la forme "n m" où n représente la ligne et m la colonne)
- Organisation de tournois entre IA pour évaluer leurs performances

## Installation et Exécution

### Prérequis

- **Java JDK** (version 21 ou supérieure)
- Un environnement de développement (IntelliJ IDEA, Eclipse, etc.) ou un terminal

### Exécution du .jar
```bash
java "-Dprism.order=sw" "-Djava.library.path=lib/bin" --module-path lib --add-modules javafx.controls,javafx.fxml -jar out/artifacts/Gomoku_IA_jar/Gomoku-IA.jar
```



### Compilation

Si vous travaillez en ligne de commande, placez-vous dans le répertoire racine du projet et compilez les sources par exemple avec :

```bash
javac --module-path lib --add-modules javafx.controls,javafx.fxml -d bin (Get-ChildItem -Recurse -Filter *.java -Path src).FullName
```

Assurez-vous que le dossier bin existe ou qu'il sera créé pour contenir les fichiers compilés.

### Exécution

Pour lancer le jeu, exécutez l'une des commandes suivantes :

```bash
java "-Dprism.order=sw" --module-path lib --add-modules javafx.controls,javafx.fxml -cp bin GomokuApp
```

## Auteurs

- [SHI Jianye]
- [CAI Josephine]

## Licence

Ce projet est distribué sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.



