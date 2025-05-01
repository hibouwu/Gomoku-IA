package src;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GomokuApp extends Application {

    private final int taille = 15;
    private final int tailleCase = 30;
    private final int marge = 20;
    private final int zoneSelection = 12;
    private Canvas canvas;
    private EtatDuJeu etat;
    private Stage primaryStage;
    private boolean modeIA = false;
    private int niveauIA = 0;

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
        Button btnIA  = new Button("Jouer contre l'IA");
        btnJcJ.setPrefWidth(200);
        btnIA .setPrefWidth(200);
        btnJcJ.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white;");
        btnIA .setStyle("-fx-background-color:#2196F3; -fx-text-fill:white;");

        btnJcJ.setOnAction(e -> { modeIA=false; niveauIA=0; lancerJeuUI(); });
        btnIA .setOnAction(e -> {
            VBox choix = new VBox(20);
            choix.setStyle("-fx-alignment:center; -fx-padding:50; "
                    + "-fx-background-color:rgba(245,245,220,0.84);");
            Button niv1 = new Button("Niveau Simple");
            Button niv2 = new Button("Niveau Avancé");
            niv1.setPrefWidth(250); niv2.setPrefWidth(250);
            niv1.setStyle("-fx-background-color:#4CAF50; -fx-text-fill:white;");
            niv2.setStyle("-fx-background-color:#2196F3; -fx-text-fill:white;");
            niv1.setOnAction(ev -> { modeIA=true; niveauIA=1; lancerJeuUI(); });
            niv2.setOnAction(ev -> {
                Alert a = new Alert(AlertType.INFORMATION,
                        "Le niveau avancé n'est pas encore disponible.");
                a.showAndWait();
            });
            choix.getChildren().addAll(niv1, niv2);
            primaryStage.setScene(new Scene(choix, 530, 400));
        });

        menuBox.getChildren().addAll(btnJcJ, btnIA);
        return new Scene(menuBox, 530, 400);
    }

    private void lancerJeuUI() {
        etat = new EtatDuJeu(taille);
        etat.setJoueurActuel('X');
        int size = taille*tailleCase + 2*marge;
        canvas = new Canvas(size, size);
        drawBoard();

        canvas.setOnMouseClicked(e -> {
            if (etat.estFinDuJeu()) return;
            if (modeIA && etat.getJoueurActuel()=='O') return;

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
                showAlert("Victoire ! Joueur " + etat.getJoueurActuel());
                return;
            } else if (res[0] == 0) {
                showAlert("Match nul !");
                return;
            }

            if (modeIA && etat.getJoueurActuel()=='O') {
                Platform.runLater(() -> {
                    int[] m = LancerJeu.jouerCoupIA(etat, niveauIA);
                    int[] res2 = LancerJeu.jouerCoup(etat, m[0], m[1]);
                    drawStone(m[0], m[1]);
                    if (res2[0]==1) showAlert("Victoire IA !");
                    else if (res2[0]==0) showAlert("Match nul !");
                });
            }
        });

        Button btnR = new Button("Recommencer");
        Button btnM = new Button("Retour au menu");
        btnR.setOnAction(e -> {
            etat = new EtatDuJeu(taille);
            etat.setJoueurActuel('X');
            drawBoard();
        });
        btnM.setOnAction(e -> primaryStage.setScene(creerSceneMenu()));

        HBox h = new HBox(10, btnR, btnM);
        h.setStyle("-fx-alignment:center;");

        VBox root = new VBox(10, new HBox(canvas), h);
        root.setStyle("-fx-alignment:center; -fx-padding:10; -fx-background-color:beige;");

        primaryStage.setTitle("Gomoku");
        primaryStage.setScene(new Scene(root, 600, root.getPrefHeight()));
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
        new Alert(AlertType.INFORMATION, msg).showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
