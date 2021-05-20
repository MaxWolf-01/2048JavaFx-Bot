package game;

import game.bot.GameSolver;
import game.bot.botMoveTask;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.*;

import static game.GameIO.*;


public class Main extends Application {

    private HashMap<Integer, String> pictures;
    private TileGrid tileGrid;
    private Score score;
    private Timeline timeline;
    private final String CURRENT_GAME_HISTORY_PATH = "src/data/currentGameHistory.csv";
    private final String HIGH_SCORES_PATH = "src/data/HighScores.txt";

    private TilePane tilePane;

    private MenuItem saveScore;
    private Button btnStepBack;
    private VBox leftBox;

    private StringProperty scoreProp;
    private StringProperty timeProp;

    private Timeline botTimeline;
    private Button btnStopBot;

    private Slider depthLimitSlider;
    private Spinner<Double> maxMoveTimeSpinner;
    private Slider gameHistorySlider;

    private Alert alert;
    private TextInputDialog textInputDialog;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage window) {

        window.setTitle("2048");

        int HEIGHT = 800;
        window.setMinHeight(HEIGHT);
        int WIDTH = 800;
        window.setMinWidth(WIDTH);

        initializePics();
        setTimeline();

        Menu menuScore = new Menu("HighScores");
        MenuItem showHighscores = new MenuItem("Show Highscores");
        MenuItem clearScores = new MenuItem("Clear HighScores");
        saveScore = new Menu("Save Score");
        menuScore.getItems().addAll(showHighscores, clearScores, saveScore);
        saveScore.setVisible(false);

        Menu menuIO = new Menu("Save/Load Game");
        MenuItem saveGame = new Menu("Save Game");
        MenuItem loadGame = new Menu("Load Game");
        menuIO.getItems().addAll(saveGame, loadGame);

        Menu menuBot = new Menu("Bot");
        MenuItem botMove = new MenuItem("Get a Hint (H)");
        MenuItem botPlay = new MenuItem("Let the Bot play");
        MenuItem botSettings = new MenuItem("Bot-settings");
        menuBot.getItems().addAll(botMove, botPlay, botSettings);
        MenuBar menuBar = new MenuBar();
        menuBar.getMenus().addAll(menuScore, menuIO, menuBot);

        Button btnLeft = new Button("←");
        Button btnUp = new Button("↑");
        Button btnRight = new Button("→");
        Button btnDown = new Button("↓");

        btnStepBack = new Button("Step Back");
        Button btnNewGame = new Button("New Game");
        Button btnHelp = new Button("Help");

        btnStopBot = new Button("Stop Bot");
        btnStopBot.setManaged(false);

        gameHistorySlider = new Slider();
        setDepthLimitSlider();
        setMaxMoveTimeSpinner();

        Group buttonGroup = new Group(btnUp, btnDown, btnRight, btnLeft, btnNewGame, btnHelp, btnStepBack,
                depthLimitSlider, maxMoveTimeSpinner, btnStopBot);

        buttonGroup.getChildren().forEach(node -> node.setFocusTraversable(false));

        Label lblScore = new Label();
        lblScore.setFont(Font.font(40));
        scoreProp = new SimpleStringProperty();
        score = new Score();
        scoreProp.setValue("Score: " + score.getSimpleScore());
        lblScore.textProperty().bind(scoreProp);

        Label lblTime = new Label();
        lblTime.setFont(Font.font(30));
        timeProp = new SimpleStringProperty();
        timeProp.setValue("Time: " + score.getTime());
        lblTime.textProperty().bind(timeProp);

        HBox controlsBox = new HBox(10, btnStepBack, btnLeft, btnUp, btnRight, btnDown, btnNewGame, btnHelp);
        controlsBox.setPadding(new Insets(10, 10, 10, 10));

        BorderPane mainPain = new BorderPane();

        HBox botSettingsBox = new HBox(new Label("Bot depth: "), depthLimitSlider,
                new Label("\t\t Max time/move (s): "), maxMoveTimeSpinner, new Label("\t"), btnStopBot);
        botSettingsBox.setPadding(new Insets(10, 10, 10, 10));
        botSettingsBox.setAlignment(Pos.CENTER);
        VBox topNodes = new VBox(10, lblScore, lblTime, botSettingsBox);
        topNodes.setPadding(new Insets(10, 10, 10, 10));

        VBox topBox = new VBox(menuBar, topNodes);
        mainPain.setTop(topBox);

        leftBox = new VBox(new Label("Slide trough game: "), gameHistorySlider);
        leftBox.setPadding(new Insets(10,10,10,10));
        leftBox.setPrefSize(130, 300);
        leftBox.setVisible(false);
        mainPain.setLeft(leftBox);

        VBox rightBox = new VBox();
        rightBox.setPrefSize(130, 50);
        mainPain.setRight(rightBox);

        VBox bottomBox = new VBox( controlsBox);
        bottomBox.setPadding(new Insets(10,10,10,10));
        mainPain.setBottom(bottomBox);
        controlsBox.setAlignment(Pos.TOP_CENTER);

        setTilePane();
        BorderPane.setAlignment(tilePane, Pos.CENTER);
        mainPain.setCenter(tilePane);

        mainPain.setPrefSize(420, 420);

        tilePane.setStyle("-fx-background-color: #bbada0");

        Scene scene = new Scene(mainPain, WIDTH, HEIGHT);
        window.setScene(scene);
        window.show();

        btnHelp.setOnAction(event -> showHelpAlert());

        btnNewGame.setOnAction(event -> {
            try {
                newGame();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        loadGame.setOnAction(event -> {
            File file = new FileChooser().showOpenDialog(window);
            try {
                TileGrid tileGrid = loadGrid(file);
                loadGame(tileGrid);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        saveGame.setOnAction(event -> {
            File file = new FileChooser().showSaveDialog(window);
            try {
                saveGrid(tileGrid, file, false);
            } catch (IOException | NullPointerException exception) {
                showNewGameAlert();
                exception.printStackTrace();
            }
        });

        saveScore.setOnAction(event -> {
            try {
                showSaveScoreDialog();
                score.setName(textInputDialog.getEditor().getText());
                saveScore(score.getName(), score.getSimpleScore(), score.getTime(),
                            score.getTimeScore(), new File(HIGH_SCORES_PATH));
                saveScore.setVisible(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        clearScores.setOnAction(event -> {
            try {
                alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setHeaderText("Do you really want to delete all sores?");
                Optional<ButtonType> confirmation = alert.showAndWait();
                if (confirmation.isPresent() && (confirmation.get() == ButtonType.OK))
                    clearFile(new File(HIGH_SCORES_PATH));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        showHighscores.setOnAction(event -> {
            try {
                showScoreTableAlert();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnUp.setOnAction(event -> {
            try {
                move(Direction.UP);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        btnRight.setOnAction(event -> {
            try {
                move(Direction.RIGHT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        btnDown.setOnAction(event -> {
            try {
                move(Direction.DOWN);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        btnLeft.setOnAction(event -> {
            try {
                move(Direction.LEFT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        window.addEventHandler(KeyEvent.KEY_PRESSED, (event) -> {
            KeyCode key = event.getCode();
            Direction direction = null;
            if (event.getCode() == KeyCode.ENTER)  tilePane.requestFocus(); // draws focus away from Spinner / Slider
            else if (key == KeyCode.UP || key == KeyCode.W) direction = Direction.UP;
            else if (key == KeyCode.RIGHT || key == KeyCode.D) direction = Direction.RIGHT;
            else if (key == KeyCode.DOWN || key == KeyCode.S) direction = Direction.DOWN;
            else if (key == KeyCode.LEFT || key == KeyCode.A) direction = Direction.LEFT;
            else if (key == KeyCode.H) {
                try {
                    direction = getBotMove();
                }
                catch (NullPointerException | ExecutionException exception){
                    showNewGameAlert();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (direction != null) {
                try {
                    move(direction);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        botMove.setOnAction(event -> {
            try {
                move(getBotMove());
            }
            catch (NullPointerException exception){
                showNewGameAlert();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        botPlay.setOnAction(event -> {
            try {
                startBotTimeline();
            }
            catch (NullPointerException exception){
                showNewGameAlert();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });

        btnStopBot.setOnAction(event -> stopBotTimeline());

        botSettings.setOnAction(event -> botSettingsBox.setVisible(!botSettingsBox.isVisible()));

        btnStepBack.setOnAction(event -> {
            try {
                stepBack();
            } catch (IOException e) {
                e.printStackTrace();
            }
            catch (NullPointerException | IllegalArgumentException exception){
                alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setHeaderText("Can't go back further in the game");
                alert.show();
            }
        });

        gameHistorySlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            try {
                drawGame(loadGridAtPos(new File(CURRENT_GAME_HISTORY_PATH), (int) gameHistorySlider.getValue()));
            } catch (IOException exception) {
                exception.printStackTrace();
            }
        });
    }

    private void newGame() throws IOException {
        tileGrid = new TileGrid(4, 4);
        startGame();
    }

    private void loadGame(TileGrid tileGrid) throws IOException {
        this.tileGrid = tileGrid;
        startGame();
    }

    private void startGame() throws IOException {
        setScore();
        drawGame();
        saveGrid(tileGrid, new File(CURRENT_GAME_HISTORY_PATH), false);
        btnStepBack.setDisable(false);
        saveScore.setVisible(false);
        leftBox.setVisible(false);
        score.resetTime();
        timeline.play();
    }

    private void move(Direction direction) throws IOException {
        if (tileGrid == null){
            showNewGameAlert();
            return;
        }
        if (direction != null && tileGrid.move(direction)) {
            tileGrid.addTileRandomly();
        } else if (tileGrid.gameIsOver()) {
            gameOver();
        }
        drawGame();
        setScore();
        saveGrid(tileGrid, new File(CURRENT_GAME_HISTORY_PATH), true);
    }

    private void gameOver() throws IOException {
        timeline.stop();
        if (botTimeline != null){
            stopBotTimeline();
        }
        saveScore.setVisible(true);
        btnStepBack.setDisable(true);
        setGameHistorySlider();
        showGameOverAlert();
        alert.show();
    }

    private void startBotTimeline() {
        botTimeline = new Timeline(new KeyFrame(Duration.millis(150), //<- smooth but poor performance 
                event -> {
                    boolean exceededMoveTime = false;
                    try {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        Future<Direction> directionFuture = executor.submit(new botMoveTask(tileGrid, getDepthlimit()));
                        try {
                            Direction direction = directionFuture.get((long) getMaxMoveTime(), TimeUnit.MILLISECONDS);
                            move(direction);

                        } catch (TimeoutException  e) {
                            directionFuture.cancel(true);
                            exceededMoveTime = true;
                        }
                        catch (ExecutionException | NullPointerException e){
                            showNewGameAlert();
                            botTimeline.stop();
                            return;
                        }
                        finally {
                            executor.shutdownNow();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    adjustBotDepth(exceededMoveTime);
                }));
        botTimeline.setCycleCount(Animation.INDEFINITE);
        botTimeline.play();
        btnStopBot.setManaged(true);
        btnStopBot.setVisible(true);
    }

    private void adjustBotDepth(boolean exceededMoveTime) {
        if(exceededMoveTime){
            if(getDepthlimit()-1 > 0)
                setDepthLimit(getDepthlimit()-1);
        }
        else{
            int emptyTiles = tileGrid.countEmptyTiles();
//            setDepthLimit(emptyTiles > 8 ? 1 : emptyTiles > 6 ? 2 : emptyTiles > 4 ? 3 : emptyTiles > 2 ? 4 : 5);
          setDepthLimit(emptyTiles > 8 ? 1 : emptyTiles > 5 ? 2 : emptyTiles > 4 ? 3 : emptyTiles > 2 ? 4 : 5);
        }
    }

    private void stopBotTimeline() {
        botTimeline.stop();
        btnStopBot.setVisible(false);
        btnStopBot.setManaged(false);
    }

    private int getDepthlimit() {
        return (int) depthLimitSlider.getValue();
    }

    private void setDepthLimit(int depth) {
        depthLimitSlider.setValue(depth);
    }

    private double getMaxMoveTime(){
        return maxMoveTimeSpinner.getValue()*1000;
    }

    private Direction getBotMove() throws Exception{
        GameSolver gameSolver = new GameSolver((TileGrid) TileGrid.deepCopy(this.tileGrid), getDepthlimit());
        try{
            return gameSolver.nexMove();
        }catch (NullPointerException exception){
            showNewGameAlert();
            return null;
        }
    }

    private void setTimeline() {
        timeline = new Timeline(new KeyFrame(Duration.millis(1000),
                event -> {
                    score.incrementTime();
                    timeProp.setValue(String.format("Time: %02d:%02d",
                            score.getGetMinutes(), score.getSeconds()));
                }));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    private void setTilePane() {
        tilePane = new TilePane();
        int rows = 4;
        int cols = 4;
        tilePane.setPrefRows(rows);
        tilePane.setPrefColumns(cols);
        int gap = 5;
        tilePane.setMinSize(420, 420);
        tilePane.setMaxSize(420, 420);
        tilePane.setVgap(gap);
        tilePane.setHgap(gap);
        tilePane.setAlignment(Pos.TOP_LEFT);
    }

    private void setMaxMoveTimeSpinner(){
        maxMoveTimeSpinner = new Spinner<>();
        maxMoveTimeSpinner.setMaxWidth(60);
        SpinnerValueFactory<Double> valueFactory = new SpinnerValueFactory
                .DoubleSpinnerValueFactory(0.2, 2, 0.35, 0.05);
        maxMoveTimeSpinner.setValueFactory(valueFactory);
    }

    private void setDepthLimitSlider() {
        depthLimitSlider = new Slider(1, 8, 2);
        depthLimitSlider.setBlockIncrement(1);
        depthLimitSlider.setMajorTickUnit(1);
        depthLimitSlider.setMinorTickCount(0);
        depthLimitSlider.setShowTickLabels(true);
        depthLimitSlider.setSnapToTicks(true);
        depthLimitSlider.setSnapToTicks(true);
        depthLimitSlider.setMaxSize(150, 20);
    }

    private void setGameHistorySlider() throws IOException {
        int moveCount = getMoveCount(new File(CURRENT_GAME_HISTORY_PATH));
        gameHistorySlider.setMax(moveCount);
        gameHistorySlider.setMin(0);
        gameHistorySlider.setMax(moveCount);
        gameHistorySlider.setBlockIncrement(1);
        gameHistorySlider.setMajorTickUnit(1);
        gameHistorySlider.setMinorTickCount(0);
        gameHistorySlider.setShowTickLabels(true);
        gameHistorySlider.setSnapToTicks(true);
        gameHistorySlider.setSnapToTicks(true);
        gameHistorySlider.setMinHeight(450);
        gameHistorySlider.setOrientation(Orientation.VERTICAL);
        leftBox.setVisible(true);
    }

    private void showSaveScoreDialog() {
        textInputDialog  = new TextInputDialog();
        textInputDialog.setHeaderText("Save your highscore");
        textInputDialog.setContentText("Enter you name");
        textInputDialog.showAndWait();
    }

    private void showNewGameAlert() {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Not so fast buddy");
        alert.setHeaderText("Click on 'New Game' first to start!");
        alert.show();
    }

    private void showGameOverAlert() {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setResizable(true);
        alert.setTitle("Game Over");
        alert.setHeaderText(String.format("Score: %s Time: %02d:%02d",
                score.timeScore(tileGrid), score.getGetMinutes(), score.getSeconds()));
        alert.setContentText("You can now save your score in 'HighScores'!");
        alert.show();
    }

    private void showImageNotFoundAlert() {
        alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Ran out of / didn't find  images");
        alert.setContentText("Wow you are so good you are breaking the game");
        alert.show();
    }

    private void showHelpAlert() {
        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Help");
        alert.setHeaderText(" Shortcuts: \n    W A S D + Arrow keys for movement \n    H for Bot move \n\n " +
                "After you have finished a game, you'll be able to save your score. \n\n Press Enter " +
                "if Focus is stuck on any Node. \n\n You can adjust the bot depth manually and play by pressing H " +
                "or give the bot a max move time and let it do the work.\n Start the bot with a low depth.");
        alert.show();
    }

    private void setScore() {
        scoreProp.setValue("Score: " + score.simpleScore(tileGrid));
    }

    private void showScoreTableAlert() throws IOException {
        TableColumn<Score, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        TableColumn<Score, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("simpleScore"));
        TableColumn<Score, Integer> timeCol = new TableColumn<>("Time(s)");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("time"));
        TableColumn<Score, Integer> timeScoreCol = new TableColumn<>("TimedScore");
        timeScoreCol.setCellValueFactory(new PropertyValueFactory<>("timeScore"));
        TableView<Score> scoreTable = new TableView<>();
        scoreTable.getColumns().addAll(scoreCol, nameCol, timeCol, timeScoreCol);
        scoreTable.setItems(getScores());

        alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setGraphic(scoreTable);
        alert.setContentText("");
        alert.setHeaderText("");
        alert.setTitle("Highscores");
        alert.show();
    }

    private ObservableList<Score> getScores() throws IOException {
        return FXCollections.observableArrayList(loadHighScores());
    }

    private void stepBack() throws IOException {
        displayGridAtMove(getMoveCount(new File(CURRENT_GAME_HISTORY_PATH))-1);
        removeLastLines(new File(CURRENT_GAME_HISTORY_PATH), tileGrid.getnCols()+1);
    }

    private void displayGridAtMove(int moveNumber) throws IOException {
        tileGrid = loadGridAtPos(new File(CURRENT_GAME_HISTORY_PATH), moveNumber);
        drawGame();
    }

    private void drawGame() throws FileNotFoundException {
        tilePane.getChildren().clear();
        for (ArrayList<Integer> row : tileGrid.getTiles()) {
            for (int col : row)
                tilePane.getChildren().add(getIview(pictures.get(col)));
        }
    }

    private void drawGame(TileGrid grid) throws FileNotFoundException {
        tilePane.getChildren().clear();
        for (ArrayList<Integer> row : grid.getTiles()) {
            for (int col : row)
                tilePane.getChildren().add(getIview(pictures.get(col)));
        }
    }

    private ImageView getIview(String filePath) throws FileNotFoundException {
        Image image = loadImage(filePath);
        ImageView iView = new ImageView(image);
        int imageSize = 100;
        iView.setFitWidth(imageSize);
        iView.setFitHeight(imageSize);
        iView.setPreserveRatio(true);
        return iView;
    }

    private Image loadImage(String filepath) throws FileNotFoundException {
        return new Image(getFileInputStream(filepath));
    }

    private FileInputStream getFileInputStream(String filepath) throws FileNotFoundException {
        try {
            return new FileInputStream(filepath);
        } catch (NullPointerException | FileNotFoundException ex) {
            showImageNotFoundAlert();
            String imgNotFound = "src/imgs/INF.png";
            return new FileInputStream(imgNotFound);
        }
    }

    private void initializePics() {
        pictures = new HashMap<>();
        for (int i = 0; i < 17; i++) {
            int num = (int) Math.pow(2, i);
            if (i == 0) {
                num = i;
            }
            pictures.put(num, "src/imgs/" + num + ".png");
        }
    }
}
