package cz.bain.autosweeper.solver;

import cz.bain.autosweeper.Cell;
import cz.bain.autosweeper.PlayingField;

import java.util.*;

public class Solver {
    private final PlayingField field;

    public Solver(PlayingField field) {
        this.field = field;
    }

    public enum SolverFlags {
        MINE(1),
        PROBABILITY_LOW(2),
        PROBABILITY_MEDIUM(4),
        PROBABILITY_HIGH(8);

        public int i;

        SolverFlags(int i) {
            this.i = i;
        }
    }

    public int guessed = 0;
    public int lvl2 = 0;
    public int last_level = 1;
    public int lvl2_bombs = 0;

    public List<Cell> chooseSquare() {
//        System.out.println("algo");
        if (field.getGameRound() < 4 && !checkStart()) {
            List<Cell> candidates = new ArrayList<>();
            switch (field.getGameRound()) {
                case 0:
                    candidates.add(field.getCellAt(0, 0));
                    break;
                case 1:
                    candidates.add(field.getCellAt(0, field.getH() - 1));
                    break;
                case 2:
                    candidates.add(field.getCellAt(field.getW() - 1, 0));
                    break;
                case 3:
                    candidates.add(field.getCellAt(field.getW() - 1, field.getH() - 1));
                    break;
            }
            return candidates;
        }

        List<Cell> candidates;
        candidates = level1();
        if (candidates.size() == 0) candidates = level2();
        if (candidates.size() == 0) candidates = level3();

        return candidates;
    }

    /**
     * Runs all three levels of the solver and throws away their candidates. (Sets solver_flags)
     */
    public void runAnalysis() {
        level1();
        level3();
        // we need to run the second level last because it doesn't find bombs but empty spaces
        for (Cell c : level2()) {
            setFlag(c, SolverFlags.PROBABILITY_HIGH, false);
            setFlag(c, SolverFlags.PROBABILITY_MEDIUM, false);
            setFlag(c, SolverFlags.PROBABILITY_LOW, true);
        }
    }

    public boolean checkStart() {
        for (int x = 0; x < field.getW(); x++) {
            for (int y = 0; y < field.getH(); y++) {
                Cell c = field.getCellAt(x, y);
                if (c.isUncovered() && c.getBombCount() == 0) return true;
            }
        }
        return false;
    }

    public static boolean hasFlag(Cell cell, SolverFlags flag) {
        return (cell.solver_flags & flag.i) > 0;
    }

    public void setFlag(Cell cell, SolverFlags flag, boolean enable) {
        cell.solver_flags = enable ? cell.solver_flags | flag.i : cell.solver_flags & ~flag.i;
    }

    /**
     * Simplest algorithm and completely deterministic. Checks if the amount of covered squares equals to the amount
     * of bombs in the neighbourhood.
     *
     * @return list of possible candidates
     */
    private List<Cell> level1() {
        last_level = 1;

        List<Cell> candidates = new ArrayList<>();
        // mark all directly visible bombs from the field
        for (int x = 0; x < field.getW(); x++) {
            for (int y = 0; y < field.getH(); y++) {
                Cell cell = field.getCellAt(x, y);
                // check if cell is uncovered
                if (!cell.isUncovered()) continue;

                // flag all neighbours as bombs if the bomb count equals the amount of covered neighbours
                int bomb_count = cell.getBombCount();
                if (bomb_count == 0) continue;
                for (Cell c : cell.getNeighbours())
                    if (!c.isUncovered()) bomb_count--;
                if (bomb_count == 0) {
                    for (Cell c : cell.getNeighbours()) {
                        if (!c.isUncovered()) {
                            setFlag(c, SolverFlags.MINE, true);
                        }
                    }
                }
            }
        }

        // gather candidates
        for (int x = 0; x < field.getW(); x++) {
            for (int y = 0; y < field.getH(); y++) {
                Cell cell = field.getCellAt(x, y);
                if (!cell.isUncovered() || cell.getBombCount() == 0) continue;

                // if we have found all the bombs but some covered neighbours still remain
                int covered_neighbours = 0;
                int flagged_neighbours = 0;
                for (Cell n : cell.getNeighbours()) {
                    if (!n.isUncovered()) covered_neighbours++;
                    if (hasFlag(n, SolverFlags.MINE)) flagged_neighbours++;
                }
                if (flagged_neighbours == cell.getBombCount() && covered_neighbours > flagged_neighbours) {
                    for (Cell n : cell.getNeighbours()) {
                        if (!n.isUncovered() && !hasFlag(n, SolverFlags.MINE)) {
                            candidates.add(n);
                        }
                    }
                }
            }
        }
        return candidates;
    }

