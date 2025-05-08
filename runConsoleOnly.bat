@echo off
set JAVA_HOME=D:\JDK\jdk-23.0.1
set PATH=%JAVA_HOME%\bin;%PATH%

echo Création d'un répertoire temporaire pour la compilation...
mkdir temp_compile 2>nul
cd temp_compile

echo Copie des fichiers nécessaires...
copy ..\src\EtatDuJeu.java .
copy ..\src\Joueur.java .
copy ..\src\IAHeuristiqueSimple.java .
copy ..\src\MinMaxBasique.java .
copy ..\src\MinMaxAlphaBeta.java .
copy ..\src\LancerJeu.java .

echo Création d'un lanceur sans package...
echo public class TournoiIA { > TournoiIA.java
echo     public static void main(String[] args) { >> TournoiIA.java
echo         System.out.println("Bienvenue au tournoi d'IA de Gomoku!"); >> TournoiIA.java
echo         System.out.println("===================================="); >> TournoiIA.java
echo         int tailleTableau = 15; >> TournoiIA.java
echo         EtatDuJeu etat = new EtatDuJeu(tailleTableau); >> TournoiIA.java
echo         java.util.Scanner scanner = new java.util.Scanner(System.in); >> TournoiIA.java
echo         System.out.println("\nSélectionnez le niveau de l'IA 1:"); >> TournoiIA.java
echo         System.out.println("1 - IA Simple (heuristique)"); >> TournoiIA.java
echo         System.out.println("2 - IA MinMax"); >> TournoiIA.java
echo         System.out.println("3 - IA Alpha-Beta"); >> TournoiIA.java
echo         System.out.print("Votre choix (1-3): "); >> TournoiIA.java
echo         int niveauIA1 = Integer.parseInt(scanner.nextLine().trim()); >> TournoiIA.java
echo         System.out.println("\nSélectionnez le niveau de l'IA 2:"); >> TournoiIA.java
echo         System.out.println("1 - IA Simple (heuristique)"); >> TournoiIA.java
echo         System.out.println("2 - IA MinMax"); >> TournoiIA.java
echo         System.out.println("3 - IA Alpha-Beta"); >> TournoiIA.java
echo         System.out.print("Votre choix (1-3): "); >> TournoiIA.java
echo         int niveauIA2 = Integer.parseInt(scanner.nextLine().trim()); >> TournoiIA.java
echo         System.out.print("Nombre de parties à jouer: "); >> TournoiIA.java
echo         int nombreParties = Integer.parseInt(scanner.nextLine().trim()); >> TournoiIA.java
echo         String timestamp = new java.text.SimpleDateFormat("yyyyMMdd_HHmmss").format(new java.util.Date()); >> TournoiIA.java
echo         String nomFichier = "resultats_" + niveauIA1 + "_vs_" + niveauIA2 + "_" + timestamp + ".txt"; >> TournoiIA.java
echo         LancerJeu.demarrerTournoiIA(etat, niveauIA1, niveauIA2, nombreParties, nomFichier); >> TournoiIA.java
echo         scanner.close(); >> TournoiIA.java
echo     } >> TournoiIA.java
echo } >> TournoiIA.java

echo Suppression des références de package dans les fichiers...
powershell -Command "(Get-Content EtatDuJeu.java) -replace 'package src;', '' | Set-Content EtatDuJeu.java"
powershell -Command "(Get-Content Joueur.java) -replace 'package src;', '' | Set-Content Joueur.java"
powershell -Command "(Get-Content IAHeuristiqueSimple.java) -replace 'package src;', '' | Set-Content IAHeuristiqueSimple.java"
powershell -Command "(Get-Content MinMaxBasique.java) -replace 'package src;', '' | Set-Content MinMaxBasique.java"
powershell -Command "(Get-Content MinMaxAlphaBeta.java) -replace 'package src;', '' | Set-Content MinMaxAlphaBeta.java"
powershell -Command "(Get-Content LancerJeu.java) -replace 'package src;', '' | Set-Content LancerJeu.java"

echo Compilation des fichiers sans package...
javac *.java
if %errorlevel% neq 0 goto error

echo Exécution du tournoi...
java TournoiIA
goto end

:error
echo Une erreur s'est produite pendant la compilation.
echo Veuillez vérifier les messages d'erreur ci-dessus.

:end
cd ..
pause 