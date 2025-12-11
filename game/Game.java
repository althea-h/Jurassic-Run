package game;

import game.entities.*;
import game.levels.LevelManager;
import game.audio.AudioManager;
import game.camera.Camera;
import game.input.InputHandler;
import menu.DeathMenu;
import menu.PauseMenu;
import menu.VictoryMenu;
import java.io.InputStream;

import javafx.animation.AnimationTimer;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;

// main game controller, designed this to manage the entire game loop and state
public class Game {
    // needed this to switch between menu and game scenes
    private Stage stage;
    //chose Canvas because it gives pixel-perfect control over rendering
    private Canvas canvas;
    // this is the API to draw everything on the canvas
    private GraphicsContext gc;
    
    //core game objects, organized these as separate managers for clean, modular code
    private Player player; //the player character - this is the central entity the user controls
    private LevelManager levelManager; // handles level loading and all level data
    private EnemyManager enemyManager; // manages all enemies and their AI behavior
    private AudioManager audioManager; // singleton pattern for all sound effects and music
    private Camera camera; // follows the player smoothly across the level
    private InputHandler inputHandler; // decouples keyboard input from game logic
    
    // UI images, preload these to avoid lag during gameplay
    private Image gateImg; // the exit gate image players must reach
    private Image escapeImg; // victory screen background
    private Image gameOverImg; // death screen background
    
    // use booleans to control the flow of the game
    private boolean paused; // controls whether game updates should run
    private boolean gameOver; // triggers death screen when player dies
    private boolean gameComplete; // triggers victory screen when all levels are beaten
    
    // keep these to show/hide overlays without recreating them
    private PauseMenu pauseMenu;
    private DeathMenu deathMenu;
    private VictoryMenu victoryMenu;
    
    // HUD icons, show collected fossils and remaining lives
    private Image fossilIconImg;
    private Image lifeIconImg;
    
    
    private AnimationTimer gameLoop;
    
