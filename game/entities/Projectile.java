package game.entities;

import game.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class Projectile extends GameObject {
    private double velocityX;
    private Image image;
    private boolean dead;
    
    public Projectile(double x, double y, double velocityX, Image image) {
        super(x, y, 32, 32);
        this.velocityX = velocityX;
        this.image = image;
        this.dead = false;
    }
    
    public void update() {
        x += velocityX;
        if (x < -100 || x > 9700) {
            dead = true;
        }
    }
    
    @Override
    public void render(GraphicsContext gc, double camX) {
        if (dead) return;
        double drawX = x - camX;
        if (image != null && !image.isError()) {
            gc.drawImage(image, drawX, y, width, height);
        } else {
            gc.setFill(Color.ORANGE);
            gc.fillRect(drawX, y, width, height);
        }
    }
    
    public boolean collides(double px, double py, double pw, double ph) {
        if (dead) return false;
        return collidesWith(px, py, pw, ph);
    }
    
    public boolean isDead() {
        return dead;
    }
    
    public void markForRemoval() {
        dead = true;
    }
}