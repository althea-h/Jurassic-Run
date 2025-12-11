package game.entities;

import game.core.LivingEntity;
import game.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.io.InputStream;

import java.util.ArrayList;

// handles all enemy types with different behaviors and animations
public class Enemy extends LivingEntity {
    private final String type; // Enemy type (raptor, pterodactyl, triceratops, trex)
    
    private double speed; // Movement speed
    private Image[] frames; // Animation frames for this enemy
    private int frameIndex; // Current animation frame
    private long lastAnimTime; // Last time animation frame changed
    private long animDelay; // Time between animation frames
    
    private Image fireImg; // Projectile image for T-Rex
    private final ArrayList<Projectile> projectiles; // Active projectiles
    
    private long lastShotTime; // Last time projectile was fired
    private final long shotCooldown; // Minimum time between shots
    
    private double scale; // Size multiplier for enemy
    private double scaledWidth; // Width after scaling
    private double scaledHeight; // Height after scaling
    
    private boolean deathAnimStarted; // Has death animation begun
    private int deathFrameIndex; // Current death animation frame
    private long lastDeathAnimTime; // Last death frame change time
    private final long deathAnimDelay; // Death animation frame duration
    private Image[] deathFrames; // Death animation frames
    
    // Patrol system - enemies move back and forth in a defined area
    private double patrolStartX; // Left boundary of patrol area
    private double patrolEndX; // Right boundary of patrol area
    private int patrolDistance; // Patrol range in blocks
    private boolean movingRight; // Current movement direction
    private int movementCounter; // Tracks distance moved (for turning)
    
    // Constructor - creates enemy at position with specified type
    public Enemy(String type, double x, double y) {
        super(x, y, 48, 48, 1); // Parent constructor with position, size, health
        
        this.type = type.toLowerCase();
        this.speed = 0.3; // Default speed
        this.scale = 1.0; // Default scale
        this.frameIndex = 0;
        this.lastAnimTime = 0;
        this.animDelay = GameConfig.ANIMATION_DELAY;
        
        this.projectiles = new ArrayList<>();
        this.lastShotTime = 0;
        this.shotCooldown = 2_000_000_000L; // 2 seconds in nanoseconds
        
        this.deathAnimStarted = false;
        this.deathFrameIndex = 0;
        this.lastDeathAnimTime = 0;
        this.deathAnimDelay = 150_000_000L; // 150ms per death frame
        
        // Initialize patrol system
        this.patrolStartX = x;
        this.patrolDistance = GameConfig.DEFAULT_PATROL_DISTANCE;
        this.patrolEndX = x + (patrolDistance * GameConfig.BLOCK_SIZE);
        this.movingRight = true; // Start moving right
        this.movementCounter = 0;
        this.facingRight = true;
        
        setDefaultSizeForType(); // Set size based on enemy type
        applyScale(); // Calculate scaled dimensions
        loadFrames(); // Load movement animation
        loadDeathFrames(); // Load death animation
        loadSpecial(); // Load special features (projectiles)
        
        this.velocityX = Math.abs(speed);
        
        System.out.println("Created " + type + " enemy at (" + x + ", " + y + ") with speed " + speed);
        System.out.println("  Patrol range: " + patrolStartX + " to " + patrolEndX + " (" + patrolDistance + " blocks)");
    }
    
    // Set default size and properties for each enemy type
    private void setDefaultSizeForType() {
        switch (type) {
            case "raptor":
                width = 74;
                height = 48;
                speed = 0.3; // Fast ground enemy
                animDelay = 100_000_000L; // Quick animation
                break;
            case "pterodactyl":
                width = 74;
                height = 46;
                speed = 0.4; // Flying enemy
                animDelay = 80_000_000L; // Faster animation (flapping wings)
                break;
            case "triceratops":
                width = 56;
                height = 48;
                speed = 0.2; // Slow tank enemy
                animDelay = 140_000_000L; // Slower animation
                break;
            case "trex":
                width = 80;
                height = 72;
                speed = 0.4; // Fast boss enemy
                animDelay = 160_000_000L; // Slower animation (big dinosaur)
                break;
            default:
                width = 48;
                height = 48;
                speed = 0.3;
                animDelay = 120_000_000L;
                break;
        }
    }
    
