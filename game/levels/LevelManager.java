package game.levels;

import game.entities.*;
import game.audio.AudioManager;
import game.GameConfig;
import javafx.scene.image.Image;
import java.io.InputStream;

import java.util.ArrayList;

// handles level loading, data management, and transitions
public class LevelManager {
    private int currentLevel; // current level number (1-4)
    
    private Image mapImage; // background image for level
    private double mapWidth; // total width of level in pixels
    
    private double spawnX; // player spawn X position
    private double spawnY; // player spawn Y position
    
    private double gateX; // exit gate X position
    
    // these are loaded from level files
    private ArrayList<Platform> platforms;
    private ArrayList<Fossil> fossils;
    private ArrayList<PowerUp> powerUps;
    private ArrayList<Enemy> enemies;
    
    private int fossilsCollected; // how many fossils collected in current level
    
    private AudioManager audioManager; // for playing level music
    
    // initializes with default values
    public LevelManager() {
        this.currentLevel = 1;
        this.mapWidth = GameConfig.WORLD_WIDTH;
        this.spawnX = 80;
        this.spawnY = 432;
        this.gateX = GameConfig.GATE_X_POSITION;
        
        this.platforms = new ArrayList<>();
        this.fossils = new ArrayList<>();
        this.powerUps = new ArrayList<>();
        this.enemies = new ArrayList<>();
        
        this.fossilsCollected = 0;
        this.audioManager = AudioManager.getInstance();
    }
    
    // load level data from file
    public void loadLevel(int level) {
        currentLevel = level;
        fossilsCollected = 0;
        
        // clear previous level data
        platforms.clear();
        fossils.clear();
        powerUps.clear();
        enemies.clear();
        
        gateX = GameConfig.GATE_X_POSITION;
        
        System.out.println("==== LOADING LEVEL " + level + " ====");
        
        loadMapImage(level); // load background image
        
        // load level objects from text file
        LevelLoader.LevelData data = LevelLoader.loadLevelFromFile(level);
        
        platforms = data.platforms;
        fossils = data.fossils;
        powerUps = data.powerUps;
        enemies = data.enemies;
        gateX = data.gateX;
        
        mapWidth = GameConfig.WORLD_WIDTH;
        
        adjustSpawnPosition(); // find safe spawn location
        playLevelMusic(level); // start appropriate music
        debugEnemyInfo(); // output debug info
        
        System.out.println("Platforms: " + platforms.size());
        System.out.println("Fossils: " + fossils.size());
        System.out.println("PowerUps: " + powerUps.size());
        System.out.println("Enemies: " + enemies.size());
        System.out.println("Map Width: " + mapWidth);
        System.out.println("Gate X: " + gateX);
        System.out.println("Spawn: (" + spawnX + ", " + spawnY + ")");
    }
    
    // play appropriate music for level
    private void playLevelMusic(int level) {
        if (level == 4) {
            audioManager.playLevel4Music(); // level 4 has eerie music
            System.out.println("Playing Level 4 (scary) music");
        } else {
            audioManager.playLevel1_3Music(); // levels 1-3 share music
            System.out.println("Playing Level 1-3 music");
        }
    }
    
    // load background map image
    private void loadMapImage(int level) {
        String mapPath = "/assets/maps/level" + level + ".png";
        
        try {
            InputStream stream = LevelManager.class.getResourceAsStream(mapPath);
            
            if (stream == null) {
                throw new Exception("Map image not found: " + mapPath);
            }
            
            Image img = new Image(stream);
            
            if (!img.isError()) {
                mapImage = img;
                System.out.println("Loaded map image: " + mapPath);
            } else {
                throw new Exception("Map image load error.");
            }
        } catch (Exception e) {
            System.out.println("Map load error: " + e.getMessage());
            mapImage = null;
        }
    }
    
    // find safe spawn position on first platform
    private void adjustSpawnPosition() {
        if (!platforms.isEmpty()) {
            Platform firstPlatform = null;
            
            // find leftmost platform near start
            for (Platform p : platforms) {
                if (p.getX() < 200) {
                    if (firstPlatform == null || p.getX() < firstPlatform.getX()) {
                        firstPlatform = p;
                    }
                }
            }
            
            if (firstPlatform != null) {
                spawnX = firstPlatform.getX() + 50;
                spawnY = firstPlatform.getY() - 70; // spawn above platform
                System.out.println("Adjusted spawn to platform: (" + spawnX + ", " + spawnY + ")");
            } else {
                // no platform found near start - use first platform
                Platform p = platforms.get(0);
                spawnX = p.getX() + 50;
                spawnY = p.getY() - 70;
                System.out.println("Using fallback spawn: (" + spawnX + ", " + spawnY + ")");
            }
        }
    }
    
