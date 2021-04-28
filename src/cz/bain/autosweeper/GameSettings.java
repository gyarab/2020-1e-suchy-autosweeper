package cz.bain.autosweeper;

public class GameSettings {
    public int width = 19;
    public int height = 19;
    public double mine_probability = 0.07;
    public enum GAME_TYPE {
        NORMAL,
        ASSISTED,
        AUTOMATIC
    }
    public GAME_TYPE game_type = GAME_TYPE.ASSISTED;
}
