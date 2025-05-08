

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Implémentation de l'algorithme Monte Carlo Tree Search pour Gomoku
 */
public class MCTS {
    private static final double UCT_CONSTANT = Math.sqrt(2); // UCB1公式中的常数
    private static final int MAX_SIMULATIONS = 1000; // 最大模拟次数
    private Random random = new Random();

    /**
     * 代表MCTS搜索树中的节点
     */
    private class Node {
        EtatDuJeu etat;
        Node parent;
        List<Node> children;
        int visits;
        double winScore;
        int[] move; // [row, col]

        public Node(EtatDuJeu etat) {
            this.etat = deepCopyState(etat);
            this.children = new ArrayList<>();
            this.visits = 0;
            this.winScore = 0;
            this.move = null;
        }

        public Node(EtatDuJeu etat, Node parent, int[] move) {
            this.etat = deepCopyState(etat);
            this.parent = parent;
            this.children = new ArrayList<>();
            this.visits = 0;
            this.winScore = 0;
            this.move = move;
        }

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
    }

    /**
     * 寻找最佳落子位置
     */
    public int[] trouverMeilleurCoup(EtatDuJeu etat, int tempsMaxMS) {
        long startTime = System.currentTimeMillis();
        int simulations = 0;
        
        // 根节点，代表当前状态
        Node rootNode = new Node(etat);
        
        // 在时间限制内执行尽可能多的模拟
        while (simulations < MAX_SIMULATIONS && (System.currentTimeMillis() - startTime) < tempsMaxMS) {
            // 1. 选择：从根节点选择一个最有前途的叶子节点
            Node nodeToExplore = selectPromisingNode(rootNode);
            
            // 2. 扩展：如果选择的节点不是终止状态且有未尝试的走法，则扩展它
            if (!LancerJeu.verifierVictoire(nodeToExplore.etat, -1, -1) && 
                !LancerJeu.estPlateauPlein(nodeToExplore.etat)) {
                expandNode(nodeToExplore);
            }
            
            // 3. 模拟：如果有子节点，从中随机选择一个；否则使用已选择的节点
            Node nodeToSimulate = nodeToExplore;
            if (!nodeToExplore.children.isEmpty()) {
                nodeToSimulate = getRandomChildNode(nodeToExplore);
            }
            
            // 进行随机游戏模拟
            char result = simulateRandomPlayout(nodeToSimulate);
            
            // 4. 回传：更新节点数据
            backPropagation(nodeToSimulate, result);
            
            simulations++;
        }
        
        // 选择访问次数最多的子节点
        Node bestChild = rootNode.getChildWithMaxVisits();
        if (bestChild == null) {
            // 如果没有子节点（罕见情况），返回一个合法的随机走法
            List<int[]> legalMoves = getAllLegalMoves(etat);
            if (!legalMoves.isEmpty()) {
                return legalMoves.get(random.nextInt(legalMoves.size()));
            }
            return new int[]{-1, -1}; // 无法找到合法走法，应该不会发生
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("MCTS recherche: " + (endTime - startTime) + " ms, " + 
                           simulations + " simulations, " + 
                           "meilleur coup: " + (bestChild.move[0]+1) + "," + (bestChild.move[1]+1));
        
        return bestChild.move;
    }

    /**
     * 选择最有前途的节点
     */
    private Node selectPromisingNode(Node rootNode) {
        Node node = rootNode;
        while (!node.children.isEmpty()) {
            node = node.getChildWithMaxUCT();
        }
        return node;
    }

    /**
     * 扩展节点：添加一个未尝试的走法作为新的子节点
     */
    private void expandNode(Node node) {
        List<int[]> possibleMoves = node.getUntriedMoves();
        if (possibleMoves.isEmpty()) {
            return;
        }
        
        int[] move = possibleMoves.get(random.nextInt(possibleMoves.size()));
        
        // 创建一个代表这个走法的新状态
        EtatDuJeu newState = deepCopyState(node.etat);
        newState.getPlateau()[move[0]][move[1]] = newState.getJoueurActuel();
        
        // 更改玩家
        char nextPlayer = newState.getJoueurActuel() == 'X' ? 'O' : 'X';
        newState.setJoueurActuel(nextPlayer);
        
        // 创建这个新状态的节点
        Node newNode = new Node(newState, node, move);
        node.children.add(newNode);
    }

    /**
     * 从节点的子节点中随机选择一个
     */
    private Node getRandomChildNode(Node node) {
        if (node.children.isEmpty()) {
            return null;
        }
        return node.children.get(random.nextInt(node.children.size()));
    }

    /**
     * 进行随机游戏模拟，返回获胜者 'X', 'O' 或 'T'（平局）
     */
    private char simulateRandomPlayout(Node node) {
        // 创建游戏状态的深拷贝，避免修改原始节点
        EtatDuJeu tempState = deepCopyState(node.etat);
        
        // 检查当前状态是否已经结束
        if (LancerJeu.verifierVictoire(tempState, -1, -1)) {
            // 游戏已经结束，获胜者是上一个玩家
            return tempState.getJoueurActuel() == 'X' ? 'O' : 'X'; 
        }
        
        if (LancerJeu.estPlateauPlein(tempState)) {
            return 'T'; // 平局
        }
        
        // 随机落子直到游戏结束
        while (true) {
            // 获取所有合法走法
            List<int[]> legalMoves = getAllLegalMoves(tempState);
            if (legalMoves.isEmpty()) {
                return 'T'; // 平局
            }
            
            // 随机选择一个走法
            int[] move = legalMoves.get(random.nextInt(legalMoves.size()));
            
            // 执行走法
            tempState.getPlateau()[move[0]][move[1]] = tempState.getJoueurActuel();
            
            // 检查是否获胜
            if (LancerJeu.verifierVictoire(tempState, move[0], move[1])) {
                return tempState.getJoueurActuel(); // 当前玩家获胜
            }
            
            // 检查是否平局
            if (LancerJeu.estPlateauPlein(tempState)) {
                return 'T'; // 平局
            }
            
            // 切换玩家
            char nextPlayer = tempState.getJoueurActuel() == 'X' ? 'O' : 'X';
            tempState.setJoueurActuel(nextPlayer);
        }
    }

    /**
     * 从叶子节点向上回传结果
     */
    private void backPropagation(Node nodeToExplore, char playerWhoWon) {
        Node tempNode = nodeToExplore;
        while (tempNode != null) {
            tempNode.visits++;
            
            // 根据模拟的获胜者来更新分数
            if (playerWhoWon == 'T') {
                // 平局给一半的分数
                tempNode.winScore += 0.5;
            } else if (playerWhoWon == 'O') { // 假设MCTS为'O'
                // 如果当前节点的玩家是'X'，那么'O'获胜是不利的
                if (tempNode.etat.getJoueurActuel() == 'X') {
                    tempNode.winScore += 1;
                }
            } else if (playerWhoWon == 'X') { // 假设对手为'X'
                // 如果当前节点的玩家是'O'，那么'X'获胜是不利的
                if (tempNode.etat.getJoueurActuel() == 'O') {
                    tempNode.winScore += 1;
                }
            }
            
            tempNode = tempNode.parent;
        }
    }

    /**
     * 计算节点的UCT值
     */
    private double calculateUCT(Node node) {
        if (node.visits == 0) {
            return Double.MAX_VALUE; // 确保未访问的节点会被选择
        }
        
        return (node.winScore / node.visits) + 
               UCT_CONSTANT * Math.sqrt(Math.log(node.parent.visits) / node.visits);
    }

    /**
     * 获取所有合法走法
     */
    private List<int[]> getAllLegalMoves(EtatDuJeu etat) {
        List<int[]> legalMoves = new ArrayList<>();
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == '.') {
                    legalMoves.add(new int[]{i, j});
                }
            }
        }
        
        return legalMoves;
    }

    /**
     * 创建游戏状态的深拷贝
     */
    private EtatDuJeu deepCopyState(EtatDuJeu originalState) {
        EtatDuJeu newState = new EtatDuJeu(originalState.getTaillePlateau());
        newState.setJoueurActuel(originalState.getJoueurActuel());
        newState.setFinDuJeu(originalState.estFinDuJeu());
        
        // 复制棋盘
        char[][] originalBoard = originalState.getPlateau();
        char[][] newBoard = newState.getPlateau();
        
        for (int i = 0; i < originalState.getTaillePlateau(); i++) {
            for (int j = 0; j < originalState.getTaillePlateau(); j++) {
                newBoard[i][j] = originalBoard[i][j];
            }
        }
        
        return newState;
    }
} 
