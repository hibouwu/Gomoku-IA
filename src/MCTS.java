package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Implémentation de l'algorithme Monte Carlo Tree Search (MCTS) pour le jeu de Gomoku.
 * 
 * Cette classe utilise l'algorithme MCTS pour trouver le meilleur coup à jouer dans une partie de Gomoku.
 * L'algorithme combine la recherche en arbre avec des simulations Monte Carlo pour évaluer les positions.
 * 
 * Caractéristiques principales :
 * - Utilise la formule UCB1 pour équilibrer l'exploration et l'exploitation
 * - Implémente une simulation améliorée avec une stratégie semi-aléatoire
 * - Optimise la recherche en considérant uniquement les coups pertinents
 * - Utilise des heuristiques pour évaluer les positions et les formations
 * 
 * L'algorithme se compose de quatre étapes principales :
 * 1. Sélection : Sélectionne un nœud prometteur à partir de la racine
 * 2. Expansion : Développe l'arbre en ajoutant un nouveau nœud
 * 3. Simulation : Simule une partie à partir du nœud sélectionné
 * 4. Rétropropagation : Met à jour les statistiques des nœuds visités
 */
public class MCTS extends Joueur {
    private static final double UCT_CONSTANT = 1.414;
    private static final int MAX_SIMULATIONS = 10000;
    private Random random = new Random();
    private int taille; // taille du plateau
    private int centre; // centre du plateau

    /**
     * Constructeur de l'IA MCTS
     * @param nom Le nom de l'IA
     * @param symbole Le symbole utilisé par l'IA ('X' ou 'O')
     */
    public MCTS(String nom, char symbole) {
        super(nom, symbole);
    }

