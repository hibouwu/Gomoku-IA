

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
    /** Délègue à l'IA selon le niveau choisi. */
    /**
     * @param niveau 1 = simple, 2 = MinMax basique, 3 = MinMax+αβ, 4 = MCTS
     */
    public static int[] jouerCoupIA(EtatDuJeu etat, int niveau) {
        switch (niveau) {
            case 1:
                return new IAHeuristiqueSimple().trouverMeilleurCoup(etat);
            case 2:
                return new MinMaxBasique().trouverMeilleurCoup(etat, 1);
            case 3:
                return new MinMaxAlphaBeta().trouverMeilleurCoup(etat, 2);
            case 4:
                return new MCTS().trouverMeilleurCoup(etat, 2000); // 2000ms (2秒)的时间限制
            default:
                throw new IllegalArgumentException("Niveau IA invalide : " + niveau);
        }
    }


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
        int niveauIA = -1;
        
        // 询问是否玩家 vs AI
        System.out.print("Activer l'IA ? (o/n): ");
        String vsAI = sc.nextLine().trim().toLowerCase();
        
        if (vsAI.equals("o") || vsAI.equals("oui") || vsAI.equals("y") || vsAI.equals("yes")) {
            System.out.println("Choisissez le niveau de l'IA:");
            System.out.println("1 - Simple   2 - Moyen   3 - Avancé");
            try {
                niveauIA = Integer.parseInt(sc.nextLine().trim());
                if (niveauIA < 1 || niveauIA > 3) {
                    niveauIA = 1; // 默认简单难度
                    System.out.println("Niveau par défaut: Simple");
                }
            } catch (Exception e) {
                niveauIA = 1; // 默认简单难度
                System.out.println("Niveau par défaut: Simple");
            }
        }
        
        while (!etat.estFinDuJeu()) {
            afficherPlateauConsole(etat);
            if (estPlateauPlein(etat)) {
                System.out.println("Match nul !");
                break;
            }
            
            // 如果是AI回合
            if (niveauIA > 0 && etat.getJoueurActuel() == 'O') {
                System.out.println("L'IA réfléchit...");
                long startTime = System.currentTimeMillis();
                int[] move = jouerCoupIA(etat, niveauIA);
                long endTime = System.currentTimeMillis();
                System.out.println("L'IA joue: " + (move[0]+1) + " " + (move[1]+1) + " (temps: " + (endTime-startTime) + "ms)");
                int[] res = jouerCoup(etat, move[0], move[1]);
                if (res[0] == 1) {
                    afficherPlateauConsole(etat);
                    System.out.println("L'IA a gagné !");
                }
                continue;
            }
            
            // 玩家回合
            String joueurName = etat.getJoueurActuel() == 'X' ? j1.getNom() : j2.getNom();
            System.out.print(joueurName + "(" + etat.getJoueurActuel() + ") entrez ligne et colonne (ex: 8 8): ");
            try {
                int l = sc.nextInt() - 1, c = sc.nextInt() - 1;
                
                // 检查输入是否有效
                if (l < 0 || l >= etat.getTaillePlateau() || c < 0 || c >= etat.getTaillePlateau()) {
                    System.out.println("Position hors du plateau, réessayez !");
                    continue;
                }
                if (etat.getPlateau()[l][c] != '.') {
                    System.out.println("Case occupée, réessayez !");
                    continue;
                }
                
                int[] res = jouerCoup(etat, l, c);
                if (res[0] == 1) {
                    afficherPlateauConsole(etat);
                    System.out.println(joueurName + " a gagné !");
                }
            } catch (Exception e) {
                System.out.println("Format invalide, entrez deux nombres séparés par un espace, ex: 8 8");
                sc.nextLine(); // 清除输入缓冲区
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

    /**
     * Méthode spéciale pour faire jouer deux IA l'une contre l'autre et sauvegarder les résultats
     * @param etat État du jeu
     * @param niveauIA1 Niveau de la première IA (joueur X)
     * @param niveauIA2 Niveau de la seconde IA (joueur O)
     * @param nombreParties Nombre de parties à jouer
     * @param nomFichier Nom du fichier pour sauvegarder les résultats
     */
    public static void demarrerTournoiIA(EtatDuJeu etat, int niveauIA1, int niveauIA2, int nombreParties, String nomFichier) {
        int victoires1 = 0;
        int victoires2 = 0;
        int matchNuls = 0;
        long tempsTotal1 = 0;
        long tempsTotal2 = 0;
        int coupsTotaux1 = 0;
        int coupsTotaux2 = 0;
        
        String nomIA1 = getNomIA(niveauIA1);
        String nomIA2 = getNomIA(niveauIA2);
        
        System.out.println("Tournoi entre " + nomIA1 + " (X) et " + nomIA2 + " (O)");
        System.out.println("Nombre de parties: " + nombreParties);
        System.out.println("==========================================");
        
        java.io.PrintWriter writer = null;
        try {
            writer = new java.io.PrintWriter(new java.io.FileWriter(nomFichier));
            writer.println("Résultats du tournoi entre " + nomIA1 + " (X) et " + nomIA2 + " (O)");
            writer.println("Nombre de parties: " + nombreParties);
            writer.println("==========================================");
            
            for (int partie = 1; partie <= nombreParties; partie++) {
                // Réinitialiser le plateau pour chaque partie
                etat = new EtatDuJeu(etat.getTaillePlateau());
                etat.setJoueurActuel('X');
                int coupsIA1 = 0;
                int coupsIA2 = 0;
                
                System.out.println("\nPartie " + partie + ":");
                writer.println("\nPartie " + partie + ":");
                
                // Afficher le plateau initial
                if (partie == 1) {
                    afficherPlateauConsole(etat);
                }
                
                while (!etat.estFinDuJeu()) {
                    if (estPlateauPlein(etat)) {
                        System.out.println("Match nul!");
                        writer.println("Match nul!");
                        matchNuls++;
                        break;
                    }
                    
                    // Déterminer quelle IA joue
                    int niveauIAActuel = etat.getJoueurActuel() == 'X' ? niveauIA1 : niveauIA2;
                    String nomIAActuel = etat.getJoueurActuel() == 'X' ? nomIA1 : nomIA2;
                    
                    System.out.print("Tour de " + nomIAActuel + " (" + etat.getJoueurActuel() + ")... ");
                    writer.print("Tour de " + nomIAActuel + " (" + etat.getJoueurActuel() + ")... ");
                    
                    // Mesurer le temps de réflexion
                    long startTime = System.currentTimeMillis();
                    int[] move = jouerCoupIA(etat, niveauIAActuel);
                    long endTime = System.currentTimeMillis();
                    long tempsPris = endTime - startTime;
                    
                    // Mettre à jour les statistiques
                    if (etat.getJoueurActuel() == 'X') {
                        tempsTotal1 += tempsPris;
                        coupsIA1++;
                    } else {
                        tempsTotal2 += tempsPris;
                        coupsIA2++;
                    }
                    
                    System.out.println("Joue: " + (move[0]+1) + "," + (move[1]+1) + " (temps: " + tempsPris + "ms)");
                    writer.println("Joue: " + (move[0]+1) + "," + (move[1]+1) + " (temps: " + tempsPris + "ms)");
                    
                    // Jouer le coup
                    int[] res = jouerCoup(etat, move[0], move[1]);
                    
                    // Vérifier si la partie est terminée
                    if (res[0] == 1) {
                        String vainqueur = etat.getJoueurActuel() == 'X' ? nomIA2 : nomIA1;
                        char symbolVainqueur = etat.getJoueurActuel() == 'X' ? 'O' : 'X';
                        
                        if (symbolVainqueur == 'X') {
                            victoires1++;
                        } else {
                            victoires2++;
                        }
                        
                        System.out.println("Victoire de " + vainqueur + " (" + symbolVainqueur + ")!");
                        writer.println("Victoire de " + vainqueur + " (" + symbolVainqueur + ")!");
                        
                        if (partie == nombreParties) {
                            afficherPlateauConsole(etat);
                        }
                        break;
                    }
                }
                
                // Ajouter le nombre de coups aux totaux
                coupsTotaux1 += coupsIA1;
                coupsTotaux2 += coupsIA2;
                
                // Afficher des statistiques pour cette partie
                System.out.println("Nombre de coups: " + nomIA1 + "=" + coupsIA1 + ", " + nomIA2 + "=" + coupsIA2);
                writer.println("Nombre de coups: " + nomIA1 + "=" + coupsIA1 + ", " + nomIA2 + "=" + coupsIA2);
            }
            
            // Afficher et enregistrer les statistiques globales
            System.out.println("\n==========================================");
            System.out.println("Résultats finaux:");
            System.out.println(nomIA1 + " (X): " + victoires1 + " victoires");
            System.out.println(nomIA2 + " (O): " + victoires2 + " victoires");
            System.out.println("Matchs nuls: " + matchNuls);
            System.out.println("Temps moyen par coup: " + nomIA1 + "=" + (tempsTotal1/Math.max(1, coupsTotaux1)) + "ms, " 
                    + nomIA2 + "=" + (tempsTotal2/Math.max(1, coupsTotaux2)) + "ms");
            
            writer.println("\n==========================================");
            writer.println("Résultats finaux:");
            writer.println(nomIA1 + " (X): " + victoires1 + " victoires");
            writer.println(nomIA2 + " (O): " + victoires2 + " victoires");
            writer.println("Matchs nuls: " + matchNuls);
            writer.println("Temps moyen par coup: " + nomIA1 + "=" + (tempsTotal1/Math.max(1, coupsTotaux1)) + "ms, " 
                    + nomIA2 + "=" + (tempsTotal2/Math.max(1, coupsTotaux2)) + "ms");
            
            System.out.println("Résultats sauvegardés dans " + nomFichier);
            
        } catch (java.io.IOException e) {
            System.err.println("Erreur lors de l'écriture des résultats: " + e.getMessage());
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
    
    /**
     * Retourne le nom d'une IA selon son niveau
     */
    private static String getNomIA(int niveau) {
        switch (niveau) {
            case 1: return "IA Simple";
            case 2: return "IA MinMax";
            case 3: return "IA Alpha-Beta";
            case 4: return "IA MCTS";
            default: return "IA Inconnue";
        }
    }
}
