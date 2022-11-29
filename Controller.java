package com.connect4;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import javax.xml.soap.Text;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Controller implements Initializable {
    @FXML
    public GridPane rootGridPane;
    @FXML
    public Pane insertedDiscsPane;
    @FXML
    public Label playerNameLabel;
    @FXML
    public TextField playerOne;
    @FXML
    public TextField playerTwo;
    @FXML
    public Button setNameBtn;

    private boolean isAllowedToInsert = true;
    private static final int column = 7;
    private static final int Row = 6;
    private static final int Circle_Diameter = 80;
    private static final String discColor1 = "#24303E";
    private static final String discColor2 = "#4CAA88";
    private boolean isPlayerOneTurn = true;
    private static String Player_One = "";
    private static String Player_Two = "";

    private Disc[][] insertedDiscArray = new Disc[Row][column];

   public void setNames(){
       setNameBtn.setOnAction(event -> {
           Player_One = playerOne.getText();
           Player_Two = playerTwo.getText();
       });
   }

    public void createPlayGround() {
       setNames();
       Shape rectangleWithHoles = createGameStructure();
        rootGridPane.add(rectangleWithHoles, 0, 1);

        List<Rectangle> rectangleList = createClickableColumn();
        for (Rectangle rectangle: rectangleList){
            rootGridPane.add(rectangle, 0, 1);
        }

    }

    private List<Rectangle> createClickableColumn() {
        List<Rectangle> rectangleList = new ArrayList<>();

        for (int col=0; col<column; col++){
            Rectangle rectangle = new Rectangle(Circle_Diameter, (Row+1)*Circle_Diameter);
            rectangle.setFill(Color.TRANSPARENT);
            rectangle.setTranslateX(col*(Circle_Diameter+8) + Circle_Diameter/4);
            rectangle.setOnMouseEntered(event -> rectangle.setFill(Color.valueOf("#eeeeee26")));
            rectangle.setOnMouseExited(event -> rectangle.setFill(Color.TRANSPARENT));

            final int Column = col;
            rectangle.setOnMouseClicked((MouseEvent event) -> {
                if(isAllowedToInsert) {
                    isAllowedToInsert = false;
                    insertDisc(new Disc(isPlayerOneTurn), Column);
                }
            });

            rectangleList.add(rectangle);
        }
        return rectangleList;
    }
    private  void insertDisc(Disc disc, int Column){
        int row = Row-1;
        while (row>=0){
            if (insertedDiscArray[row][Column] ==null)
                break;
            row--;
        }
        if (row<0)           //if it is full we cannot insert anymore
            return;

        insertedDiscArray[row][Column]= disc;
        insertedDiscsPane.getChildren().add(disc);
        disc.setTranslateX(Column*(Circle_Diameter+8) + Circle_Diameter/4);
        TranslateTransition transition = new TranslateTransition(Duration.seconds(0.5), disc);
        transition.setToY(row*(Circle_Diameter+8) + Circle_Diameter/4);

        int currentRow = row;
        transition.setOnFinished(event -> {

            isAllowedToInsert = true;
            if (gameEnded(currentRow, Column)) {
                gameOver();
                return;
            }
            isPlayerOneTurn =!isPlayerOneTurn;
            playerNameLabel.setText(isPlayerOneTurn?Player_One : Player_Two);



        });

        transition.play();
    }

    private boolean gameEnded(int row, int Column) {
        List<Point2D> verticalPoint = IntStream.rangeClosed(row-3,row+3)
                                      .mapToObj(r->new Point2D(r, Column))
                                        .collect(Collectors.toList());

        List<Point2D> horizontalPoint = IntStream.rangeClosed(Column-3,Column+3)
                .mapToObj(col->new Point2D(row, col))
                .collect(Collectors.toList());

        Point2D startPoint1 = new Point2D(row-3, Column+3);
        List<Point2D> diagonal1Point = IntStream.rangeClosed(0,6)
                                        .mapToObj(i->startPoint1.add(i, -i))
                                        .collect(Collectors.toList());

        Point2D startPoint2 = new Point2D(row-3, Column+3);
        List<Point2D> diagonal2Point = IntStream.rangeClosed(0,6)
                .mapToObj(i->startPoint2.add(i,i))
                .collect(Collectors.toList());

        boolean isEnded = checkCombinations(verticalPoint) || checkCombinations(horizontalPoint)
                          || checkCombinations(diagonal1Point) || checkCombinations(diagonal2Point);

        return isEnded;
    }

    private boolean checkCombinations(List<Point2D> points) {
        int chain = 0;

        for (Point2D point: points){
            int rowIndexForArray = (int) point.getX();
            int columnIndexForArray = (int) point.getY();

            Disc disc = getDiscIfPresent(rowIndexForArray, columnIndexForArray);

            if(disc!= null && disc.isPlayerOneMove == isPlayerOneTurn){
                chain++;
                if(chain==4){
                    return true;
                }
            }else{
                chain = 0;
            }
        }
        return false;
    }

    private Disc getDiscIfPresent(int row, int Column) {
        if (row>=Row || row<0 || Column>=column || Column<0)
            return null;

        return insertedDiscArray[row][Column];
    }
    private void gameOver(){
        String winner = isPlayerOneTurn? Player_One : Player_Two;
        System.out.println("Winner is: "+winner);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Connect Four");
        alert.setHeaderText("The Winner is "+winner);
        alert.setContentText("Do you want to play again?");

        ButtonType yesBtn = new ButtonType("Yes");
        ButtonType noBtn = new ButtonType("No, Exit");
        alert.getButtonTypes().setAll(yesBtn, noBtn);

        Platform.runLater(()->{
            Optional<ButtonType> btnClicked = alert.showAndWait();

            if(btnClicked.isPresent() && btnClicked.get()==yesBtn){
                resetGame();
            }else{
                Platform.exit();
                System.exit(0);
            }
        });
    }
    public void resetGame(){
        insertedDiscsPane.getChildren().clear();

        for (int row=0; row<insertedDiscArray.length; row++){
            for (int col=0; col<insertedDiscArray[row].length; col++){
                insertedDiscArray[row][col]=null;
            }
        }
        isPlayerOneTurn = true;
        playerNameLabel.setText(Player_One);
        createPlayGround();
    }

    private static class Disc extends Circle{
        private final boolean isPlayerOneMove;

        public Disc(boolean isPlayerOneMove) {
            this.isPlayerOneMove = isPlayerOneMove;
            setRadius(Circle_Diameter/2);
            setFill(isPlayerOneMove?Color.valueOf(discColor1): Color.valueOf(discColor2));
            setCenterX(Circle_Diameter/2);
            setCenterY(Circle_Diameter/2);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }


    private Shape createGameStructure() {
        Shape rectangleWithHoles = new Rectangle((column+1)*Circle_Diameter,(Row+1)*Circle_Diameter);

        for (int row=0; row<Row; row++){
            for (int col=0; col<column; col++){
                Circle circle = new Circle();
                circle.setRadius(Circle_Diameter/2);
                circle.setCenterX(Circle_Diameter/2);
                circle.setCenterY(Circle_Diameter/2);
                circle.setSmooth(true);

                circle.setTranslateX(col*(Circle_Diameter+8)+Circle_Diameter/4);
                circle.setTranslateY(row*(Circle_Diameter+8)+Circle_Diameter/4);
                rectangleWithHoles = Shape.subtract(rectangleWithHoles, circle);
            }
        }

        rectangleWithHoles.setFill(Color.WHITE);
        return rectangleWithHoles;
    }
}

