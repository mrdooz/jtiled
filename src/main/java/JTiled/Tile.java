package JTiled;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import javafx.scene.canvas.GraphicsContext;

@XStreamAlias("tile")
public class Tile {

    static Tile findByRef(TileRef ref) {
        if (ref == null)
            return null;

        Tileset tileset = Editor.instance.tilesets.get(ref.tilesetId);
        int x = ref.tileId % tileset.numTiles.x;
        int y = ref.tileId / tileset.numTiles.x;
        return tileset.tiles[x][y];
    }

    Vector2i pos;
    @XStreamAsAttribute
    int tilesetId;
    @XStreamAsAttribute
    int terrian;
    @XStreamAsAttribute
    int wallFlags;

    Tile(Vector2i pos, int tilesetId) {
        this.pos = pos;
        this.tilesetId = tilesetId;
        this.terrian = 0;
        this.wallFlags = 0;
    }

    void Draw(GraphicsContext gc, double destX, double destY) {
        Tileset tileset = Editor.instance.tilesets.get(tilesetId);
        Vector2i size = tileset.tileSize;

        int x = tileset.offset.x + pos.x * (size.x + tileset.padding.x);
        int y = tileset.offset.y + pos.y * (size.y + tileset.padding.y);
        gc.drawImage(tileset.img,
                x, y, size.x, size.y,
                destX, destY, size.x, size.y);
    }
}
