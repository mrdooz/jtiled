package JTiled;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Layer {
    String name;
    Map map;
    boolean visible;
    Tile[][] tiles;

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

    Tile getTile(Vector2i v) {
        return tiles[v.x][v.y];
    }

    boolean sameTerrain(Vector2i v, int ofsX, int ofsY, int terrain) {
        int ox = v.x + ofsX;
        int oy = v.y + ofsY;
        if (ox < 0 || ox > map.size.x || oy < 0 || oy > map.size.y || tiles[ox][oy] == null)
            return false;

        return tiles[ox][oy].terrian == terrain;
    }


    @Override
    public String toString() {
        return name;
    }
}
