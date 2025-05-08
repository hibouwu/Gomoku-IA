package src;

import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ConsoleGameLauncher {
    
    public static void main(String[] args) {
        System.out.println("Bienvenue au tournoi d'IA de Gomoku!");
        System.out.println("====================================");
        
        // 创建游戏状态
        int tailleTableau = 15; // 15x15棋盘
        EtatDuJeu etat = new EtatDuJeu(tailleTableau);
        
        // 让用户选择两个AI并配置比赛
        Scanner scanner = new Scanner(System.in);
        
        // 选择第一个AI (X)
        int niveauIA1 = selectionnerIA(scanner, 1);
        
        // 选择第二个AI (O)
        int niveauIA2 = selectionnerIA(scanner, 2);
        
        // 选择比赛次数
        System.out.print("Nombre de parties à jouer: ");
        int nombreParties = 0;
        try {
            nombreParties = Integer.parseInt(scanner.nextLine().trim());
            if (nombreParties <= 0) {
                nombreParties = 10; // 默认10场比赛
                System.out.println("Valeur invalide, nombre de parties fixé à 10.");
            }
        } catch (Exception e) {
            nombreParties = 10;
            System.out.println("Valeur invalide, nombre de parties fixé à 10.");
        }
        
        // 生成结果文件名
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String nomFichier = "resultats_" + getNomIA(niveauIA1) + "_vs_" + getNomIA(niveauIA2) + "_" + timestamp + ".txt";
        nomFichier = nomFichier.replace(' ', '_').toLowerCase();
        
        System.out.println("\nDémarrage du tournoi...");
        System.out.println("Les résultats seront sauvegardés dans: " + nomFichier);
        System.out.println();
        
        // 开始比赛
        LancerJeu.demarrerTournoiIA(etat, niveauIA1, niveauIA2, nombreParties, nomFichier);
        
        scanner.close();
    }
    
    private static int selectionnerIA(Scanner scanner, int numero) {
        System.out.println("\nSélectionnez le niveau de l'IA " + numero + ":");
        System.out.println("1 - IA Simple (heuristique)");
        System.out.println("2 - IA MinMax");
        System.out.println("3 - IA Alpha-Beta");
        
        int niveau = 0;
        while (niveau < 1 || niveau > 3) {
            System.out.print("Votre choix (1-3): ");
            try {
                niveau = Integer.parseInt(scanner.nextLine().trim());
                if (niveau < 1 || niveau > 3) {
                    System.out.println("Choix invalide, veuillez entrer un nombre entre 1 et 3.");
                }
            } catch (Exception e) {
                System.out.println("Entrée invalide, veuillez entrer un nombre.");
            }
        }
        
        return niveau;
    }
    
    private static String getNomIA(int niveau) {
        switch (niveau) {
            case 1: return "IA Simple";
            case 2: return "IA MinMax";
            case 3: return "IA Alpha-Beta";
            default: return "IA Inconnue";
        }
    }
} 