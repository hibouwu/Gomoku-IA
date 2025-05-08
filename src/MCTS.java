package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

/**
 * Implémentation de l'algorithme Monte Carlo Tree Search pour Gomoku
 */
public class MCTS {
    private static final double UCT_CONSTANT = 1.414; // UCB1公式中的常数 (√2更精确)
    private static final int MAX_SIMULATIONS = 10000; // 增加最大模拟次数
    private Random random = new Random();
    private int taille; // 棋盘大小
    private int centre; // 棋盘中心点

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

    /**
     * 寻找最佳落子位置
     */
    public int[] trouverMeilleurCoup(EtatDuJeu etat, int tempsMaxMS) {
        taille = etat.getTaillePlateau();
        centre = taille / 2;
        
        // 如果是第一步，直接落在中心点附近
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
            // 第一步随机选择中心点附近的位置
            int offset = random.nextInt(2); // 0 或 1
            int row = centre;
            int col = centre;
            if (random.nextBoolean()) row += offset;
            else col += offset;
            System.out.println("MCTS: Premier coup, je joue près du centre (" + (row+1) + "," + (col+1) + ")");
            return new int[]{row, col};
        }
        
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
            
            // 进行改进的模拟游戏
            char result = simulateImprovedPlayout(nodeToSimulate);
            
            // 4. 回传：更新节点数据
            backPropagation(nodeToSimulate, result);
            
