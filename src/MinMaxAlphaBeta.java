package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Implémentation de l'algorithme Minimax avec élagage Alpha-Beta pour le jeu de Gomoku.
 * 
 * Cette classe utilise l'algorithme Minimax amélioré avec élagage Alpha-Beta pour trouver le meilleur coup à jouer.
 * L'algorithme combine la recherche en profondeur avec des techniques d'optimisation pour évaluer les positions.
 * 
 * Caractéristiques principales :
 * - Utilise l'élagage Alpha-Beta pour réduire l'espace de recherche
 * - Implémente une recherche itérative progressive
 * - Utilise un cache d'évaluation pour éviter les calculs redondants
 * - Optimise la recherche en triant les coups candidats
 * - Inclut des heuristiques avancées pour l'évaluation des positions
 * 
 * Optimisations :
 * - Limite de temps pour éviter les dépassements
 * - Profondeur de recherche maximale configurable
 * - Mode agressif/défensif ajustable
 * - Évaluation des formations avec scores pondérés
 * - Prise en compte de la position centrale
 */
public class MinMaxAlphaBeta extends Joueur {

    // Contrôle du timeout et meilleurs paramètres
    private long startTime;
    private long timeLimit = 9000; // Augmentation de la limite de timeout à 9 secondes
    private boolean timeOut = false;
    private Random random = new Random();
    private int maxSearchDepth = 4; // Profondeur de recherche maximale
    private final int WIN_SCORE = 1000000;
    private final int THREAT_SCORE = 20000; // Augmentation du score de menace
    private boolean isAggressive = true; // Mode plus agressif

    // Cache d'évaluation pour éviter les calculs redondants
    private Map<String, Integer> evaluationCache = new HashMap<>();

    /**
     * Constructeur de l'IA MinMax avec Alpha-Beta
     * @param nom Le nom de l'IA
     * @param symbole Le symbole utilisé par l'IA ('X' ou 'O')
     */
    public MinMaxAlphaBeta(String nom, char symbole) {
        super(nom, symbole);
    }

