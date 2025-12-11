package game.entities;

import game.core.GameObject;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import java.io.InputStream;

public class Gate extends GameObject {
    public static final int WIDTH = 96;
    public static final int HEIGHT = 96;
    
    private Image image;
    
    public Gate(double x, double y, String imagePath) {
        super(x, y, WIDTH, HEIGHT);
        loadImage(imagePath);
    }
    
    private void loadImage(String imagePath) {
        try {
            // Normalize path
            String resourcePath = imagePath;
            if (!resourcePath.startsWith("/")) {
                resourcePath = "/" + resourcePath;
            }
            
            InputStream stream = Gate.class.getResourceAsStream(resourcePath);
            
            if (stream == null) {
                System.err.println("Gate image not found: " + resourcePath);
                image = null;
                return;
            }
            
            image = new Image(stream);
            if (image.isError()) {
                image = null;
            }
        } catch (Exception e) {
            image = null;
        }
    }
    
    @Override
    public void render(GraphicsContext gc, double cameraX) {
        if (image != null) {
            gc.drawImage(image, x - cameraX, y, WIDTH, HEIGHT);
        } else {
            gc.setFill(Color.WHITE);
            gc.fillRect(x - cameraX, y, WIDTH, HEIGHT);
            gc.setFill(Color.BLACK);
            gc.fillText("GATE", x - cameraX + 10, y + 50);
        }
    }
}