
import java.util.Scanner;

public class LancerJeu {

    /** Vérifie la victoire ; si ligne/colonne = -1, fait une vérification exhaustive (pour Minimax). */
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

    /** Retourne true si le plateau est plein. */
    public static boolean estPlateauPlein(EtatDuJeu etat) {
        char[][] p = etat.getPlateau();
        for (int i = 0; i < etat.getTaillePlateau(); i++) {
            for (int j = 0; j < etat.getTaillePlateau(); j++) {
                if (p[i][j] == '.') return false;
            }
        }
        return true;
    }

    /** Pose un pion au plateau. */
    public static void poserPion(EtatDuJeu etat, int ligne, int colonne) {
        etat.getPlateau()[ligne][colonne] = etat.getJoueurActuel();
    }

    /** Change X ↔ O. */
    public static void passerAuJoueurSuivant(EtatDuJeu etat) {
        char next = etat.getJoueurActuel() == 'X' ? 'O' : 'X';
        etat.setJoueurActuel(next);
    }
    /** Délègue à l’IA selon le niveau choisi. */
    /**
     * @param niveau 1 = simple, 2 = MinMax basique, 3 = MinMax+αβ
     */
    public static int[] jouerCoupIA(EtatDuJeu etat, int niveau) {
        switch (niveau) {
            case 1:
                return new IAHeuristiqueSimple().trouverMeilleurCoup(etat);
            case 2:
                return new MinMaxBasique().trouverMeilleurCoup(etat, 1);
            case 3:
                //return MinMaxAlphaBeta.trouverMeilleurCoup(etat);
            default:
                throw new IllegalArgumentException("Niveau IA invalide : " + niveau);
        }
    }

//    /** Délègue à l’IA basique (MinMaxBasique). */
//    public static int[] jouerCoupIA(EtatDuJeu etat, int profondeur) {
//        return new MinMaxBasique().trouverMeilleurCoup(etat, profondeur);
//    }

    /**
     * Joue un coup et renvoie :
     *   1 si victoire,
     *   0 si match nul,
     *  -1 si la partie continue.
     */
    public static int[] jouerCoup(EtatDuJeu etat, int ligne, int colonne) {
        poserPion(etat, ligne, colonne);
        if (verifierVictoire(etat, ligne, colonne)) {
            etat.setFinDuJeu(true);
            return new int[]{1, ligne, colonne};
        }
        if (estPlateauPlein(etat)) {
            etat.setFinDuJeu(true);
            return new int[]{0, ligne, colonne};
        }
        passerAuJoueurSuivant(etat);
        return new int[]{-1, ligne, colonne};
    }

    // (Optionnel) Mode console, inchangé :
    public static void demarrerPartieConsole(EtatDuJeu etat, Joueur j1, Joueur j2) {
        Scanner sc = new Scanner(System.in);
        etat.setJoueurActuel(j1.getSymbole());
        while (!etat.estFinDuJeu()) {
            afficherPlateauConsole(etat);
            if (estPlateauPlein(etat)) {
                System.out.println("Match nul.");
                break;
            }
            System.out.print("Entrez ligne et colonne : ");
            int l = sc.nextInt() - 1, c = sc.nextInt() - 1;
            int[] res = jouerCoup(etat, l, c);
            if (res[0] == 1) {
                afficherPlateauConsole(etat);
                System.out.println("Victoire du joueur " + etat.getJoueurActuel());
            }
        }
        sc.close();
    }

    private static void afficherPlateauConsole(EtatDuJeu etat) {
        char[][] p = etat.getPlateau();
        int n = etat.getTaillePlateau();
        System.out.print("   ");
        for (int j = 0; j < n; j++) System.out.printf("%2d ", j+1);
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.printf("%2d ", i+1);
            for (int j = 0; j < n; j++) {
                System.out.print(" " + p[i][j] + " ");
            }
            System.out.println();
        }
    }
}