    // Load death animation frames - all enemies have 3-frame death animation
    private void loadDeathFrames() {
        try {
            deathFrames = new Image[3];
            String basePath = getDeathAnimationPath();
            
            for (int i = 0; i < 3; i++) {
                String path = basePath + (i + 1) + ".png";
                deathFrames[i] = loadEnemyImage(path);
                if (deathFrames[i] == null) {
                    System.out.println("Failed to load death frame: " + path);
                }
            }
            System.out.println("Loaded 3 death frames for " + type + " from path: " + basePath);
        } catch (Exception e) {
            System.out.println("Error loading death frames for " + type + ": " + e.getMessage());
            createDeathPlaceholders();
        }
    }

    private String getDeathAnimationPath() {
        switch (type) {
            case "raptor":
                return "/assets/enemies/raptor/death";
            case "pterodactyl":
                return "/assets/enemies/ptero/death";
            case "triceratops":
                return "/assets/enemies/trike/death";
            case "trex":
                return "/assets/enemies/trex/death";
            default:
                return "/assets/enemies/default/death";
        }
    }
    
    // Create placeholder death frames if loading fails
    private void createDeathPlaceholders() {
        deathFrames = new Image[3];
        System.out.println("Created placeholder death frames for " + type);
    }
    
    // Set scale multiplier - used to make enemies bigger or smaller
    public void setScale(double scale) {
        this.scale = scale;
        applyScale();
    }
    
    public double getScale() {
        return scale;
    }
    
    // Calculate scaled dimensions
    private void applyScale() {
        this.scaledWidth = width * scale;
        this.scaledHeight = height * scale;
    }
    
    // Override width/height getters to return scaled values
    @Override
    public double getWidth() {
        return scaledWidth;
    }
    
    @Override
    public double getHeight() {
        return scaledHeight;
    }
    
    // Load movement animation frames based on enemy type
    private void loadFrames() {
        try {
            switch (type) {
                case "raptor":
                    frames = loadSequence("/assets/enemies/raptor", 3);
                    break;
                case "pterodactyl":
                    frames = loadSequence("/assets/enemies/ptero", 5);
                    break;
                case "triceratops":
                    frames = loadSequence("/assets/enemies/trike", 5);
                    break;
                case "trex":
                    frames = loadSequence("/assets/enemies/trex", 5);
                    break;
                default:
                    frames = null;
                    break;
            }
            
            if (frames != null && frames[0] != null) {
                System.out.println("Loaded " + frames.length + " frames for " + type);
            } else {
                System.out.println("WARNING: No frames loaded for " + type);
            }
        } catch (Exception e) {
            System.out.println("Enemy frame load error for " + type + ": " + e.getMessage());
            frames = null;
        }
    }

    private Image[] loadSequence(String basePath, int count) {
        Image[] arr = new Image[count];
        for (int i = 0; i < count; i++) {
            try {
                String path = basePath + (i + 1) + ".png";
                arr[i] = loadEnemyImage(path);
                if (arr[i] == null) {
                    System.out.println("Failed to load enemy image: " + path);
                }
            } catch (Exception e) {
                System.out.println("Error loading enemy frame " + (i+1) + ": " + e.getMessage());
                arr[i] = null;
            }
        }
        return arr;
    }
    
    
    // Load special features (projectile image for T-Rex)
    private void loadSpecial() {
        if ("trex".equals(type)) {
            try {
                fireImg = loadEnemyImage("/assets/images/fire.png");
                if (fireImg != null) {
                    System.out.println("Loaded fire projectile for T-Rex");
                } else {
                    System.out.println("FAILED to load fire image");
                }
            } catch (Exception e) {
                System.out.println("Error loading fire image: " + e.getMessage());
                fireImg = null;
            }
        }
    }
    
    public String getType() {
        return type;
    }
    
    public double getSpeed() {
        return Math.abs(speed);
    }
    
    public ArrayList<Projectile> getProjectiles() {
        return projectiles;
    }
    
    // Check if death animation has completed (all 3 frames shown)
    public boolean isDeathAnimComplete() {
        return dead && deathAnimStarted && deathFrameIndex >= 3;
    }
    
    @Override
    protected void die() {
        dead = true;
        deathAnimStarted = true;
        deathFrameIndex = 0;
        lastDeathAnimTime = System.nanoTime();
        System.out.println(type + " enemy died! Starting death animation...");
    }
    
    @Override
    protected void onDamaged() {
        // Enemies die in one hit, so this method isn't used
    }
    
