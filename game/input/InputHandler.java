package game.input;

import game.entities.Player;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;

public class InputHandler {
    private Player player;
    private Runnable onPause;
    private Runnable onRestart;
    private Runnable onMenu;
    
    private boolean gameOver;
    private boolean gameComplete;
    
    public InputHandler(Player player) {
        this.player = player;
        this.gameOver = false;
        this.gameComplete = false;
    }
    
    public void setOnPause(Runnable onPause) {
        this.onPause = onPause;
    }
    
    public void setOnRestart(Runnable onRestart) {
        this.onRestart = onRestart;
    }
    
    public void setOnMenu(Runnable onMenu) {
        this.onMenu = onMenu;
    }
    
    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }
    
    public void setGameComplete(boolean gameComplete) {
        this.gameComplete = gameComplete;
    }
    
    public void handleKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();
        
        switch (code) {
            case A:
            case LEFT:
                if (player != null) player.startMoveLeft();
                break;
            case D:
            case RIGHT:
                if (player != null) player.startMoveRight();
                break;
            case SPACE:
            case W:
            case UP:
                if (player != null) player.jump();
                break;
            case ESCAPE:
            case P:
                if (onPause != null) onPause.run();
                break;
            case R:
                if ((gameOver || gameComplete) && onRestart != null) {
                    onRestart.run();
                }
                break;
            case M:
                if ((gameOver || gameComplete) && onMenu != null) {
                    onMenu.run();
                }
                break;
            default:
                break;
        }
    }
    
    public void handleKeyReleased(KeyEvent e) {
        KeyCode code = e.getCode();
        
        switch (code) {
            case A:
            case LEFT:
            case D:
            case RIGHT:
                if (player != null) player.stopMove();
                break;
            default:
                break;
        }
    }
}