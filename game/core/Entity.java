package game.core;

import game.GameConfig;

public abstract class Entity extends GameObject implements Updatable {
    protected double velocityX;
    protected double velocityY;
    protected boolean onGround;
    protected boolean facingRight;
    
    public Entity(double x, double y, double width, double height) {
        super(x, y, width, height);
        this.velocityX = 0;
        this.velocityY = 0;
        this.onGround = false;
        this.facingRight = true;
    }
    
    protected void applyGravity() {
        velocityY += GameConfig.GRAVITY;
    }
    
    protected void applyVelocity() {
        x += velocityX;
        y += velocityY;
    }
    
    public double getVelocityX() {
        return velocityX;
    }
    
    public double getVelocityY() {
        return velocityY;
    }
    
    public void setVelocityX(double vx) {
        this.velocityX = vx;
    }
    
    public void setVelocityY(double vy) {
        this.velocityY = vy;
    }
    
    public boolean isOnGround() {
        return onGround;
    }
    
    public void setOnGround(boolean onGround) {
        this.onGround = onGround;
    }
    
    public boolean isFacingRight() {
        return facingRight;
    }
    
    public void setFacingRight(boolean facingRight) {
        this.facingRight = facingRight;
    }
    
    @Override
    public abstract void update();
}