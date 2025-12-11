package game.entities;

import game.core.LivingEntity;
import game.GameConfig;
import game.audio.AudioManager;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.io.InputStream;

import java.util.List;

// Player class - I designed this to handle all player behavior, physics, and animations
public class Player extends LivingEntity {
    // Movement physics - I tuned these values for responsive platformer feel
    private double speedX; // Current horizontal speed
    private double targetSpeedX; // Speed player wants to reach (for acceleration)
    private double accelerate; // How fast we reach target speed
    private double maximumSpeed; // Max horizontal speed
    private double jumpForce; // Upward velocity when jumping
    
    // Animation frames - I organized these by state for smooth animations
    private Image[] idleFrames; // Standing still animation
    private Image[] runFrames; // Running animation
    private Image[] jumpFrames; // Jumping/falling animation
    private Image[] hurtFrames; // Taking damage animation
    private Image[] deathFrames; // Death animation
    
    // Animation state - I track which frame to display
    private int frameIndex; // Current frame in animation
    private long lastFrameTime; // When we last changed frames
    private long frameDelay; // How long to show each frame
    private String state; // Current animation state (idle, run, jump, hurt, death)
    
    // Player state flags
    private boolean deathAnimComplete; // Has death animation finished?
    private boolean speedBoost; // Is speed power-up active?
    private boolean jumpBoost; // Is jump power-up active?
    private long powerUpEnd; // When does power-up expire?
    private long invincibleDuration; // How long invincibility lasts
    
    // Knockback physics - I added this so enemies can push player back
    private double knockbackX; // Horizontal knockback force
    private double knockbackY; // Vertical knockback force
    private boolean isKnockbackActive; // Is knockback currently applying?
    
    // Audio reference - I need this to play sound effects
    private AudioManager audioManager;
    
    // Constructor - I initialize the player at spawn position
    public Player(double x, double y) {
        // Call parent constructor with position, size, and starting lives
        super(x, y, 60, 70, GameConfig.PLAYER_STARTING_LIVES);
        
        // Initialize movement values from config
        this.speedX = 0;
        this.targetSpeedX = 0;
        this.accelerate = GameConfig.PLAYER_ACCELERATION;
        this.maximumSpeed = GameConfig.PLAYER_MAX_SPEED;
        this.jumpForce = GameConfig.PLAYER_JUMP_FORCE;
        
        // Initialize animation system
        this.frameIndex = 0;
        this.lastFrameTime = 0;
        this.frameDelay = GameConfig.ANIMATION_DELAY;
        this.state = "idle"; // Start in idle state
        
        // Initialize power-up state
        this.deathAnimComplete = false;
        this.speedBoost = false;
        this.jumpBoost = false;
        this.powerUpEnd = 0;
        this.invincibleDuration = GameConfig.INVINCIBILITY_DURATION;
        
        // Initialize knockback
        this.knockbackX = 0;
        this.knockbackY = 0;
        this.isKnockbackActive = false;
        
        // Get audio manager singleton
        this.audioManager = AudioManager.getInstance();
        
        // Load all animation frames
        loadFrames();
        System.out.println("Player created at (" + x + ", " + y + ")");
    }
    
    // Load all animation frames - I load 6 states with different frame counts
    private void loadFrames() {
        try {
            // Load idle animation (6 frames)
            idleFrames = new Image[6];
            for (int i = 0; i < 6; i++) {
                idleFrames[i] = loadPlayerImage("/assets/images/player/idle" + (i + 1) + ".png");
                if (idleFrames[i] == null) throw new Exception("Failed to load idle frame " + (i+1));
            }
            
            // Load run animation (4 frames)
            runFrames = new Image[4];
            for (int i = 0; i < 4; i++) {
                runFrames[i] = loadPlayerImage("/assets/images/player/run" + (i + 1) + ".png");
                if (runFrames[i] == null) throw new Exception("Failed to load run frame " + (i+1));
            }
            
            // Load jump animation (5 frames)
            jumpFrames = new Image[5];
            for (int i = 0; i < 5; i++) {
                jumpFrames[i] = loadPlayerImage("/assets/images/player/jump" + (i + 1) + ".png");
                if (jumpFrames[i] == null) throw new Exception("Failed to load jump frame " + (i+1));
            }
            
            // Load hurt animation (3 frames)
            hurtFrames = new Image[3];
            for (int i = 0; i < 3; i++) {
                hurtFrames[i] = loadPlayerImage("/assets/images/player/hurt" + (i + 1) + ".png");
                if (hurtFrames[i] == null) throw new Exception("Failed to load hurt frame " + (i+1));
            }
            
            // Load death animation (6 frames)
            deathFrames = new Image[6];
            for (int i = 0; i < 6; i++) {
                deathFrames[i] = loadPlayerImage("/assets/images/player/death" + (i + 1) + ".png");
                if (deathFrames[i] == null) throw new Exception("Failed to load death frame " + (i+1));
            }
            
            System.out.println("All player frames loaded successfully");
        } catch (Exception e) {
            System.out.println("Error loading player frames: " + e.getMessage());
            createPlaceholderFrames();
        }
    }

