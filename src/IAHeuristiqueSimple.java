package src;

import java.util.Collections;
import java.util.List;

/**
 * Implémentation d'une IA basée sur des heuristiques simples pour le jeu de Gomoku.
 * 
 * Cette classe utilise une approche heuristique basique pour :
 * - Évaluer les positions sur le plateau
 * - Identifier les formations de pions
 * - Choisir le meilleur coup à jouer
 * 
 * Caractéristiques :
 * - Utilise des règles simples pour évaluer les positions
 * - Considère les alignements de pions
 * - Prend en compte la position centrale
 * - Implémente une stratégie défensive basique
 * 
 * Cette IA est plus simple que les versions MinMax et MCTS,
 * mais peut servir de base pour des implémentations plus avancées.
 */
public class IAHeuristiqueSimple extends Joueur {

    /**
     * Constructeur de l'IA heuristique simple
     * @param nom Le nom de l'IA
     * @param symbole Le symbole utilisé par l'IA ('X' ou 'O')
     */
    public IAHeuristiqueSimple(String nom, char symbole) {
        super(nom, symbole);
    }

    /**
     * Trouve le meilleur coup à jouer pour l'IA en utilisant une approche heuristique simple
     * @param etat L'état actuel du jeu
     * @return Un tableau de deux entiers [ligne, colonne] représentant le meilleur coup
     */
    public int[] trouverMeilleurCoup(EtatDuJeu etat) {
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        int meilleurScore = Integer.MIN_VALUE;
        int meilleureLigne = -1;
        int meilleureColonne = -1;

        // Parcourir toutes les cases vides
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == '.') {
                    int score = evaluerPosition(etat, i, j);
                    if (score > meilleurScore) {
                        meilleurScore = score;
                        meilleureLigne = i;
                        meilleureColonne = j;
                    }
                }
            }
        }

        return new int[]{meilleureLigne, meilleureColonne};
    }

    /**
     * Évalue le score d'une position potentielle
     * @param etat L'état du jeu
     * @param ligne La ligne à évaluer
     * @param colonne La colonne à évaluer
     * @return Le score de la position
     */
    private int evaluerPosition(EtatDuJeu etat, int ligne, int colonne) {
        int score = 0;
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();

        // Directions à vérifier : horizontal, vertical, diagonales
        int[][] directions = {
            {0, 1},  // horizontal
            {1, 0},  // vertical
            {1, 1},  // diagonale descendante
            {1, -1}  // diagonale ascendante
        };

        for (int[] dir : directions) {
            score += evaluerDirection(plateau, ligne, colonne, dir[0], dir[1], taille);
        }

        return score;
    }

    /**
     * Évalue une direction spécifique pour une position donnée
     */
    private int evaluerDirection(char[][] plateau, int ligne, int colonne, int deltaLigne, int deltaColonne, int taille) {
        int score = 0;
        char symboleIA = 'O';  // L'IA joue avec 'O'
        char symboleAdversaire = 'X';

        // Vérifier dans les deux sens de la direction
        for (int sens = -1; sens <= 1; sens += 2) {
            int compteurIA = 0;
            int compteurAdversaire = 0;
            int espaces = 0;

            for (int i = 1; i <= 4; i++) {
                int newLigne = ligne + sens * i * deltaLigne;
                int newColonne = colonne + sens * i * deltaColonne;

                if (newLigne < 0 || newLigne >= taille || newColonne < 0 || newColonne >= taille) {
                    break;
                }

                char caseActuelle = plateau[newLigne][newColonne];
                if (caseActuelle == symboleIA) {
                    compteurIA++;
                } else if (caseActuelle == symboleAdversaire) {
                    compteurAdversaire++;
                } else {
                    espaces++;
                }
            }

            // Attribution des scores
            if (compteurIA == 4) score += 10000;  // Victoire imminente
            else if (compteurIA == 3 && espaces >= 1) score += 1000;  // Forte menace
            else if (compteurIA == 2 && espaces >= 2) score += 100;   // Menace moyenne
            else if (compteurIA == 1 && espaces >= 3) score += 10;    // Menace faible

            // Bloquer l'adversaire
            if (compteurAdversaire == 4) score += 5000;  // Bloquer victoire
            else if (compteurAdversaire == 3 && espaces >= 1) score += 500;  // Bloquer menace forte
            else if (compteurAdversaire == 2 && espaces >= 2) score += 50;   // Bloquer menace moyenne
        }

        return score;
    }
}
