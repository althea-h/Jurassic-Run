package game.entities;

import game.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class PowerUp extends Collectible {
    public enum Type { SPEED, JUMP, INVINCIBLE }
    
    private Type type;
    
    public PowerUp(double x, double y, Type type, String imgPath) {
        super(x, y, GameConfig.POWERUP_SIZE, imgPath);
        this.type = type;
        System.out.println("Loaded power-up: " + type + " at (" + x + ", " + y + ")");
    }
    
    public void update() {
    }
    
    public boolean overlaps(double px, double py, double pw, double ph) {
        return !collected && collidesWith(px, py, pw, ph);
    }
    
    public Type getType() {
        return type;
    }
    
    public void reset() {
        collected = false;
    }
    
    @Override
    protected void onCollected() {
        System.out.println("Power-up collected: " + type);
    }
    
    @Override
    protected void renderFallback(GraphicsContext gc, double drawX, double drawY) {
        Color color;
        String symbol = "";
        
        switch (type) {
            case SPEED:
                color = Color.CYAN;
                symbol = "S";
                break;
            case JUMP:
                color = Color.GREEN;
                symbol = "J";
                break;
            case INVINCIBLE:
                color = Color.YELLOW;
                symbol = "I";
                break;
            default:
                color = Color.PURPLE;
                symbol = "?";
                break;
        }
        
        gc.setFill(color);
        gc.fillOval(drawX, drawY, size, size);
        
        gc.setStroke(Color.WHITE);
        gc.strokeOval(drawX, drawY, size, size);
        
        gc.setFill(Color.BLACK);
        gc.fillText(symbol, drawX + size/2 - 3, drawY + size/2 + 3);
    }
    
    @Override
    public void render(GraphicsContext gc, double cameraX) {
        if (collected) return;
        
        double drawX = x - cameraX;
        
        if (image != null && !image.isError()) {
            gc.drawImage(image, drawX, y, size, size);
        } else {
            renderFallback(gc, drawX, y);
        }
        
        long time = System.currentTimeMillis();
        double pulse = Math.sin(time * 0.01) * 0.2 + 0.8;
        gc.setGlobalAlpha(pulse);
        gc.setGlobalAlpha(1.0);
    }
}