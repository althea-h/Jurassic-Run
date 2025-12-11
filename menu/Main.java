package menu; 

import javafx.application.Application;
import javafx.stage.Stage;
import game.Menu;

public class Main extends Application {
    @Override
    public void start(Stage stage) {
        Menu menu = new Menu(stage);
        menu.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}