            simulations++;
        }
        
        // 选择胜率最高的子节点
        Node bestChild = rootNode.getChildWithMaxScore();
        if (bestChild == null) {
            // 如果没有子节点（罕见情况），返回一个合法的启发式走法
            List<int[]> legalMoves = getOrderedMoves(etat);
            if (!legalMoves.isEmpty()) {
                return legalMoves.get(0); // 返回启发式评分最高的走法
            }
            return new int[]{-1, -1}; // 无法找到合法走法，应该不会发生
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("MCTS recherche: " + (endTime - startTime) + " ms, " + 
                           simulations + " simulations, " + 
                           "meilleur coup: " + (bestChild.move[0]+1) + "," + (bestChild.move[1]+1) + 
                           ", taux de victoire: " + String.format("%.2f", bestChild.winScore/bestChild.visits));
        
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
     * 扩展节点：添加未尝试的走法作为新的子节点，使用启发式排序
     */
    private void expandNode(Node node) {
        List<int[]> possibleMoves = node.getUntriedMoves();
        if (possibleMoves.isEmpty()) {
            return;
        }
        
        // 按启发式价值对可能的走法进行排序
        possibleMoves = getOrderedMoves(node.etat);
        // 保留未尝试的走法
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
                break; // 只添加最优的未尝试走法
            }
        }
        
        if (untriedMoves.isEmpty()) return;
        
        // 选择分数最高的未尝试走法
        int[] move = untriedMoves.get(0);
        
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
     * 从节点的子节点中随机选择一个，偏好选择较少访问的节点
     */
    private Node getRandomChildNode(Node node) {
        if (node.children.isEmpty()) {
            return null;
        }
        
        // 使用Roulette Wheel Selection，访问次数越少的节点被选择的概率越高
        double totalInverseVisits = 0;
        for (Node child : node.children) {
            totalInverseVisits += 1.0 / (child.visits + 1); // +1避免除以0
        }
        
        double rand = random.nextDouble() * totalInverseVisits;
        double sum = 0;
        for (Node child : node.children) {
            sum += 1.0 / (child.visits + 1);
            if (sum >= rand) {
                return child;
            }
        }
        
        // 如果因为浮点精度问题没有选中，返回最后一个子节点
        return node.children.get(node.children.size() - 1);
    }

    /**
     * 进行改进的游戏模拟，返回获胜者 'X', 'O' 或 'T'（平局）
     * 使用半随机策略，优先选择有威胁的走法
     */
    private char simulateImprovedPlayout(Node node) {
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
        
        // 最多模拟50步，避免无限循环
        int maxSteps = 50; 
        int steps = 0;
        
        // 半随机落子直到游戏结束
        while (steps < maxSteps) {
            steps++;
            
            // 获取按启发式价值排序的走法
            List<int[]> orderedMoves = getOrderedMoves(tempState);
            if (orderedMoves.isEmpty()) {
                return 'T'; // 平局
            }
            
            // 选择走法：80%概率选择最佳走法，20%概率随机选择
            int[] move;
            if (random.nextDouble() < 0.8) {
                move = orderedMoves.get(0); // 最佳走法
            } else {
                move = orderedMoves.get(random.nextInt(Math.min(3, orderedMoves.size()))); // 随机选择前3个最佳走法中的一个
            }
            
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
        
        // 如果超过最大步数，根据棋盘评估决定结果
        double score = evaluateBoard(tempState);
        if (Math.abs(score) < 100) return 'T'; // 差距不大，平局
        return score > 0 ? 'O' : 'X';
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
            } else {
                char nodePlayer = tempNode.etat.getJoueurActuel();
                // 如果获胜方与当前节点的玩家相反，则当前节点的玩家获胜
                if ((nodePlayer == 'X' && playerWhoWon == 'O') ||
                    (nodePlayer == 'O' && playerWhoWon == 'X')) {
                    tempNode.winScore += 1.0;
                }
            }
            
            tempNode = tempNode.parent;
        }
    }

    /**
     * 计算节点的UCT值，平衡探索与利用
     */
    private double calculateUCT(Node node) {
        if (node.visits == 0) {
            return Double.MAX_VALUE; // 确保未访问的节点会被选择
        }
        
        // 利用项：当前节点的胜率
        double exploitation = node.winScore / node.visits;
        
        // 探索项：偏好访问次数少的节点
        double exploration = UCT_CONSTANT * Math.sqrt(Math.log(node.parent.visits) / node.visits);
        
        // 位置项：偏好中心位置
        double positionBonus = 0;
        if (node.move != null) {
            int distanceToCenter = Math.abs(node.move[0] - centre) + Math.abs(node.move[1] - centre);
            positionBonus = 0.1 * (1.0 - distanceToCenter / (taille - 1)); // 距离中心越近，加分越高
        }
        
        return exploitation + exploration + positionBonus;
    }

    /**
     * 获取按启发式价值排序的走法
     */
    private List<int[]> getOrderedMoves(EtatDuJeu etat) {
        List<int[]> legalMoves = getAllLegalMoves(etat);
        
        // 对走法进行启发式评分
        final char currentPlayer = etat.getJoueurActuel();
        final char[][] plateau = etat.getPlateau();
        
        // 按分数排序走法
        Collections.sort(legalMoves, new Comparator<int[]>() {
            @Override
            public int compare(int[] move1, int[] move2) {
                int score1 = evaluateMove(plateau, move1[0], move1[1], currentPlayer);
                int score2 = evaluateMove(plateau, move2[0], move2[1], currentPlayer);
                return Integer.compare(score2, score1); // 降序排列
            }
        });
        
        return legalMoves;
    }
    
    /**
     * 评估一个落子位置的价值
     */
    private int evaluateMove(char[][] plateau, int row, int col, char player) {
        int score = 0;
        
        // 位置分：优先考虑中心位置
        int distanceToCenter = Math.abs(row - centre) + Math.abs(col - centre);
        score += 10 * (taille - distanceToCenter); // 距离中心越近越好
        
        // 临时模拟落子
        plateau[row][col] = player;
        
        // 计算防守分和进攻分
        char opponent = (player == 'X') ? 'O' : 'X';
        int attackScore = calculatePatternScore(plateau, row, col, player);
        
        // 恢复棋盘
        plateau[row][col] = '.';
        
        // 临时模拟对手落子
        plateau[row][col] = opponent;
        int defenseScore = calculatePatternScore(plateau, row, col, opponent);
        
        // 恢复棋盘
        plateau[row][col] = '.';
        
        // 进攻略重于防守
        score += attackScore * 1.1 + defenseScore;
        
        return score;
    }
    
    /**
     * 计算棋型分数
     */
    private int calculatePatternScore(char[][] plateau, int row, int col, char player) {
        int score = 0;
        int taille = plateau.length;
        
        // 8个方向
        int[][] directions = {
            {0, 1}, {1, 0}, {1, 1}, {1, -1}, 
            {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}
        };
        
        for (int[] dir : directions) {
            int dx = dir[0], dy = dir[1];
            
            // 计算连子数和空位数
            int count = 1; // 当前位置算1个
            int emptyBefore = 0, emptyAfter = 0;
            boolean blockedBefore = false, blockedAfter = false;
            
            // 向前检查
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
            
            // 向后检查
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
            
            // 判断棋型并计分
            if (count >= 5) {
                score += 100000; // 成五
            } else if (count == 4) {
                if (!blockedBefore && !blockedAfter) score += 10000; // 活四
                else if (!blockedBefore || !blockedAfter) score += 1000; // 冲四
            } else if (count == 3) {
                if (!blockedBefore && !blockedAfter) score += 500; // 活三
                else if (!blockedBefore || !blockedAfter) score += 100; // 眠三
            } else if (count == 2) {
                if (!blockedBefore && !blockedAfter) score += 50; // 活二
                else if (!blockedBefore || !blockedAfter) score += 10; // 眠二
            }
        }
        
        return score;
    }
    
    /**
     * 评估棋盘的整体状态
     */
    private double evaluateBoard(EtatDuJeu etat) {
        char[][] plateau = etat.getPlateau();
        double score = 0;
        
        // 检查所有行、列和对角线
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
     * 获取所有合法走法，根据距离最近棋子的远近优化
     */
    private List<int[]> getAllLegalMoves(EtatDuJeu etat) {
        List<int[]> legalMoves = new ArrayList<>();
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        
        // 只考虑已有棋子周围3格内的空位
        boolean hasExistingPieces = false;
        
        // 先找到所有已有棋子
        List<int[]> existingPieces = new ArrayList<>();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] != '.') {
                    existingPieces.add(new int[]{i, j});
                    hasExistingPieces = true;
                }
            }
        }
        
        // 如果棋盘为空，返回中心点附近的走法
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
        
        // 设置为记录每个空位是否已添加到合法走法列表
        boolean[][] added = new boolean[taille][taille];
        
        // 对每个已有棋子，考虑其周围3格内的空位
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
        
        // 如果没有找到合法走法（极端情况），返回所有空位
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