    // Public method to damage enemy (called when player stomps on head)
    public void damage() {
        takeDamage(1);
    }
    
    // Set patrol distance in blocks - enemy will patrol this many blocks left/right
    public void setPatrolDistance(int blocks) {
        this.patrolDistance = blocks;
        this.patrolStartX = x;
        this.patrolEndX = x + (patrolDistance * GameConfig.BLOCK_SIZE);
        this.movementCounter = 0;
        System.out.println(type + " patrol distance set to " + blocks + " blocks");
    }
    
    // Basic update without platforms - simple back-and-forth movement
    @Override
    public void update() {
        if (dead) {
            updateDeathAnimation();
            return;
        }
        
        applyGravity(); // All enemies affected by gravity
        
        // Pterodactyls have reduced gravity (flying)
        if ("pterodactyl".equals(type)) {
            velocityY *= 0.8; // Dampen vertical velocity
        }
        
        // Patrol movement - move back and forth between boundaries
        if (movingRight) {
            x += Math.abs(speed);
            facingRight = true;
            movementCounter++;
            
            // Turn around when reaching patrol end
            if (x >= patrolEndX || movementCounter >= (patrolDistance * (GameConfig.BLOCK_SIZE / Math.abs(speed)))) {
                movingRight = false;
                movementCounter = 0;
                facingRight = false;
                System.out.println(type + " reached patrol end, turning left");
            }
        } else {
            x -= Math.abs(speed);
            facingRight = false;
            movementCounter++;
            
            // Turn around when reaching patrol start
            if (x <= patrolStartX || movementCounter >= (patrolDistance * (GameConfig.BLOCK_SIZE / Math.abs(speed)))) {
                movingRight = true;
                movementCounter = 0;
                facingRight = true;
                System.out.println(type + " reached patrol start, turning right");
            }
        }
        
        y += velocityY; // Apply vertical movement
        
        animate(); // Update animation frame
        updateProjectiles(); // Update any active projectiles
    }
    
    // Advanced update with platform collision and player tracking
    public void updateWithPlatformsAndPlayer(ArrayList<Platform> platforms, double playerX) {
        if (dead) {
            updateDeathAnimation();
            return;
        }
        
        applyGravity();
        
        // Pterodactyls float (reduced gravity)
        if ("pterodactyl".equals(type)) {
            velocityY *= 0.8;
        }
        
        double oldX = x; // Store old position for collision detection
        double oldY = y;
        
        // Patrol movement logic (same as basic update)
        if (movingRight) {
            x += Math.abs(speed);
            facingRight = true;
            movementCounter++;
            
            if (x >= patrolEndX || movementCounter >= (patrolDistance * (GameConfig.BLOCK_SIZE / Math.abs(speed)))) {
                movingRight = false;
                movementCounter = 0;
                facingRight = false;
                System.out.println(type + " reached patrol end, turning left");
            }
        } else {
            x -= Math.abs(speed);
            facingRight = false;
            movementCounter++;
            
            if (x <= patrolStartX || movementCounter >= (patrolDistance * (GameConfig.BLOCK_SIZE / Math.abs(speed)))) {
                movingRight = true;
                movementCounter = 0;
                facingRight = true;
                System.out.println(type + " reached patrol start, turning right");
            }
        }
        
        y += velocityY;
        
        checkPlatformCollision(platforms, oldX, oldY); // Handle collisions
        updateAI(platforms); // Check for edges and obstacles
        
        // T-Rex shoots fireballs at player
        if ("trex".equals(type) && fireImg != null) {
            checkShooting(playerX);
        }
        
        animate();
        updateProjectiles();
    }
    
    // Update death animation frames
    private void updateDeathAnimation() {
        if (!deathAnimStarted) return;
        
        long now = System.nanoTime();
        // Advance to next death frame after delay
        if (now - lastDeathAnimTime > deathAnimDelay && deathFrameIndex < 3) {
            deathFrameIndex++;
            lastDeathAnimTime = now;
            System.out.println(type + " death animation frame: " + deathFrameIndex + "/3");
        }
    }
    