    // debug output for enemy data
    private void debugEnemyInfo() {
        System.out.println("=== ENEMY DEBUG INFO ===");
        System.out.println("Total enemies loaded: " + enemies.size());
        
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            System.out.println("Enemy " + i + ": " + enemy.getType() + 
                             " at (" + enemy.getX() + ", " + enemy.getY() + ")" +
                             " speed: " + enemy.getSpeed() +
                             " patrol: " + GameConfig.DEFAULT_PATROL_DISTANCE + " blocks");
        }
        
        if (enemies.isEmpty()) {
            System.out.println("WARNING: No enemies loaded for level " + currentLevel);
        }
    }
    
    // check if player collected all fossils
    public boolean isLevelComplete() {
        return fossilsCollected >= GameConfig.FOSSILS_PER_LEVEL;
    }
    
    // called when player completes level
    public void onLevelComplete() {
        audioManager.playPlayerEscape();
        audioManager.stopMusic();
        System.out.println("Level " + currentLevel + " completed! Playing escape sound.");
    }
    
    // called when player dies
    public void onGameOver() {
        audioManager.playGameOver();
        audioManager.stopMusic();
        System.out.println("Game over! Playing game over sound.");
    }
    
    // pause level music
    public void pauseLevelMusic() {
        audioManager.pauseMusic();
        System.out.println("Level music paused");
    }
    
    // resume level music
    public void resumeLevelMusic() {
        audioManager.resumeMusic();
        System.out.println("Level music resumed");
    }
    
    // reset collectibles without reloading level
    public void resetLevelState() {
        fossilsCollected = 0;
        
        for (Fossil fossil : fossils) {
            fossil.reset();
        }
        
        for (PowerUp powerUp : powerUps) {
            powerUp.reset();
        }
    }
    
    // print detailed level information
    public void printLevelInfo() {
        System.out.println("=== LEVEL " + currentLevel + " INFO ===");
        System.out.println("Map Width: " + mapWidth);
        System.out.println("Gate X: " + gateX);
        System.out.println("Spawn: (" + spawnX + ", " + spawnY + ")");
        System.out.println("Platforms: " + platforms.size());
        System.out.println("Fossils: " + fossils.size() + " (Collected: " + fossilsCollected + ")");
        System.out.println("Enemies: " + enemies.size());
        System.out.println("Enemy Patrol Distance: " + GameConfig.DEFAULT_PATROL_DISTANCE + " blocks");
        
        if (!platforms.isEmpty()) {
            Platform first = platforms.get(0);
            Platform last = platforms.get(platforms.size() - 1);
            System.out.println("Platform Range: " + first.getX() + " to " + last.getX());
        }
    }
    
    // debug platform positions
    public void debugPlatformPositions() {
        System.out.println("=== PLATFORM POSITIONS ===");
        for (int i = 0; i < Math.min(platforms.size(), 10); i++) {
            Platform p = platforms.get(i);
            System.out.println("Platform " + i + ": (" + p.getX() + ", " + p.getY() + ")");
        }
        
        if (platforms.size() > 0) {
            Platform last = platforms.get(platforms.size() - 1);
            System.out.println("Last platform at: " + last.getX());
        }
    }
    
    // Getters
    public Image getMapImage() { 
        return mapImage; 
    }
    
    public ArrayList<Platform> getPlatforms() { 
        return platforms; 
    }
    
    public ArrayList<Fossil> getFossils() { 
        return fossils; 
    }
    
    public ArrayList<PowerUp> getPowerUps() { 
        return powerUps; 
    }
    
    public ArrayList<Enemy> getEnemies() { 
        return enemies; 
    }
    
    public int getCurrentLevel() { 
        return currentLevel; 
    }
    
    public double getMapWidth() { 
        return mapWidth; 
    }
    
    public double getGateX() { 
        return gateX; 
    }
    
    public double getSpawnX() { 
        return spawnX; 
    }
    
    public double getSpawnY() { 
        return spawnY; 
    }
    
    public int getCollected() { 
        return fossilsCollected; 
    }
    
    public void collectFossil() { 
        fossilsCollected++; 
    }
}