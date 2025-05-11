
import java.util.ArrayList;
import java.util.List;

/**
 * Classe représentant l'état du jeu de Gomoku.
 * 
 * Cette classe gère :
 * - Le plateau de jeu
 * - Le joueur actuel
 * - L'état de fin de partie
 * - La taille du plateau
 * 
 * Elle fournit des méthodes pour :
 * - Accéder et modifier l'état du jeu
 * - Vérifier la validité des coups
 * - Gérer le tour des joueurs
 */
public class EtatDuJeu {
    private char[][] plateau;
    private char joueurActuel;
    private boolean finDuJeu;
    private int taillePlateau;

    /**
     * Constructeur de l'état du jeu
     * @param taille Taille du plateau de jeu
     */
    public EtatDuJeu(int taille) {
        this.taillePlateau = taille;
        this.plateau = new char[taille][taille];
        this.joueurActuel = 'X';
        this.finDuJeu = false;
        
        // Initialiser le plateau avec des cases vides
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                plateau[i][j] = '.';
            }
        }
    }

    /**
     * Obtient le plateau de jeu
     * @return Le plateau de jeu
     */
    public char[][] getPlateau() {
        return plateau;
    }

    /**
     * Obtient le joueur actuel
     * @return Le symbole du joueur actuel ('X' ou 'O')
     */
    public char getJoueurActuel() {
        return joueurActuel;
    }

    /**
     * Définit le joueur actuel
     * @param joueurActuel Le symbole du joueur ('X' ou 'O')
     */
    public void setJoueurActuel(char joueurActuel) {
        this.joueurActuel = joueurActuel;
    }

    /**
     * Vérifie si la partie est terminée
     * @return true si la partie est terminée, false sinon
     */
    public boolean estFinDuJeu() {
        return finDuJeu;
    }

    /**
     * Définit l'état de fin de partie
     * @param finDuJeu true si la partie est terminée, false sinon
     */
    public void setFinDuJeu(boolean finDuJeu) {
        this.finDuJeu = finDuJeu;
    }

    /**
     * Obtient la taille du plateau
     * @return La taille du plateau
     */
    public int getTaillePlateau() {
        return taillePlateau;
    }

    /**
     * Vérifie si un coup est valide
     * @param ligne La ligne du coup
     * @param colonne La colonne du coup
     * @return true si le coup est valide, false sinon
     */
    public boolean estCoupValide(int ligne, int colonne) {
        return ligne >= 0 && ligne < taillePlateau && 
               colonne >= 0 && colonne < taillePlateau && 
               plateau[ligne][colonne] == '.';
    }

    public String getJoueurActuelCouleur(char joueur){
        String couleur;
        if (joueurActuel == 'X') {
            couleur = "Noir";
        } else {
            couleur = "Blanc";
        }
        return couleur;
    }
}
