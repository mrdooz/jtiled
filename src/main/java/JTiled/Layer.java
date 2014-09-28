package JTiled;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.scene.canvas.GraphicsContext;

import java.util.Vector;

@XStreamAlias("layer")
public class Layer {
    @XStreamAsAttribute
    String name;
    @XStreamAsAttribute
    boolean visible;

    @XStreamOmitField
    Map map;

    @XStreamImplicit(itemFieldName="tiles")
    TileRef[][] tiles;

    boolean [][] borderTiles;

    Layer(String name, Map map) {
        this.name = name;
        this.map = map;
        int x = map.size.x;
        int y = map.size.y;
        tiles = new TileRef[x][y];
        this.borderTiles = new boolean[x][y];
        this.visible = true;
    }

    void Draw(GraphicsContext gc) {

        Vector2i size = map.size;
        Vector2i tileSize = map.tileSize;

        for (int i = 0; i < size.y; ++i) {
            for (int j = 0; j < size.x; ++j) {
                Tile tile = Tile.findByRef(tiles[j][i]);
                if (tile != null) {
                    tile.Draw(gc, j * tileSize.x, i * tileSize.y);
                }
            }
        }
    }

    void setTile(Vector2i pos, TileRef tile) {
        tiles[pos.x][pos.y] = tile;
    }

    boolean isBorder(Vector2i pos, int xOfs, int yOfs) {
        int x = pos.x + xOfs;
        int y = pos.y + yOfs;

        if (x < 0 || x >= map.size.x || y < 0 || y >= map.size.y)
            return true;

        return borderTiles[x][y];
    }

    void calcBorder() {

        for (int y = 0; y < map.size.y; ++y) {
            for (int x = 0; x < map.size.x; ++x) {
                TileRef r = tiles[x][y];
                borderTiles[x][y] = true;
                if (r == null)
                    continue;

                Tile t = Tile.findByRef(r);
                int terrain = t.terrian;
                int[] ofs = {
                        -1, +0, +1, +0, +0, +1, +0, -1,
                        -1, -1, +1, -1, -1, +1, +1, +1 };

                borderTiles[x][y] = false;
                for (int i = 0; i < 8; ++i) {
                    if (!sameTerrain(new Vector2i(x, y), ofs[i * 2 + 0], ofs[i * 2 + 1], terrain)) {
                        borderTiles[x][y] = true;
                        break;
                    }
                }
            }
        }
    }

    boolean sameTerrain(Vector2i v, int ofsX, int ofsY, int terrain) {
        int ox = v.x + ofsX;
        int oy = v.y + ofsY;
        if (ox < 0 || ox > map.size.x || oy < 0 || oy > map.size.y || tiles[ox][oy] == null)
            return false;

        return Tile.findByRef(tiles[ox][oy]).terrian == terrain;
    }

    TileRef getTile(Vector2i pos) {
        if (pos.x < 0 || pos.x >= map.size.x || pos.y < 0 || pos.y >= map.size.y)
            return null;

        return tiles[pos.x][pos.y];
    }


    @Override
    public String toString() {
        return name;
    }
}