    // Check and resolve platform collisions
    private void checkPlatformCollision(ArrayList<Platform> platforms, double oldX, double oldY) {
        boolean supported = false; // Is enemy standing on something?
        
        for (Platform p : platforms) {
            double px = p.getX();
            double py = p.getY();
            double pSize = Platform.SIZE;
            
            // Check if enemy overlaps with platform
            if (x + scaledWidth > px && x < px + pSize &&
                y + scaledHeight > py && y < py + pSize) {
                
                // Calculate overlap amounts from each direction
                double overlapLeft = (oldX + scaledWidth) - px;
                double overlapRight = (px + pSize) - oldX;
                double overlapTop = (oldY + scaledHeight) - py;
                double overlapBottom = (py + pSize) - oldY;
                
                // Find smallest overlap (most likely collision direction)
                double minOverlap = Math.min(Math.min(overlapLeft, overlapRight), 
                                           Math.min(overlapTop, overlapBottom));
                
                if (minOverlap == overlapTop && velocityY >= 0) {
                    // Landing on top of platform
                    y = py - scaledHeight;
                    velocityY = 0;
                    supported = true;
                    onGround = true;
                } else if (minOverlap == overlapBottom && velocityY < 0) {
                    // Hit ceiling
                    y = py + pSize;
                    velocityY = 0;
                } else if (minOverlap == overlapLeft && movingRight) {
                    // Hit wall from left while moving right
                    x = px - scaledWidth;
                    teleportTurn(); // Turn around when hitting obstacle
                } else if (minOverlap == overlapRight && !movingRight) {
                    // Hit wall from right while moving left
                    x = px + pSize;
                    teleportTurn();
                }
            }
        }
        
        // Pterodactyls ignore ground checks (they fly)
        if (!"pterodactyl".equals(type)) {
            onGround = supported;
        }
    }
    
    // Turn around when hitting obstacle (called "teleport turn" because it's instant)
    private void teleportTurn() {
        movingRight = !movingRight;
        movementCounter = 0;
        facingRight = movingRight;
        System.out.println(type + " teleport turn due to obstacle, now moving " + (movingRight ? "right" : "left"));
    }
    
    // AI logic - check for edges and boundaries
    private void updateAI(ArrayList<Platform> platforms) {
        // Pterodactyls use simple patrol (they fly, no ground needed)
        if ("pterodactyl".equals(type)) {
            if (movingRight) {
                x += Math.abs(speed);
                facingRight = true;
                movementCounter++;
                
                if (x >= patrolEndX || movementCounter >= (patrolDistance * (GameConfig.BLOCK_SIZE / Math.abs(speed)))) {
                    movingRight = false;
                    movementCounter = 0;
                    facingRight = false;
                }
            } else {
                x -= Math.abs(speed);
                facingRight = false;
                movementCounter++;
                
                if (x <= patrolStartX || movementCounter >= (patrolDistance * (GameConfig.BLOCK_SIZE / Math.abs(speed)))) {
                    movingRight = true;
                    movementCounter = 0;
                    facingRight = true;
                }
            }
            
            // Stay within world bounds
            if (x < 0) {
                x = 0;
                movingRight = true;
                facingRight = true;
                movementCounter = 0;
            } else if (x > 9600 - scaledWidth) {
                x = 9600 - scaledWidth;
                movingRight = false;
                facingRight = false;
                movementCounter = 0;
            }
            return;
        }
        
        // Ground enemies turn around at edges (no ground ahead)
        boolean hasGroundAhead = checkGroundAhead(platforms);
        
        if (!hasGroundAhead && onGround) {
            teleportTurn(); // Turn around to avoid falling off edge
        }
        
        // Keep within world bounds
        if (x < 0) {
            x = 0;
            movingRight = true;
            facingRight = true;
            movementCounter = 0;
        } else if (x > 9600 - scaledWidth) {
            x = 9600 - scaledWidth;
            movingRight = false;
            facingRight = false;
            movementCounter = 0;
        }
    }
    
    // Look ahead to see if there's ground in front of enemy
    private boolean checkGroundAhead(ArrayList<Platform> platforms) {
        double lookAheadDistance = 20; // Check 20 pixels ahead
        double probeX = movingRight ? (x + scaledWidth + lookAheadDistance) : (x - lookAheadDistance);
        double probeY = y + scaledHeight + 10; // Check slightly below feet
        
        // See if probe point intersects any platform
        for (Platform p : platforms) {
            double px = p.getX();
            double py = p.getY();
            double pSize = Platform.SIZE;
            
            if (probeX >= px && probeX <= px + pSize && 
                probeY >= py && probeY <= py + pSize) {
                return true; // Ground detected ahead
            }
        }
        return false; // No ground ahead (edge detected)
    }
    
