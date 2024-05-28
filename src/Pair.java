/**
 * This class represents a pair of coordinates (x, y).
 * It is typically used to store and manipulate the coordinates of a point in a 2D space.
 */
public class Pair {
    private double x;  // The x-coordinate
    private double y;  // The y-coordinate

    /**
     * Constructs a new Pair with specified x and y coordinates.
     * @param x the x-coordinate of this pair
     * @param y the y-coordinate of this pair
     */
    public Pair(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-coordinate of this pair.
     * @return the x-coordinate
     */
    public double getX() {
        return x;
    }

    /**
     * Sets the x-coordinate of this pair.
     * @param x the new x-coordinate
     */
    public void setX(double x) {
        this.x = x;
    }

    /**
     * Returns the y-coordinate of this pair.
     * @return the y-coordinate
     */
    public double getY() {
        return y;
    }

    /**
     * Sets the y-coordinate of this pair.
     * @param y the new y-coordinate
     */
    public void setY(double y) {
        this.y = y;
    }

    /**
     * Returns a string representation of this pair.
     * The string is formatted as "(x, y)".
     * @return a string representation of this pair
     */
    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