    private Image loadPlayerImage(String path) {
        try {
            InputStream stream = Player.class.getResourceAsStream(path);
            if (stream == null) {
                System.err.println("Player image not found: " + path);
                return null;
            }
            
            Image img = new Image(stream);
            if (img.isError()) {
                System.err.println("Player image error: " + path);
                return null;
            }
            return img;
        } catch (Exception e) {
            System.err.println("Exception loading player image " + path + ": " + e.getMessage());
            return null;
        }
    }
    
    // Create placeholder frames if images fail to load
    private void createPlaceholderFrames() {
        System.out.println("Creating placeholder frames...");
        hurtFrames = new Image[1];
        deathFrames = new Image[1];
    }
    
    // Input handling methods - I call these from InputHandler based on key presses
    
    // Start moving right - I set target speed so acceleration applies smoothly
    public void startMoveRight() {
        if (dead) return; // Don't move if dead
        targetSpeedX = maximumSpeed; // Accelerate to max speed
        facingRight = true; // Update facing direction
        if (onGround && !state.equals("hurt")) state = "run"; // Switch to run animation
    }
    
    // Start moving left
    public void startMoveLeft() {
        if (dead) return;
        targetSpeedX = -maximumSpeed; // Negative for left movement
        facingRight = false;
        if (onGround && !state.equals("hurt")) state = "run";
    }
    
    // Stop horizontal movement
    public void stopMove() {
        if (dead) return;
        targetSpeedX = 0; // Decelerate to stop
        if (onGround && !state.equals("hurt")) state = "idle"; // Switch to idle
    }
    
    // Jump - I only allow jumping when on ground (no double jump)
    public void jump() {
        if (dead || !onGround) return; // Must be alive and on ground
        
        velocityY = jumpForce; // Apply upward force (negative Y is up)
        onGround = false; // Player is now airborne
        if (!state.equals("hurt")) state = "jump"; // Switch to jump animation
        audioManager.playJump(); // Play jump sound effect
        System.out.println("Player jumped with force: " + jumpForce);
    }
    
    // Collection methods - I trigger sound effects when collecting items
    
    public void collectFossil() {
        audioManager.playCollectFossil();
        System.out.println("Collected fossil with sound!");
    }
    
    public void collectPowerUp() {
        audioManager.playCollectPowerUp();
        System.out.println("Collected power-up with sound!");
    }
    
    public void escape() {
        audioManager.playPlayerEscape();
        System.out.println("Player escaped with sound!");
    }
    
    // Knockback - I apply this when hit by enemies to push player back
    public void knockback(double knockbackX, double knockbackY) {
        this.knockbackX = knockbackX; // Horizontal push
        this.knockbackY = knockbackY; // Vertical push (usually upward)
        this.isKnockbackActive = true;
        this.velocityY = knockbackY; // Override current velocity
        System.out.println("Knockback applied: (" + knockbackX + ", " + knockbackY + ")");
    }
    
    // Update knockback physics - I apply decay so player gradually stops
    private void updateKnockback() {
        if (!isKnockbackActive) return;
        
        // Apply knockback forces to position
        x += knockbackX;
        y += knockbackY;
        
        // Decay forces over time (multiplied by decay factor < 1)
        knockbackX *= GameConfig.KNOCKBACK_DECAY;
        knockbackY *= GameConfig.KNOCKBACK_DECAY;
        
        // Stop knockback when forces become negligible
        if (Math.abs(knockbackX) < 0.1 && Math.abs(knockbackY) < 0.1) {
            isKnockbackActive = false;
            knockbackX = 0;
            knockbackY = 0;
        }
    }
    
    // Death handling - I override from LivingEntity to add player-specific behavior
    @Override
    protected void die() {
        dead = true;
        state = "death"; // Switch to death animation
        frameIndex = 0; // Start from first frame
        velocityY = 0; // Stop falling
        speedX = 0; // Stop moving
        targetSpeedX = 0;
        audioManager.playGameOver(); // Play death sound
        System.out.println("Player died!");
    }
    
