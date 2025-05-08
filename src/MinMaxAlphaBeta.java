package src;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class MinMaxAlphaBeta {

    // 添加超时控制和更好的设置
    private long startTime;
    private long timeLimit = 9000; // 增加到9秒超时限制
    private boolean timeOut = false;
    private Random random = new Random();
    private int maxSearchDepth = 4; // 最大搜索深度
    private final int WIN_SCORE = 1000000;
    private final int THREAT_SCORE = 20000; // 增加威胁分数
    private boolean isAggressive = true; // 更具攻击性的模式

    // 缓存评估分数，避免重复计算
    private Map<String, Integer> evaluationCache = new HashMap<>();

    /**
     * 使用Alpha-Beta剪枝的Minimax算法找到最佳落子位置
     * @param etat 当前游戏状态
     * @param profondeur 搜索深度
     * @return 包含[行,列]的最佳落子位置数组
     */
    public int[] trouverMeilleurCoup(EtatDuJeu etat, int profondeur) {
        // 清空评估缓存
        evaluationCache.clear();
        
        profondeur = Math.min(profondeur, maxSearchDepth); // 限制最大搜索深度
        startTime = System.currentTimeMillis();
        timeOut = false;
        
        // 检查是否是空棋盘(第一步)
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
        
        // 如果是第一步，在中心区域随机选择一个点
        if (estPremierCoup) {
            int centre = etat.getTaillePlateau() / 2;
            int offset = random.nextInt(2) - 1; // -1, 0, 或 1
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
        
        // Alpha-Beta参数
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        // 获取所有候选走法并按启发式得分排序
        List<int[]> candidatMoves = getOrderedMoves(etat);
        
        // 使用迭代深化搜索，从浅层开始
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
                    plateau[i][j] = 'O'; // 假设AI是'O'
                    char joueurPrecedent = etat.getJoueurActuel();
                    etat.setJoueurActuel('X');
                    
                    int score = alphaBeta(etat, currentDepth - 1, alpha, beta, false); // 从最小化开始（对手回合）
                    
                    plateau[i][j] = '.'; // 撤销走子
                    etat.setJoueurActuel(joueurPrecedent);
                    
                    if (score > currentBestScore) {
                        currentBestScore = score;
                        currentBestRow = i;
                        currentBestCol = j;
                        
                        // 更新Alpha值
                        alpha = Math.max(alpha, currentBestScore);
                    }
                    
                    // 检查是否超时
                    if (timeOut) {
                        System.out.println("Alpha-Beta: Timeout at depth " + currentDepth);
                        break;
                    }
                    
                    // 如果找到了必胜走法，立即返回
                    if (score >= WIN_SCORE) {
                        System.out.println("Alpha-Beta: Found winning move at depth " + currentDepth);
                        return new int[]{i, j};
                    }
                }
            }
            
            // 更新当前深度的最佳走法
            if (currentBestRow != -1 && !timeOut) {
                meilleurScore = currentBestScore;
                meilleureLigne = currentBestRow;
                meilleureColonne = currentBestCol;
                
                // 将最佳走法移到列表前面
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
            // 如果超时或其他原因，返回候选走法中的第一个
            if (!candidatMoves.isEmpty()) {
                return candidatMoves.get(0);
            }
            
            // 最后的备选方案，找一个空位
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    if (plateau[i][j] == '.') {
                        return new int[]{i, j};
                    }
                }
            }
            return null; // 这不应该发生
        }
    }
    
    /**
     * 获取所有候选走法并手动排序
     */
    private List<int[]> getOrderedMoves(EtatDuJeu etat) {
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        List<int[]> moves = new ArrayList<>();
        
        // 只考虑已有棋子附近3格内的空位
        boolean hasNeighbor = false;
        boolean[][] considered = new boolean[taille][taille];
        
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] != '.') {
                    // 考虑周围3格内的空位
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
        
        // 如果棋盘上没有棋子或没有找到合适的位置
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
        
        // 手动对走法按分数排序
        List<MoveScore> scoredMoves = new ArrayList<>();
        for (int[] move : moves) {
            int score = evaluateMoveScore(etat, move[0], move[1], 'O');
            scoredMoves.add(new MoveScore(move, score));
        }
        
        // 稳定排序
        Collections.sort(scoredMoves, new Comparator<MoveScore>() {
            @Override
            public int compare(MoveScore m1, MoveScore m2) {
                if (m1.score != m2.score) {
                    return Integer.compare(m2.score, m1.score); // 降序
                }
                if (m1.move[0] != m2.move[0]) {
                    return Integer.compare(m1.move[0], m2.move[0]); // 行升序
                }
                return Integer.compare(m1.move[1], m2.move[1]); // 列升序
            }
        });
        
        // 提取排序后的走法
        List<int[]> orderedMoves = new ArrayList<>();
        for (MoveScore ms : scoredMoves) {
            orderedMoves.add(ms.move);
        }
        
        return orderedMoves;
    }
    
    // 内部类，用于走法评分排序
    private class MoveScore {
        int[] move;
        int score;
        
        MoveScore(int[] move, int score) {
            this.move = move;
            this.score = score;
        }
    }
    
    /**
     * 评估某个位置的走法得分
     */
    private int evaluateMoveScore(EtatDuJeu etat, int row, int col, char player) {
        // 缓存键
        String key = row + "," + col + "," + player;
        if (evaluationCache.containsKey(key)) {
            return evaluationCache.get(key);
        }
        
        int score = 0;
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();
        char opponent = (player == 'X') ? 'O' : 'X';
        
        // 先模拟落子
        plateau[row][col] = player;
        
        // 检查是否连五
        if (checkWin(plateau, row, col, taille)) {
            score += WIN_SCORE;
        }
        
        // 计算棋形分数
        score += calculatePatternScore(plateau, row, col, player, taille);
        
        // 撤销落子
        plateau[row][col] = '.';
        
        // 模拟对手落子，计算防守分数
        plateau[row][col] = opponent;
        if (checkWin(plateau, row, col, taille)) {
            score += WIN_SCORE / 2; // 优先阻止对手连五
        }
        
        // 在攻击模式下，对防守的权重降低
        double defenseWeight = isAggressive ? 0.6 : 0.8;
        score += calculatePatternScore(plateau, row, col, opponent, taille) * defenseWeight;
        
        // 恢复空位
        plateau[row][col] = '.';
        
        // 位置加分，越靠近中心越好
        int centre = taille / 2;
        int distanceToCenter = Math.abs(row - centre) + Math.abs(col - centre);
        score += (taille - distanceToCenter) * 2;
        
        // 缓存结果
        evaluationCache.put(key, score);
        
        return score;
    }
    
    /**
     * 检查从指定位置是否构成五连
     */
    private boolean checkWin(char[][] plateau, int row, int col, int taille) {
        char player = plateau[row][col];
        if (player == '.') return false;
        
        // 8个方向
        int[][] directions = {
            {0, 1}, {1, 0}, {1, 1}, {1, -1},
            {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}
        };
        
        for (int[] dir : directions) {
            int dx = dir[0], dy = dir[1];
            int count = 1; // 当前位置算1个
            
            // 向正方向检查
            for (int i = 1; i < 5; i++) {
                int nx = row + i * dx, ny = col + i * dy;
                if (nx < 0 || nx >= taille || ny < 0 || ny >= taille || plateau[nx][ny] != player) {
                    break;
                }
                count++;
            }
            
            // 向反方向检查
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
     * 计算棋形分数
     */
    private int calculatePatternScore(char[][] plateau, int row, int col, char player, int taille) {
        int score = 0;
        
        // 8个方向
        int[][] directions = {
            {0, 1}, {1, 0}, {1, 1}, {1, -1},
            {0, -1}, {-1, 0}, {-1, -1}, {-1, 1}
        };
        
        for (int[] dir : directions) {
            int dx = dir[0], dy = dir[1];
            
            // 计算连子数和空位
            int count = 1; // 当前位置算1个
            int openEnds = 0; // 开放端数
            boolean blockedBefore = false, blockedAfter = false;
            
            // 向正方向检查
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
            
            // 向反方向检查
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
            
            // 根据棋形评分，更积极的评分
            if (count >= 5) {
                score += WIN_SCORE; // 连五
            } else if (count == 4) {
                if (openEnds == 2) score += THREAT_SCORE * 10; // 活四
                else if (openEnds == 1) score += THREAT_SCORE; // 冲四
            } else if (count == 3) {
                if (openEnds == 2) score += 1500; // 活三，增加分数
                else if (openEnds == 1) score += 150; // 眠三，增加分数
            } else if (count == 2) {
                if (openEnds == 2) score += 70; // 活二，增加分数
                else if (openEnds == 1) score += 15; // 眠二，增加分数
            } else if (count == 1) {
                if (openEnds == 2) score += 5; // 活一，增加分数
            }
        }
        
        return score;
    }

    /**
     * Alpha-Beta剪枝算法的递归实现
     * @param etat 当前游戏状态
     * @param profondeur 剩余搜索深度
     * @param alpha Alpha值
     * @param beta Beta值
     * @param estMaximisant 是否为最大化玩家回合（AI）
     * @return 搜索分支的评分
     */
    private int alphaBeta(EtatDuJeu etat, int profondeur, int alpha, int beta, boolean estMaximisant) {
        // 检查是否超时
        if (System.currentTimeMillis() - startTime > timeLimit) {
            timeOut = true;
            return 0; // 返回中性分数
        }
        
        // 终止条件
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        
        // 检查是否有胜者
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] != '.') {
                    if (checkWin(plateau, i, j, taille)) {
                        // 根据胜者返回分数
                        if (plateau[i][j] == 'O') { // AI赢
                            return WIN_SCORE + profondeur * 100; // 越早赢越好，增加深度奖励
                        } else { // 对手赢
                            return -WIN_SCORE - profondeur * 100;
                        }
                    }
                }
            }
        }
        
        // 到达最大深度或者棋盘已满
        if (profondeur == 0 || LancerJeu.estPlateauPlein(etat)) {
            return evaluerPosition(etat);
        }
        
        // 获取下一步可能的走法
        List<int[]> moves = new ArrayList<>();
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == '.') {
                    moves.add(new int[]{i, j});
                }
            }
        }
        
        // 限制搜索的走法数量，以提高效率
        if (moves.size() > 15) {
            // 手动排序走法
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
            
            // 稳定排序
            Collections.sort(scoredMoves, new Comparator<MoveScore>() {
                @Override
                public int compare(MoveScore m1, MoveScore m2) {
                    if (m1.score != m2.score) {
                        return Integer.compare(m2.score, m1.score); // 降序
                    }
                    if (m1.move[0] != m2.move[0]) {
                        return Integer.compare(m1.move[0], m2.move[0]); // 行升序
                    }
                    return Integer.compare(m1.move[1], m2.move[1]); // 列升序
                }
            });
            
            // 只考虑最好的15个走法
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
     * 评估当前棋盘状态
     */
    private int evaluerPosition(EtatDuJeu etat) {
        // 缓存键
        String key = boardToString(etat.getPlateau());
        if (evaluationCache.containsKey(key)) {
            return evaluationCache.get(key);
        }
        
        int score = 0;
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();
        
        // 对每个位置进行评估
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == 'O') { // AI
                    score += calculatePatternScore(plateau, i, j, 'O', taille);
                } else if (plateau[i][j] == 'X') { // 对手
                    // 在攻击模式下，对对手的评分降低一点权重
                    double opponentWeight = isAggressive ? 0.9 : 1.0;
                    score -= calculatePatternScore(plateau, i, j, 'X', taille) * opponentWeight;
                }
            }
        }
        
        // 缓存结果
        evaluationCache.put(key, score);
        
        return score;
    }
    
    /**
     * 将棋盘转换为字符串，用作缓存键
     */
    private String boardToString(char[][] plateau) {
        StringBuilder sb = new StringBuilder();
        for (char[] row : plateau) {
            sb.append(new String(row));
        }
        return sb.toString();
    }
} 