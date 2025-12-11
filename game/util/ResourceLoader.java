package game.util;

import javafx.scene.image.Image;
import java.io.InputStream;
import java.net.URL;

/**
 * Utility class for loading resources from JAR files
 * This ensures assets work both in development and when exported as JAR
 */
public class ResourceLoader {
    
    /**
     * Load an image from resources
     * @param path Resource path (e.g., "/assets/images/player/idle1.png")
     * @return Image object, or null if not found
     */
    public static Image loadImage(String path) {
        try {
            // Ensure path starts with /
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            
            // Try to get resource as stream
            InputStream stream = ResourceLoader.class.getResourceAsStream(path);
            
            if (stream == null) {
                System.err.println("Image resource not found: " + path);
                return null;
            }
            
            Image img = new Image(stream);
            
            if (img.isError()) {
                System.err.println("Failed to load image: " + path);
                return null;
            }
            
            System.out.println("Loaded image: " + path);
            return img;
            
        } catch (Exception e) {
            System.err.println("Exception loading image " + path + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Load an image with fallback path
     * @param path Primary resource path
     * @param fallbackPath Fallback resource path if primary fails
     * @return Image object, or null if both fail
     */
    public static Image loadImageWithFallback(String path, String fallbackPath) {
        Image img = loadImage(path);
        if (img == null && fallbackPath != null) {
            System.out.println("Trying fallback path: " + fallbackPath);
            img = loadImage(fallbackPath);
        }
        return img;
    }
    
    /**
     * Get resource URL (useful for Media files in JavaFX)
     * @param path Resource path
     * @return URL string, or null if not found
     */
    public static String getResourceURL(String path) {
        try {
            // Ensure path starts with /
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            
            URL resource = ResourceLoader.class.getResource(path);
            
            if (resource == null) {
                System.err.println("Resource URL not found: " + path);
                return null;
            }
            
            return resource.toExternalForm();
            
        } catch (Exception e) {
            System.err.println("Exception getting resource URL " + path + ": " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Check if a resource exists
     * @param path Resource path
     * @return true if resource exists
     */
    public static boolean resourceExists(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        URL resource = ResourceLoader.class.getResource(path);
        return resource != null;
    }
    
    /**
     * Normalize path - converts "file:" paths to resource paths
     * @param path Original path (may have "file:" prefix or "assets/" prefix)
     * @return Normalized resource path starting with /
     */
    public static String normalizePath(String path) {
        // Remove "file:" prefix if present
        if (path.startsWith("file:")) {
            path = path.substring(5);
        }
        
        // Ensure leading slash
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        return path;
    }
}