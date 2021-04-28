package cz.bain.autosweeper;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

public class SevenSegmentDisplay extends HBox {

    private ImageView[] display;
    private int maxNumber;

    public SevenSegmentDisplay(int digits) {
        super();
        display = new ImageView[digits];
        maxNumber = (int)(Math.pow(10, digits)-1);
        Image image = new Image("file:resources/7segment.png");
        // create and add all digits to the display
        for (int i = digits-1; i >= 0; i--) {
            display[i] = new ImageView(image);
            display[i].setViewport(new Rectangle2D(0, 0, 64, 124));
            display[i].setFitHeight(31);
            display[i].setFitWidth(16);
            this.getChildren().add(display[i]);
        }
        this.setSpacing(2);
        this.setStyle("-fx-background-color: #000");
        this.setAlignment(Pos.TOP_RIGHT);
        this.setMaxWidth(16*digits);
        this.setPadding(new Insets(2, 2, 2, 2));
    }

    public void display(int number) {
        if (number > maxNumber || number < 0) return;
        for (ImageView digit : display) {
            digit.setViewport(new Rectangle2D((number % 10)*64, 0, 64, 124));
            number /= 10;
        }
    }
}