    // T-Rex shooting logic - fires at player when in range
    private void checkShooting(double playerX) {
        double distance = Math.abs(playerX - x);
        boolean sameSide = (facingRight && playerX > x) || (!facingRight && playerX < x);
        
        // Fire if player in range (200-600 pixels) and on same side
        if (distance < 600 && sameSide && distance > 200) {
            System.out.println("T-Rex CAN shoot - Distance: " + distance + ", Same side: " + sameSide);
            shootFire();
        } else {
            // Debug output for why not shooting
            if (distance >= 600) {
                System.out.println("T-Rex TOO FAR - Distance: " + distance + " (needs < 600)");
            } else if (distance <= 200) {
                System.out.println("T-Rex TOO CLOSE - Distance: " + distance + " (needs > 200)");
            } else if (!sameSide) {
                System.out.println("T-Rex WRONG SIDE - Facing: " + (facingRight ? "right" : "left") + ", Player X: " + playerX);
            }
        }
    }
    
    // Fire projectile toward player
    private void shootFire() {
        long now = System.nanoTime();
        // Check cooldown to prevent spam
        if (now - lastShotTime < shotCooldown) {
            System.out.println("T-Rex on cooldown - " + ((shotCooldown - (now - lastShotTime)) / 1_000_000_000.0) + "s remaining");
            return;
        }
        lastShotTime = now;
        
        double dir = facingRight ? 1 : -1; // Projectile direction
        double px = x + (facingRight ? scaledWidth - 10 : 10); // Spawn at mouth position
        double py = y + scaledHeight * 0.4;
        
        try {
            Projectile fireball = new Projectile(px, py, 6 * dir, fireImg);
            projectiles.add(fireball);
            System.out.println("T-Rex FIRED PROJECTILE! Position: (" + px + ", " + py + ")");
            System.out.println("   Total projectiles: " + projectiles.size());
        } catch (Exception e) {
            System.out.println("Failed to create projectile: " + e.getMessage());
        }
    }
    
    // Update all active projectiles (movement, cleanup)
    private void updateProjectiles() {
        // Remove projectiles that are dead or off-screen
        projectiles.removeIf(p -> p.isDead() || p.getX() < -100 || p.getX() > 9700);
        
        // Update remaining projectiles
        for (Projectile p : projectiles) {
            p.update();
        }
    }
    
    // Get current animation frame
    private Image getFrame() {
        if (dead) {
            return null; // Death animation handled separately
        }
        
        if (frames == null || frames.length == 0) return null;
        if (frameIndex >= frames.length) frameIndex = 0;
        return frames[frameIndex];
    }
    
    // Advance animation frame
    private void animate() {
        if (dead) return;
        
        long now = System.nanoTime();
        if (now - lastAnimTime > animDelay) {
            frameIndex = (frameIndex + 1) % (frames != null ? frames.length : 1);
            lastAnimTime = now;
        }
    }
    
    // Render enemy to screen
    @Override
    public void render(GraphicsContext gc, double camX) {
        if (dead) {
            renderDeathAnimation(gc, camX);
            return;
        }
        
        Image frame = getFrame();
        double drawX = x - camX; // Convert to screen space
        double drawY = y;
        
        // Cull if off-screen
        if (drawX + scaledWidth < 0 || drawX > GameConfig.WINDOW_WIDTH) return;
        if (drawY + scaledHeight < 0 || drawY > GameConfig.WINDOW_HEIGHT) return;
        
        if (frame != null && !frame.isError()) {
            if (facingRight) {
                gc.drawImage(frame, drawX, drawY, scaledWidth, scaledHeight);
            } else {
                // Flip horizontally when facing left
                gc.drawImage(frame, drawX + scaledWidth, drawY, -scaledWidth, scaledHeight);
            }
        } else {
            // Fallback rendering - colored rectangle with debug info
            gc.setFill(getEnemyColor());
            gc.fillRect(drawX, drawY, scaledWidth, scaledHeight);
            
            gc.setFill(Color.WHITE);
            gc.fillText(type.substring(0, 3), drawX + 5, drawY + 15);
            
            // Show movement direction (green = right, yellow = left)
            gc.setFill(movingRight ? Color.GREEN : Color.YELLOW);
            gc.fillRect(drawX + (movingRight ? scaledWidth - 5 : 0), drawY, 5, 5);
            
            // Show facing direction (red = right, blue = left)
            gc.setFill(facingRight ? Color.RED : Color.BLUE);
            gc.fillOval(drawX + (facingRight ? 0 : scaledWidth - 5), drawY + 10, 5, 5);
        }
        
        // Render all active projectiles
        for (Projectile p : projectiles) {
            if (!p.isDead()) {
                p.render(gc, camX);
            }
        }
    }
    
