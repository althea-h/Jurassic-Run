package game.levels;

import game.entities.*;
import game.GameConfig;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.InputStream;
import java.util.ArrayList;

public class LevelLoader {
    
    public static class LevelData {
        public ArrayList<Platform> platforms;
        public ArrayList<Fossil> fossils;
        public ArrayList<PowerUp> powerUps;
        public ArrayList<Enemy> enemies;
        public double gateX;
        
        public LevelData() {
            platforms = new ArrayList<>();
            fossils = new ArrayList<>();
            powerUps = new ArrayList<>();
            enemies = new ArrayList<>();
            gateX = GameConfig.GATE_X_POSITION;
        }
    }
    
    public static LevelData loadLevelFromFile(int level) {
        LevelData data = new LevelData();
        String path = "/assets/levels/level" + level + ".txt";
        
        try {
            // Use getResourceAsStream instead of FileReader
            InputStream inputStream = LevelLoader.class.getResourceAsStream(path);
            
            if (inputStream == null) {
                System.err.println("Could not find resource: " + path);
                return createFallbackLevel(level);
            }
            
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                
                String[] tokens = line.split(" ");
                
                switch (tokens[0]) {
                    case "BLOCK":
                        parsePlatform(tokens, level, data);
                        break;
                    case "FOSSIL":
                        parseFossil(tokens, level, data);
                        break;
                    case "POWERUP":
                        parsePowerUp(tokens, data);
                        break;
                    case "ENEMY":
                        parseEnemy(tokens, level, data);
                        break;
                    case "GATE":
                        parseGate(tokens, data);
                        break;
                }
            }
            
            br.close();
            
        } catch (Exception e) {
            System.out.println("Level text load error for level " + level + ": " + e.getMessage());
            e.printStackTrace();
            return createFallbackLevel(level);
        }
        
