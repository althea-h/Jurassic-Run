package game.audio;

import game.GameConfig;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {
    private static AudioManager instance;
    
    private MediaPlayer menuMusic;
    private MediaPlayer pauseMusic;
    private MediaPlayer helpMusic;
    private MediaPlayer level1_3Music;
    private MediaPlayer level4Music;
    private MediaPlayer currentMusic;
    
    private Map<String, AudioClip> soundEffects;
    
    private double musicVolume;
    private double sfxVolume;
    private boolean musicEnabled;
    private boolean sfxEnabled;
    
    private boolean mediaAvailable;
    
    private static final String MUSIC_PATH = "/assets/audio/music/";
    private static final String SFX_PATH = "/assets/audio/sfx/";
    
    private AudioManager() {
        soundEffects = new HashMap<>();
        musicVolume = GameConfig.MUSIC_VOLUME;
        sfxVolume = GameConfig.SFX_VOLUME;
        musicEnabled = true;
        sfxEnabled = true;
        mediaAvailable = true;
        System.out.println("AudioManager initialized (lazy loading)");
    }
    
    public static AudioManager getInstance() {
        if (instance == null) {
            instance = new AudioManager();
        }
        return instance;
    }
    
    // Load music using resource stream
    private MediaPlayer loadMusicLazy(String name, String filename) {
        if (!mediaAvailable) return null;
        
        try {
            String resourcePath = MUSIC_PATH + filename;
            URL resource = AudioManager.class.getResource(resourcePath);
            
            if (resource == null) {
                System.err.println("Music resource not found: " + resourcePath);
                return null;
            }
            
            String urlString = resource.toExternalForm();
            Media sound = new Media(urlString);
            MediaPlayer player = new MediaPlayer(sound);
            player.setVolume(musicVolume);
            player.setCycleCount(MediaPlayer.INDEFINITE);
            
            System.out.println("Loaded music: " + name + " from " + resourcePath);
            return player;
            
        } catch (Exception e) {
            System.err.println("Failed to load music " + name + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    // Load sound effect using resource stream
    private AudioClip loadSoundEffectLazy(String name, String filename) {
        try {
            String resourcePath = SFX_PATH + filename;
            URL resource = AudioManager.class.getResource(resourcePath);
            
            if (resource == null) {
                System.err.println("Sound effect resource not found: " + resourcePath);
                return null;
            }
            
            String urlString = resource.toExternalForm();
            AudioClip clip = new AudioClip(urlString);
            clip.setVolume(sfxVolume);
            
            System.out.println("Loaded SFX: " + name + " from " + resourcePath);
            return clip;
            
        } catch (Exception e) {
            System.err.println("Failed to load SFX " + name + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    public void playMenuMusic() {
        if (!musicEnabled) return;
        
        if (menuMusic == null) {
            menuMusic = loadMusicLazy("menu", "menu.mp3");
        }
        playMusic(menuMusic, "menu");
    }
    
    public void playPauseMusic() {
        if (!musicEnabled) return;
        
        if (pauseMusic == null) {
            pauseMusic = loadMusicLazy("pause", "pause.mp3");
        }
        playMusic(pauseMusic, "pause");
    }
    
    public void playHelpMusic() {
        if (!musicEnabled) return;
        
        if (helpMusic == null) {
            helpMusic = loadMusicLazy("help", "help.mp3");
        }
        playMusic(helpMusic, "help");
    }
    
    public void playLevel1_3Music() {
        if (!musicEnabled) return;
        
        if (level1_3Music == null) {
            level1_3Music = loadMusicLazy("level1_3", "level1_3.mp3");
        }
        playMusic(level1_3Music, "level1_3");
    }
    
    public void playLevel4Music() {
        if (!musicEnabled) return;
        
        if (level4Music == null) {
            level4Music = loadMusicLazy("level4", "level4.mp3");
        }
        playMusic(level4Music, "level4");
    }
    
    private void playMusic(MediaPlayer music, String name) {
        if (music == null) {
            System.err.println("Cannot play " + name + " music: not loaded");
            return;
        }
        
        if (currentMusic != null && currentMusic != music) {
            currentMusic.stop();
        }
        
        currentMusic = music;
        currentMusic.seek(Duration.ZERO);
        currentMusic.play();
        System.out.println("Playing " + name + " music");
    }
    
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
        }
    }
    
    public void pauseMusic() {
        if (currentMusic != null) {
            currentMusic.pause();
        }
    }
    
    public void resumeMusic() {
        if (currentMusic != null && musicEnabled) {
            currentMusic.play();
        }
    }
    
    private void playSoundEffect(String effectName, String filename) {
        if (!sfxEnabled) return;
        
        AudioClip clip = soundEffects.get(effectName);
        if (clip == null) {
            clip = loadSoundEffectLazy(effectName, filename);
            if (clip != null) {
                soundEffects.put(effectName, clip);
            }
        }
        
        if (clip != null) {
            clip.play();
        }
    }
    
    public void playJump() { 
        playSoundEffect("jump", "jump.wav"); 
    }
    
    public void playCollectFossil() { 
        playSoundEffect("collect_fossil", "collect_fossil.mp3"); 
    }
    
    public void playCollectPowerUp() { 
        playSoundEffect("collect_powerup", "collect_powerup.wav"); 
    }
    
    public void playEnemyDie() { 
        playSoundEffect("enemy_die", "enemy_die.mp3"); 
    }
    
    public void playPlayerHurt() { 
        playSoundEffect("player_hurt", "player_hurt.mp3"); 
    }
    
    public void playGameOver() { 
        playSoundEffect("game_over", "game_over.mp3"); 
    }
    
    public void playPlayerEscape() { 
        playSoundEffect("player_escape", "player_escape.mp3"); 
    }
    
    public void setMusicVolume(double volume) {
        musicVolume = Math.max(0.0, Math.min(1.0, volume));
        
        MediaPlayer[] players = {menuMusic, pauseMusic, helpMusic, level1_3Music, level4Music};
        for (MediaPlayer player : players) {
            if (player != null) {
                player.setVolume(musicVolume);
            }
        }
    }
    
    public void setSfxVolume(double volume) {
        sfxVolume = Math.max(0.0, Math.min(1.0, volume));
        
        for (AudioClip clip : soundEffects.values()) {
            if (clip != null) {
                clip.setVolume(sfxVolume);
            }
        }
    }
    
    public double getMusicVolume() {
        return musicVolume;
    }
    
    public double getSfxVolume() {
        return sfxVolume;
    }
    
    public void setMusicEnabled(boolean enabled) {
        musicEnabled = enabled;
        if (!enabled) {
            stopMusic();
        }
    }
    
    public void setSfxEnabled(boolean enabled) {
        sfxEnabled = enabled;
    }
    
    public boolean isMusicEnabled() {
        return musicEnabled;
    }
    
    public boolean isSfxEnabled() {
        return sfxEnabled;
    }
    
    public void preloadEssentialSounds() {
        System.out.println("Preloading essential sounds...");
        
        if (!soundEffects.containsKey("jump")) {
            AudioClip jump = loadSoundEffectLazy("jump", "jump.wav");
            if (jump != null) soundEffects.put("jump", jump);
        }
        
        if (menuMusic == null) {
            menuMusic = loadMusicLazy("menu", "menu.mp3");
        }
        
        System.out.println("Preloading complete");
    }
    
    public void preloadAllSounds() {
        System.out.println("Preloading all sounds...");
        
        if (menuMusic == null) menuMusic = loadMusicLazy("menu", "menu.mp3");
        if (pauseMusic == null) pauseMusic = loadMusicLazy("pause", "pause.mp3");
        if (helpMusic == null) helpMusic = loadMusicLazy("help", "help.mp3");
        if (level1_3Music == null) level1_3Music = loadMusicLazy("level1_3", "level1_3.mp3");
        if (level4Music == null) level4Music = loadMusicLazy("level4", "level4.mp3");
        
        String[] effects = {
            "jump", "collect_fossil", "collect_powerup", 
            "enemy_die", "player_hurt", "game_over", "player_escape"
        };
        
        String[] files = {
            "jump.wav", "collect_fossil.mp3", "collect_powerup.mp3",
            "enemy_die.mp3", "player_hurt.mp3", "game_over.mp3", "player_escape.mp3"
        };
        
        for (int i = 0; i < effects.length; i++) {
            if (!soundEffects.containsKey(effects[i])) {
                AudioClip clip = loadSoundEffectLazy(effects[i], files[i]);
                if (clip != null) soundEffects.put(effects[i], clip);
            }
        }
        
        System.out.println("All sounds preloaded");
    }
    
    public void cleanup() {
        stopMusic();
        
        MediaPlayer[] players = {menuMusic, pauseMusic, helpMusic, level1_3Music, level4Music};
        for (MediaPlayer player : players) {
            if (player != null) player.dispose();
        }
        
        soundEffects.clear();
        System.out.println("AudioManager cleaned up");
    }
    
    public void printLoadedSounds() {
        System.out.println("=== LOADED SOUNDS ===");
        System.out.println("Music loaded: " + 
            (menuMusic != null ? "menu " : "") +
            (pauseMusic != null ? "pause " : "") +
            (helpMusic != null ? "help " : "") +
            (level1_3Music != null ? "level1_3 " : "") +
            (level4Music != null ? "level4 " : ""));
        
        System.out.println("Sound effects loaded: " + soundEffects.size());
        System.out.println("Sound effects: " + String.join(", ", soundEffects.keySet()));
    }
}