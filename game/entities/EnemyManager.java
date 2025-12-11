package game.entities;

import game.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import java.util.ArrayList;

// centralized manager for all enemies in the current level
public class EnemyManager {
    private ArrayList<Enemy> enemies; // All active enemies
    private Player player; // Reference to player for tracking
    
    private int playerLives; // Cached player life count
    private boolean playerDead; // Is player currently dead
    private int defaultPatrolDistance; // Default patrol range for enemies
    
    //  initializes manager with player reference
    public EnemyManager(Player player) {
        this.player = player;
        this.enemies = new ArrayList<>();
        this.playerLives = GameConfig.PLAYER_STARTING_LIVES;
        this.playerDead = false;
        this.defaultPatrolDistance = GameConfig.DEFAULT_PATROL_DISTANCE;
    }
    
    // set new enemy list (called when loading level)
    public void setEnemies(ArrayList<Enemy> list) {
        enemies = list;
        // apply default patrol distance to all enemies
        for (Enemy enemy : enemies) {
            if (enemy != null) {
                enemy.setPatrolDistance(defaultPatrolDistance);
            }
        }
        System.out.println("EnemyManager: Set " + enemies.size() + " enemies with " + defaultPatrolDistance + "-block patrol");
    }
    
    // add single enemy to manager
    public void addEnemy(Enemy enemy) {
        if (enemy != null) {
            enemy.setPatrolDistance(defaultPatrolDistance);
            enemies.add(enemy);
            System.out.println("Added " + enemy.getType() + " with " + defaultPatrolDistance + "-block patrol");
        }
    }
    
    // set patrol distance for all current and future enemies
    public void setPatrolDistance(int blocks) {
        this.defaultPatrolDistance = blocks;
        for (Enemy enemy : enemies) {
            if (enemy != null) {
                enemy.setPatrolDistance(blocks);
            }
        }
        System.out.println("All enemies set to " + blocks + "-block patrol");
    }
    
    public ArrayList<Enemy> getEnemies() {
        return enemies;
    }
    
    public int getPlayerLives() {
        return playerLives;
    }
    
    public boolean isPlayerDead() {
        return playerDead;
    }
    
