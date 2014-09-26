package JTiled;

public class TileRef {
    int tilesetId;
    int tileId;

    TileRef(int tilesetId, int tileId) {
        this.tilesetId = tilesetId;
        this.tileId = tileId;
    }

    static TileRef valueOf(Tile tile) {
        Tileset tileset = Tileset.findByRef(tile.tilesetId);
        return new TileRef(tile.tilesetId, tile.pos.y * tileset.numTiles.x + tile.pos.x);
    }
}
