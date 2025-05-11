package src;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * Application principale de Gomoku avec interface graphique.
 * 
 * Cette classe implémente l'interface graphique du jeu en utilisant JavaFX :
 * - Gestion de la fenêtre principale
 * - Affichage du plateau de jeu
 * - Interaction avec les joueurs
 * - Configuration des parties
 * 
 * Fonctionnalités :
 * - Interface graphique moderne et intuitive
 * - Support des joueurs humains et IA
 * - Options de configuration du jeu
 * - Affichage des statistiques
 * - Gestion des événements utilisateur
 * 
 * Cette classe est le point d'entrée principal de l'application
 * et gère toute l'interface utilisateur.
 */
public class GomokuApp extends Application {

    private final int taille = 15;
    private final int tailleCase = 30;
    private final int marge = 20;
    private final int zoneSelection = 12;
    private Canvas canvas;
    private EtatDuJeu etat;
    private Stage primaryStage;
    private boolean modeIA = false;
    private boolean modeIAvsIA = false;
    private int niveauIA = 0;
    private int niveauIA1 = 0;
    private int niveauIA2 = 0;
    private boolean tournoiEnCours = false;
    private PauseTransition iaTimer;
    
    private int currentRound = 0;
    private int totalRounds = 1;
    private int ia1Victories = 0;
    private int ia2Victories = 0;
    private int draws = 0;
    private TextArea infoTextArea;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        primaryStage.setScene(creerSceneMenu());
        primaryStage.setTitle("Menu Gomoku");
        primaryStage.show();
    }

    private Scene creerSceneMenu() {
        VBox menuBox = new VBox(20);
        menuBox.setStyle("-fx-alignment:center; -fx-padding:50; "
                + "-fx-background-color:rgba(245,245,220,0.84);");

        Button btnJcJ = new Button("Jouer contre un joueur");
        Button btnIA = new Button("Jouer contre l'IA");
        Button btnIAvsIA = new Button("Mode IA vs IA");
        
        btnJcJ.setPrefWidth(200);
        btnIA.setPrefWidth(200);
        btnIAvsIA.setPrefWidth(200);
        
        btnJcJ.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white;");
        btnIA.setStyle("-fx-background-color:#2196F3; -fx-text-fill:white;");
        btnIAvsIA.setStyle("-fx-background-color:#9C27B0; -fx-text-fill:white;");

        btnJcJ.setOnAction(e -> { 
            modeIA = false;
            modeIAvsIA = false;
            niveauIA = 0; 
            lancerJeuUI(); 
        });
        
        btnIA.setOnAction(e -> {
            VBox choix = new VBox(20);
            choix.setStyle("-fx-alignment:center; -fx-padding:50; "
                    + "-fx-background-color:rgba(245,245,220,0.84);");
            Button niv1 = new Button("Niveau Simple");
            Button niv2 = new Button("Niveau Medium");
            Button niv3 = new Button("Niveau Avancé");
            Button niv4 = new Button("Niveau MCTS");
            niv1.setPrefWidth(250); niv2.setPrefWidth(250); niv3.setPrefWidth(250); niv4.setPrefWidth(250);
            niv1.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white;");
            niv2.setStyle("-fx-background-color:#2196F3; -fx-text-fill:white;");
            niv3.setStyle("-fx-background-color:#f32171; -fx-text-fill:white;");
            niv4.setStyle("-fx-background-color:#9C27B0; -fx-text-fill:white;");
            niv1.setOnAction(ev -> { modeIA=true; modeIAvsIA=false; niveauIA=1; lancerJeuUI(); });
            niv2.setOnAction(ev -> { modeIA=true; modeIAvsIA=false; niveauIA=2; lancerJeuUI(); });
            niv3.setOnAction(ev -> { modeIA=true; modeIAvsIA=false; niveauIA=3; lancerJeuUI(); });
            niv4.setOnAction(ev -> { modeIA=true; modeIAvsIA=false; niveauIA=4; lancerJeuUI(); });
            choix.getChildren().addAll(niv1, niv2, niv3, niv4);
            primaryStage.setScene(new Scene(choix, 530, 400));
        });
        
        btnIAvsIA.setOnAction(e -> {
            GridPane choix = new GridPane();
            choix.setStyle("-fx-alignment:center; -fx-padding:50; -fx-hgap:10; -fx-vgap:20; "
                    + "-fx-background-color:rgba(245,245,220,0.84);");
            
            Label lblIA1 = new Label("IA 1 (X - Premier joueur):");
            Label lblIA2 = new Label("IA 2 (O - Second joueur):");
            Label lblRounds = new Label("Nombre de parties:");
            
            ComboBox<String> cbIA1 = new ComboBox<>();
            cbIA1.getItems().addAll("IA Simple", "IA MinMax", "IA Alpha-Beta", "IA MCTS");
            cbIA1.setValue("IA Simple");
            
            ComboBox<String> cbIA2 = new ComboBox<>();
            cbIA2.getItems().addAll("IA Simple", "IA MinMax", "IA Alpha-Beta", "IA MCTS");
            cbIA2.setValue("IA Simple");
            
            // Ajout du slider pour le nombre de parties de deux IA
            Slider sliderRounds = new Slider(1, 50, 1);
            sliderRounds.setShowTickMarks(true);
            sliderRounds.setShowTickLabels(true);
            sliderRounds.setMajorTickUnit(10);
            sliderRounds.setMinorTickCount(9);
            sliderRounds.setSnapToTicks(true);
            sliderRounds.setPrefWidth(200);
            
            Label lblRoundValue = new Label("1");
            sliderRounds.valueProperty().addListener((obs, oldVal, newVal) -> {
                lblRoundValue.setText(String.valueOf(newVal.intValue()));
            });
            
            Button btnStart = new Button("Commencer la partie");
            btnStart.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white;");
            btnStart.setPrefWidth(200);
            
            btnStart.setOnAction(ev -> {
                modeIA = false;
                modeIAvsIA = true;
                niveauIA1 = cbIA1.getSelectionModel().getSelectedIndex() + 1;
                niveauIA2 = cbIA2.getSelectionModel().getSelectedIndex() + 1;
                totalRounds = (int) sliderRounds.getValue();
                currentRound = 0;
                ia1Victories = 0;
                ia2Victories = 0;
                draws = 0;
                lancerJeuUI();
            });
            
            choix.add(lblIA1, 0, 0);
            choix.add(cbIA1, 1, 0);
            choix.add(lblIA2, 0, 1);
            choix.add(cbIA2, 1, 1);
            choix.add(lblRounds, 0, 2);
            
            HBox roundsBox = new HBox(10, sliderRounds, lblRoundValue);
            roundsBox.setAlignment(Pos.CENTER_LEFT);
            choix.add(roundsBox, 1, 2);
            
            choix.add(btnStart, 0, 3, 2, 1);
            
            primaryStage.setScene(new Scene(choix, 530, 400));
        });

        menuBox.getChildren().addAll(btnJcJ, btnIA, btnIAvsIA);
        return new Scene(menuBox, 530, 400);
    }

    private void lancerJeuUI() {
        etat = new EtatDuJeu(taille);
        etat.setJoueurActuel('X');
        int size = taille*tailleCase + 2*marge;
        canvas = new Canvas(size, size);
        drawBoard();
        tournoiEnCours = false;
        
        // créer la zone d'affichage des informations
        infoTextArea = new TextArea();
        infoTextArea.setPrefWidth(250);
        infoTextArea.setEditable(false);
        infoTextArea.setWrapText(true);
        updateInfoPanel();

        // événement de clic sur le plateau
        canvas.setOnMouseClicked(e -> {
            if (etat.estFinDuJeu() || tournoiEnCours) return;
            if (modeIA && etat.getJoueurActuel()=='O') return;
            if (modeIAvsIA) return; // si le mode est AI vs AI, les humains ne peuvent pas jouer

            double x = e.getX() - marge, y = e.getY() - marge;
            int col = (int)Math.round(x/ tailleCase),
                    row = (int)Math.round(y/ tailleCase);
            double dx = Math.abs(x - col*tailleCase),
                    dy = Math.abs(y - row*tailleCase);
            if (row<0||row>=taille||col<0||col>=taille
                    || dx>zoneSelection||dy>zoneSelection) return;
            if (etat.getPlateau()[row][col] != '.') return;

            int[] res = LancerJeu.jouerCoup(etat, row, col);
            drawStone(row, col);
            if (res[0] == 1) {
                showAlert("Victoire ! Joueur " + etat.getJoueurActuelCouleur(etat.getJoueurActuel()));
                return;
            } else if (res[0] == 0) {
                showAlert("Match nul !");
                return;
            }

            if (modeIA && etat.getJoueurActuel()=='O') {
                PauseTransition pause = new PauseTransition(Duration.seconds(0.5));
                pause.setOnFinished(evt -> {
                    // Coup de l'IA
                    int[] m = LancerJeu.jouerCoupIA(etat, niveauIA);
                    int[] res2 = LancerJeu.jouerCoup(etat, m[0], m[1]);
                    drawStone(m[0], m[1]);
                    // On reporte la boîte de dialogue sur la file d'attente JavaFX
                    Platform.runLater(() -> {
                        if (res2[0] == 1) showAlert("Victoire IA !");
                        else if (res2[0] == 0) showAlert("Match nul !");
                    });
                });
                pause.play();
            }
        });

        // zone des boutons
        Button btnR = new Button("Recommencer");
        Button btnM = new Button("Retour au menu");
        Button btnS = null;
        
        if (modeIAvsIA) {
            btnS = new Button("Démarrer IA vs IA");
            btnS.setStyle("-fx-background-color:#9C27B0; -fx-text-fill:white;");
        }
        
        btnR.setOnAction(e -> {
            if (iaTimer != null) {
                iaTimer.stop();
                tournoiEnCours = false;
            }
            etat = new EtatDuJeu(taille);
            etat.setJoueurActuel('X');
            drawBoard();
            updateInfoPanel();
        });
        
        btnM.setOnAction(e -> {
            if (iaTimer != null) {
                iaTimer.stop();
                tournoiEnCours = false;
            }
            primaryStage.setScene(creerSceneMenu());
        });
        
        HBox h;
        if (modeIAvsIA) {
            Button finalBtnS = btnS;
            finalBtnS.setOnAction(e -> {
                if (!tournoiEnCours) {
                    tournoiEnCours = true;
                    finalBtnS.setText("Arrêter IA vs IA");
                    startNextRound();
                } else {
                    tournoiEnCours = false;
                    finalBtnS.setText("Démarrer IA vs IA");
                    if (iaTimer != null) {
                        iaTimer.stop();
                    }
                }
            });
            h = new HBox(10, btnR, btnM, finalBtnS);
        } else {
            h = new HBox(10, btnR, btnM);
        }
        
        h.setStyle("-fx-alignment:center;");

        // utiliser BorderPane pour placer le plateau au centre et l'infoTextArea à droite
        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(new HBox(canvas));
        
        if (modeIAvsIA) {
            borderPane.setRight(infoTextArea);
        }
        
        VBox root = new VBox(10, borderPane, h);
        root.setStyle("-fx-alignment:center; -fx-padding:10; -fx-background-color:beige;");

        primaryStage.setTitle("Gomoku");
        primaryStage.setScene(new Scene(root, 850, root.getPrefHeight()));
    }
    
    // mettre à jour le panneau d'informations
    private void updateInfoPanel() {
        if (infoTextArea == null) return;
        
        StringBuilder sb = new StringBuilder();
        sb.append("=========== INFORMATIONS ===========\n\n");
        
        sb.append("IA 1 (X): ").append(LancerJeu.getNomIA(niveauIA1)).append("\n");
        sb.append("IA 2 (O): ").append(LancerJeu.getNomIA(niveauIA2)).append("\n\n");
        
        sb.append("Nombre total de parties: ").append(totalRounds).append("\n");
        sb.append("Partie actuelle: ").append(currentRound).append("/").append(totalRounds).append("\n\n");
        
        sb.append("RÉSULTATS:\n");
        sb.append("Victoires IA 1 (X): ").append(ia1Victories).append("\n");
        sb.append("Victoires IA 2 (O): ").append(ia2Victories).append("\n");
        sb.append("Matchs nuls: ").append(draws).append("\n\n");
        
        if (currentRound > 0) {
            double ia1Rate = (double) ia1Victories / currentRound * 100;
            double ia2Rate = (double) ia2Victories / currentRound * 100;
            double drawRate = (double) draws / currentRound * 100;
            
            sb.append(String.format("Taux de victoire IA 1: %.1f%%\n", ia1Rate));
            sb.append(String.format("Taux de victoire IA 2: %.1f%%\n", ia2Rate));
            sb.append(String.format("Taux de matchs nuls: %.1f%%\n", drawRate));
        }
        
        infoTextArea.setText(sb.toString());
    }
    
    // commencer la prochaine partie
    private void startNextRound() {
        if (!tournoiEnCours) return;
        
        currentRound++;
        if (currentRound > totalRounds) {
            showFinalResults();
            return;
        }
        
        // réinitialiser le plateau
        etat = new EtatDuJeu(taille);
        etat.setJoueurActuel('X');
        drawBoard();
        updateInfoPanel();
        
        // commencer la partie IA vs IA
        jouerPartieIAvsIA();
    }
    
    // afficher les résultats finaux
    private void showFinalResults() {
        tournoiEnCours = false;
        
        // trouver le bouton et mettre à jour le texte
        Scene scene = primaryStage.getScene();
        if (scene.getRoot() instanceof VBox) {
            VBox root = (VBox) scene.getRoot();
            if (root.getChildren().size() > 1 && root.getChildren().get(1) instanceof HBox) {
                HBox btnBox = (HBox) root.getChildren().get(1);
                if (btnBox.getChildren().size() > 2 && btnBox.getChildren().get(2) instanceof Button) {
                    Button btnS = (Button) btnBox.getChildren().get(2);
                    btnS.setText("Démarrer IA vs IA");
                }
            }
        }
        
        // créer le rapport des résultats finaux
        StringBuilder results = new StringBuilder();
        results.append("===== RÉSULTATS FINAUX =====\n\n");
        results.append("IA 1 (X): ").append(LancerJeu.getNomIA(niveauIA1)).append("\n");
        results.append("IA 2 (O): ").append(LancerJeu.getNomIA(niveauIA2)).append("\n\n");
        results.append("Nombre de parties: ").append(totalRounds).append("\n\n");
        results.append("Victoires IA 1 (X): ").append(ia1Victories).append(" (")
                .append(String.format("%.1f%%", (double) ia1Victories / totalRounds * 100)).append(")\n");
        results.append("Victoires IA 2 (O): ").append(ia2Victories).append(" (")
                .append(String.format("%.1f%%", (double) ia2Victories / totalRounds * 100)).append(")\n");
        results.append("Matchs nuls: ").append(draws).append(" (")
                .append(String.format("%.1f%%", (double) draws / totalRounds * 100)).append(")\n\n");
        
        // déterminer le gagnant
        String winner;
        if (ia1Victories > ia2Victories) {
            winner = "IA 1 (" + LancerJeu.getNomIA(niveauIA1) + ")";
        } else if (ia2Victories > ia1Victories) {
            winner = "IA 2 (" + LancerJeu.getNomIA(niveauIA2) + ")";
        } else {
            winner = "ÉGALITÉ";
        }
        
        results.append("GAGNANT FINAL: ").append(winner);
        
        // afficher les résultats
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Résultats du tournoi");
        alert.setHeaderText("Tournoi terminé!");
        alert.setContentText(results.toString());
        alert.showAndWait();
    }
    
    // logique de la partie IA vs IA
    private void jouerPartieIAvsIA() {
        if (!tournoiEnCours || etat.estFinDuJeu()) return;
        
        // déterminer lequel des deux IA doit jouer
        int niveauIAActuel = etat.getJoueurActuel() == 'X' ? niveauIA1 : niveauIA2;
        
        // créer un délai, permettre à l'UI de mettre à jour
        iaTimer = new PauseTransition(Duration.seconds(0.5));
        iaTimer.setOnFinished(evt -> {
            // AI joue
            int[] m = LancerJeu.jouerCoupIA(etat, niveauIAActuel);
            char currentPlayer = etat.getJoueurActuel(); // 保存当前玩家，用于后续判断
            int[] res = LancerJeu.jouerCoup(etat, m[0], m[1]);
            drawStone(m[0], m[1]);
            
            // vérifier si la partie est terminée
            final boolean gameOver = res[0] >= 0;
            if (gameOver) {
                // mettre à jour les informations statistiques
                if (res[0] == 1) {
                    // lorsque res[0]=1, indique que le joueur actuel a gagné
                    if (currentPlayer == 'X') {
                        ia1Victories++;
                    } else {
                        ia2Victories++;
                    }
                    
                    String winner = currentPlayer == 'X' ? "IA 1 (X)" : "IA 2 (O)";
                    updateInfoPanel();
                    
                    // utiliser Platform.runLater pour déplacer l'affichage de la boîte de dialogue sur la file d'attente JavaFX
                    if (currentRound == totalRounds) {
                        Platform.runLater(() -> {
                            showAlert("Victoire de " + winner + " !");
                            startNextRound(); // lancer la prochaine partie ou afficher les résultats finaux
                        });
                    } else {
                        // si ce n'est pas la dernière partie, ne pas afficher la boîte de dialogue, passer à la partie suivante
                        Platform.runLater(() -> {
                            startNextRound();
                        });
                    }
                } else if (res[0] == 0) {
                    draws++;
                    updateInfoPanel();
                    
                    if (currentRound == totalRounds) {
                        Platform.runLater(() -> {
                            showAlert("Match nul !");
                            startNextRound(); // lancer la prochaine partie ou afficher les résultats finaux
                        });
                    } else {
                        // si ce n'est pas la dernière partie, ne pas afficher la boîte de dialogue, passer à la partie suivante
                        Platform.runLater(() -> {
                            startNextRound();
                        });
                    }
                }
                return;
            }
            
            // si la partie n'est pas terminée, continuer à la prochaine partie
            jouerPartieIAvsIA();
        });
        
        iaTimer.play();
    }

    private void drawBoard() {
        GraphicsContext g = canvas.getGraphicsContext2D();
        g.setFill(Color.BEIGE);
        g.fillRect(0,0,canvas.getWidth(),canvas.getHeight());
        g.setStroke(Color.BLACK);
        for (int i=0; i<taille; i++) {
            g.strokeLine(marge, marge+i*tailleCase,
                    marge+(taille-1)*tailleCase, marge+i*tailleCase);
            g.strokeLine(marge+i*tailleCase, marge,
                    marge+i*tailleCase, marge+(taille-1)*tailleCase);
        }
    }

    private void drawStone(int row, int col) {
        GraphicsContext g = canvas.getGraphicsContext2D();
        double x = marge + col*tailleCase,
                y = marge + row*tailleCase,
                r = tailleCase*0.45;
        g.setFill(etat.getPlateau()[row][col]=='X' ? Color.BLACK : Color.WHITE);
        g.fillOval(x-r, y-r, r*2, r*2);
        g.strokeOval(x-r, y-r, r*2, r*2);
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("");
        alert.setHeaderText(null);
        alert.setGraphic(null);
        alert.setContentText(msg);
        
        if (Platform.isFxApplicationThread()) {
        alert.showAndWait();
        } else {
            Platform.runLater(() -> alert.showAndWait());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