    // handles all enemy behavior and interactions
    public void update(ArrayList<Platform> platforms) {
        if (playerDead || player == null) return; // don't update if player dead
        
        ArrayList<Enemy> toRemove = new ArrayList<>(); // track enemies to remove
        
        for (Enemy e : enemies) {
            if (e == null) continue;
            
            // update enemy AI and physics
            e.updateWithPlatformsAndPlayer(platforms, player.getX());
            
            // remove enemies with completed death animation
            if (e.isDead() && e.isDeathAnimComplete()) {
                toRemove.add(e);
                continue;
            }
            
            // check collision with player
            if (!e.isDead() && e.overlaps(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                handleEnemyCollision(e);
            }
            
            // check projectile collisions (T-Rex fireballs)
            if (e.getProjectiles() != null) {
                ArrayList<Projectile> projectilesToRemove = new ArrayList<>();
                
                for (Projectile p : e.getProjectiles()) {
                    if (p == null) continue;
                    
                    p.update(); // update projectile position
                    
                    // remove if dead or off-screen
                    if (p.isDead() || p.getX() < -100 || p.getX() > 10000) {
                        projectilesToRemove.add(p);
                        continue;
                    }
                    
                    // check if projectile hit player
                    if (p.collides(player.getX(), player.getY(), player.getWidth(), player.getHeight())) {
                        p.markForRemoval();
                        projectilesToRemove.add(p);
                        handleProjectileHit();
                    }
                }
                
                e.getProjectiles().removeAll(projectilesToRemove);
            }
        }
        
        // clean up dead enemies
        if (!toRemove.isEmpty()) {
            enemies.removeAll(toRemove);
            System.out.println("Removed " + toRemove.size() + " dead enemies. Remaining: " + enemies.size());
        }
        
        // update cached player state
        if (player != null) {
            playerLives = player.getLives();
            if (playerLives <= 0) {
                playerDead = true;
            }
        }
    }
    
    // handle collision between enemy and player
    private void handleEnemyCollision(Enemy enemy) {
        if (player == null || player.isInvincible() || player.isDead()) return;
        
        // check if player is stomping on enemy head
        if (isPlayerStomping(enemy)) {
            enemy.damage(); // Kill enemy
            player.killEnemy(); // Bounce player
            System.out.println("Enemy stomped: " + enemy.getType());
        } else {
            // player hit from side - take damage
            damagePlayer();
            applyKnockback(enemy);
        }
    }
    
    // check if player is landing on enemy head from above
    private boolean isPlayerStomping(Enemy enemy) {
        if (player == null || enemy == null) return false;
        
        double playerBottom = player.getY() + player.getHeight();
        double enemyTop = enemy.getY();
        double playerVelY = player.getVelocityY();
        
        // player must be falling, landing on top third of enemy
        return playerVelY > 0 &&
               playerBottom >= enemyTop && 
               playerBottom <= enemyTop + 20 &&
               player.getX() + player.getWidth() > enemy.getX() + 10 &&
               player.getX() < enemy.getX() + enemy.getWidth() - 10;
    }
    
    // apply knockback to player based on enemy position
    private void applyKnockback(Enemy enemy) {
        if (player == null) return;
        
        // push player away from enemy
        double knockbackX = (player.getX() < enemy.getX()) ? -8 : 8;
        double knockbackY = -5;
        
        player.knockback(knockbackX, knockbackY);
    }
    
    // handle player being hit by projectile
    private void handleProjectileHit() {
        if (player == null || player.isInvincible() || player.isDead()) return;
        
        damagePlayer();
        System.out.println("Hit by projectile! Lives: " + playerLives);
    }
    
    // damage player and check for death
    private void damagePlayer() {
        if (player == null || player.isInvincible() || player.isDead()) return;
        
        player.takeDamage(1);
        playerLives = player.getLives();
        
        System.out.println("Player damaged! Lives: " + playerLives);
        
        if (playerLives <= 0) {
            playerDead = true;
            System.out.println("Player died!");
        }
    }
    
    // render all enemies and projectiles
    public void render(GraphicsContext gc, double camX) {
        if (gc == null) return;
        
        // render all enemies
        for (Enemy e : enemies) {
            if (e != null) {
                e.render(gc, camX);
            }
        }
        
        // render all projectiles
        for (Enemy e : enemies) {
            if (e != null && e.getProjectiles() != null) {
                for (Projectile p : e.getProjectiles()) {
                    if (p != null && !p.isDead()) {
                        p.render(gc, camX);
                    }
                }
            }
        }
    }
    
    // reset manager state
    public void reset() {
        playerLives = GameConfig.PLAYER_STARTING_LIVES;
        playerDead = false;
        System.out.println("EnemyManager reset - ready for new enemies");
    }
    
    // clear all enemies from manager
    public void clearEnemies() {
        enemies.clear();
        System.out.println("Cleared all enemies");
    }
    
    public int getEnemyCount() {
        return enemies.size();
    }
    
    public int getPatrolDistance() {
        return defaultPatrolDistance;
    }
    
    // debug output for all enemies
    public void debugEnemyInfo() {
        System.out.println("=== ENEMY MANAGER DEBUG ===");
        System.out.println("Total enemies: " + enemies.size());
        System.out.println("Player lives: " + playerLives);
        System.out.println("Player dead: " + playerDead);
        System.out.println("Patrol distance: " + defaultPatrolDistance + " blocks");
        
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            if (e != null) {
                e.debugInfo();
            }
        }
    }
    
    // check if any enemies are near a position
    public boolean hasEnemiesNear(double x, double y, double radius) {
        for (Enemy e : enemies) {
            if (e != null && !e.isDead()) {
                double dx = e.getX() - x;
                double dy = e.getY() - y;
                double distance = Math.sqrt(dx * dx + dy * dy);
                if (distance < radius) {
                    return true;
                }
            }
        }
        return false;
    }
    
    // render debug visualization
    public void renderDebug(GraphicsContext gc, double camX) {
        if (gc == null) return;
        
        gc.setFill(Color.RED);
        gc.setFont(Font.font(12));
        
        for (Enemy e : enemies) {
            if (e != null) {
                double screenX = e.getX() - camX;
                double screenY = e.getY();
                
                // Draw debug marker
                gc.fillRect(screenX, screenY, 10, 10);
                
                // Show enemy info
                gc.fillText(e.getType() + " (" + e.getX() + "," + e.getY() + ")", screenX, screenY - 5);
                gc.fillText("Alive: " + !e.isDead(), screenX, screenY - 20);
                
                // Show patrol range
                gc.setStroke(Color.YELLOW);
                gc.setLineWidth(1);
                double patrolStartX = e.getX() - camX;
                double patrolEndX = patrolStartX + (defaultPatrolDistance * GameConfig.BLOCK_SIZE);
                gc.strokeLine(patrolStartX, screenY - 30, patrolEndX, screenY - 30);
            }
        }
    }
}