    // Render death animation
    private void renderDeathAnimation(GraphicsContext gc, double camX) {
        double drawX = x - camX;
        double drawY = y;
        
        // Cull if off-screen
        if (drawX + scaledWidth < 0 || drawX > GameConfig.WINDOW_WIDTH) return;
        if (drawY + scaledHeight < 0 || drawY > GameConfig.WINDOW_HEIGHT) return;
        
        // Use death animation frames if available
        if (deathFrames != null && deathFrameIndex < 3 && deathFrames[deathFrameIndex] != null) {
            Image deathFrame = deathFrames[deathFrameIndex];
            if (!deathFrame.isError()) {
                if (facingRight) {
                    gc.drawImage(deathFrame, drawX, drawY, scaledWidth, scaledHeight);
                } else {
                    gc.drawImage(deathFrame, drawX + scaledWidth, drawY, -scaledWidth, scaledHeight);
                }
                return;
            }
        }
        
        // Fallback death animation - shrink and fade
        double progress = (double)deathFrameIndex / 3;
        double scale = 1.0 - progress * 0.5; // Shrink to 50%
        double alpha = 1.0 - progress; // Fade out
        
        gc.setFill(Color.color(0.5, 0, 0, alpha));
        gc.fillRect(drawX + (scaledWidth * (1 - scale)) / 2, 
                   drawY + (scaledHeight * (1 - scale)) / 2,
                   scaledWidth * scale, 
                   scaledHeight * scale);
        
        gc.setFill(Color.WHITE);
        gc.fillText("DEATH " + (deathFrameIndex + 1) + "/3", drawX + 5, drawY + 15);
    }
    
    // Get color for fallback rendering based on enemy type
    private Color getEnemyColor() {
        switch (type) {
            case "raptor": return Color.DARKGREEN;
            case "pterodactyl": return Color.DARKBLUE;
            case "triceratops": return Color.DARKGRAY;
            case "trex": return Color.DARKRED;
            default: return Color.PURPLE;
        }
    }
    
    // Check if enemy overlaps with player hitbox
    public boolean overlaps(double px, double py, double pw, double ph) {
        if (dead) return false; // Dead enemies don't collide
        return collidesWith(px, py, pw, ph);
    }
    
    // Check if player is hitting vulnerable spot (head from above)
    public boolean isHeadVulnerable(double playerX, double playerY, double playerWidth, double playerHeight) {
        if (dead) return false;
        
        boolean above = playerY + playerHeight < y + scaledHeight * 0.3; // Within top 30%
        boolean horizontalOverlap = playerX + playerWidth > x + scaledWidth * 0.2 &&
                                   playerX < x + scaledWidth * 0.8; // Center 60%
        
        return above && horizontalOverlap;
    }
    
    // Alternative collision check method
    public boolean collides(double px, double py, int pw, int ph) {
        return overlaps(px, py, (double)pw, (double)ph);
    }
    
    // Set enemy speed
    public void setSpeed(double speed) {
        this.speed = Math.abs(speed);
        System.out.println(type + " speed set to: " + speed);
    }
    
    // Debug output for enemy state
    public void debugInfo() {
        System.out.printf("Enemy %s: (%.1f, %.1f) Size: %.1fx%.1f Speed: %.1f Moving: %s Facing: %s Dead: %s DeathFrame: %d/3%n",
            type, x, y, scaledWidth, scaledHeight, Math.abs(speed), 
            movingRight ? "right" : "left", facingRight ? "right" : "left", 
            dead, deathFrameIndex);
    }
    
    private Image loadEnemyImage(String path) {
        try {
            InputStream stream = Enemy.class.getResourceAsStream(path);
            if (stream == null) {
                System.err.println("Enemy image not found: " + path);
                return null;
            }
            
            Image img = new Image(stream);
            if (img.isError()) {
                System.err.println("Enemy image error: " + path);
                return null;
            }
            return img;
        } catch (Exception e) {
            System.err.println("Exception loading enemy image " + path + ": " + e.getMessage());
            return null;
        }
    }
}

