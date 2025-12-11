package game.core;

public interface Collidable {
    boolean collidesWith(double x, double y, double width, double height);
    double getX();
    double getY();
    double getWidth();
    double getHeight();
}