package src;

import java.util.Scanner;

/**
 * Classe utilitaire pour gérer la logique de base du jeu de Gomoku.
 * 
 * Cette classe fournit des méthodes statiques pour :
 * - Vérifier les conditions de victoire
 * - Valider les coups
 * - Gérer l'état du plateau
 * - Contrôler le flux du jeu
 * 
 * Fonctionnalités principales :
 * - Détection des alignements gagnants
 * - Vérification de la validité des coups
 * - Gestion des tours de jeu
 * - Contrôle de fin de partie
 * 
 * Cette classe est utilisée par toutes les autres classes
 * pour accéder aux règles de base du jeu.
 */
public class LancerJeu {

    /** Vérifie la victoire à partir d'une position donnée */
    public static boolean verifierVictoire(EtatDuJeu etat, int ligne, int colonne) {
        int n = etat.getTaillePlateau();
        char[][] p = etat.getPlateau();
        char sym = etat.getJoueurActuel();

        if (ligne < 0 || colonne < 0) {
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    if (p[i][j] == sym && checkWinFrom(etat, i, j)) {
                        return true;
                    }
                }
            }
            return false;
        }
        return checkWinFrom(etat, ligne, colonne);
    }

    private static boolean checkWinFrom(EtatDuJeu etat, int ligne, int colonne) {
        char[][] p = etat.getPlateau();
        char sym = etat.getJoueurActuel();
        int n = etat.getTaillePlateau();
        int[][] dirs = {{0,1},{1,0},{1,1},{1,-1}};

        for (int[] d : dirs) {
            int count = 1, dx = d[0], dy = d[1];
            int x = ligne + dx, y = colonne + dy;
            while (x>=0 && x<n && y>=0 && y<n && p[x][y]==sym) {
                count++; x+=dx; y+=dy;
            }
            x = ligne - dx; y = colonne - dy;
            while (x>=0 && x<n && y>=0 && y<n && p[x][y]==sym) {
                count++; x-=dx; y-=dy;
            }
            if (count >= 5) return true;
        }
        return false;
    }

    /** Retourne true si le plateau est plein */
    public static boolean estPlateauPlein(EtatDuJeu etat) {
        char[][] p = etat.getPlateau();
        for (int i = 0; i < etat.getTaillePlateau(); i++) {
            for (int j = 0; j < etat.getTaillePlateau(); j++) {
                if (p[i][j] == '.') return false;
            }
        }
        return true;
    }

    /** Joue un coup et retourne le résultat */
    public static int[] jouerCoup(EtatDuJeu etat, int ligne, int colonne) {
        etat.getPlateau()[ligne][colonne] = etat.getJoueurActuel();
        
        if (verifierVictoire(etat, ligne, colonne)) {
            etat.setFinDuJeu(true);
            return new int[]{1, ligne, colonne};
        }
        if (estPlateauPlein(etat)) {
            etat.setFinDuJeu(true);
            return new int[]{0, ligne, colonne};
        }
        
        etat.setJoueurActuel(etat.getJoueurActuel() == 'X' ? 'O' : 'X');
        return new int[]{-1, ligne, colonne};
    }

    /** Joue un coup pour l'IA selon le niveau choisi */
    public static int[] jouerCoupIA(EtatDuJeu etat, int niveau) {
        switch (niveau) {
            case 1: return new IAHeuristiqueSimple("IA Simple", 'O').trouverMeilleurCoup(etat);
            case 2: return new MinMaxBasique("IA MinMax", 'O').trouverMeilleurCoup(etat, 1);
            case 3: return new MinMaxAlphaBeta("IA Alpha-Beta", 'O').trouverMeilleurCoup(etat, 2);
            case 4: return new MCTS("IA MCTS", 'O').trouverMeilleurCoup(etat, 2000);
            default: throw new IllegalArgumentException("Niveau IA invalide : " + niveau);
        }
    }

    /** Retourne le nom d'une IA selon son niveau */
    public static String getNomIA(int niveau) {
        switch (niveau) {
            case 1: return "IA Simple";
            case 2: return "IA MinMax";
            case 3: return "IA Alpha-Beta";
            case 4: return "IA MCTS";
            default: return "IA Inconnue";
        }
    }
}