    // called when starting a new game from the menu
    public Game(Stage stage) {
        this.stage = stage; // store the stage reference to control scenes
        
        // configured it to match our window dimensions
        canvas = new Canvas(GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        gc = canvas.getGraphicsContext2D(); 
        
        
        Pane root = new Pane(canvas);
        Scene scene = new Scene(root);
        stage.setScene(scene); // replace menu scene with game scene
        
        // initialize game systems in order
        initializeGame(); // load all game objects and managers
        setupControls(scene); // attach keyboard listeners
        startLoop(); // begin the game loop
    }
    
    // separated this for code organization
    private void initializeGame() {
        // Get singleton audio manager, so all classes share one instance
        this.audioManager = AudioManager.getInstance();
        // create new camera - starts at position 0,0
        this.camera = new Camera();
        
        // create level manager and load first level
        levelManager = new LevelManager();
        levelManager.loadLevel(1);//start at level 1
        
        //load all UI images
        loadImages();
        
        // Create player at spawn point - spawn Y comes from level data
        player = new Player(80, levelManager.getSpawnY());
        // Create enemy manager with player reference - enemies need to track player
        enemyManager = new EnemyManager(player);
        enemyManager.setEnemies(levelManager.getEnemies()); // Load level's enemies
        
        // Set up input handling
        inputHandler = new InputHandler(player);
        inputHandler.setOnPause(this::togglePause); // Bind pause functionality
        inputHandler.setOnRestart(this::restartGame); // Bind restart functionality
        inputHandler.setOnMenu(this::backToMenu); // Bind menu return functionality
        
        // Initialize game state flags
        paused = false;
        gameOver = false;
        gameComplete = false;
    }
    
    // Load all UI images - I use try-catch to handle missing files gracefully
    private void loadImages() {
        gateImg = loadImageResource("/assets/images/gate.png");
        escapeImg = loadImageResource("/assets/background/escaped.png");
        gameOverImg = loadImageResource("/assets/background/game_over.png");
        fossilIconImg = loadImageResource("/assets/images/hud/fossil_icon.png");
        lifeIconImg = loadImageResource("/assets/images/hud/life_icon.png");
    }

    private Image loadImageResource(String path) {
        try {
            InputStream stream = Game.class.getResourceAsStream(path);
            if (stream == null) {
                System.err.println("Image not found: " + path);
                return null;
            }
            
            Image img = new Image(stream);
            if (img.isError()) {
                System.err.println("Failed to load: " + path);
                return null;
            }
            return img;
        } catch (Exception e) {
            System.err.println("Exception loading " + path + ": " + e.getMessage());
            return null;
        }
    }
    
    // Set up keyboard controls - I attach listeners to the scene
    private void setupControls(Scene scene) {
        scene.setOnKeyPressed(e -> {
            // Pass game state to input handler so it knows context
            inputHandler.setGameOver(gameOver);
            inputHandler.setGameComplete(gameComplete);
            inputHandler.handleKeyPressed(e); // Delegate to input handler
        });
        
        scene.setOnKeyReleased(e -> {
            inputHandler.handleKeyReleased(e); // Handle key release
        });
    }
    
    // Toggle pause state - I implemented this to pause/resume gameplay
    private void togglePause() {
        if (gameOver || gameComplete) return; // Don't allow pause in end screens
        
        paused = !paused; // Toggle the flag
        
        if (paused) {
            System.out.println("GAME PAUSED - Press P or ESC to resume");
            levelManager.pauseLevelMusic(); // Pause background music
            try {
                pauseMenu = new PauseMenu(stage, this); // Create pause overlay
                pauseMenu.show(); // Display pause menu
            } catch (Exception e) {
                System.out.println("Pause menu not available: " + e.getMessage());
            }
        } else {
            System.out.println("GAME RESUMED");
            levelManager.resumeLevelMusic(); // Resume background music
            if (pauseMenu != null) {
                pauseMenu.hide(); // Hide pause menu
                pauseMenu = null; // Clear reference
            }
        }
    }
    
    // Resume game from pause menu - separate from togglePause for menu button
    public void resumeGame() {
        paused = false;
        System.out.println("GAME RESUMED");
        levelManager.resumeLevelMusic();
        if (pauseMenu != null) {
            pauseMenu.hide();
            pauseMenu = null;
        }
    }
    
    // Restart the entire game from level 1 - I reset all game state here
    public void restartGame() {
        System.out.println("=== BEFORE RESTART ===");
        System.out.println("LevelManager enemies: " + levelManager.getEnemies().size());
        System.out.println("EnemyManager enemies: " + enemyManager.getEnemyCount());
        
        // Reset all state flags
        paused = false;
        gameOver = false;
        gameComplete = false;
        
        // Stop any playing music
        audioManager.stopMusic();
        
        // Reload level 1
        levelManager.loadLevel(1);
        
        // Reset player to starting position
        player.reset(80, levelManager.getSpawnY());
        
        // Reset enemy manager and load new enemies
        enemyManager.reset();
        enemyManager.setEnemies(levelManager.getEnemies());
        
        // Reset camera to starting position
        camera.reset();
        
        // Hide all menu overlays
        if (deathMenu != null) {
            deathMenu.hide();
            deathMenu = null;
        }
        if (pauseMenu != null) {
            pauseMenu.hide();
            pauseMenu = null;
        }
        if (victoryMenu != null) {
            victoryMenu.hide();
            victoryMenu = null;
        }
        
        System.out.println("GAME RESTARTED - Level 1");
        System.out.println("Enemies loaded: " + levelManager.getEnemies().size());
        
        System.out.println("=== AFTER RESTART ===");
        System.out.println("LevelManager enemies: " + levelManager.getEnemies().size());
        System.out.println("EnemyManager enemies: " + enemyManager.getEnemyCount());
    }
    
    // Return to main menu - I stop the game loop and switch scenes
    public void backToMenu() {
        if (gameLoop != null) {
            gameLoop.stop(); // Stop game loop to prevent updates
        }
        
        // Switch audio to menu music
        audioManager.stopMusic();
        audioManager.playMenuMusic();
        
        // Hide all menus
        if (deathMenu != null) {
            deathMenu.hide();
            deathMenu = null;
        }
        if (pauseMenu != null) {
            pauseMenu.hide();
            pauseMenu = null;
        }
        if (victoryMenu != null) {
            victoryMenu.hide();
            victoryMenu = null;
        }
        
        // Create new menu instance - this replaces the game scene
        new Menu(stage);
    }
    
    // Start the game loop - AnimationTimer runs at monitor refresh rate (~60 FPS)
    private void startLoop() {
        gameLoop = new AnimationTimer() {
            public void handle(long now) {
                if (!paused) {
                    update(); // Only update game logic if not paused
                }
                render(); // Always render to show pause menu
            }
        };
        gameLoop.start(); // Begin the loop
    }
    
    // Update game logic - runs every frame when not paused
    private void update() {
        if (gameOver || gameComplete) return; // Don't update if game ended
        
        // Update player physics and state
        player.update();
        player.checkPlatformCollision(levelManager.getPlatforms());
        
        // Check if player fell into pit
        checkPitFalling();
        
        // Keep player within world bounds
        double worldWidth = levelManager.getMapWidth();
        if (player.getX() < 0) {
            player.setPosition(0, player.getY());
        }
        if (player.getX() > worldWidth - player.getWidth()) {
            player.setPosition(worldWidth - player.getWidth(), player.getY());
        }
        
        // Update camera to follow player
        camera.update(player.getX(), worldWidth);
        
        // Check for collectible pickups
        checkFossilCollection();
        checkPowerUpCollection();
        
        // Update all enemies
        enemyManager.update(levelManager.getPlatforms());
        
        // Check combat interactions
        checkEnemyCollisions();
        checkEnemyKill();
        checkPlayerDeath();
        checkGate(); // Check if player reached exit
    }
    
    // Check if player is falling into a pit - I implemented this for death zones
    private void checkPitFalling() {
        double fallDeathThreshold = 650; // Below this Y position is instant death
        
        if (player.getY() > fallDeathThreshold) {
            handlePlayerFall(); // Player fell too far
        }
        
        // Check if player is falling with no platform below
        if (player.getVelocityY() > 0) {
            boolean hasPlatformBelow = false;
            double playerBottom = player.getY() + player.getHeight();
            
            // Look ahead 300 pixels down for any platform
            for (Platform p : levelManager.getPlatforms()) {
                double platformTop = p.getY();
                double platformSize = Platform.SIZE;
                
                if (platformTop >= playerBottom && platformTop <= playerBottom + 300) {
                    // Check horizontal overlap
                    if (player.getX() + player.getWidth() > p.getX() && 
                        player.getX() < p.getX() + platformSize) {
                        hasPlatformBelow = true;
                        break;
                    }
                }
            }
            
            // If no platform below and falling deep, it's a pit
            if (!hasPlatformBelow && playerBottom > 500) {
                handlePlayerFall();
            }
        }
    }
    
    // Handle player falling into pit - I respawn player or trigger death
    private void handlePlayerFall() {
        if (player.getLives() > 1) {
            player.takeDamage(1); // Remove one life
            if (!player.isDead()) {
                // Respawn at level start
                player.setPosition(80, levelManager.getSpawnY());
                camera.reset();
                System.out.println("Fell into pit! Lives left: " + player.getLives());
            }
        } else {
            player.takeDamage(1); // Final life lost - triggers death
        }
    }
    
    // Check if player collected any fossils - I loop through all fossils
    private void checkFossilCollection() {
        for (Fossil f : levelManager.getFossils()) {
            if (!f.isCollected() && f.intersects(player.getX(), player.getY(), 
                player.getWidth(), player.getHeight())) {
                f.collect(); // Mark fossil as collected
                levelManager.collectFossil(); // Update level counter
                player.collectFossil(); // Play collection sound
                System.out.println("Fossil collected! " + levelManager.getCollected() + "/" + GameConfig.FOSSILS_PER_LEVEL);
            }
        }
    }
    
    // Check if player collected any power-ups
    private void checkPowerUpCollection() {
        ArrayList<PowerUp> powerUps = levelManager.getPowerUps();
        for (PowerUp p : powerUps) {
            p.update(); // Update power-up animation
            if (!p.isCollected() && p.overlaps(player.getX(), player.getY(), 
                player.getWidth(), player.getHeight())) {
                p.collect(); // Mark as collected
                player.applyPowerUp(p.getType()); // Apply boost effect
                player.collectPowerUp(); // Play sound
                System.out.println("Power-up collected: " + p.getType());
            }
        }
    }
    
    // Check if player touched any enemies - this handles damage
    private void checkEnemyCollisions() {
        ArrayList<Enemy> enemies = enemyManager.getEnemies();
        
        for (Enemy e : enemies) {
            if (e.isDead()) continue; // Skip dead enemies
            
            if (e.overlaps(player.getX(), player.getY(), 
                player.getWidth(), player.getHeight())) {
                
                // Only damage if not invincible and hitting from side/below
                if (!player.isInvincible() && 
                    player.getY() + player.getHeight() > e.getY() + 10) {
                    player.takeDamage(1);
                    System.out.println("Hit by enemy! Lives: " + player.getLives());
                    
                    // Apply knockback based on relative position
                    if (player.getX() < e.getX()) {
                        player.knockback(-5, -3); // Push left
                    } else {
                        player.knockback(5, -3); // Push right
                    }
                }
            }
        }
    }
    
    // Check if player stomped on enemy head - this is how players kill enemies
    private void checkEnemyKill() {
        ArrayList<Enemy> enemies = enemyManager.getEnemies();
        
        for (Enemy e : enemies) {
            if (e.isDead()) continue;
            
            // Check if player is falling onto enemy head
            if (player.getVelocityY() > 0 && // Player must be falling
                player.getY() + player.getHeight() >= e.getY() && // Feet at head height
                player.getY() + player.getHeight() <= e.getY() + e.getHeight() * 0.3 && // Within top 30%
                player.getX() + player.getWidth() > e.getX() + 5 && // Horizontal overlap
                player.getX() < e.getX() + e.getWidth() - 5) { // (with margin)
                
                e.damage(); // Kill enemy
                player.killEnemy(); // Bounce player up
                System.out.println("Enemy stomped!");
            }
        }
    }
    
    // Check if player death animation is complete
    private void checkPlayerDeath() {
        if (player.isDead() && player.isDeathAnimComplete() && !gameOver) {
            gameOver = true;
            System.out.println("GAME OVER");
            levelManager.onGameOver(); // Trigger game over music
            try {
                deathMenu = new DeathMenu(stage, this); // Show death menu
                deathMenu.show();
            } catch (Exception e) {
                System.out.println("Death menu not available: " + e.getMessage());
            }
        }
    }
    
    // Check if player reached the exit gate
    private void checkGate() {
        double gateX = levelManager.getGateX();
        
        // Player must have all fossils and touch gate
        if (levelManager.getCollected() >= GameConfig.FOSSILS_PER_LEVEL && 
            player.getX() + player.getWidth() > gateX &&
            player.getX() < gateX + 80) {
            
            int next = levelManager.getCurrentLevel() + 1;
            
            // Check if this was the final level
            if (next > GameConfig.TOTAL_LEVELS) {
                gameComplete = true;
                System.out.println("ALL LEVELS COMPLETED!");
                player.escape(); // Play escape sound
                try {
                    victoryMenu = new VictoryMenu(stage, this); // Show victory screen
                    victoryMenu.show();
                } catch (Exception e) {
                    System.out.println("Victory menu not available: " + e.getMessage());
                }
                return;
            }
            
            // Load next level
            levelManager.loadLevel(next);
            player.setPosition(80, levelManager.getSpawnY()); // Respawn at new level start
            enemyManager.setEnemies(levelManager.getEnemies()); // Load new enemies
            camera.reset();
            System.out.println("Level " + (next-1) + " completed! Loading level " + next);
            
            player.escape(); // Play level complete sound
        }
    }
    
    // Render everything to screen - this runs every frame
    private void render() {
        // Clear screen with dark blue background
        gc.setFill(Color.rgb(20, 24, 82));
        gc.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
        
        // Draw background map if available
        if (levelManager.getMapImage() != null) {
            gc.drawImage(levelManager.getMapImage(), -camera.getX(), 0);
        }
        
        // Render all game objects in order (back to front)
        renderPlatforms();
        renderFossils();
        renderPowerUps();
        renderGate();
        renderEnemies();
        renderPlayer();
        
        // Render UI on top of everything
        renderHUD();
        
        // Render end game overlays
        renderGameComplete();
        renderGameOver();
        renderPauseScreen();
    }
    
    // Render all platforms
    private void renderPlatforms() {
        for (Platform p : levelManager.getPlatforms())
            p.render(gc, camera.getX());
    }
    
    // Render all fossils
    private void renderFossils() {
        for (Fossil f : levelManager.getFossils())
            f.render(gc, camera.getX());
    }
    
    // Render all power-ups
    private void renderPowerUps() {
        for (PowerUp p : levelManager.getPowerUps())
            p.render(gc, camera.getX());
    }
    
    // Render the exit gate
    private void renderGate() {
        double gx = levelManager.getGateX() - camera.getX(); // Convert to screen space
        double gateY = levelManager.getSpawnY() - 100; // Position above spawn
        if (gateImg != null) {
            gc.drawImage(gateImg, gx, gateY, 80, 150);
        } else {
            // Fallback if image missing
            gc.setFill(Color.GOLD);
            gc.fillRect(gx, gateY, 80, 150);
            gc.setFill(Color.BLACK);
            gc.fillRect(gx + 30, gateY + 50, 20, 100); // Door opening
        }
    }
    
    // Render all enemies
    private void renderEnemies() {
        enemyManager.render(gc, camera.getX());
    }
    
    // Render the player
    private void renderPlayer() {
        player.render(gc, camera.getX());
    }
    
    // Render HUD (heads-up display) - shows fossils, lives, level number
    private void renderHUD() {
        double hudX = GameConfig.HUD_PADDING;
        double hudY = GameConfig.HUD_PADDING;
        
        // Render fossil icons at top left
        renderFossilHUD(hudX, hudY);
        
        // Move down for life icons
        hudY += GameConfig.HUD_FOSSIL_SIZE + GameConfig.HUD_ICON_SPACING + 10;
        
        // Render life icons
        renderLivesHUD(hudX, hudY);
        
        // Move down for level text
        hudY += GameConfig.HUD_LIFE_SIZE + GameConfig.HUD_ICON_SPACING + 10;
        
        // Render level number
        renderLevelHUD(hudX, hudY);
    }

    // Render fossil collection icons - I show 6 slots, filling collected ones
    private void renderFossilHUD(double startX, double startY) {
        int collected = levelManager.getCollected();
        int total = GameConfig.FOSSILS_PER_LEVEL;
        
        double iconX = startX;
        
        for (int i = 0; i < total; i++) {
            if (i < collected) {
                // Draw filled icon for collected fossil
                if (fossilIconImg != null) {
                    gc.drawImage(fossilIconImg, iconX, startY, 
                        GameConfig.HUD_FOSSIL_SIZE, GameConfig.HUD_FOSSIL_SIZE);
                } else {
                    // Fallback: colored square
                    gc.setFill(Color.LIGHTBLUE);
                    gc.fillRect(iconX, startY, 
                        GameConfig.HUD_FOSSIL_SIZE, GameConfig.HUD_FOSSIL_SIZE);
                    gc.setStroke(Color.WHITE);
                    gc.strokeRect(iconX, startY, 
                        GameConfig.HUD_FOSSIL_SIZE, GameConfig.HUD_FOSSIL_SIZE);
                }
            } else {
                // Draw empty slot for uncollected fossil
                gc.setFill(Color.color(0.3, 0.3, 0.3, 0.5));
                gc.fillRect(iconX, startY, 
                    GameConfig.HUD_FOSSIL_SIZE, GameConfig.HUD_FOSSIL_SIZE);
                gc.setStroke(Color.color(0.5, 0.5, 0.5, 0.8));
                gc.strokeRect(iconX, startY, 
                    GameConfig.HUD_FOSSIL_SIZE, GameConfig.HUD_FOSSIL_SIZE);
            }
            
            iconX += GameConfig.HUD_FOSSIL_SIZE + GameConfig.HUD_ICON_SPACING; // Move right
        }
    }

    // Render life icons - I show a heart for each remaining life
    private void renderLivesHUD(double startX, double startY) {
        int lives = player.getLives();
        double iconX = startX;
        
        for (int i = 0; i < lives; i++) {
            if (lifeIconImg != null) {
                gc.drawImage(lifeIconImg, iconX, startY, 
                    GameConfig.HUD_LIFE_SIZE, GameConfig.HUD_LIFE_SIZE);
            } else {
                // Fallback: red circle with heart symbol
                gc.setFill(Color.RED);
                gc.fillOval(iconX, startY, 
                    GameConfig.HUD_LIFE_SIZE, GameConfig.HUD_LIFE_SIZE);
                gc.setStroke(Color.WHITE);
                gc.strokeOval(iconX, startY, 
                    GameConfig.HUD_LIFE_SIZE, GameConfig.HUD_LIFE_SIZE);
                
                gc.setFill(Color.WHITE);
                gc.setFont(javafx.scene.text.Font.font(20));
                gc.fillText("♥", iconX + 12, startY + 28);
            }
            
            iconX += GameConfig.HUD_LIFE_SIZE + GameConfig.HUD_ICON_SPACING; // Move right
        }
    }

    // Render current level number
    private void renderLevelHUD(double startX, double startY) {
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font(18));
        gc.fillText("Level: " + levelManager.getCurrentLevel(), startX, startY + 20);
    }
    
