package game;

import menu.AbstractMenu;
import menu.HelpScreen;
import game.audio.AudioManager;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class Menu extends AbstractMenu {
    private AudioManager audioManager;
    
    private Image background;
    private Image playButton;
    private Image helpButton;
    private Image exitButton;
    
    private double playX;
    private double playY;
    
    private double helpX;
    private double helpY;
    
    private double exitX;
    private double exitY;
    
    private boolean hoverPlay;
    private boolean hoverHelp;
    private boolean hoverExit;
    
    public Menu(Stage stage) {
        super(stage, 800, 600);
        
        Scene scene = new Scene(root);
        stage.setTitle("Jurassic Run - KKKCodeFusion");
        stage.setScene(scene);
        
        this.audioManager = AudioManager.getInstance();
        audioManager.playMenuMusic();
        
        calculateButtonPositions();
        loadMenuImages();
        
        setupMouseHandlers();
        
        drawMenu();
    }
    
    private void calculateButtonPositions() {
        playX = (800 - BTN_WIDTH) / 2;
        playY = 320;
        
        helpX = (800 - BTN_WIDTH) / 2;
        helpY = 400;
        
        exitX = (800 - BTN_WIDTH) / 2;
        exitY = 480;
        
        hoverPlay = false;
        hoverHelp = false;
        hoverExit = false;
    }
    
    private void loadMenuImages() {
        background = loadImage("/assets/background/menu_background.png");
        playButton = loadImage("/assets/buttons/play_button.png");
        helpButton = loadImage("/assets/buttons/help_button.png");
        exitButton = loadImage("/assets/buttons/exit_button.png");
    }
    
    @Override
    public void show() {
        stage.show();
    }
    
    @Override
    protected void draw() {
        drawMenu();
    }
    
    private void drawMenu() {
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, 800, 600);
        
        if (background != null) {
            gc.drawImage(background, 0, 0, 800, 600);
        }
        
        renderButton(playButton, playX, playY, hoverPlay, "PLAY");
        renderButton(helpButton, helpX, helpY, hoverHelp, "HELP");
        renderButton(exitButton, exitX, exitY, hoverExit, "EXIT");
    }
    
    private void renderButton(Image buttonImg, double x, double y, boolean hover, String text) {
        if (hover) {
            gc.setFill(Color.color(1, 1, 1, 0.35));
            gc.fillRoundRect(x - 6, y - 6, BTN_WIDTH + 12, BTN_HEIGHT + 12, 18, 18);
        }
        
        if (buttonImg != null) {
            gc.drawImage(buttonImg, x, y, BTN_WIDTH, BTN_HEIGHT);
        } else {
            Color btnColor = getButtonColor(text);
            gc.setFill(btnColor);
            gc.fillRoundRect(x, y, BTN_WIDTH, BTN_HEIGHT, 8, 8);
            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font(20));
            gc.fillText(text, x + getTextOffset(text), y + 38);
        }
    }
    
    private Color getButtonColor(String text) {
        switch (text) {
            case "PLAY": return Color.GREEN;
            case "HELP": return Color.CYAN;
            case "EXIT": return Color.RED;
            default: return Color.GRAY;
        }
    }
    
    private double getTextOffset(String text) {
        switch (text) {
            case "PLAY": return 50;
            case "HELP": return 45;
            case "EXIT": return 50;
            default: return 40;
        }
    }
    
    @Override
    protected void handleHover(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        
        hoverPlay = isInButton(mx, my, playX, playY);
        hoverHelp = isInButton(mx, my, helpX, helpY);
        hoverExit = isInButton(mx, my, exitX, exitY);
        
        drawMenu();
    }
    
    @Override
    protected void handleClick(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        
        if (isInButton(mx, my, playX, playY)) {
            System.out.println("Play Clicked!");
            audioManager.stopMusic();
            new Game(stage);
            stage.show();
        }
        
        if (isInButton(mx, my, helpX, helpY)) {
            System.out.println("Help Clicked!");
            audioManager.playHelpMusic();
            HelpScreen helpScreen = new HelpScreen(stage, this);
            helpScreen.show();
        }
        
        if (isInButton(mx, my, exitX, exitY)) {
            System.out.println("Exit Clicked!");
            audioManager.cleanup();
            stage.close();
        }
    }
    
    public void returnToMenu() {
        audioManager.stopMusic();
        audioManager.playMenuMusic();
        stage.show();
    }
    
    public AudioManager getAudioManager() {
        return audioManager;
    }
}