package menu;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import java.io.InputStream;

public abstract class AbstractMenu {
    protected Stage stage;
    protected Canvas canvas;
    protected GraphicsContext gc;
    protected Pane root;
    
    protected static final int BTN_WIDTH = 200; 
    protected static final int BTN_HEIGHT = 60;
    
    public AbstractMenu(Stage stage, int width, int height) {
        this.stage = stage;
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();
        this.root = new Pane(canvas);
    }
    
    protected Image loadImage(String path) {
        try {
            // Ensure path starts with /
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            
            // Load as resource stream
            InputStream stream = getClass().getResourceAsStream(path);
            
            if (stream == null) {
                System.out.println("Failed to load: " + path);
                return null;
            }
            
            Image img = new Image(stream);
            if (img.isError()) {
                System.out.println("Failed to load: " + path);
                return null;
            }
            return img;
        } catch (Exception e) {
            System.out.println("Exception loading " + path + ": " + e.getMessage());
            return null;
        }
    }
    
    protected boolean isInButton(double mx, double my, double bx, double by, int width, int height) {
        return mx > bx && mx < bx + width && my > by && my < by + height;
    }
    
    protected boolean isInButton(double mx, double my, double bx, double by) {
        return isInButton(mx, my, bx, by, BTN_WIDTH, BTN_HEIGHT);
    }
    
    protected void setupMouseHandlers() {
        canvas.setOnMouseMoved(this::handleHover);
        canvas.setOnMouseClicked(this::handleClick);
    }
    
    protected abstract void handleHover(MouseEvent e);
    
    protected abstract void handleClick(MouseEvent e);
    
    protected abstract void draw();
    
    public abstract void show();
    
    public void hide() {
        if (root.getParent() != null) {
            Pane currentRoot = (Pane) stage.getScene().getRoot();
            currentRoot.getChildren().remove(root);
        }
    }
}