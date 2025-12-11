package menu;

import game.Game;
import game.audio.AudioManager;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class VictoryMenu extends AbstractMenu {
    private Game game;
    private AudioManager audioManager;
    
    private Image escapeImg;
    private Image restartBtn;
    private Image menuBtn;
    private Image exitBtn;
    
    private double restartX;
    private double restartY;
    private double menuX;
    private double menuY;
    private double exitX;
    private double exitY;
    
    private boolean hoverRestart;
    private boolean hoverMenu;
    private boolean hoverExit;
    
    public VictoryMenu(Stage stage, Game game) {
        super(stage, 800, 600);
        
        this.game = game;
        this.audioManager = AudioManager.getInstance();
        
        root.setStyle("-fx-background-color: transparent;");
        canvas.setMouseTransparent(false);
        canvas.setPickOnBounds(false);
        
        calculateButtonPositions();
        loadImages();
        setupMouseHandlers();
    }
    
    private void calculateButtonPositions() {
        restartX = (800 - BTN_WIDTH) / 2;
        restartY = 320;
        
        menuX = (800 - BTN_WIDTH) / 2;
        menuY = 400;
        
        exitX = (800 - BTN_WIDTH) / 2;
        exitY = 480;
        
        hoverRestart = false;
        hoverMenu = false;
        hoverExit = false;
    }
    
    private void loadImages() {
        escapeImg = loadImage("/assets/background/escaped.png");
        restartBtn = loadImage("/assets/buttons/restart_button.png");
        menuBtn = loadImage("/assets/buttons/menu_button.png");
        exitBtn = loadImage("/assets/buttons/exit_button.png");
    }
    
    @Override
    public void show() {
        System.out.println("Victory menu shown");
        
        draw();
        
        Pane currentRoot = (Pane) stage.getScene().getRoot();
        if (!currentRoot.getChildren().contains(root)) {
            currentRoot.getChildren().add(root);
        }
        
        canvas.requestFocus();
    }
    
    @Override
    public void hide() {
        Pane currentRoot = (Pane) stage.getScene().getRoot();
        currentRoot.getChildren().remove(root);
        
        System.out.println("Victory menu hidden");
    }
    
    @Override
    protected void draw() {
        gc.clearRect(0, 0, 800, 600);
        
        if (escapeImg != null) {
            gc.drawImage(escapeImg, 0, 0, 800, 600);
        } else {
            gc.setFill(Color.color(0, 0, 0, 0.7));
            gc.fillRect(0, 0, 800, 600);
            
            gc.setFill(Color.GOLD);
            gc.setFont(javafx.scene.text.Font.font("Arial Black", 48));
            gc.fillText("YOU ESCAPED!", 200, 300);
        }
        
        renderButton(restartBtn, restartX, restartY, hoverRestart, "RESTART", Color.ORANGE);
        renderButton(menuBtn, menuX, menuY, hoverMenu, "MAIN MENU", Color.CYAN);
        renderButton(exitBtn, exitX, exitY, hoverExit, "EXIT GAME", Color.RED);
    }
    
    private void renderButton(Image buttonImg, double x, double y, boolean hover, String text, Color fallbackColor) {
        if (hover) {
            gc.setFill(Color.color(1, 1, 1, 0.3));
            gc.fillRoundRect(x - 6, y - 6, BTN_WIDTH + 12, BTN_HEIGHT + 12, 18, 18);
        }
        
        if (buttonImg != null) {
            gc.drawImage(buttonImg, x, y, BTN_WIDTH, BTN_HEIGHT);
        } else {
            gc.setFill(fallbackColor);
            gc.fillRoundRect(x, y, BTN_WIDTH, BTN_HEIGHT, 8, 8);
            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font(20));
            gc.fillText(text, x + getTextOffset(text), y + 38);
        }
    }
    
    private double getTextOffset(String text) {
        switch (text) {
            case "RESTART": return 60;
            case "MAIN MENU": return 50;
            case "EXIT GAME": return 50;
            default: return 50;
        }
    }
    
    @Override
    protected void handleHover(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        
        hoverRestart = isInButton(mx, my, restartX, restartY);
        hoverMenu = isInButton(mx, my, menuX, menuY);
        hoverExit = isInButton(mx, my, exitX, exitY);
        
        draw();
    }
    
    @Override
    protected void handleClick(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        
        if (isInButton(mx, my, restartX, restartY)) {
            hide();
            game.restartGame();
        } else if (isInButton(mx, my, menuX, menuY)) {
            hide();
            game.backToMenu();
        } else if (isInButton(mx, my, exitX, exitY)) {
            audioManager.cleanup();
            stage.close();
        }
    }
}