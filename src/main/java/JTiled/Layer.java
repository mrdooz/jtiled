package JTiled;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Layer {
    String name;
    Map map;
    boolean visible;

    Layer(String name, Map map) {
        this.name = name;
        this.map = map;
        tiles = new Tile[map.size.x][map.size.y];
        this.visible = true;
    }

    void Draw(GraphicsContext gc) {

        Vector2i size = map.size;
        Vector2i tileSize = map.tileSize;

        for (int i = 0; i < size.y; ++i) {
            for (int j = 0; j < size.x; ++j) {
                Tile tile = tiles[j][i];
                if (tile != null) {
                    tile.Draw(gc, j * tileSize.x, i * tileSize.y);
                }
            }
        }
    }

    Tile[][] tiles;

    @Override
    public String toString() {
        return name;
    }
}