        return data;
    }
    
    private static void parsePlatform(String[] tokens, int level, LevelData data) {
        double x = Double.parseDouble(tokens[1]);
        double y = Double.parseDouble(tokens[2]);
        
        double pixelX = x;
        double pixelY = y;
        
        if (x < 100 && y < 100) {
            pixelX = x * GameConfig.BLOCK_SIZE;
            pixelY = y * GameConfig.BLOCK_SIZE;
            System.out.println("Converted tile coordinates: (" + x + "," + y + ") to pixels: (" + pixelX + "," + pixelY + ")");
        }
        
        double adjustedY = adjustPlatformY(pixelY);
        
        // Use resource path (no "file:" prefix)
        String tex = "/assets/blocks/newblock" + level + ".png";
        Platform platform = new Platform(pixelX, adjustedY, tex);
        data.platforms.add(platform);
    }
    
    private static void parseFossil(String[] tokens, int level, LevelData data) {
        double x = Double.parseDouble(tokens[1]);
        double y = Double.parseDouble(tokens[2]);
        
        double pixelX = x;
        double pixelY = y;
        
        if (x < 100 && y < 100) {
            pixelX = x * GameConfig.BLOCK_SIZE;
            pixelY = y * GameConfig.BLOCK_SIZE;
        }
        
        double adjustedY = pixelY - (GameConfig.FOSSIL_SIZE - 28) / 2;
        
        // Use resource path (no "file:" prefix)
        String tex = "/assets/images/fossils/fossil" + level + ".png";
        Fossil fossil = new Fossil(pixelX, adjustedY, tex);
        data.fossils.add(fossil);
        System.out.println("Loaded fossil at: (" + pixelX + ", " + adjustedY + ")");
    }
    
    private static void parsePowerUp(String[] tokens, LevelData data) {
        double x = Double.parseDouble(tokens[1]);
        double y = Double.parseDouble(tokens[2]);
        
        double pixelX = x;
        double pixelY = y;
        
        if (x < 100 && y < 100) {
            pixelX = x * GameConfig.BLOCK_SIZE;
            pixelY = y * GameConfig.BLOCK_SIZE;
        }
        
        PowerUp.Type type = PowerUp.Type.valueOf(tokens[3].toUpperCase());
        // Use resource path (no "file:" prefix)
        String tex = "/assets/powerups/" + tokens[3].toLowerCase() + ".png";
        PowerUp powerUp = new PowerUp(pixelX, pixelY, type, tex);
        data.powerUps.add(powerUp);
    }
    
    private static void parseEnemy(String[] tokens, int level, LevelData data) {
        String type = tokens[1].toLowerCase();
        double x = Double.parseDouble(tokens[2]);
        double y = Double.parseDouble(tokens[3]);
        
        double pixelX = x;
        double pixelY = y;
        
        if (x < 100 && y < 100) {
            pixelX = x * GameConfig.BLOCK_SIZE;
            pixelY = y * GameConfig.BLOCK_SIZE;
            System.out.println("Converted enemy tile coordinates: (" + x + "," + y + ") to pixels: (" + pixelX + "," + pixelY + ")");
        }
        
        double adjustedY = pixelY - 60;
        
        Enemy enemy = new Enemy(type, pixelX, adjustedY);
        enemy.setScale(GameConfig.ENEMY_SCALE_FACTOR);
        
        adjustEnemySpeed(enemy, level);
        setEnemyPatrolDistance(enemy, level);
        
        data.enemies.add(enemy);
        System.out.println("Loaded " + type + " enemy at: (" + pixelX + ", " + adjustedY + ") - Original Y: " + pixelY);
    }
    
    private static void parseGate(String[] tokens, LevelData data) {
        double x = Double.parseDouble(tokens[1]);
        
        if (x < 100) {
            data.gateX = x * GameConfig.BLOCK_SIZE;
            System.out.println("Converted gate tile coordinate: " + x + " to pixels: " + data.gateX);
        } else {
            data.gateX = x;
        }
        System.out.println("Gate position set to: " + data.gateX);
    }
    
    private static double adjustPlatformY(double originalY) {
        double groundLevel = 500;
        double platformHeight = Platform.SIZE;
        
        if (originalY + platformHeight > groundLevel + 50) {
            double adjustedY = groundLevel - platformHeight;
            System.out.println("Adjusting platform Y from " + originalY + " to " + adjustedY);
            return adjustedY;
        }
        
        return originalY;
    }
    
    private static void adjustEnemySpeed(Enemy enemy, int level) {
        String type = enemy.getType().toLowerCase();
        
        if ("pterodactyl".equals(type) && level == 2) {
            enemy.setSpeed(0.5);
            System.out.println("Adjusted pterodactyl speed for level 2: 0.5");
            return;
        }
        
        switch (level) {
            case 1:
                enemy.setSpeed(0.4);
                break;
            case 2:
                enemy.setSpeed(0.5);
                break;
            case 3:
                enemy.setSpeed(0.6);
                break;
            case 4:
                enemy.setSpeed(0.8);
                break;
        }
    }
    
    private static void setEnemyPatrolDistance(Enemy enemy, int level) {
        int patrolDistance = GameConfig.DEFAULT_PATROL_DISTANCE;
        
        switch (level) {
            case 1:
            case 2:
            case 3:
            case 4:
                patrolDistance = GameConfig.DEFAULT_PATROL_DISTANCE;
                break;
        }
        
        enemy.setPatrolDistance(patrolDistance);
        System.out.println("Set " + enemy.getType() + " patrol distance to " + patrolDistance + " blocks on level " + level);
    }
    
    private static LevelData createFallbackLevel(int level) {
        System.out.println("Creating fallback level for level " + level + "...");
        
        LevelData data = new LevelData();
        
        for (int i = 0; i < 300; i++) {
            data.platforms.add(new Platform(i * GameConfig.BLOCK_SIZE, 468, "/assets/blocks/block1.png"));
        }
        
        data.platforms.add(new Platform(500, 368, "/assets/blocks/block1.png"));
        data.platforms.add(new Platform(600, 368, "/assets/blocks/block1.png"));
        data.platforms.add(new Platform(1500, 368, "/assets/blocks/block1.png"));
        data.platforms.add(new Platform(1600, 368, "/assets/blocks/block1.png"));
        
        for (int i = 0; i < GameConfig.FOSSILS_PER_LEVEL; i++) {
            double x = 800 + (i * 200);
            double y = 400;
            Fossil fossil = new Fossil(x, y, "/assets/images/fossils/fossil" + level + ".png");
            data.fossils.add(fossil);
        }
        
        PowerUp powerUp1 = new PowerUp(450, 400, PowerUp.Type.SPEED, "/assets/images/powerups/speed.png");
        data.powerUps.add(powerUp1);
        
        PowerUp powerUp2 = new PowerUp(1200, 300, PowerUp.Type.JUMP, "/assets/images/powerups/jump.png");
        data.powerUps.add(powerUp2);
        
        PowerUp powerUp3 = new PowerUp(2000, 400, PowerUp.Type.INVINCIBLE, "/assets/images/powerups/invincible.png");
        data.powerUps.add(powerUp3);
        
        switch (level) {
            case 1:
                addFallbackEnemy("raptor", 1000, 468, data);
                addFallbackEnemy("raptor", 1800, 468, data);
                break;
            case 2:
                addFallbackEnemy("pterodactyl", 1000, 300, data);
                addFallbackEnemy("pterodactyl", 1500, 250, data);
                break;
            case 3:
                addFallbackEnemy("triceratops", 1000, 468, data);
                addFallbackEnemy("triceratops", 1800, 468, data);
                addFallbackEnemy("raptor", 2200, 468, data);
                break;
            case 4:
                addFallbackEnemy("trex", 1200, 468, data);
                addFallbackEnemy("pterodactyl", 800, 200, data);
                addFallbackEnemy("triceratops", 2000, 468, data);
                break;
        }
        
        data.gateX = GameConfig.GATE_X_POSITION;
        
        System.out.println("Fallback level " + level + " created with " + data.enemies.size() + " enemies");
        return data;
    }
    
    private static void addFallbackEnemy(String type, double x, double y, LevelData data) {
        double adjustedY = y - 60;
        Enemy enemy = new Enemy(type, x, adjustedY);
        enemy.setScale(GameConfig.ENEMY_SCALE_FACTOR);
        enemy.setPatrolDistance(GameConfig.DEFAULT_PATROL_DISTANCE);
        data.enemies.add(enemy);
        System.out.println("Added fallback " + type + " with " + GameConfig.DEFAULT_PATROL_DISTANCE + "-block patrol at (" + x + ", " + adjustedY + ")");
    }
}