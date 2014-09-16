package JTiled;

import javafx.scene.canvas.GraphicsContext;

public class Brush
{
    Vector2i size;
    Tileset tileset;
    Tile tiles[][];
    Vector2i tileSize;

    Brush(Vector2i pos, Vector2i size, Tileset tileset) {
        // create a brush from a quad in the given tileset

        this.size = size;
        this.tileset = tileset;
        this.tileSize = tileset.tileSize;
        tiles = new Tile[size.x][size.y];

        for (int i = 0; i < size.y; ++i) {
            for (int j = 0; j < size.x; ++j) {
                tiles[j][i] = new Tile(new Vector2i(pos.x + j, pos.y + i), tileset);
            }
        }
    }

    void Draw(GraphicsContext gc, double destX, double destY) {
        if (tileset == null)
            return;

        double ofsY = 0;
        for (int i = 0; i < size.y; ++i) {
            double ofsX = 0;
            for (int j = 0; j < size.x; ++j) {
                Tile tile = tiles[j][i];
                tile.Draw(gc, destX + ofsX, destY + ofsY);
                ofsX += tileSize.x;
            }
            ofsY += tileSize.y;
        }
    }
}
