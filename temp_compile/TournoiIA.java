public class TournoiIA { 
    public static void main(String[] args) { 
        System.out.println("Bienvenue au tournoi d'IA de Gomoku!"); 
        System.out.println("===================================="); 
        int tailleTableau = 15; 
        EtatDuJeu etat = new EtatDuJeu(tailleTableau); 
        java.util.Scanner scanner = new java.util.Scanner(System.in); 
        System.out.println("\nSélectionnez le niveau de l'IA 1:"); 
        System.out.println("1 - IA Simple (heuristique)"); 
        System.out.println("2 - IA MinMax"); 
        System.out.println("3 - IA Alpha-Beta"); 
        System.out.print("Votre choix (1-3): "); 
        int niveauIA1 = Integer.parseInt(scanner.nextLine().trim()); 
        System.out.println("\nSélectionnez le niveau de l'IA 2:"); 
        System.out.println("1 - IA Simple (heuristique)"); 
        System.out.println("2 - IA MinMax"); 
        System.out.println("3 - IA Alpha-Beta"); 
        System.out.print("Votre choix (1-3): "); 
        int niveauIA2 = Integer.parseInt(scanner.nextLine().trim()); 
        System.out.print("Nombre de parties à jouer: "); 
        int nombreParties = Integer.parseInt(scanner.nextLine().trim()); 
        String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()); 
        String nomFichier = "resultats_" + niveauIA1 + "_vs_" + niveauIA2 + "_" + timestamp + ".txt"; 
        LancerJeu.demarrerTournoiIA(etat, niveauIA1, niveauIA2, nombreParties, nomFichier); 
        scanner.close(); 
    } 
} 
