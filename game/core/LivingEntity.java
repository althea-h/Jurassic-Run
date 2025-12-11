package game.core;

public abstract class LivingEntity extends Entity {
    protected int health;
    protected int maxHealth;
    protected boolean dead;
    protected boolean invincible;
    protected long invincibleStart;
    
    public LivingEntity(double x, double y, double width, double height, int maxHealth) {
        super(x, y, width, height);
        this.maxHealth = maxHealth;
        this.health = maxHealth;
        this.dead = false;
        this.invincible = false;
        this.invincibleStart = 0;
    }
    
    public void takeDamage(int amount) {
        if (invincible || dead) return;
        
        health -= amount;
        if (health <= 0) {
            health = 0;
            die();
        } else {
            onDamaged();
        }
    }
    
    public void heal(int amount) {
        if (dead) return;
        health = Math.min(health + amount, maxHealth);
    }
    
    protected abstract void die();
    
    protected abstract void onDamaged();
    
    public boolean isDead() {
        return dead;
    }
    
    public int getHealth() {
        return health;
    }
    
    public int getMaxHealth() {
        return maxHealth;
    }
    
    public boolean isInvincible() {
        return invincible;
    }
    
    public void setInvincible(boolean invincible) {
        this.invincible = invincible;
        if (invincible) {
            this.invincibleStart = System.nanoTime();
        }
    }
}