    // Damage handling - I trigger hurt animation and invincibility
    @Override
    protected void onDamaged() {
        audioManager.playPlayerHurt(); // Play hurt sound
        System.out.println("Player took damage! Lives: " + health);
        state = "hurt"; // Switch to hurt animation
        frameIndex = 0; // Reset animation
        invincible = true; // Grant temporary invincibility
        invincibleStart = System.nanoTime(); // Record when invincibility started
        knockback(facingRight ? -4 : 4, -3); // Push player back from hit
    }
    
    // Override takeDamage to respect invincibility
    @Override
    public void takeDamage(int amount) {
        if (invincible || dead) return; // Ignore damage if invincible or dead
        
        health -= amount;
        if (health <= 0) {
            health = 0;
            die(); // Trigger death if no lives left
        } else {
            onDamaged(); // Trigger hurt response
        }
    }
    
    // Enemy kill - I call this when stomping on enemy to bounce player
    public void killEnemy() {
        velocityY = -7; // Bounce player upward
        audioManager.playEnemyDie(); // Play enemy death sound
        System.out.println("Enemy killed - player bouncing!");
    }
    
    // Check if death animation finished
    public boolean isDeathAnimComplete() {
        return deathAnimComplete;
    }
    
    // Get remaining lives
    public int getLives() {
        return health;
    }
    
    // Reset player to initial state - I use this when restarting game
    public void reset(double spawnX, double spawnY) {
        x = spawnX;
        y = spawnY;
        health = GameConfig.PLAYER_STARTING_LIVES; // Restore lives
        dead = false;
        deathAnimComplete = false;
        invincible = false;
        state = "idle";
        velocityY = 0;
        speedX = 0;
        targetSpeedX = 0;
        frameIndex = 0;
        speedBoost = false;
        jumpBoost = false;
        maximumSpeed = GameConfig.PLAYER_MAX_SPEED; // Reset to default
        jumpForce = GameConfig.PLAYER_JUMP_FORCE; // Reset to default
        isKnockbackActive = false;
        knockbackX = 0;
        knockbackY = 0;
        System.out.println("Player reset at (" + spawnX + ", " + spawnY + ")");
    }
    
    // Apply power-up effects - I modify player stats based on type
    public void applyPowerUp(PowerUp.Type type) {
        long now = System.nanoTime();
        powerUpEnd = now + GameConfig.POWERUP_DURATION; // Set expiration time
        
        switch (type) {
            case SPEED:
                speedBoost = true;
                maximumSpeed = 3.5; // Increase max speed
                System.out.println("Speed boost activated! Max speed: " + maximumSpeed);
                break;
            case JUMP:
                jumpBoost = true;
                jumpForce = -11.0; // Increase jump power
                System.out.println("Jump boost activated! Jump force: " + jumpForce);
                break;
            case INVINCIBLE:
                invincible = true;
                invincibleStart = now;
                System.out.println("Invincibility activated!");
                break;
        }
    }
    
    // Update power-up timers - I remove boosts when they expire
    private void updatePowerUps() {
        long now = System.nanoTime();
        
        // End damage invincibility after duration
        if (invincible && !speedBoost && !jumpBoost && now - invincibleStart > invincibleDuration) {
            invincible = false;
            System.out.println("Invincibility ended");
        }
        
        // End power-up effects after duration
        if (now > powerUpEnd) {
            boolean hadPowerUp = speedBoost || jumpBoost;
            
            if (speedBoost) {
                speedBoost = false;
                maximumSpeed = GameConfig.PLAYER_MAX_SPEED; // Restore default
                System.out.println("Speed boost ended");
            }
            if (jumpBoost) {
                jumpBoost = false;
                jumpForce = GameConfig.PLAYER_JUMP_FORCE; // Restore default
                System.out.println("Jump boost ended");
            }
            if (invincible && hadPowerUp && !state.equals("hurt")) {
                invincible = false;
                System.out.println("Power-up invincibility ended");
            }
        }
    }
    
    // Power-up status getters
    public boolean hasSpeedBoost() {
        return speedBoost;
    }
    
    public boolean hasJumpBoost() {
        return jumpBoost;
    }
    
