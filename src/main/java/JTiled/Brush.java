package JTiled;

import javafx.scene.canvas.GraphicsContext;

public class Brush
{
    Vector2i size;
    Tileset tileset;
    TileRef tiles[][];
    Vector2i tileSize;

    Brush(Vector2i pos, Vector2i size, Tileset tileset) {
        // create a brush from a quad in the given tileset
        this.size = size;
        this.tileset = tileset;
        this.tileSize = tileset.tileSize;
        tiles = new TileRef[size.x][size.y];

        for (int y = 0; y < size.y; ++y) {
            for (int x = 0; x < size.x; ++x) {
                tiles[x][y] = TileRef.valueOf(tileset.tiles[x+pos.x][y+pos.y]);
            }
        }
    }

    void Draw(GraphicsContext gc, double destX, double destY) {
        if (tileset == null)
            return;

        if (Editor.instance.paintMode == PaintMode.Terrain) {
            // draw the brush for terrain painting
            Tile tile = Tile.findByRef(tiles[0][0]);
            tile.Draw(gc, destX, destY);

        } else {
            // draw the brush for tile painting
            double ofsY = 0;
            for (int i = 0; i < size.y; ++i) {
                double ofsX = 0;
                for (int j = 0; j < size.x; ++j) {
                    Tile tile = Tile.findByRef(tiles[j][i]);
                    tile.Draw(gc, destX + ofsX, destY + ofsY);
                    ofsX += tileSize.x;
                }
                ofsY += tileSize.y;
            }
        }
    }
}