    /**
     * Trouve le meilleur coup à jouer pour l'IA en utilisant l'algorithme MCTS
     * @param etat État actuel du jeu
     * @param tempsMaxMS Temps maximum de recherche en millisecondes
     * @return Tableau contenant [ligne,colonne] du meilleur coup
     */
    public int[] trouverMeilleurCoup(EtatDuJeu etat, int tempsMaxMS) {
        taille = etat.getTaillePlateau();
        centre = taille / 2;
        
        // si c'est la première fois, jouer près du centre
        boolean firstMove = true;
        char[][] plateau = etat.getPlateau();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] != '.') {
                    firstMove = false;
                    break;
                }
            }
            if (!firstMove) break;
        }
        
        if (firstMove) {
            // première fois, choisir une position aléatoire près du centre
            int offset = random.nextInt(2); // 0 ou 1
            int row = centre;
            int col = centre;
            if (random.nextBoolean()) row += offset;
            else col += offset;
            System.out.println("MCTS: Premier coup, je joue près du centre (" + (row+1) + "," + (col+1) + ")");
            return new int[]{row, col};
        }
        
        long startTime = System.currentTimeMillis();
        int simulations = 0;
        
        // racine, représente l'état actuel
        Node rootNode = new Node(etat);
        
        // exécuter autant de simulations que possible dans le temps limite
        while (simulations < MAX_SIMULATIONS && (System.currentTimeMillis() - startTime) < tempsMaxMS) {
            // 1. sélectionner: sélectionner un noeud feuille prometteur à partir de la racine
            Node nodeToExplore = selectPromisingNode(rootNode);
            
            // 2. développer: si le noeud sélectionné n'est pas un état terminal et a des coups non essayés, le développer
            if (!LancerJeu.verifierVictoire(nodeToExplore.etat, -1, -1) && 
                !LancerJeu.estPlateauPlein(nodeToExplore.etat)) {
                expandNode(nodeToExplore);
            }
            
            // 3. simulation: si le noeud sélectionné a des enfants, choisir un enfant aléatoirement; sinon, utiliser le noeud sélectionné
            Node nodeToSimulate = nodeToExplore;
            if (!nodeToExplore.children.isEmpty()) {
                nodeToSimulate = getRandomChildNode(nodeToExplore);
            }
            
            // simulation améliorée
            char result = simulateImprovedPlayout(nodeToSimulate);
            
            // 4. backpropagation: mettre à jour les données du noeud
            backPropagation(nodeToSimulate, result);
            
            simulations++;
        }
        
        // sélectionner le noeud avec le meilleur taux de victoire
        Node bestChild = rootNode.getChildWithMaxScore();
        if (bestChild == null) {
            // si aucun enfant (rarement), retourner un coup légal avec une valeur heuristique
            List<int[]> legalMoves = getOrderedMoves(etat);
            if (!legalMoves.isEmpty()) {
                return legalMoves.get(0); // retourner le coup avec la valeur heuristique la plus élevée
            }
            return new int[]{-1, -1}; // ne devrait pas arriver
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("MCTS recherche: " + (endTime - startTime) + " ms, " + 
                           simulations + " simulations, " + 
                           "meilleur coup: " + (bestChild.move[0]+1) + "," + (bestChild.move[1]+1) + 
                           ", taux de victoire: " + String.format("%.2f", bestChild.winScore/bestChild.visits));
        
        return bestChild.move;
    }

    /**
     * Sélectionne le nœud prometteur à partir de la racine
     * @param rootNode Nœud racine de l'arbre
     * @return Nœud sélectionné pour l'exploration
     */
    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        while (!node.children.isEmpty()) {
            node = node.getChildWithMaxUCT();
        }
        return node;
    }

    /**
     * Développe le nœud en ajoutant un nouveau coup
     * @param node Nœud à développer
     */
    private void expandNode(Node node) {
        List<int[]> possibleMoves = node.getUntriedMoves();
        if (possibleMoves.isEmpty()) {
            return;
        }
        
        // trier les coups possibles par valeur heuristique
        possibleMoves = getOrderedMoves(node.etat);
        // garder les coups non essayés
        List<int[]> untriedMoves = new ArrayList<>();
        for (int[] move : possibleMoves) {
            boolean alreadyTried = false;
            for (Node child : node.children) {
                if (child.move[0] == move[0] && child.move[1] == move[1]) {
                    alreadyTried = true;
                    break;
                }
            }
            if (!alreadyTried) {
                untriedMoves.add(move);
                break; // ajouter uniquement le meilleur coup non essayé
            }
        }
        
        if (untriedMoves.isEmpty()) return;
        
        // sélectionner le coup avec la valeur heuristique la plus élevée
        int[] move = untriedMoves.get(0);
        
        // créer un nouvel état représentant ce coup
        EtatDuJeu newState = deepCopyState(node.etat);
        newState.getPlateau()[move[0]][move[1]] = newState.getJoueurActuel();
        
        // changer le joueur
        char nextPlayer = newState.getJoueurActuel() == 'X' ? 'O' : 'X';
        newState.setJoueurActuel(nextPlayer);
        
        // créer le noeud de cet nouvel état
        Node newNode = new Node(newState, node, move);
        node.children.add(newNode);
    }

    /**
     * Sélectionne un nœud enfant aléatoirement
     * @param node Nœud parent
     * @return Nœud enfant sélectionné
     */
    private Node getRandomChildNode(Node node) {
        if (node.children.isEmpty()) {
            return null;
        }
        
        // utiliser la sélection de Roulette Wheel, les noeuds moins visités ont plus de chances d'être sélectionnés
        double totalInverseVisits = 0;
        for (Node child : node.children) {
            totalInverseVisits += 1.0 / (child.visits + 1); // +1 pour éviter la division par 0
        }
        
        double rand = random.nextDouble() * totalInverseVisits;
        double sum = 0;
        for (Node child : node.children) {
            sum += 1.0 / (child.visits + 1);
            if (sum >= rand) {
                return child;
            }
        }
        
        // si parce que des problèmes de précision flottante n'ont pas été sélectionnés, retourner le dernier enfant
        return node.children.get(node.children.size() - 1);
    }

    /**
     * Simule une partie à partir d'un nœud
     * @param node Nœud de départ pour la simulation
     * @return Symbole du gagnant ('X', 'O' ou 'T' pour match nul)
     */
    private char simulateImprovedPlayout(Node node) {
        // créer une copie profonde de l'état du jeu, pour éviter de modifier le noeud original
        EtatDuJeu tempState = deepCopyState(node.etat);
        
        // vérifier si l'état actuel est terminé
        if (LancerJeu.verifierVictoire(tempState, -1, -1)) {
            // le jeu est terminé, le gagnant est le dernier joueur
            return tempState.getJoueurActuel() == 'X' ? 'O' : 'X'; 
        }
        
        if (LancerJeu.estPlateauPlein(tempState)) {
            return 'T'; // match nul
        }
        
        // simuler au maximum 50 coups, pour éviter les boucles infinies
        int maxSteps = 50; 
        int steps = 0;
        
        // semi-aléatoire, jouer jusqu'à la fin du jeu
        while (steps < maxSteps) {
            steps++;
            
            // obtenir les coups triés par valeur heuristique
            List<int[]> orderedMoves = getOrderedMoves(tempState);
            if (orderedMoves.isEmpty()) {
                return 'T'; // match nul
            }
            
            // sélectionner le coup: 80% de chances de choisir le meilleur coup, 20% de chances de choisir un coup aléatoire
            int[] move;
            if (random.nextDouble() < 0.8) {
                move = orderedMoves.get(0); // meilleur coup
            } else {
                move = orderedMoves.get(random.nextInt(Math.min(3, orderedMoves.size()))); // choisir un coup aléatoire parmi les 3 meilleurs
            }
            
            // exécuter le coup
            tempState.getPlateau()[move[0]][move[1]] = tempState.getJoueurActuel();
            
            // vérifier si le joueur actuel a gagné
            if (LancerJeu.verifierVictoire(tempState, move[0], move[1])) {
                return tempState.getJoueurActuel(); // le joueur actuel a gagné
            }
            
            // vérifier si match nul
            if (LancerJeu.estPlateauPlein(tempState)) {
                return 'T'; // match nul
            }
            
            // changer le joueur
            char nextPlayer = tempState.getJoueurActuel() == 'X' ? 'O' : 'X';
            tempState.setJoueurActuel(nextPlayer);
        }
        
        // si plus de 50 coups, décider en fonction de l'évaluation du plateau
        double score = evaluateBoard(tempState);
        if (Math.abs(score) < 100) return 'T'; // différence pas grande, match nul
        return score > 0 ? 'O' : 'X';
    }

    /**
     * Met à jour les statistiques des nœuds visités
     * @param nodeToExplore Nœud à partir duquel remonter
     * @param playerWhoWon Symbole du gagnant
     */
    private void backPropagation(Node nodeToExplore, char playerWhoWon) {
        Node tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.visits++;
            
            // mettre à jour le score en fonction du gagnant de la simulation
            if (playerWhoWon == 'T') {
                // match nul, donner la moitié des points
                tempNode.winScore += 0.5;
            } else {
                char nodePlayer = tempNode.etat.getJoueurActuel();
                // si le gagnant est le joueur opposé, le joueur actuel a gagné
                if ((nodePlayer == 'X' && playerWhoWon == 'O') ||
                    (nodePlayer == 'O' && playerWhoWon == 'X')) {
                    tempNode.winScore += 1.0;
                }
            }
            
            tempNode = tempNode.parent;
        }
    }

    /**
     * Calcule la valeur UCT d'un nœud
     * @param node Nœud à évaluer
     * @return Valeur UCT du nœud
     */
    private double calculateUCT(Node node) {
        if (node.visits == 0) {
            return Double.MAX_VALUE; // s'assurer que les noeuds non visités seront sélectionnés
        }

        // exploitation : le taux de victoire du noeud
        double exploitation = node.winScore / node.visits;
        
        // exploration : le noeud avec le moins de visites sera privilégié
        double exploration = UCT_CONSTANT * Math.sqrt(Math.log(node.parent.visits) / node.visits);
        
        // position : le noeud avec la position la plus proche du centre sera privilégié
        double positionBonus = 0;
        if (node.move != null) {
            int distanceToCenter = Math.abs(node.move[0] - centre) + Math.abs(node.move[1] - centre);
            positionBonus = 0.1 * (1.0 - distanceToCenter / (taille - 1)); // Plus proche du centre, meilleur bonus
        }
        
        return exploitation + exploration + positionBonus;
    }

    /**
     * Obtient les coups triés par valeur heuristique
     * @param etat État actuel du jeu
     * @return Liste des coups triés
     */
    private List<int[]> getOrderedMoves(EtatDuJeu etat) {
        List<int[]> legalMoves = getAllLegalMoves(etat);
        
        // évaluer les coups par valeur heuristique
        final char currentPlayer = etat.getJoueurActuel();
        final char[][] plateau = etat.getPlateau();
        
        // trier les coups par score
        Collections.sort(legalMoves, new Comparator<int[]>() {
            @Override
            public int compare(int[] move1, int[] move2) {
                int score1 = evaluateMove(plateau, move1[0], move1[1], currentPlayer);
                int score2 = evaluateMove(plateau, move2[0], move2[1], currentPlayer);
                return Integer.compare(score2, score1); // trier par score décroissant
            }
        });
        
        return legalMoves;
    }
    
    /**
     * Évalue la valeur d'un coup
     * @param plateau Plateau de jeu
     * @param row Ligne du coup
     * @param col Colonne du coup
     * @param player Symbole du joueur
     * @return Score du coup
     */
    private int evaluateMove(char[][] plateau, int row, int col, char player) {
        int score = 0;
        
        // position : privilégier la position centrale
        int distanceToCenter = Math.abs(row - centre) + Math.abs(col - centre);
        score += 10 * (taille - distanceToCenter); // plus proche du centre, meilleur
        
        // Simuler temporairement le coup
        plateau[row][col] = player;
        
        // Calculer les scores d'attaque et de défense
        char opponent = (player == 'X') ? 'O' : 'X';
        int attackScore = calculatePatternScore(plateau, row, col, player);
        
        // Restaurer le plateau
        plateau[row][col] = '.';
        
        // Simuler temporairement le coup de l'adversaire
        plateau[row][col] = opponent;
        int defenseScore = calculatePatternScore(plateau, row, col, opponent);
        
        // Restaurer le plateau
        plateau[row][col] = '.';
        
        // L'attaque est légèrement plus importante que la défense
        score += attackScore * 1.1 + defenseScore;
        
        return score;
    }
    
    /**
     * Calcule le score des formations
     * @param plateau Plateau de jeu
     * @param row Ligne de la position
     * @param col Colonne de la position
     * @param player Symbole du joueur
     * @return Score de la formation
     */
    private int calculatePatternScore(char[][] plateau, int row, int col, char player) {
        int score = 0;
        int taille = plateau.length;
        
        // 8 directions
        int[][] directions = {
            {0, 1}, {1, 0}, {1, 1}, {1, -1}, 
            {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}
        };
        
        for (int[] dir : directions) {
            int dx = dir[0], dy = dir[1];
            
            // Compter les pièces consécutives et les cases vides
            int count = 1; // Compter la position actuelle
            int emptyBefore = 0, emptyAfter = 0;
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
                    emptyBefore++;
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
                    emptyAfter++;
                    break;
                } else {
                    blockedAfter = true;
                    break;
                }
            }
            
            // Évaluer la formation et attribuer un score
            if (count >= 5) {
                score += 100000; // Alignement de 5
            } else if (count == 4) {
                if (!blockedBefore && !blockedAfter) score += 10000; // Quatre libres
                else if (!blockedBefore || !blockedAfter) score += 1000; // Quatre bloqué
            } else if (count == 3) {
                if (!blockedBefore && !blockedAfter) score += 500; // Trois libres
                else if (!blockedBefore || !blockedAfter) score += 100; // Trois bloqué
            } else if (count == 2) {
                if (!blockedBefore && !blockedAfter) score += 50; // Deux libres
                else if (!blockedBefore || !blockedAfter) score += 10; // Deux bloqué
            }
        }
        
        return score;
    }
    
    /**
     * Évalue l'état global du plateau
     * @param etat État actuel du jeu
     * @return Score global du plateau
     */
    private double evaluateBoard(EtatDuJeu etat) {
        char[][] plateau = etat.getPlateau();
        double score = 0;
        
        // Vérifier toutes les lignes, colonnes et diagonales
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == 'O') {
                    score += calculatePatternScore(plateau, i, j, 'O');
                } else if (plateau[i][j] == 'X') {
                    score -= calculatePatternScore(plateau, i, j, 'X');
                }
            }
        }
        
        return score;
    }

    /**
     * Obtient tous les coups légaux
     * @param etat État actuel du jeu
     * @return Liste des coups légaux
     */
    private List<int[]> getAllLegalMoves(EtatDuJeu etat) {
        List<int[]> legalMoves = new ArrayList<>();
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        
        // Ne considérer que les cases vides à 3 cases des pièces existantes
        boolean hasExistingPieces = false;
        
        // D'abord trouver toutes les pièces existantes
        List<int[]> existingPieces = new ArrayList<>();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] != '.') {
                    existingPieces.add(new int[]{i, j});
                    hasExistingPieces = true;
                }
            }
        }
        
        // Si le plateau est vide, retourner les coups près du centre
        if (!hasExistingPieces) {
            int c = taille / 2;
            for (int i = c-1; i <= c+1; i++) {
                for (int j = c-1; j <= c+1; j++) {
                    if (i >= 0 && i < taille && j >= 0 && j < taille) {
                        legalMoves.add(new int[]{i, j});
                    }
                }
            }
            return legalMoves;
        }
        
        // Tableau pour marquer les cases déjà ajoutées aux coups légaux
        boolean[][] added = new boolean[taille][taille];
        
        // Pour chaque pièce existante, considérer les cases vides dans un rayon de 3 cases
        for (int[] piece : existingPieces) {
            int row = piece[0], col = piece[1];
            for (int i = Math.max(0, row-3); i <= Math.min(taille-1, row+3); i++) {
                for (int j = Math.max(0, col-3); j <= Math.min(taille-1, col+3); j++) {
                    if (plateau[i][j] == '.' && !added[i][j]) {
                        legalMoves.add(new int[]{i, j});
                        added[i][j] = true;
                    }
                }
            }
        }
        
        // Si aucun coup légal n'est trouvé (cas extrême), retourner toutes les cases vides
        if (legalMoves.isEmpty()) {
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    if (plateau[i][j] == '.') {
                        legalMoves.add(new int[]{i, j});
                    }
                }
            }
        }
        
        return legalMoves;
    }

    /**
     * Crée une copie profonde de l'état du jeu
     * @param originalState État original à copier
     * @return Nouvelle copie de l'état
     */
    private EtatDuJeu deepCopyState(EtatDuJeu originalState) {
        EtatDuJeu newState = new EtatDuJeu(originalState.getTaillePlateau());
        newState.setJoueurActuel(originalState.getJoueurActuel());
        newState.setFinDuJeu(originalState.estFinDuJeu());
        
        // Copier le plateau
        char[][] originalBoard = originalState.getPlateau();
        char[][] newBoard = newState.getPlateau();
        
        for (int i = 0; i < originalState.getTaillePlateau(); i++) {
            for (int j = 0; j < originalState.getTaillePlateau(); j++) {
                newBoard[i][j] = originalBoard[i][j];
            }
        }
        
        return newState;
    }

    /**
     * Représente un nœud dans l'arbre de recherche MCTS
     */
    private class Node {
        EtatDuJeu etat;
        Node parent;
        List<Node> children;
        int visits;
        double winScore;
        int[] move; // [row, col]

        /**
         * Constructeur pour un nœud racine
         * @param etat État du jeu à ce nœud
         */
        public Node(EtatDuJeu etat) {
            this.etat = deepCopyState(etat);
            this.children = new ArrayList<>();
            this.visits = 0;
            this.winScore = 0;
            this.move = null;
        }

        /**
         * Constructeur pour un nœud enfant
         * @param etat État du jeu à ce nœud
         * @param parent Nœud parent
         * @param move Coup menant à ce nœud
         */
        public Node(EtatDuJeu etat, Node parent, int[] move) {
            this.etat = deepCopyState(etat);
            this.parent = parent;
            this.children = new ArrayList<>();
            this.visits = 0;
            this.winScore = 0;
            this.move = move;
        }

        /**
         * Obtient la liste des coups non encore essayés
         * @return Liste des coups non essayés
         */
        public List<int[]> getUntriedMoves() {
            List<int[]> legalMoves = getAllLegalMoves(etat);
            if (children.isEmpty()) {
                return legalMoves;
            }

            List<int[]> triedMoves = new ArrayList<>();
            for (Node child : children) {
                triedMoves.add(child.move);
            }

            List<int[]> untriedMoves = new ArrayList<>();
            for (int[] move : legalMoves) {
                boolean alreadyTried = false;
                for (int[] triedMove : triedMoves) {
                    if (move[0] == triedMove[0] && move[1] == triedMove[1]) {
                        alreadyTried = true;
                        break;
                    }
                }
                if (!alreadyTried) {
                    untriedMoves.add(move);
                }
            }
            return untriedMoves;
        }

        /**
         * Obtient l'enfant avec la meilleure valeur UCT
         * @return Nœud enfant avec la meilleure valeur UCT
         */
        public Node getChildWithMaxUCT() {
            double maxUCT = Double.NEGATIVE_INFINITY;
            Node result = null;

            for (Node child : children) {
                double uctValue = calculateUCT(child);
                if (uctValue > maxUCT) {
                    maxUCT = uctValue;
                    result = child;
                }
            }
            return result;
        }

        /**
         * Obtient l'enfant avec le plus grand nombre de visites
         * @return Nœud enfant le plus visité
         */
        public Node getChildWithMaxVisits() {
            int maxVisits = Integer.MIN_VALUE;
            Node result = null;

            for (Node child : children) {
                if (child.visits > maxVisits) {
                    maxVisits = child.visits;
                    result = child;
                }
            }
            return result;
        }
        
        /**
         * Obtient l'enfant avec le meilleur score
         * @return Nœud enfant avec le meilleur score
         */
        public Node getChildWithMaxScore() {
            double maxScore = Double.NEGATIVE_INFINITY;
            Node result = null;

            for (Node child : children) {
                double winRate = child.visits > 0 ? child.winScore / child.visits : 0;
                if (winRate > maxScore) {
                    maxScore = winRate;
                    result = child;
                }
            }
            return result != null ? result : getChildWithMaxVisits();
        }
    }
} 