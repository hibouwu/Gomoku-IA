# Gomoku-IA

C'est un projet réalisé dans le cadre de l'UE Intelligence Artificielle, à réaliser en binôme. L'objectif est de concevoir et programmer un jeu stratégique dans lequel un joueur humain affronte une Intelligence Artificielle (IA). Le jeu choisi est le Gomoku, dont l'objectif est d'aligner cinq pions consécutifs horizontalement, verticalement ou en diagonale.

## Contexte

Dans ce projet, vous devez implémenter au moins trois niveaux d'IA, allant d'une stratégie simple à une stratégie optimale. Pour y parvenir, vous utiliserez notamment :

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

- **Java JDK** (version 8 ou supérieure)
- Un environnement de développement (IntelliJ IDEA, Eclipse, etc.) ou un terminal

### Compilation

Si vous travaillez en ligne de commande, placez-vous dans le répertoire racine du projet et compilez les sources par exemple avec :

```bash
javac -d bin src/*.java
```

Assurez-vous que le dossier bin existe ou qu'il sera créé pour contenir les fichiers compilés.

### Exécution

Pour lancer le jeu, exécutez l'une des commandes suivantes :

```bash
java -cp bin LancerUnJeu
```

## Auteurs

- [Shi Jianye]
- [Cai Josephine]

## Licence

Ce projet est distribué sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.


## Résultats


joueur 1 : IA Simple
joueur 2 : IA MinMax
joueur 3 : IA Alpha-Beta
joueur 4 : IA MCTS

joueur 1 vs joueur 2:
IA Simple (X): 25 victoires
IA MinMax (O): 0 victoires

joueur 2 vs joueur 1
IA MinMax (X): 0 victoires
IA Simple (O): 25 victoires

joueur 1 vs joueur 3
IA Simple (X): 25 victoires
IA Alpha-Beta (O): 0 victoires

joueur 3 vs joueur 1
IA Alpha-Beta (X): 0 victoires
IA Simple (O): 25 victoires

joueur 1 vs joueur 4
IA Simple (X): 0 victoires
IA MCTS (O): 25 victoires

joueur 4 vs joueur 1
IA MCTS (X): 25 victoires
IA Simple (O): 0 victoires