    /**
     * Trouve la meilleure position en utilisant l'algorithme Minimax avec élagage Alpha-Beta
     * @param etat État actuel du jeu
     * @param profondeur Profondeur de recherche
     * @return Tableau contenant [ligne,colonne] de la meilleure position
     */
    public int[] trouverMeilleurCoup(EtatDuJeu etat, int profondeur) {
        // Vider le cache d'évaluation
        evaluationCache.clear();
        
        profondeur = Math.min(profondeur, maxSearchDepth); // Limiter la profondeur maximale
        startTime = System.currentTimeMillis();
        timeOut = false;
        
        // Vérifier si c'est le premier coup (plateau vide)
        boolean estPremierCoup = true;
        for (int i = 0; i < etat.getTaillePlateau(); i++) {
            for (int j = 0; j < etat.getTaillePlateau(); j++) {
                if (etat.getPlateau()[i][j] != '.') {
                    estPremierCoup = false;
                    break;
                }
            }
            if (!estPremierCoup) break;
        }
        
        // Si c'est le premier coup, choisir un point aléatoire près du centre
        if (estPremierCoup) {
            int centre = etat.getTaillePlateau() / 2;
            int offset = random.nextInt(2) - 1; // -1, 0, ou 1
            int x = centre + offset;
            int y = centre + (offset != 0 ? 0 : (random.nextBoolean() ? 1 : -1));
            System.out.println("Alpha-Beta: Premier coup, je joue près du centre (" + (x+1) + "," + (y+1) + ")");
            return new int[]{x, y};
        }
        
        int meilleurScore = Integer.MIN_VALUE;
        int meilleureLigne = -1;
        int meilleureColonne = -1;
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        
        // Paramètres Alpha-Beta
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // Obtenir tous les coups candidats et les trier par score heuristique
        List<int[]> candidatMoves = getOrderedMoves(etat);
        
        // Utiliser la recherche itérative progressive, en commençant par une profondeur faible
        for (int currentDepth = 2; currentDepth <= profondeur; currentDepth++) {
            if (timeOut) break;
            
            int currentBestScore = Integer.MIN_VALUE;
            int currentBestRow = -1;
            int currentBestCol = -1;
            alpha = Integer.MIN_VALUE;
            beta = Integer.MAX_VALUE;
            
            for (int[] move : candidatMoves) {
                int i = move[0];
                int j = move[1];
                
                if (plateau[i][j] == '.') {
                    plateau[i][j] = 'O'; // Supposer que l'IA est 'O'
                    char joueurPrecedent = etat.getJoueurActuel();
                    etat.setJoueurActuel('X');
                    
                    int score = alphaBeta(etat, currentDepth - 1, alpha, beta, false); // Commencer par la minimisation (tour de l'adversaire)
                    
                    plateau[i][j] = '.'; // Annuler le coup
                    etat.setJoueurActuel(joueurPrecedent);
                    
                    if (score > currentBestScore) {
                        currentBestScore = score;
                        currentBestRow = i;
                        currentBestCol = j;
                        
                        // Mettre à jour la valeur Alpha
                        alpha = Math.max(alpha, currentBestScore);
                    }
                    
                    // Vérifier le timeout
                    if (timeOut) {
                        System.out.println("Alpha-Beta: Timeout à la profondeur " + currentDepth);
                        break;
                    }
                    
                    // Si un coup gagnant est trouvé, le retourner immédiatement
                    if (score >= WIN_SCORE) {
                        System.out.println("Alpha-Beta: Coup gagnant trouvé à la profondeur " + currentDepth);
                        return new int[]{i, j};
                    }
                }
            }
            
            // Mettre à jour le meilleur coup de la profondeur actuelle
            if (currentBestRow != -1 && !timeOut) {
                meilleurScore = currentBestScore;
                meilleureLigne = currentBestRow;
                meilleureColonne = currentBestCol;
                
                // Déplacer le meilleur coup au début de la liste
                for (int i = 0; i < candidatMoves.size(); i++) {
                    int[] move = candidatMoves.get(i);
                    if (move[0] == currentBestRow && move[1] == currentBestCol) {
                        if (i > 0) {
                            candidatMoves.remove(i);
                            candidatMoves.add(0, move);
                        }
                        break;
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Alpha-Beta recherche: " + (endTime - startTime) + " ms, score: " + meilleurScore);

        if (meilleureLigne != -1) {
            return new int[]{meilleureLigne, meilleureColonne};
        } else {
            // Si timeout ou autre raison, retourner le premier coup candidat
            if (!candidatMoves.isEmpty()) {
                return candidatMoves.get(0);
            }
            
            // Dernier recours, trouver une case vide
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    if (plateau[i][j] == '.') {
                        return new int[]{i, j};
                    }
                }
            }
            return null; // Ne devrait pas arriver
        }
    }
    
    /**
     * Obtient tous les coups candidats et les trie manuellement
     */
    private List<int[]> getOrderedMoves(EtatDuJeu etat) {
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        List<int[]> moves = new ArrayList<>();
        
        // Ne considérer que les cases vides à 3 cases des pièces existantes
        boolean hasNeighbor = false;
        boolean[][] considered = new boolean[taille][taille];
        
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] != '.') {
                    // Considérer les cases vides dans un rayon de 3 cases
                    for (int di = -3; di <= 3; di++) {
                        for (int dj = -3; dj <= 3; dj++) {
                            int ni = i + di;
                            int nj = j + dj;
                            if (ni >= 0 && ni < taille && nj >= 0 && nj < taille &&
                                    !considered[ni][nj] && plateau[ni][nj] == '.') {
                                moves.add(new int[]{ni, nj});
                                considered[ni][nj] = true;
                                hasNeighbor = true;
                            }
                        }
                    }
                }
            }
        }
        
        // Si le plateau est vide ou si aucune position appropriée n'est trouvée
        if (!hasNeighbor) {
            int centre = taille / 2;
            moves.add(new int[]{centre, centre});
            for (int d = -1; d <= 1; d++) {
                for (int d2 = -1; d2 <= 1; d2++) {
                    if (d == 0 && d2 == 0) continue;
                    int ni = centre + d;
                    int nj = centre + d2;
                    if (ni >= 0 && ni < taille && nj >= 0 && nj < taille && plateau[ni][nj] == '.') {
                        moves.add(new int[]{ni, nj});
                    }
                }
            }
        }
        
        // Trier manuellement les coups par score
        List<MoveScore> scoredMoves = new ArrayList<>();
        for (int[] move : moves) {
            int score = evaluateMoveScore(etat, move[0], move[1], 'O');
            scoredMoves.add(new MoveScore(move, score));
        }
        
        // Tri stable
        Collections.sort(scoredMoves, new Comparator<MoveScore>() {
            @Override
            public int compare(MoveScore m1, MoveScore m2) {
                if (m1.score != m2.score) {
                    return Integer.compare(m2.score, m1.score); // Ordre décroissant
                }
                if (m1.move[0] != m2.move[0]) {
                    return Integer.compare(m1.move[0], m2.move[0]); // Ordre croissant des lignes
                }
                return Integer.compare(m1.move[1], m2.move[1]); // Ordre croissant des colonnes
            }
        });
        
        // Extraire les coups triés
        List<int[]> orderedMoves = new ArrayList<>();
        for (MoveScore ms : scoredMoves) {
            orderedMoves.add(ms.move);
        }
        
        return orderedMoves;
    }
    
    /**
     * Classe utilitaire pour stocker et trier les coups avec leurs scores.
     * 
     * Cette classe est utilisée pour :
     * - Associer un coup (position) avec son score d'évaluation
     * - Faciliter le tri des coups par score
     * - Maintenir l'ordre stable lors du tri
     * 
     * Les coups sont triés principalement par score, puis par position
     * pour assurer un ordre déterministe des coups de même score.
     */
    private class MoveScore {
        int[] move;
        int score;
        
        MoveScore(int[] move, int score) {
            this.move = move;
            this.score = score;
        }
    }
    
    /**
     * Évalue le score d'un coup à une position donnée
     */
    private int evaluateMoveScore(EtatDuJeu etat, int row, int col, char player) {
        // Clé de cache
        String key = row + "," + col + "," + player;
        if (evaluationCache.containsKey(key)) {
            return evaluationCache.get(key);
        }
        
        int score = 0;
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();
        char opponent = (player == 'X') ? 'O' : 'X';
        
        // Simuler d'abord le coup
        plateau[row][col] = player;
        
        // Vérifier l'alignement de 5
        if (checkWin(plateau, row, col, taille)) {
            score += WIN_SCORE;
        }
        
        // Calculer le score des formations
        score += calculatePatternScore(plateau, row, col, player, taille);
        
        // Annuler le coup
        plateau[row][col] = '.';
        
        // Simuler le coup de l'adversaire, calculer le score défensif
        plateau[row][col] = opponent;
        if (checkWin(plateau, row, col, taille)) {
            score += WIN_SCORE / 2; // Priorité à bloquer l'alignement de 5 de l'adversaire
        }
        
        // En mode attaque, réduire le poids de la défense
        double defenseWeight = isAggressive ? 0.6 : 0.8;
        score += calculatePatternScore(plateau, row, col, opponent, taille) * defenseWeight;
        
        // Restaurer la case vide
        plateau[row][col] = '.';
        
        // Bonus de position, meilleur près du centre
        int centre = taille / 2;
        int distanceToCenter = Math.abs(row - centre) + Math.abs(col - centre);
        score += (taille - distanceToCenter) * 2;
        
        // Mettre en cache le résultat
        evaluationCache.put(key, score);
        
        return score;
    }
    
    /**
     * Vérifie si un alignement de 5 est formé à partir de la position donnée
     */
    private boolean checkWin(char[][] plateau, int row, int col, int taille) {
        char player = plateau[row][col];
        if (player == '.') return false;
        
        // 8 directions
        int[][] directions = {
            {0, 1}, {1, 0}, {1, 1}, {1, -1},
            {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}
        };
        
        for (int[] dir : directions) {
            int dx = dir[0], dy = dir[1];
            int count = 1; // Compter la position actuelle
            
            // Vérifier dans la direction positive
            for (int i = 1; i < 5; i++) {
                int nx = row + i * dx, ny = col + i * dy;
                if (nx < 0 || nx >= taille || ny < 0 || ny >= taille || plateau[nx][ny] != player) {
                    break;
                }
                count++;
            }
            
            // Vérifier dans la direction négative
            for (int i = 1; i < 5; i++) {
                int nx = row - i * dx, ny = col - i * dy;
                if (nx < 0 || nx >= taille || ny < 0 || ny >= taille || plateau[nx][ny] != player) {
                    break;
                }
                count++;
            }
            
            if (count >= 5) return true;
        }
        
        return false;
    }
    
    /**
     * Calcule le score des formations
     */
    private int calculatePatternScore(char[][] plateau, int row, int col, char player, int taille) {
        int score = 0;
        
        // 8 directions
        int[][] directions = {
            {0, 1}, {1, 0}, {1, 1}, {1, -1},
            {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}
        };
        
        for (int[] dir : directions) {
            int dx = dir[0], dy = dir[1];
            
            // Compter les pièces consécutives et les extrémités libres
            int count = 1; // Compter la position actuelle
            int openEnds = 0; // Nombre d'extrémités libres
            boolean blockedBefore = false, blockedAfter = false;
            
            // Vérifier dans la direction positive
            for (int i = 1; i <= 4; i++) {
                int nx = row + i * dx, ny = col + i * dy;
                if (nx < 0 || nx >= taille || ny < 0 || ny >= taille) {
                    blockedBefore = true;
                    break;
                }
                
                if (plateau[nx][ny] == player) {
                    count++;
                } else if (plateau[nx][ny] == '.') {
                    openEnds++;
                    break;
                } else {
                    blockedBefore = true;
                    break;
                }
            }
            
            // Vérifier dans la direction négative
            for (int i = 1; i <= 4; i++) {
                int nx = row - i * dx, ny = col - i * dy;
                if (nx < 0 || nx >= taille || ny < 0 || ny >= taille) {
                    blockedAfter = true;
                    break;
                }
                
                if (plateau[nx][ny] == player) {
                    count++;
                } else if (plateau[nx][ny] == '.') {
                    openEnds++;
                    break;
                } else {
                    blockedAfter = true;
                    break;
                }
            }
            
            // Évaluer la formation selon le score, évaluation plus agressive
            if (count >= 5) {
                score += WIN_SCORE; // Alignement de 5
            } else if (count == 4) {
                if (openEnds == 2) score += THREAT_SCORE * 10; // Quatre libres
                else if (openEnds == 1) score += THREAT_SCORE; // Quatre bloqué
            } else if (count == 3) {
                if (openEnds == 2) score += 1500; // Trois libres, score augmenté
                else if (openEnds == 1) score += 150; // Trois bloqué, score augmenté
            } else if (count == 2) {
                if (openEnds == 2) score += 70; // Deux libres, score augmenté
                else if (openEnds == 1) score += 15; // Deux bloqué, score augmenté
            } else if (count == 1) {
                if (openEnds == 2) score += 5; // Un libre, score augmenté
            }
        }
        
        return score;
    }

    /**
     * Implémentation récursive de l'algorithme Alpha-Beta
     * @param etat État actuel du jeu
     * @param profondeur Profondeur de recherche restante
     * @param alpha Valeur Alpha
     * @param beta Valeur Beta
     * @param estMaximisant Si c'est le tour du joueur maximisant (IA)
     * @return Score de la branche de recherche
     */
    private int alphaBeta(EtatDuJeu etat, int profondeur, int alpha, int beta, boolean estMaximisant) {
        // Vérifier le timeout
        if (System.currentTimeMillis() - startTime > timeLimit) {
            timeOut = true;
            return 0; // Retourner un score neutre
        }
        
        // Conditions d'arrêt
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        
        // Vérifier s'il y a un gagnant
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] != '.') {
                    if (checkWin(plateau, i, j, taille)) {
                        // Retourner le score selon le gagnant
                        if (plateau[i][j] == 'O') { // IA gagne
                            return WIN_SCORE + profondeur * 100; // Mieux vaut gagner plus tôt, bonus de profondeur
                        } else { // Adversaire gagne
                            return -WIN_SCORE - profondeur * 100;
                        }
                    }
                }
            }
        }
        
        // Atteint la profondeur maximale ou plateau plein
        if (profondeur == 0 || LancerJeu.estPlateauPlein(etat)) {
            return evaluerPosition(etat);
        }
        
        // Obtenir les coups possibles suivants
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == '.') {
                    moves.add(new int[]{i, j});
                }
            }
        }
        
        // Limiter le nombre de coups à explorer pour améliorer l'efficacité
        if (moves.size() > 15) {
            // Trier manuellement les coups
            List<MoveScore> scoredMoves = new ArrayList<>();
            for (int[] move : moves) {
                int score;
                if (estMaximisant) {
                    score = evaluateMoveScore(etat, move[0], move[1], 'O');
                } else {
                    score = evaluateMoveScore(etat, move[0], move[1], 'X');
                }
                scoredMoves.add(new MoveScore(move, score));
            }
            
            // Tri stable
            Collections.sort(scoredMoves, new Comparator<MoveScore>() {
                @Override
                public int compare(MoveScore m1, MoveScore m2) {
                    if (m1.score != m2.score) {
                        return Integer.compare(m2.score, m1.score); // Ordre décroissant
                    }
                    if (m1.move[0] != m2.move[0]) {
                        return Integer.compare(m1.move[0], m2.move[0]); // Ordre croissant des lignes
                    }
                    return Integer.compare(m1.move[1], m2.move[1]); // Ordre croissant des colonnes
                }
            });
            
            // Ne considérer que les 15 meilleurs coups
            moves.clear();
            for (int i = 0; i < Math.min(15, scoredMoves.size()); i++) {
                moves.add(scoredMoves.get(i).move);
            }
        }
        
        if (estMaximisant) {
            int maxScore = Integer.MIN_VALUE;
            for (int[] move : moves) {
                int i = move[0], j = move[1];
                plateau[i][j] = 'O';
                char joueurPrecedent = etat.getJoueurActuel();
                etat.setJoueurActuel('X');
                
                int score = alphaBeta(etat, profondeur - 1, alpha, beta, false);
                
                plateau[i][j] = '.';
                etat.setJoueurActuel(joueurPrecedent);
                
                maxScore = Math.max(score, maxScore);
                alpha = Math.max(alpha, maxScore);
                
                if (beta <= alpha || timeOut) {
                    break;
                }
            }
            return maxScore;
        } else {
            int minScore = Integer.MAX_VALUE;
            for (int[] move : moves) {
                int i = move[0], j = move[1];
                plateau[i][j] = 'X';
                char joueurPrecedent = etat.getJoueurActuel();
                etat.setJoueurActuel('O');
                
                int score = alphaBeta(etat, profondeur - 1, alpha, beta, true);
                
                plateau[i][j] = '.';
                etat.setJoueurActuel(joueurPrecedent);
                
                minScore = Math.min(score, minScore);
                beta = Math.min(beta, minScore);
                
                if (beta <= alpha || timeOut) {
                    break;
                }
            }
            return minScore;
        }
    }

    /**
     * Évalue l'état actuel du plateau
     */
    private int evaluerPosition(EtatDuJeu etat) {
        // Clé de cache
        String key = boardToString(etat.getPlateau());
        if (evaluationCache.containsKey(key)) {
            return evaluationCache.get(key);
        }
        
        int score = 0;
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();
        
        // Évaluer chaque position
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == 'O') { // IA
                    score += calculatePatternScore(plateau, i, j, 'O', taille);
                } else if (plateau[i][j] == 'X') { // Adversaire
                    // En mode attaque, réduire légèrement le poids de l'évaluation de l'adversaire
                    double opponentWeight = isAggressive ? 0.9 : 1.0;
                    score -= calculatePatternScore(plateau, i, j, 'X', taille) * opponentWeight;
                }
            }
        }
        
        // Mettre en cache le résultat
        evaluationCache.put(key, score);
        
        return score;
    }
    
    /**
     * Convertit le plateau en chaîne de caractères pour la clé de cache
     */
    private String boardToString(char[][] plateau) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : plateau) {
            sb.append(new String(row));
        }
        return sb.toString();
    }
} 