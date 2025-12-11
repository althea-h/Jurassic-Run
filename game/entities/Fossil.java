package game.entities;

import game.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Fossil extends Collectible {
    
    public Fossil(double x, double y, String imgPath) {
        super(x, y, GameConfig.FOSSIL_SIZE, imgPath);
        System.out.println("Loaded fossil: " + imgPath + " at (" + x + ", " + y + ")");
    }
    
    @Override
    protected void onCollected() {
        System.out.println("Fossil collected at (" + x + ", " + y + ")");
    }
    
    @Override
    protected void renderFallback(GraphicsContext gc, double drawX, double drawY) {
        gc.setFill(Color.LIGHTBLUE);
        gc.fillRect(drawX, drawY, size, size);
        gc.setStroke(Color.WHITE);
        gc.strokeRect(drawX, drawY, size, size);
        
        gc.setFill(Color.BLACK);
        gc.fillText("F", drawX + size/2 - 3, drawY + size/2 + 3);
    }
    
    public boolean intersects(double px, double py, double pw, double ph) {
        return !collected && collidesWith(px, py, pw, ph);
    }
}