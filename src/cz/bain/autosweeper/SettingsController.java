package cz.bain.autosweeper;

import javafx.beans.value.ObservableValue;
import javafx.fxml.Initializable;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Slider;
import javafx.scene.text.Text;

import java.net.URL;
import java.util.ResourceBundle;

public class SettingsController implements Initializable {
    public Text text_mine_prob;
    public Text text_height;
    public Text text_width;
    private GameSettings gameSettings = new GameSettings();

    public ChoiceBox<GameSettings.GAME_TYPE> game_type_input;
    public Slider slider_width;
    public Slider slider_height;
    public Slider slider_mine_prob;

    public void setGameSettings(GameSettings gs) {
        this.gameSettings = gs;
        game_type_input.setValue(gs.game_type);
        slider_width.valueProperty().setValue(gs.width);
        slider_height.valueProperty().setValue(gs.height);
        slider_mine_prob.valueProperty().setValue(gs.mine_probability);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        game_type_input.getItems().addAll(GameSettings.GAME_TYPE.NORMAL,
                GameSettings.GAME_TYPE.ASSISTED, GameSettings.GAME_TYPE.AUTOMATIC);
        game_type_input.setValue(GameSettings.GAME_TYPE.NORMAL);

        slider_width.valueProperty().addListener(this::width_changed);
        slider_height.valueProperty().addListener(this::height_changed);
        slider_mine_prob.valueProperty().addListener(this::mine_changed);
        game_type_input.valueProperty().addListener(this::game_mode_changed);
    }

    private void width_changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        gameSettings.width = t1.intValue();
        text_width.setText("Field width: " + t1.intValue());
    }

    private void height_changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        gameSettings.height = t1.intValue();
        text_height.setText("Field height: " + t1.intValue());
    }

    private void mine_changed(ObservableValue<? extends Number> observableValue, Number number, Number t1) {
        gameSettings.mine_probability = Math.round(t1.doubleValue() * 100) / 100.0f;
        text_mine_prob.setText("Mine probability: " + Math.round(t1.doubleValue() * 100) / 100.0f);
    }

    private void game_mode_changed(ObservableValue<? extends GameSettings.GAME_TYPE> observableValue,
                                   GameSettings.GAME_TYPE game_type, GameSettings.GAME_TYPE t1) {
        gameSettings.game_type = t1;
    }
}
