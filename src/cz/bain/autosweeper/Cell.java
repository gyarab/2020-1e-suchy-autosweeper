package cz.bain.autosweeper;

import cz.bain.autosweeper.solver.Solver;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.ArrayList;
import java.util.List;

public class Cell extends ImageView {
    private final PlayingField playingField;
    private final Coord coord;
    public boolean bomb;
    private boolean uncovered;
    private boolean flagged = false;
    private int neighbour_bombs = -2;
    public int solver_flags = 0;

    public static class Coord {
        public int x;
        public int y;

        public Coord(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public Cell(Image image, PlayingField playingField, int x, int y) {
        super(image);
        this.playingField = playingField;
        this.coord = new Coord(x, y);
        setViewport(new Rectangle2D(0, 0, 64, 64));
        setFitHeight(24);
        setFitWidth(24);

        setOnMousePressed(e -> {
            if (playingField.gameOver) return;
            if (e.isPrimaryButtonDown()) {
                if (uncovered || flagged) return;
                playingField.fireEvent(PlayingField.EventType.TRYING_UNCOVER_ENTER);
                setState(1);
            }
        });
        setOnMouseReleased(e -> {
            if (playingField.gameOver) return;
            switch (e.getButton()) {
                case PRIMARY:
                    if (flagged) break;
                    playingField.fireEvent(PlayingField.EventType.TRYING_UNCOVER_LEAVE);
                    setUncovered();
                    break;
                case SECONDARY:
                    setFlagged(!flagged);
                    break;
            }
        });
    }

    /**
     * Get coordinates of this cell.
     *
     * @return coordinates
     */
    public Coord getCoord() {
        return coord;
    }

    public Cell[] getNeighbours() {
        List<Cell> cells = new ArrayList<>();

        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                Cell c = playingField.getCellAt(coord.x + i, coord.y + j);
                if (c == null) continue;
                cells.add(c);
            }
        }
        return cells.toArray(new Cell[0]);
    }

    public int calculateNumber() {
        if (bomb) return -1;

        int number = 0;
        Cell[] cells = getNeighbours();
        for (Cell cell : cells) {
            if (cell.bomb) number++;
        }
        return number;
    }

    public int getBombCount() {
        if (neighbour_bombs == -2) neighbour_bombs = calculateNumber();
        return neighbour_bombs;
    }

    public void setState(int state) {
        setViewport(new Rectangle2D(64 * state, 0, 64, 64));
    }

    public void setStateByFlag(int flag) {
        if ((flag & Solver.SolverFlags.PROBABILITY_LOW.i) > 0)
            setViewport(new Rectangle2D(192, 64, 64, 64));
        if ((flag & Solver.SolverFlags.PROBABILITY_MEDIUM.i) > 0)
            setViewport(new Rectangle2D(128, 64, 64, 64));
        if ((flag & Solver.SolverFlags.PROBABILITY_HIGH.i) > 0)
            setViewport(new Rectangle2D(64, 64, 64, 64));
        if ((flag & Solver.SolverFlags.MINE.i) > 0)
            setViewport(new Rectangle2D(0, 64, 64, 64));
    }

    public void setUncovered() {
        if (this.uncovered || playingField.gameOver) return;
        this.uncovered = true;
        playingField.incGameRound();
        if (this.bomb) {
            Platform.runLater(() -> setState(11));
            playingField.fireEvent(PlayingField.EventType.MINE_UNCOVER);
            return;
        }
        int n = calculateNumber();
        Platform.runLater(() -> setState(n + 1));
        playingField.fireEvent(PlayingField.EventType.UNCOVER);
        if (n == 0) uncoverNeighbours();
    }

    public void uncoverNeighbours() {
        Cell[] cells = getNeighbours();
        for (Cell cell : cells) {
            cell.setUncovered();
        }
    }

    public boolean isUncovered() {
        return uncovered;
    }

    public void setFlagged(boolean b) {
        if (!uncovered) {
            flagged = b;
            setState(flagged ? 12 : 0);
            playingField.fireEvent(flagged ? PlayingField.EventType.FLAG : PlayingField.EventType.UNFLAG);
        }
    }

    public boolean isFlagged() {
        return flagged;
    }
}
