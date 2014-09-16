package JTiled;

import javafx.scene.canvas.GraphicsContext;

public class Tile {
    Vector2i pos;
    Vector2i size;
    Tileset tileset;

    TileFlag flag = TileFlag.None;

    Tile(Vector2i pos, Tileset tileset) {
        this.pos = pos;
        this.tileset = tileset;
        this.size = tileset.tileSize;
    }

    void Draw(GraphicsContext gc, double destX, double destY) {
        if (tileset == null)
            return;

        gc.drawImage(tileset.img,
                pos.x * size.x, pos.y * size.y, size.x, size.y,
                destX, destY, size.x, size.y);
    }
}
