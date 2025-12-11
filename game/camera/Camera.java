package game.camera;

import game.GameConfig;

public class Camera {
    private double x;
    
    public Camera() {
        this.x = 0;
    }
    
    public double getX() {
        return x;
    }
    
    public void setX(double x) {
        this.x = x;
    }
    
    public void update(double playerX, double worldWidth) {
        x = playerX - (GameConfig.WINDOW_WIDTH / 2);
        
        if (x < 0) {
            x = 0;
        }
        
        if (worldWidth > GameConfig.WINDOW_WIDTH && x > worldWidth - GameConfig.WINDOW_WIDTH) {
            x = worldWidth - GameConfig.WINDOW_WIDTH;
        }
    }
    
    public void centerOnPlayer(double playerX) {
        x = playerX - (GameConfig.WINDOW_WIDTH / 2);
        if (x < 0) {
            x = 0;
        }
    }
    
    public void reset() {
        x = 0;
    }
}