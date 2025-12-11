package game.entities;

import game.core.GameObject;
import game.GameConfig;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.io.InputStream;

public class Platform extends GameObject {
    public static final int SIZE = GameConfig.PLATFORM_SIZE;
    
    private Image image;
    
    public Platform(double x, double y, String imgPath) {
        super(x, y, SIZE, SIZE);
        loadImage(imgPath);
    }
    
    private void loadImage(String imgPath) {
        try {
            // Normalize path - remove "file:" prefix and ensure leading slash
            String resourcePath = imgPath;
            if (resourcePath.startsWith("file:")) {
                resourcePath = resourcePath.substring(5);
            }
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            
            // Load as resource stream
            InputStream stream = Platform.class.getResourceAsStream(resourcePath);
            
            if (stream == null) {
                System.err.println("Platform image not found: " + resourcePath);
                image = null;
                return;
            }
            
            Image temp = new Image(stream);
            if (!temp.isError()) {
                image = temp;
                System.out.println("Loaded platform image: " + resourcePath);
            } else {
                image = null;
                System.err.println("Platform image error: " + resourcePath);
            }
        } catch (Exception e) {
            System.err.println("Exception loading platform: " + e.getMessage());
            image = null;
        }
    }
    
    @Override
    public void render(GraphicsContext gc, double camX) {
        double drawX = x - camX;
        if (image != null) {
            gc.drawImage(image, drawX, y, SIZE, SIZE);
        } else {
            gc.setFill(Color.web("#7f7f7f"));
            gc.fillRect(drawX, y, SIZE, SIZE);
            gc.setStroke(Color.BLACK);
            gc.strokeRect(drawX, y, SIZE, SIZE);
        }
    }
}