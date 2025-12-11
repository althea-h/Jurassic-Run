package menu;

import game.Menu;
import game.audio.AudioManager;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class HelpScreen extends AbstractMenu {
    private Menu previousMenu;
    private AudioManager audioManager;
    
    private Image helpImg;
    private Image backBtn;
    
    private double backX;
    private double backY;
    
    private boolean hoverBack;
    
    public HelpScreen(Stage stage, Menu previousMenu) {
        super(stage, 800, 600);
        
        this.previousMenu = previousMenu;
        this.audioManager = AudioManager.getInstance();
        
        root.setStyle("-fx-background-color: transparent;");
        
        calculateButtonPositions();
        loadImages();
        setupMouseHandlers();
        setupKeyHandlers();
        
        draw();
    }
    
    public HelpScreen(Stage stage) {
        this(stage, null);
    }
    
    private void calculateButtonPositions() {
        backX = (800 - 150) / 2;
        backY = 520;
        hoverBack = false;
    }
    
    private void loadImages() {
        helpImg = loadImage("/assets/background/help_screen.png");
        backBtn = loadImage("/assets/buttons/menu_button.png");
    }
    
    private void setupKeyHandlers() {
        root.setOnKeyPressed(this::handleKeyPress);
        root.setFocusTraversable(true);
    }
    
    @Override
    public void show() {
        System.out.println("Help screen shown");
        
        Pane currentRoot = (Pane) stage.getScene().getRoot();
        if (!currentRoot.getChildren().contains(root)) {
            currentRoot.getChildren().add(root);
        }
        root.requestFocus();
    }
    
    @Override
    public void hide() {
        Pane currentRoot = (Pane) stage.getScene().getRoot();
        currentRoot.getChildren().remove(root);
        
        System.out.println("Help screen hidden");
    }
    
    @Override
    protected void draw() {
        gc.setFill(Color.rgb(20, 24, 82));
        gc.fillRect(0, 0, 800, 600);
        
        if (helpImg != null) {
            gc.drawImage(helpImg, 0, 0, 800, 600);
        } else {
            drawFallbackHelp();
        }
        
        renderBackButton();
    }
    
    private void renderBackButton() {
        if (hoverBack) {
            gc.setFill(Color.color(1, 1, 1, 0.3));
            gc.fillRoundRect(backX - 6, backY - 6, 150 + 12, BTN_HEIGHT + 12, 18, 18);
        }
        
        if (backBtn != null) {
            gc.drawImage(backBtn, backX, backY, 150, BTN_HEIGHT);
        } else {
            gc.setFill(Color.CYAN);
            gc.fillRoundRect(backX, backY, 150, BTN_HEIGHT, 8, 8);
            gc.setFill(Color.BLACK);
            gc.setFont(javafx.scene.text.Font.font(20));
            gc.fillText("BACK", backX + 45, backY + 38);
        }
    }
    
    private void drawFallbackHelp() {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(36));
        gc.fillText("HOW TO PLAY", 280, 60);
        
        gc.setFont(javafx.scene.text.Font.font(18));
        
        gc.fillText("CONTROLS:", 50, 120);
        gc.setFont(javafx.scene.text.Font.font(16));
        gc.fillText("A / LEFT ARROW - Move Left", 80, 150);
        gc.fillText("D / RIGHT ARROW - Move Right", 80, 175);
        gc.fillText("W / UP / SPACE - Jump", 80, 200);
        gc.fillText("ESC / P - Pause Game", 80, 225);
        
        gc.setFont(javafx.scene.text.Font.font(18));
        gc.fillText("OBJECTIVE:", 50, 270);
        gc.setFont(javafx.scene.text.Font.font(16));
        gc.fillText("Collect 6 fossils per level", 80, 300);
        gc.fillText("Reach the gate to progress", 80, 325);
        gc.fillText("Complete all 4 levels to win!", 80, 350);
        
        gc.setFont(javafx.scene.text.Font.font(18));
        gc.fillText("COMBAT:", 420, 120);
        gc.setFont(javafx.scene.text.Font.font(16));
        gc.fillText("Jump on enemy heads to defeat them", 450, 150);
        gc.fillText("Avoid touching enemies from sides", 450, 175);
        gc.fillText("You have 3 lives", 450, 200);
        
        gc.setFont(javafx.scene.text.Font.font(18));
        gc.fillText("POWER-UPS:", 420, 270);
        gc.setFont(javafx.scene.text.Font.font(16));
        gc.fillText("Speed Boost - Move faster", 450, 300);
        gc.fillText("Jump Boost - Jump higher", 450, 325);
        gc.fillText("Invincibility - Immune to damage", 450, 350);
        
        gc.setFont(javafx.scene.text.Font.font(18));
        gc.fillText("TIPS:", 50, 400);
        gc.setFont(javafx.scene.text.Font.font(16));
        gc.fillText("• Explore to find all fossils", 80, 430);
        gc.fillText("• Use power-ups wisely", 80, 455);
        gc.fillText("• Death returns you to Level 1", 80, 480);
    }
    
    @Override
    protected void handleHover(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        
        hoverBack = (mx > backX && mx < backX + 150 &&
                     my > backY && my < backY + BTN_HEIGHT);
        
        draw();
    }
    
    @Override
    protected void handleClick(MouseEvent e) {
        double mx = e.getX();
        double my = e.getY();
        
        if (mx > backX && mx < backX + 150 &&
            my > backY && my < backY + BTN_HEIGHT) {
            goBack();
        }
    }
    
    private void handleKeyPress(KeyEvent e) {
        switch (e.getCode()) {
            case ESCAPE:
            case ENTER:
            case SPACE:
                goBack();
                break;
            default:
                break;
        }
    }
    
    private void goBack() {
        hide();
        
        audioManager.stopMusic();
        
        if (previousMenu != null) {
            audioManager.playMenuMusic();
            previousMenu.show();
        } else {
            System.out.println("Returning to game from help screen");
        }
        
        System.out.println("Returning from help screen");
    }
}