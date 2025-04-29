public class MinMaxBasique {

    /**
     * Trouve le meilleur coup à jouer pour l'IA en utilisant l'algorithme Minimax.
     * @param etat L'état actuel du jeu.
     * @param profondeur La profondeur de recherche de l'algorithme.
     * @return Un tableau de deux entiers [ligne, colonne] représentant le meilleur coup, ou null si aucun coup n'est possible.
     */
    public int[] trouverMeilleurCoup(EtatDuJeu etat, int profondeur) {
        int meilleurScore = Integer.MIN_VALUE;
        int meilleureLigne = -1;
        int meilleureColonne = -1;
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();

        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == '.') {
                    plateau[i][j] = 'O'; // Supposer que l'IA est 'O'
                    int score = minimax(etat, profondeur, false); // Commencer par minimiser (tour de l'adversaire)
                    plateau[i][j] = '.'; // Annuler le coup

                    if (score > meilleurScore) {
                        meilleurScore = score;
                        meilleureLigne = i;
                        meilleureColonne = j;
                    }
                }
            }
        }

        if (meilleureLigne != -1) {
            return new int[]{meilleureLigne, meilleureColonne};
        } else {
            return null; // Aucun coup possible
        }
    }

    /**
     * Fonction récursive Minimax.
     * @param etat L'état du jeu.
     * @param profondeur La profondeur restante.
     * @param estMaximisant true si c'est le tour du joueur maximisant (IA), false sinon.
     * @return Le score de la branche explorée.
     */
    private int minimax(EtatDuJeu etat, int profondeur, boolean estMaximisant) {
        // Condition d'arrêt: profondeur atteinte, victoire détectée, ou plateau plein
        if (profondeur == 0 || LancerJeu.verifierVictoire(etat, -1, -1) || LancerJeu.estPlateauPlein(etat)) {
            return evaluerPosition(etat);
        }

        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();

        if (estMaximisant) { // Tour de l'IA ('O')
            int meilleurScore = Integer.MIN_VALUE;
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    if (plateau[i][j] == '.') {
                        plateau[i][j] = 'O';
                        char joueurPrecedent = etat.getJoueurActuel(); // Sauvegarder l'état
                        etat.setJoueurActuel('X');
                        int score = minimax(etat, profondeur - 1, false);
                        plateau[i][j] = '.'; // Annuler le coup
                        etat.setJoueurActuel(joueurPrecedent); // Restaurer l'état
                        meilleurScore = Math.max(score, meilleurScore);
                    }
                }
            }
            return meilleurScore;
        } else { // Tour de l'adversaire ('X')
            int meilleurScore = Integer.MAX_VALUE;
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    if (plateau[i][j] == '.') {
                        plateau[i][j] = 'X';
                        char joueurPrecedent = etat.getJoueurActuel(); // Sauvegarder l'état
                        etat.setJoueurActuel('O');
                        int score = minimax(etat, profondeur - 1, true);
                        plateau[i][j] = '.'; // Annuler le coup
                        etat.setJoueurActuel(joueurPrecedent); // Restaurer l'état
                        meilleurScore = Math.min(score, meilleurScore);
                    }
                }
            }
            return meilleurScore;
        }
    }

    /**
     * Évalue la position actuelle du plateau.
     * @param etat L'état du jeu.
     * @return Le score de la position (positif pour IA, négatif pour adversaire).
     */
    private int evaluerPosition(EtatDuJeu etat) {
        int score = 0;
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();

        // Évaluation basée sur les séquences (simplifiée)
        // Horizontal
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j <= taille - 5; j++) {
                score += evaluerSequence(plateau[i][j], plateau[i][j+1], plateau[i][j+2], plateau[i][j+3], plateau[i][j+4]);
            }
        }
        // Vertical
        for (int i = 0; i <= taille - 5; i++) {
            for (int j = 0; j < taille; j++) {
                score += evaluerSequence(plateau[i][j], plateau[i+1][j], plateau[i+2][j], plateau[i+3][j], plateau[i+4][j]);
            }
        }
        // Diagonal (descendant)
        for (int i = 0; i <= taille - 5; i++) {
            for (int j = 0; j <= taille - 5; j++) {
                score += evaluerSequence(plateau[i][j], plateau[i+1][j+1], plateau[i+2][j+2], plateau[i+3][j+3], plateau[i+4][j+4]);
            }
        }
        // Diagonal (ascendant)
         for (int i = 4; i < taille; i++) {
            for (int j = 0; j <= taille - 5; j++) {
                 score += evaluerSequence(plateau[i][j], plateau[i-1][j+1], plateau[i-2][j+2], plateau[i-3][j+3], plateau[i-4][j+4]);
            }
        }

        return score;
    }

    /**
     * Évalue une séquence de 5 cases.
     * @param c1 Case 1
     * @param c2 Case 2
     * @param c3 Case 3
     * @param c4 Case 4
     * @param c5 Case 5
     * @return Le score de la séquence.
     */
    private int evaluerSequence(char c1, char c2, char c3, char c4, char c5) {
        int score = 0;
        int countO = 0;
        int countX = 0;
        char[] sequence = {c1, c2, c3, c4, c5};

        for (char c : sequence) {
            if (c == 'O') countO++;
            else if (c == 'X') countX++;
        }

        // Priorité aux coups gagnants/perdants immédiats et aux menaces
        if (countO == 5) return 100000; // Victoire IA
        if (countX == 5) return -100000; // Défaite IA
        if (countO == 4 && countX == 0) return 5000; // Forte menace IA
        if (countX == 4 && countO == 0) return -5000; // Forte menace Adversaire
        if (countO == 3 && countX == 0) return 100;   // Menace IA
        if (countX == 3 && countO == 0) return -100;  // Menace Adversaire
        if (countO == 2 && countX == 0) return 10;
        if (countX == 2 && countO == 0) return -10;
        if (countO == 1 && countX == 0) return 1;
        if (countX == 1 && countO == 0) return -1;

        return 0; // Séquence bloquée ou vide
    }
}
