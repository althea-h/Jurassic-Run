package game.entities;

import game.core.GameObject;
import javafx.scene.image.Image;
import javafx.scene.canvas.GraphicsContext;
import java.io.InputStream;

public abstract class Collectible extends GameObject {
    protected boolean collected;
    protected Image image;
    protected double size;
    
    public Collectible(double x, double y, double size, String imagePath) {
        super(x, y, size, size);
        this.size = size;
        this.collected = false;
        loadImage(imagePath);
    }
    
    private void loadImage(String imagePath) {
        try {
            // Normalize path - remove "file:" prefix and ensure leading slash
            String resourcePath = imagePath;
            if (resourcePath.startsWith("file:")) {
                resourcePath = resourcePath.substring(5);
            }
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            
            // Load as resource stream
            InputStream stream = Collectible.class.getResourceAsStream(resourcePath);
            
            if (stream == null) {
                System.err.println("Collectible image not found: " + resourcePath);
                image = null;
                return;
            }
            
            image = new Image(stream);
            if (image.isError()) {
                System.err.println("Failed to load collectible image: " + resourcePath);
                image = null;
            } else {
                System.out.println("Loaded collectible: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Error loading collectible image: " + e.getMessage());
            image = null;
        }
    }
    
    public boolean isCollected() {
        return collected;
    }
    
    public void collect() {
        collected = true;
        onCollected();
    }
    
    public void reset() {
        collected = false;
    }
    
    public void setSize(double newSize) {
        this.size = newSize;
        this.width = newSize;
        this.height = newSize;
    }
    
    protected abstract void onCollected();
    
    protected abstract void renderFallback(GraphicsContext gc, double drawX, double drawY);
    
    @Override
    public void render(GraphicsContext gc, double cameraX) {
        if (collected) return;
        
        double drawX = x - cameraX;
        
        if (image != null && !image.isError()) {
            gc.drawImage(image, drawX, y, size, size);
        } else {
            renderFallback(gc, drawX, y);
        }
    }
}