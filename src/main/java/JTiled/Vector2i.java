package JTiled;

public class Vector2i {
    int x, y;

    Vector2i(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString() {
        return String.format("x: %d, y: %d", x, y);
    }
}