    /**
     * Second level of the solver can solve some 3x3 situations.
     * TODO: Opravit
     */
    private List<Cell> level2() {
//        last_level = 2;
        Set<Cell> candidates = new HashSet<>();

        System.out.println("triggered lvl 2");
        lvl2++;

        // find an uncovered cell with a number that we could solve
        for (int x = 0; x < field.getW(); x++) {
            for (int y = 0; y < field.getH(); y++) {
                Cell target = field.getCellAt(x, y);
                if (!target.isUncovered() || target.getBombCount() == 0) continue;

                // get main bucket (all covered neighbours of the cell being solved)
                Bucket main_bucket = new Bucket(target);
                for (Cell c : target.getNeighbours()) {
                    if (!c.isUncovered() && !hasFlag(c, SolverFlags.MINE)) main_bucket.cells.add(c);
                    else if (hasFlag(c, SolverFlags.MINE)) main_bucket.bombs--;
                }

                // get possible sub buckets
                List<Bucket> sub_buckets = new ArrayList<>();
                for (Cell c : main_bucket.cells) {
                    for (Cell possible_owner : c.getNeighbours()) {
                        if (possible_owner.isUncovered()) {
                            boolean exists = false;
                            // check if a bucket with this owner already exists
                            for (Bucket b : sub_buckets) {
                                if (b.owner.equals(possible_owner)) {
                                    exists = true;
                                    break;
                                }
                            }
                            if (!exists) {
                                // if a bucket with this owner doesn't already exist, then create a new one and add it
                                // to the list
                                Bucket bucket = new Bucket(possible_owner);
                                for (Cell n : possible_owner.getNeighbours()) {
                                    if (!n.isUncovered() && !hasFlag(n, SolverFlags.MINE)) bucket.cells.add(n);
                                    else if (hasFlag(n, SolverFlags.MINE)) bucket.bombs--;
                                }
                                sub_buckets.add(bucket);
                            }
                        }
                    }
                }
                // remove non sub buckets and duplicates
                sub_buckets.removeIf(bucket -> {
                    if (!bucket.isSubSet(main_bucket) || bucket.cells.size() == main_bucket.cells.size()) return true;
                    for (Bucket b : sub_buckets) {
                        if (bucket == b) continue;
                        if (b.cells.containsAll(bucket.cells) && b.cells.size() == bucket.cells.size()) return true;
                    }
                    return false;
                });

                // check for intersections
                boolean got_intersection = false;
                for (Cell c : main_bucket.cells) {
                    // try to get an initial intersection
                    List<Bucket> in_buckets = new ArrayList<>();
                    for (Bucket b : sub_buckets) {
                        if (b.cells.contains(c)) in_buckets.add(b);
                    }

                    // the cell is an intersection
                    if (in_buckets.size() > 1) {
                        // get the whole intersection
                        Set<Cell> intersection = new HashSet<>();
                        Bucket least_amount_of_bombs = in_buckets.get(0);
                        for (Bucket b : in_buckets) {
                            if (b.bombs < least_amount_of_bombs.bombs) least_amount_of_bombs = b;
                        }
                        for (int i = 1; i < in_buckets.size(); i++) {
                            intersection.addAll(least_amount_of_bombs.intersectingCells(in_buckets.get(i)));
                        }

                        // remove element if not intersecting with all other buckets
                        intersection.removeIf(cell -> {
                            for (Bucket b : in_buckets) {
                                if (!b.cells.contains(cell)) return true;
                            }
                            return false;
                        });
                        // check if it contains a bomb
                        int sum = 0;
                        for (Bucket b : in_buckets) {
                            sum += Math.abs(b.bombs - least_amount_of_bombs.bombs);
                        }
                        sum += Math.abs(main_bucket.bombs - least_amount_of_bombs.bombs);
                        if (sum == 0) {
//                            System.out.println("intersection");
                            candidates.addAll(main_bucket.cells);
                            candidates.removeAll(intersection);
                            got_intersection = true;
                            break;
                        }
                        for (Bucket b : in_buckets)
                            b.cells.removeAll(intersection);
                    }
                }
                if (got_intersection) {
                    last_level = 2;
                    continue;
                }

                // subtract all sub buckets
                for (Bucket b : sub_buckets)
                    main_bucket.sub(b);

                if (main_bucket.bombs == 0 && main_bucket.cells.size() != 0) {
//                    System.out.println("normal");
                    candidates.addAll(main_bucket.cells);
                }
            }
        }

        System.out.print("Safe: ");
        for (Cell candidate : candidates) {
            System.out.print(candidate.getCoord().x + " " + candidate.getCoord().y + ", ");
        }
        System.out.println();
        return new ArrayList<>(candidates);
    }

    /**
     * Third level is a fallback level. Calculates some probabilities and picks a cell at random.
     */
    private List<Cell> level3() {
        last_level = 3;

        System.out.println("triggered lvl 3");
        guessed++;

        List<Cell> candidates = new ArrayList<>();
        HashMap<Cell, Float> probabilities = new HashMap<>();
        for (int x = 0; x < field.getW(); x++) {
            for (int y = 0; y < field.getH(); y++) {
                Cell target = field.getCellAt(x, y);
                int bomb_count = target.getBombCount();
                if (!target.isUncovered() || bomb_count == 0) continue;
                int covered_neighbours = 0;
                for (Cell c : target.getNeighbours()) {
                    if (!c.isUncovered() && !hasFlag(c, SolverFlags.MINE))
                        covered_neighbours++;
                    else if (hasFlag(c, SolverFlags.MINE)) bomb_count--;
                }
                for (Cell c : target.getNeighbours()) {
                    if (!c.isUncovered() && !hasFlag(c, SolverFlags.MINE)) {
                        float probability = (float) bomb_count / covered_neighbours;
                        if (probabilities.containsKey(c) && probabilities.get(c) > probability)
                            continue;
                        if (probability < 1f / 5)
                            setFlag(c, SolverFlags.PROBABILITY_LOW, true);
                        else if (probability < 1f / 3)
                            setFlag(c, SolverFlags.PROBABILITY_MEDIUM, true);
                        else setFlag(c, SolverFlags.PROBABILITY_HIGH, true);

                        probabilities.put(c, probability);
                    }
                }
            }
        }

        Cell min_cell = field.getCellAt(
                (int) (Math.random() * field.getW() + 1),
                (int) (Math.random() * field.getH() + 1)
        );
        float min_probability = 1.0f;
        for (Map.Entry<Cell, Float> entry : probabilities.entrySet()) {
            if (entry.getValue() < min_probability) {
                min_probability = entry.getValue();
                min_cell = entry.getKey();
            }
        }
        candidates.add(min_cell);
        System.out.println("Picking cell with probability of: " + min_probability);

        return candidates;
    }
}