    // Main update loop - called every frame from Game class
    @Override
    public void update() {
        if (dead) {
            updateDeathAnimation(); // Only animate if dead
            return;
        }
        
        updatePowerUps(); // Check for expired power-ups
        updateKnockback(); // Apply knockback physics
        
        // Apply acceleration to reach target speed smoothly
        if (!isKnockbackActive) {
            if (speedX < targetSpeedX) {
                speedX = Math.min(speedX + accelerate, targetSpeedX);
            } else if (speedX > targetSpeedX) {
                speedX = Math.max(speedX - accelerate, targetSpeedX);
            }
            
            // Stop completely if speed is tiny (prevents sliding)
            if (Math.abs(speedX) < 0.1) speedX = 0;
            
            x += speedX; // Apply horizontal movement
        }
        
        // Apply gravity and vertical movement
        applyGravity();
        y += velocityY;
        
        updateState(); // Update animation state
        animate(); // Advance animation frames
    }
    
    // Update death animation
    private void updateDeathAnimation() {
        animate();
        if (state.equals("death") && frameIndex >= deathFrames.length - 1) {
            deathAnimComplete = true; // Mark animation complete
            System.out.println("Death animation complete");
        }
    }
    
    // Update animation state based on player status
    private void updateState() {
        if (state.equals("hurt")) {
            // Return to normal state after hurt animation finishes
            if (frameIndex >= hurtFrames.length - 1 && onGround) {
                if (Math.abs(speedX) > 0.1) {
                    state = "run";
                } else {
                    state = "idle";
                }
                System.out.println("Player recovered from hurt state");
            }
        } else if (!dead) {
            // Automatically switch between idle/run/jump based on state
            if (onGround) {
                if (Math.abs(speedX) > 0.1) {
                    state = "run";
                } else {
                    state = "idle";
                }
            } else {
                state = "jump";
            }
        }
    }
    
    // Platform collision detection - I handle this separately for detailed physics
    public void checkPlatformCollision(List<game.entities.Platform> platforms) {
        boolean wasOnGround = onGround;
        onGround = false; // Assume not on ground until proven otherwise
        
        // Remember previous position for collision detection
        double prevX = x - speedX;
        double prevY = y - velocityY;
        
        // Check each platform for collision
        for (game.entities.Platform p : platforms) {
            double px = p.getX();
            double py = p.getY();
            double pSize = game.entities.Platform.SIZE;
            
            // Check horizontal overlap (with small margin to prevent edge issues)
            boolean horizontalOverlap = (x + width > px + 2) && (x < px + pSize - 2);
            
            if (!horizontalOverlap) continue;
            
            // Check vertical collision from above (landing on platform)
            if (velocityY >= 0 && !onGround) {
                double playerBottom = y + height;
                double playerPrevBottom = prevY + height;
                double platformTop = py;
                
                // Player crossed platform surface from above
                if (playerPrevBottom <= platformTop && playerBottom >= platformTop) {
                    y = platformTop - height; // Snap to platform surface
                    velocityY = 0; // Stop falling
                    onGround = true;
                    if (!wasOnGround) {
                        System.out.println("Landed on platform at (" + px + ", " + py + ")");
                    }
                    break;
                }
            }
            
            // Check vertical collision from below (hitting head on platform bottom)
            if (velocityY < 0) {
                double playerTop = y;
                double playerPrevTop = prevY;
                double platformBottom = py + pSize;
                
                // Player crossed platform bottom from below
                if (playerPrevTop >= platformBottom && playerTop <= platformBottom) {
                    y = platformBottom; // Push player down
                    velocityY = 0; // Stop upward movement
                    System.out.println("Hit head on platform");
                    break;
                }
            }
        }
        
        // Check horizontal collisions (walls)
        for (game.entities.Platform p : platforms) {
            double px = p.getX();
            double py = p.getY();
            double pSize = game.entities.Platform.SIZE;
            
            // Check vertical overlap (with margin)
            boolean verticalOverlap = (y + height > py + 8) && (y < py + pSize - 8);
            
            if (!verticalOverlap) continue;
            
            // Check collision from left (moving right into wall)
            if (speedX > 0 && !isKnockbackActive) {
                double playerRight = x + width;
                double playerPrevRight = prevX + width;
                double platformLeft = px;
                
                if (playerPrevRight <= platformLeft && playerRight >= platformLeft) {
                    x = platformLeft - width; // Push player left
                    speedX = 0; // Stop horizontal movement
                    System.out.println("Hit platform from left");
                    break;
                }
            }
            
            // Check collision from right (moving left into wall)
            if (speedX < 0 && !isKnockbackActive) {
                double playerLeft = x;
                double playerPrevLeft = prevX;
                double platformRight = px + pSize;
                
                if (playerPrevLeft >= platformRight && playerLeft <= platformRight) {
                    x = platformRight; // Push player right
                    speedX = 0; // Stop horizontal movement
                    System.out.println("Hit platform from right");
                    break;
                }
            }
        }
        
        checkPitDeath(); // Check if falling into pit
        
        // Ground level collision (fallback if no platforms)
        if (y + height > 550 && !onGround) {
            y = 550 - height;
            velocityY = 0;
            onGround = true;
            if (!wasOnGround) {
                System.out.println("Landed on ground level");
            }
        }
    }
    
