package src;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente l'état actuel d'une partie de Gomoku.
 * 
 * Cette classe maintient :
 * - Le plateau de jeu (grille)
 * - Le joueur actuel
 * - L'état de fin de partie
 * - La taille du plateau
 * 
 * Elle fournit les méthodes pour :
 * - Accéder et modifier l'état du jeu
 * - Vérifier la validité des coups
 * - Gérer le changement de joueur
 * - Contrôler l'état de fin de partie
 */
public class EtatDuJeu {
    // Taille du plateau
    private int taillePlateau;

    // Plateau de jeu (matrice 2D de caractères)
    private char[][] plateau;

    // Joueur courant (par exemple 'X' ou 'O')
    private char joueurActuel;

    // Indicateur pour la fin du jeu
    private boolean finDuJeu;

    /**
     * Constructeur de la classe EtatDuJeu
     * @param taillePlateau La taille (dimension) du plateau (ex: 15 pour un 15x15)
     */
    public EtatDuJeu(int taillePlateau) {
        this.taillePlateau = taillePlateau;
        this.plateau = new char[taillePlateau][taillePlateau];
        this.joueurActuel = 'X'; // Par défaut, on peut mettre 'X'
        this.finDuJeu = false;
        initialiserPlateau();
    }

    /**
     * Initialise chaque case du plateau à '.'
     */
    private void initialiserPlateau() {
        for (int i = 0; i < taillePlateau; i++) {
            for (int j = 0; j < taillePlateau; j++) {
                plateau[i][j] = '.';
            }
        }
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
    // --------------------- Getters & Setters ---------------------

    public int getTaillePlateau() {
        return taillePlateau;
    }

    public char[][] getPlateau() {
        return plateau;
    }

    public char getJoueurActuel() {
        return joueurActuel;
    }

    public void setJoueurActuel(char joueurActuel) {
        this.joueurActuel = joueurActuel;
    }

    public boolean estFinDuJeu() {
        return finDuJeu;
    }

    public void setFinDuJeu(boolean finDuJeu) {
        this.finDuJeu = finDuJeu;
    }
}
