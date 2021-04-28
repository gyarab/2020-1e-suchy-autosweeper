package cz.bain.autosweeper;

import javafx.application.Platform;
import javafx.scene.image.Image;
import javafx.scene.layout.GridPane;

public class PlayingField {
    public enum EventType {
        MINE_UNCOVER,
        TRYING_UNCOVER_ENTER,
        TRYING_UNCOVER_LEAVE,
        UNCOVER,
        WIN,
        FLAG,
        UNFLAG,
        START
    }

    public interface MouseActionHandler {
        void handle(EventType type);
    }

    private final Cell[][] field;
    public boolean gameOver = false;
    private MouseActionHandler eventHandler;
    public int bombs;
    private int covered;
    private final int w;
    private final int h;
    private int gameRound = 0;
    private GameSettings.GAME_TYPE gameType = GameSettings.GAME_TYPE.NORMAL;

    public PlayingField(GameSettings settings) {
        this(settings.width, settings.height, settings.mine_probability);
        gameType = settings.game_type;
    }

    public PlayingField(int w, int h, double bomb_probability) {
        field = new Cell[w][h];

        Image i = new Image("file:resources/cell-sprite.png");

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                Cell c = new Cell(i, this, x, y);
                c.bomb = Math.random() < bomb_probability;
                if (c.bomb) bombs++;
                field[x][y] = c;
            }
        }
        covered = w * h;
        this.w = w;
        this.h = h;
    }

    public Cell getCellAt(int x, int y) {
        if (x >= 0 && x < field.length && y >= 0 && y < field[0].length) {
            return field[x][y];
        }
        return null;
    }

    public void render(GridPane gp) {
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                gp.add(field[x][y], x, y);
            }
        }
    }

    public int getW() {
        return w;
    }

    public int getH() {
        return h;
    }

    public int getGameRound() {
        return gameRound;
    }

    public void incGameRound() {
        gameRound++;
        if (gameRound == 1) fireEvent(EventType.START);
    }

    public void setEventHandler(MouseActionHandler m) {
        eventHandler = m;
    }

    public void fireEvent(EventType e) {
        eventHandler.handle(e);
        switch (e) {
            case UNCOVER:
                covered--;
                if (covered == bombs) {
                    this.gameOver = true;
                    fireEvent(EventType.WIN);
                    // :( nemám rád Platform.runLater, ale už je moc pozdě
                    // na to celý projekt přepsat. Pravděpodobně by se měl
                    // render centralizovat. (dokumentace lže)
                    Platform.runLater(() -> {
                        for (Cell[] row : field) {
                            for (Cell c : row) {
                                if (c.bomb && !c.isUncovered()) c.setState(10);
                            }
                        }
                    });
                }
                break;
            case MINE_UNCOVER:
                this.gameOver = true;
                Platform.runLater(() -> {
                    for (Cell[] row : field) {
                        for (Cell c : row) {
                            if (c.bomb && !c.isUncovered()) c.setState(10);
                        }
                    }
                });
        }
    }

    public GameSettings.GAME_TYPE getGameType() {
        return gameType;
    }
}
