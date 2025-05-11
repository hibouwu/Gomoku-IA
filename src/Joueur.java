
/**
 * Classe abstraite définissant le comportement d'un joueur dans le jeu de Gomoku.
 * 
 * Cette classe abstraite spécifie :
 * - Les attributs communs à tous les joueurs (nom, symbole, score)
 * - Les méthodes abstraites que chaque joueur doit implémenter
 * - Les méthodes de base pour gérer les informations du joueur
 * 
 * Elle est étendue par :
 * - Les joueurs humains (via l'interface graphique)
 * - Les différentes implémentations d'IA
 * - Les joueurs de test
 */
public abstract class Joueur {
    // Nom du joueur
    private String nom;

    // Symbole utilisé par le joueur (ex: 'X' ou 'O')
    private char symbole;

    // Score du joueur
    private int score;

    /**
     * Constructeur de la classe Joueur
     * @param nom Le nom du joueur
     * @param symbole Le symbole du joueur (ex: 'X' ou 'O')
     */
    public Joueur(String nom, char symbole) {
        this.nom = nom;
        this.symbole = symbole;
        this.score = 0;
    }

    // --------------------- Getters & Setters ---------------------

    public String getNom() {
        return nom;
    }

    public char getSymbole() {
        return symbole;
    }

    public int getScore() {
        return score;
    }

    public void incrementerScore() {
        this.score++;
    }

    public void setScore(int score) {
        this.score = score;
    }
}
