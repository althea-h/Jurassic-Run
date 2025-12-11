package menu;

import game.Game;
import game.audio.AudioManager;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class PauseMenu extends AbstractMenu {
    private Game game;
    private AudioManager audioManager;
    
    private Image resumeBtn;
    private Image restartBtn;
    private Image menuBtn;
    private Image exitBtn;
    
    private double resumeX;
    private double resumeY;
    private double restartX;
    private double restartY;
    private double menuX;
    private double menuY;
    private double exitX;
    private double exitY;
    
    private boolean hoverResume;
    private boolean hoverRestart;
    private boolean hoverMenu;
    private boolean hoverExit;
    
    public PauseMenu(Stage stage, Game game) {
        super(stage, 800, 600);
        
        this.game = game;
        this.audioManager = AudioManager.getInstance();
        
        root.setStyle("-fx-background-color: transparent;");
        canvas.setMouseTransparent(false);
        
        calculateButtonPositions();
        loadImages();
        setupMouseHandlers();
    }
    
    private void calculateButtonPositions() {
        resumeX = (800 - BTN_WIDTH) / 2;
        resumeY = 200;
        
        restartX = (800 - BTN_WIDTH) / 2;
        restartY = 280;
        
        menuX = (800 - BTN_WIDTH) / 2;
        menuY = 360;
        
        exitX = (800 - BTN_WIDTH) / 2;
        exitY = 440;
        
        hoverResume = false;
        hoverRestart = false;
        hoverMenu = false;
        hoverExit = false;
    }
    
    private void loadImages() {
        resumeBtn = loadImage("/assets/buttons/resume_button.png");
        restartBtn = loadImage("/assets/buttons/restart_button.png");
        menuBtn = loadImage("/assets/buttons/menu_button.png");
        exitBtn = loadImage("/assets/buttons/exit_button.png");
    }
    
    @Override
    public void show() {
        audioManager.playPauseMusic();
        draw();
        
        Pane currentRoot = (Pane) stage.getScene().getRoot();
        if (!currentRoot.getChildren().contains(root)) {
            currentRoot.getChildren().add(root);
        }
        
        System.out.println("Pause menu shown - playing pause music");
    }
    
    @Override
    public void hide() {
        audioManager.stopMusic();
        
        Pane currentRoot = (Pane) stage.getScene().getRoot();
        currentRoot.getChildren().remove(root);
        
        System.out.println("Pause menu hidden - resuming level music");
    }
    
    @Override
    protected void draw() {
        gc.clearRect(0, 0, 800, 600);
        
        renderButton(resumeBtn, resumeX, resumeY, hoverResume, "RESUME", Color.GREEN);
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
            case "RESUME": return 60;
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
        
        hoverResume = isInButton(mx, my, resumeX, resumeY);
        hoverRestart = isInButton(mx, my, restartX, restartY);
        hoverMenu = isInButton(mx, my, menuX, menuY);
        hoverExit = isInButton(mx, my, exitX, exitY);
        
        draw();
    }
    
    @Override
    protected void handleClick(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        
        if (isInButton(mx, my, resumeX, resumeY)) {
            game.resumeGame();
        } else if (isInButton(mx, my, restartX, restartY)) {
            game.restartGame();
        } else if (isInButton(mx, my, menuX, menuY)) {
            game.backToMenu();
        } else if (isInButton(mx, my, exitX, exitY)) {
            audioManager.cleanup();
            stage.close();
        }
    }
}