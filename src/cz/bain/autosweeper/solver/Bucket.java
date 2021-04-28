package cz.bain.autosweeper.solver;

import cz.bain.autosweeper.Cell;

import java.util.ArrayList;
import java.util.List;

public class Bucket {

    List<Cell> cells = new ArrayList<>();
    int bombs;
    Cell owner;

    public Bucket(Cell owner) {
        this.owner = owner;
        this.bombs = owner.getBombCount();
    }

    /**
     * Subtract a bucket from this bucket
     *
     * @param b other bucket
     */
    public void sub(Bucket b) {
        cells.removeAll(b.cells);
        bombs -= b.bombs;
    }

    public boolean isSubSet(Bucket b) {
        return b.cells.containsAll(cells);
    }

    public List<Cell> intersectingCells(Bucket b) {
        List<Cell> intersection = new ArrayList<>();
        for (Cell c : cells) {
            if (b.cells.contains(c)) intersection.add(c);
        }
        return intersection;
    }

    public boolean isIntersecting(Bucket b) {
        for (Cell c : cells) {
            if (b.cells.contains(c)) return true;
        }
        return false;
    }
}
