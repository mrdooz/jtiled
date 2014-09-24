package JTiled;

import javafx.scene.canvas.GraphicsContext;

public class Tile {
    Vector2i pos;
    Vector2i size;
    Tileset tileset;
    int terrian;
    int wallFlags;

    Tile(Vector2i pos, Tileset tileset) {
        this.pos = pos;
        this.tileset = tileset;
        this.size = tileset.tileSize;
        this.terrian = 0;
        this.wallFlags = 0;
    }

    void Draw(GraphicsContext gc, double destX, double destY) {
        if (tileset == null)
            return;

        int x = tileset.offset.x + pos.x * (size.x + tileset.padding.x);
        int y = tileset.offset.y + pos.y * (size.y + tileset.padding.y);
        gc.drawImage(tileset.img,
                x, y, size.x, size.y,
                destX, destY, size.x, size.y);
    }
}
