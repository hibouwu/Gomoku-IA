package src;

public class MinMaxAlphaBeta {

    /**
     * 使用Alpha-Beta剪枝的Minimax算法找到最佳落子位置
     * @param etat 当前游戏状态
     * @param profondeur 搜索深度
     * @return 包含[行,列]的最佳落子位置数组
     */
    public int[] trouverMeilleurCoup(EtatDuJeu etat, int profondeur) {
        long startTime = System.currentTimeMillis();
        
        int meilleurScore = Integer.MIN_VALUE;
        int meilleureLigne = -1;
        int meilleureColonne = -1;
        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();
        
        // Alpha-Beta参数
        int alpha = Integer.MIN_VALUE;
        int beta = Integer.MAX_VALUE;

        for (int i = 0; i < taille; i++) {
            for (int j = 0; j < taille; j++) {
                if (plateau[i][j] == '.') {
                    plateau[i][j] = 'O'; // 假设AI是'O'
                    int score = alphaBeta(etat, profondeur, alpha, beta, false); // 从最小化开始（对手回合）
                    plateau[i][j] = '.'; // 撤销走子
                    
                    if (score > meilleurScore) {
                        meilleurScore = score;
                        meilleureLigne = i;
                        meilleureColonne = j;
                        
                        // 更新Alpha值
                        alpha = Math.max(alpha, meilleurScore);
                    }
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        System.out.println("Alpha-Beta recherche: " + (endTime - startTime) + " ms");

        if (meilleureLigne != -1) {
            return new int[]{meilleureLigne, meilleureColonne};
        } else {
            return null; // 没有可能的走法
        }
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
        // 终止条件：达到深度、检测到胜利或棋盘已满
        if (profondeur == 0 || LancerJeu.verifierVictoire(etat, -1, -1) || LancerJeu.estPlateauPlein(etat)) {
            return evaluerPosition(etat);
        }

        int taille = etat.getTaillePlateau();
        char[][] plateau = etat.getPlateau();

        if (estMaximisant) { // AI回合 ('O')
            int meilleurScore = Integer.MIN_VALUE;
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    if (plateau[i][j] == '.') {
                        plateau[i][j] = 'O';
                        char joueurPrecedent = etat.getJoueurActuel(); // 保存状态
                        etat.setJoueurActuel('X');
                        int score = alphaBeta(etat, profondeur - 1, alpha, beta, false);
                        plateau[i][j] = '.'; // 撤销走子
                        etat.setJoueurActuel(joueurPrecedent); // 恢复状态
                        
                        meilleurScore = Math.max(score, meilleurScore);
                        alpha = Math.max(alpha, meilleurScore);
                        
                        // Alpha-Beta剪枝
                        if (beta <= alpha) {
                            break; // Beta剪枝
                        }
                    }
                }
                
                // 如果已经触发剪枝，则退出外层循环
                if (beta <= alpha) {
                    break;
                }
            }
            return meilleurScore;
        } else { // 对手回合 ('X')
            int meilleurScore = Integer.MAX_VALUE;
            for (int i = 0; i < taille; i++) {
                for (int j = 0; j < taille; j++) {
                    if (plateau[i][j] == '.') {
                        plateau[i][j] = 'X';
                        char joueurPrecedent = etat.getJoueurActuel(); // 保存状态
                        etat.setJoueurActuel('O');
                        int score = alphaBeta(etat, profondeur - 1, alpha, beta, true);
                        plateau[i][j] = '.'; // 撤销走子
                        etat.setJoueurActuel(joueurPrecedent); // 恢复状态
                        
                        meilleurScore = Math.min(score, meilleurScore);
                        beta = Math.min(beta, meilleurScore);
                        
                        // Alpha-Beta剪枝
                        if (beta <= alpha) {
                            break; // Alpha剪枝
                        }
                    }
                }
                
                // 如果已经触发剪枝，则退出外层循环
                if (beta <= alpha) {
                    break;
                }
            }
            return meilleurScore;
        }
    }

    /**
     * Évalue la position actuelle du plateau.
     * @param etat L'état du jeu.
     * @return Le score de la position (positif pour IA, négatif pour adversaire).
     */
    private int evaluerPosition(EtatDuJeu etat) {
        int score = 0;
        char[][] plateau = etat.getPlateau();
        int taille = etat.getTaillePlateau();

        // Évaluation basée sur les séquences (simplifiée)
        // Horizontal
        for (int i = 0; i < taille; i++) {
            for (int j = 0; j <= taille - 5; j++) {
                score += evaluerSequence(plateau[i][j], plateau[i][j+1], plateau[i][j+2], plateau[i][j+3], plateau[i][j+4]);
            }
        }
        // Vertical
        for (int i = 0; i <= taille - 5; i++) {
            for (int j = 0; j < taille; j++) {
                score += evaluerSequence(plateau[i][j], plateau[i+1][j], plateau[i+2][j], plateau[i+3][j], plateau[i+4][j]);
            }
        }
        // Diagonal (descendant)
        for (int i = 0; i <= taille - 5; i++) {
            for (int j = 0; j <= taille - 5; j++) {
                score += evaluerSequence(plateau[i][j], plateau[i+1][j+1], plateau[i+2][j+2], plateau[i+3][j+3], plateau[i+4][j+4]);
            }
        }
        // Diagonal (ascendant)
         for (int i = 4; i < taille; i++) {
            for (int j = 0; j <= taille - 5; j++) {
                 score += evaluerSequence(plateau[i][j], plateau[i-1][j+1], plateau[i-2][j+2], plateau[i-3][j+3], plateau[i-4][j+4]);
            }
        }

        return score;
    }

    /**
     * Évalue une séquence de 5 cases.
     * @param c1 Case 1
     * @param c2 Case 2
     * @param c3 Case 3
     * @param c4 Case 4
     * @param c5 Case 5
     * @return Le score de la séquence.
     */
    private int evaluerSequence(char c1, char c2, char c3, char c4, char c5) {
        int score = 0;
        int countO = 0;
        int countX = 0;
        char[] sequence = {c1, c2, c3, c4, c5};

        for (char c : sequence) {
            if (c == 'O') countO++;
            else if (c == 'X') countX++;
        }

        // Priorité aux coups gagnants/perdants immédiats et aux menaces
        if (countO == 5) return 100000; // Victoire IA
        if (countX == 5) return -100000; // Défaite IA
        if (countO == 4 && countX == 0) return 5000; // Forte menace IA
        if (countX == 4 && countO == 0) return -5000; // Forte menace Adversaire
        if (countO == 3 && countX == 0) return 100;   // Menace IA
        if (countX == 3 && countO == 0) return -100;  // Menace Adversaire
        if (countO == 2 && countX == 0) return 10;
        if (countX == 2 && countO == 0) return -10;
        if (countO == 1 && countX == 0) return 1;
        if (countX == 1 && countO == 0) return -1;

        return 0; // Séquence bloquée ou vide
    }
} 