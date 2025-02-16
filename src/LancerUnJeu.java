import java.util.Scanner;

public class LancerUnJeu {

    public static void main(String[] args) {
        // Exemple : créer deux joueurs et un état de jeu pour un plateau 15x15
        EtatDuJeu etat = new EtatDuJeu(15);
        Joueur joueur1 = new Joueur("Alice", 'X');
        Joueur joueur2 = new Joueur("Bob", 'O');

        demarrerPartie(etat, joueur1, joueur2);
    }

    /**
     * Lance une partie de jeu avec deux joueurs
     * @param etat      L'état du jeu (plateau, joueurActuel, etc.)
     * @param joueur1   Premier joueur
     * @param joueur2   Deuxième joueur
     */
    public static void demarrerPartie(EtatDuJeu etat, Joueur joueur1, Joueur joueur2) {
        Scanner scanner = new Scanner(System.in);
        // Définir le joueur actuel au début (ex joueur1)
        etat.setJoueurActuel(joueur1.getSymbole());

        while (!etat.estFinDuJeu()) {
            // Afficher le plateau
            afficherPlateau(etat);

            // Demander les coordonnées au joueur courant
            int[] coord = demanderCoordonnees(etat, scanner);
            int ligne = coord[0];
            int colonne = coord[1];

            // Placer le pion
            etat.getPlateau()[ligne][colonne] = etat.getJoueurActuel();

            // Vérifier la victoire
            if (verifierVictoire(etat, ligne, colonne)) {
                afficherPlateau(etat);
                System.out.println("Félicitations! Le joueur " + etat.getJoueurActuel() + " a gagné!");
                etat.setFinDuJeu(true);
            } else {
                // Passer au joueur suivant
                if (etat.getJoueurActuel() == joueur1.getSymbole()) {
                    etat.setJoueurActuel(joueur2.getSymbole());
                } else {
                    etat.setJoueurActuel(joueur1.getSymbole());
                }
            }
        }

        scanner.close();
    }

    /**
     * Affiche l'état actuel du plateau
     * @param etat L'état du jeu
     */
    public static void afficherPlateau(EtatDuJeu etat) {
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();

        // Afficher l'en-tête des colonnes
        System.out.print("   ");
        for (int j = 0; j < taille; j++) {
            System.out.printf("%2d ", j+1);
        }
        System.out.println();

        // Afficher les lignes
        for (int i = 0; i < taille; i++) {
            System.out.printf("%2d ", i+1);
            for (int j = 0; j < taille; j++) {
                System.out.print(" " + plateau[i][j] + " ");
            }
            System.out.println();
        }
    }

    /**
     * Demande à l'utilisateur de saisir les coordonnées sous la forme "n m"
     * (n pour la ligne et m pour la colonne)
     * @param etat    L'état du jeu pour connaître la taille du plateau
     * @param scanner Scanner pour la lecture des entrées
     * @return Un tableau int[2] contenant la ligne et la colonne
     */
    public static int[] demanderCoordonnees(EtatDuJeu etat, Scanner scanner) {
        int ligne, colonne;
        while (true) {
            System.out.print("Entrez le numéro de la ligne et la colonne (séparés par un espace) : ");
            ligne = scanner.nextInt() - 1;
            colonne = scanner.nextInt() - 1;

            // Vérifier la validité
            if (ligne < 0 || ligne >= etat.getTaillePlateau()
                    || colonne < 0 || colonne >= etat.getTaillePlateau()) {
                System.out.println("Coordonnées invalides, réessayez.");
            } else if (etat.getPlateau()[ligne][colonne] != '.') {
                System.out.println("La case est déjà occupée, réessayez.");
            } else {
                break;
            }
        }
        return new int[] {ligne, colonne};
    }

    /**
     * Vérifie si le joueur courant a gagné après avoir joué sur (ligne, colonne)
     * @param etat   L'état du jeu
     * @param ligne  Ligne où le joueur a joué
     * @param colonne Colonne où le joueur a joué
     * @return true si le joueur courant a gagné, sinon false
     */
    public static boolean verifierVictoire(EtatDuJeu etat, int ligne, int colonne) {
        char symbole = etat.getJoueurActuel();
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();

        // 4 directions : horizontal, vertical, diagonale (gauche-droite), diagonale (droite-gauche)
        int[][] directions = {
                {0, 1},  // Horizontal
                {1, 0},  // Vertical
                {1, 1},  // Diagonale (haut-gauche -> bas-droite)
                {1, -1}  // Diagonale (haut-droite -> bas-gauche)
        };

        for (int[] d : directions) {
            int compteur = 1; // On compte déjà la case actuelle
            int dx = d[0], dy = d[1];

            // Vérifier dans la direction "positive"
            int i = ligne + dx, j = colonne + dy;
            while (i >= 0 && i < taille && j >= 0 && j < taille && plateau[i][j] == symbole) {
                compteur++;
                i += dx;
                j += dy;
            }

            // Vérifier dans la direction "inverse"
            i = ligne - dx;
            j = colonne - dy;
            while (i >= 0 && i < taille && j >= 0 && j < taille && plateau[i][j] == symbole) {
                compteur++;
                i -= dx;
                j -= dy;
            }

            // S'il y a 5 pions consécutifs ou plus, victoire
            if (compteur >= 5) {
                return true;
            }
        }

        return false;
    }
}
