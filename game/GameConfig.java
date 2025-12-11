package game;

public final class GameConfig { //stores all values as permanent and no accidental changes
    private GameConfig() {}
    
    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 600;
    
    public static final int WORLD_WIDTH = 9600; //map width
    public static final int GATE_X_POSITION = 9500; //gate position
    
    public static final int BLOCK_SIZE = 32;
    public static final int PLATFORM_SIZE = 32;
    
    public static final double GRAVITY = 0.45;
    public static final double PLAYER_ACCELERATION = 0.18;
    public static final double PLAYER_MAX_SPEED = 2.0;
    public static final double PLAYER_JUMP_FORCE = -8.5;
    
    public static final int PLAYER_STARTING_LIVES = 5;
    public static final long INVINCIBILITY_DURATION = 2_000_000_000L;
    public static final long POWERUP_DURATION = 8_000_000_000L;
    
    public static final int FOSSILS_PER_LEVEL = 6;
    public static final int TOTAL_LEVELS = 4;
    
    public static final double FOSSIL_SIZE = 58.0;
    public static final double POWERUP_SIZE = 48.0;
    public static final double ENEMY_SCALE_FACTOR = 1.3;
    
    public static final int DEFAULT_PATROL_DISTANCE = 2;
    
    public static final long ANIMATION_DELAY = 120_000_000L;
    
    public static final double KNOCKBACK_DECAY = 0.9;
    
    public static final double MUSIC_VOLUME = 0.7;
    public static final double SFX_VOLUME = 0.8;
    
    public static final int HUD_FOSSIL_SIZE = 32;
    public static final int HUD_LIFE_SIZE = 40;
    public static final int HUD_PADDING = 15;
    public static final int HUD_ICON_SPACING = 10;
}