    // Render victory screen overlay
    private void renderGameComplete() {
        if (gameComplete) {
            if (escapeImg != null) {
                // Draw full-screen victory image
                gc.drawImage(escapeImg, 0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            } else {
                // Fallback: semi-transparent overlay with text
                gc.setFill(Color.color(0, 0, 0, 0.7));
                gc.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
                
                gc.setFill(Color.GOLD);
                gc.setFont(javafx.scene.text.Font.font("Arial Black", 48));
                gc.fillText("YOU ESCAPED!", 200, 300);
            }
        }
    }
    
    // Render game over screen overlay
    private void renderGameOver() {
        if (gameOver) {
            if (gameOverImg != null) {
                gc.drawImage(gameOverImg, 0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
            } else {
                // Fallback
                gc.setFill(Color.color(0, 0, 0, 0.7));
                gc.fillRect(0, 0, GameConfig.WINDOW_WIDTH, GameConfig.WINDOW_HEIGHT);
                
                gc.setFill(Color.RED);
                gc.setFont(javafx.scene.text.Font.font("Impact", 48));
                gc.fillText("GAME OVER", 280, 300);
                
                gc.setFill(Color.WHITE);
                gc.setFont(javafx.scene.text.Font.font("Verdana", 24));
                gc.fillText("Press R to restart or M for menu", 220, 500);
            }
        }
    }
    
    // Render pause screen - I keep this transparent since pause menu is separate
    private void renderPauseScreen() {
        if (paused) {
            gc.setFill(Color.TRANSPARENT);
        }
    }
    
    // Getters - I provide these so menus can access game state
    public Player getPlayer() { 
        return player; 
    }
    
    public LevelManager getLevelManager() { 
        return levelManager; 
    }
    
    public EnemyManager getEnemyManager() { 
        return enemyManager; 
    }
    
    public boolean isGameOver() { 
        return gameOver; 
    }
    
    public boolean isGameComplete() { 
        return gameComplete; 
    }
    
    public boolean isPaused() { 
        return paused; 
    }
    
    public void setPaused(boolean paused) { 
        this.paused = paused; 
    }
    
    public AudioManager getAudioManager() { 
        return audioManager; 
    }
}