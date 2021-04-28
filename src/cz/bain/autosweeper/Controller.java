package cz.bain.autosweeper;

import cz.bain.autosweeper.solver.Solver;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    @FXML
    public GridPane playingGrid;
    public ImageView smiley;
    public ImageView settings_button;
    public ImageView analyze_button;

    public Stage settings_stage = new Stage();
    public StackPane top_bar;
    public SevenSegmentDisplay time_display;

    private PlayingField pf;
    private Solver solver;
    private boolean solver_view_enabled = false;

    private GameSettings gameSettings;

    private Task<Void> bot_task;
    private Task<Void> timer_task;

    int games_won = 0;
    int games_played = 0;
    int guessed = 0;
    int lvl2 = 0;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        gameSettings = new GameSettings();
        time_display = new SevenSegmentDisplay(4);
        top_bar.getChildren().add(time_display);
        time_display.display(0);
        StackPane.setAlignment(time_display, Pos.CENTER_RIGHT);
        startNewGame();
    }

    @FXML
    public void onSmileyClick(MouseEvent unused) {
        System.out.println("new game with probability: " + gameSettings.mine_probability);
        startNewGame();
        smiley.setViewport(new Rectangle2D(0, 0, 64, 64));
    }

    public void startNewGame() {

        time_display.display(0);

        analyze_button.setVisible(gameSettings.game_type.equals(GameSettings.GAME_TYPE.ASSISTED));

        // take care of running threads
        if (bot_task != null && bot_task.isRunning()) {
            bot_task.cancel();
        }
        if (timer_task != null && timer_task.isRunning()) timer_task.cancel();


        // clean up after old game
        playingGrid.getChildren().clear();

        pf = new PlayingField(gameSettings);
        solver = new Solver(pf);

        pf.setEventHandler(this::handleEvent);

        pf.render(playingGrid);

        if (gameSettings.game_type == GameSettings.GAME_TYPE.AUTOMATIC)
            createSolverTask();
    }

    public void createSolverTask() {
        bot_task = new Task<>() {
            @Override
            protected Void call() {
                int calculation_duration = 0;
                while (!isCancelled() && !pf.gameOver) {
                    long time_start = System.currentTimeMillis();

                    List<Cell> candidates = solver.chooseSquare();

                    calculation_duration += System.currentTimeMillis() - time_start;

                    int calculation_duration_final = calculation_duration;
                    Platform.runLater(() -> time_display.display(calculation_duration_final));

                    if (candidates.size() == 0) break;
                    if (pf.getGameType() == GameSettings.GAME_TYPE.AUTOMATIC) {
                        Platform.runLater(() -> {
                            if (pf.gameOver) return;
                            for (int x = 0; x < pf.getW(); x++) {
                                for (int y = 0; y < pf.getH(); y++) {
                                    Cell c = pf.getCellAt(x, y);
                                    if (Solver.hasFlag(c, Solver.SolverFlags.MINE))
                                        c.setFlagged(true);
                                }
                            }
                        });
                    }
                    for (Cell c : candidates) {
                        c.setUncovered();
                        try {
                            //noinspection BusyWait
                            Thread.sleep(5);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
                guessed += solver.guessed;
                lvl2 += solver.lvl2;
                System.out.println("calc duration: " + calculation_duration);
                return null;
            }
        };
        new Thread(bot_task).start();
    }

    public Task<Void> startTimerTask() {
        Task<Void> timer_task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                long start = System.currentTimeMillis();
                while (!isCancelled()) {
                    long duration = (System.currentTimeMillis() - start) / 1000;
                    Platform.runLater(() -> time_display.display((int) duration));
                    Thread.sleep(1000);
                }
                return null;
            }
        };
        new Thread(timer_task).start();
        return timer_task;
    }

    public void onSettingsPressed(MouseEvent event) {
        ImageView v = (ImageView) event.getSource();
        v.setViewport(new Rectangle2D(256, v.getViewport().getMinY(), 256, 64));
    }


    public void onSettingsReleased(MouseEvent unused) {
        settings_button.setViewport(new Rectangle2D(0, 0, 256, 64));
        Parent root;
        if (!settings_stage.isShowing()) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getResource("settings.fxml"));
                root = loader.load();
                SettingsController c = loader.getController();
                c.setGameSettings(gameSettings);
                settings_stage.setTitle("fractals-distinct-title");
                settings_stage.setScene(new Scene(root, 300, 300));
                settings_stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void analyze(MouseEvent event) {
        if (event != null) {
            ImageView v = (ImageView) event.getSource();
            v.setViewport(new Rectangle2D(0, 64, 256, 64));
        }
        solver_view_enabled = !solver_view_enabled;
        if (solver_view_enabled && !pf.gameOver) {
            solver.runAnalysis();
            for (int x = 0; x < pf.getW(); x++) {
                for (int y = 0; y < pf.getH(); y++) {
                    Cell c = pf.getCellAt(x, y);
                    if (!c.isUncovered() && c.solver_flags != 0) {
                        c.setStateByFlag(c.solver_flags);
                    }

                }
            }
        } else {
            for (int x = 0; x < pf.getW(); x++) {
                for (int y = 0; y < pf.getH(); y++) {
                    Cell c = pf.getCellAt(x, y);
                    if (!c.isUncovered() && c.solver_flags != 0) {
                        c.setState(c.isFlagged() ? 12 : 0);
                    }

                }
            }
        }
    }

    private void handleEvent(PlayingField.EventType e) {
        int smiley_state = -1;
        switch (e) {
            case START:
                if (pf.getGameType() != GameSettings.GAME_TYPE.AUTOMATIC)
                    timer_task = startTimerTask();
                break;
            case TRYING_UNCOVER_ENTER:
                if (solver_view_enabled) analyze(null);
                smiley_state = 2;
                break;
            case MINE_UNCOVER:
                smiley_state = 1;
                games_played++;
                System.out.println("Game lost");
                System.out.println("Played: " + games_played + ", won: " + games_won);
                System.out.println("lvl2: " + lvl2 + ", Guessed: " + guessed);
                System.out.println("Bombs found by lvl2: " + solver.lvl2_bombs);
                if (pf.getGameType() != GameSettings.GAME_TYPE.AUTOMATIC)
                    timer_task.cancel();
                break;
            case WIN:
                System.out.println("winnn");
                games_won++;
                games_played++;
                System.out.println("Game won");
                System.out.println("Played: " + games_played + ", won: " + games_won);
                System.out.println("lvl2: " + lvl2 + ", Guessed: " + guessed);
                System.out.println("Bombs found by lvl2: " + solver.lvl2_bombs);
                smiley_state = 3;
                if (pf.getGameType() != GameSettings.GAME_TYPE.AUTOMATIC)
                    timer_task.cancel();
                break;
            case TRYING_UNCOVER_LEAVE:
                smiley_state = 0;
                break;
        }
        if (smiley_state != -1) smiley.setViewport(new Rectangle2D(smiley_state * 64, 0, 64, 64));
    }
}