    // Check if player is falling into a pit (no platform below)
    private void checkPitDeath() {
        if (y > 650) {
            // Fell below death threshold
            System.out.println("FELL INTO PIT! Taking damage...");
            takeDamage(1);
            if (!dead) {
                // Respawn at safe location
                x = 100;
                y = 400;
                velocityY = 0;
                onGround = true;
            }
            return;
        }
        
        // Check if falling fast with no ground below (early pit detection)
        if (velocityY > 8 && y > 500 && !onGround) {
            boolean hasPlatformsBelow = false;
            // Scan downward for platforms
            for (double checkY = y + height + 5; checkY < 650; checkY += 20) {
                if (checkY > 580) {
                    hasPlatformsBelow = false;
                    break;
                }
            }
            
            if (!hasPlatformsBelow) {
                System.out.println("Falling into pit detected!");
                takeDamage(1);
            }
        }
    }
    
    // Advance animation frames based on time
    private void animate() {
        long now = System.nanoTime();
        
        // Only advance frame if enough time has passed
        if (now - lastFrameTime > frameDelay) {
            frameIndex++;
            lastFrameTime = now;
        }
        
        Image[] frames = getCurrentFrameArray();
        
        if (frames == null || frames.length == 0) return;
        
        // Handle frame wrapping based on animation type
        switch (state) {
            case "jump":
                // Hold on last frame when in air
                if (frameIndex >= frames.length) {
                    frameIndex = frames.length - 1;
                }
                break;
            case "death":
                // Hold on last frame when dead
                if (frameIndex >= frames.length) {
                    frameIndex = frames.length - 1;
                }
                break;
            case "hurt":
                // Hold on last frame while hurt
                if (frameIndex >= frames.length) {
                    frameIndex = frames.length - 1;
                }
                break;
            default:
                // Loop animation for idle/run
                if (frameIndex >= frames.length) {
                    frameIndex = 0;
                }
                break;
        }
    }
    
    // Get current animation frame array based on state
    private Image[] getCurrentFrameArray() {
        switch (state) {
            case "run": return runFrames;
            case "jump": return jumpFrames;
            case "hurt": return hurtFrames;
            case "death": return deathFrames;
            default: return idleFrames;
        }
    }
    
    // Get current frame to display
    private Image getCurrentFrame() {
        Image[] arr = getCurrentFrameArray();
        if (arr == null || arr.length == 0) return null;
        return arr[Math.min(frameIndex, arr.length - 1)];
    }
    
    // Render player to screen
    @Override
    public void render(GraphicsContext gc, double camX) {
        double drawX = x - camX; // Convert world position to screen position
        
        // Cull if off-screen (optimization)
        if (drawX + width < 0 || drawX > GameConfig.WINDOW_WIDTH) return;
        
        // Flicker effect when invincible - I alternate visibility
        boolean shouldDraw = true;
        if (invincible && !dead) {
            long now = System.nanoTime();
            shouldDraw = ((now / 100_000_000L) % 2 == 0); // Toggle every 100ms
        }
        
        if (!shouldDraw) {
            return; // Skip rendering this frame for flicker effect
        }
        
        Image frame = getCurrentFrame();
        if (frame != null && !frame.isError()) {
            if (facingRight) {
                // Draw normally when facing right
                gc.drawImage(frame, drawX, y, width, height);
            } else {
                // Flip horizontally when facing left
                gc.drawImage(frame, drawX + width, y, -width, height);
            }
        } else {
            // Fallback if image missing - draw colored rectangle
            gc.setFill(state.equals("hurt") ? Color.RED : Color.BLUE);
            gc.fillRect(drawX, y, width, height);
            
            gc.setFill(Color.WHITE);
            gc.fillText(state, drawX + 5, y + 15);
        }
    }
    
    // Getters
    public String getState() {
        return state;
    }
    
    public double getVelX() {
        return speedX;
    }
}