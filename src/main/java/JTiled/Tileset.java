package JTiled;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.Map;

public class Tileset {

    String name;
    String path;
    @XStreamAlias("size")
    Vector2i tileSize;
    @XStreamAlias("offset")
    Vector2i offset;
    @XStreamAlias("padding")
    Vector2i padding;
    @XStreamOmitField
    Image img;
    Vector2i numTiles;
    Tile tiles[][];

    @XStreamAlias("id")
    int id;

    HashMap<Long, TileRef> tilesByFlag = new HashMap<>();

    static Tileset findByRef(int id) {
        return Editor.instance.tilesets.get(id);
    }

    void setTerrain(int x, int y, int terrain, long flags) {
        tiles[x][y].terrian = terrain;
        tiles[x][y].wallFlags = flags;
        tilesByFlag.put(flags, TileRef.valueOf(tiles[x][y]));
    }

    long makeFlags(long w, long nw, long n, long ne, long e, long se, long s, long sw) {
        return  (w << WallFlag.W) + (nw << WallFlag.NW) + (n << WallFlag.N) + (ne << WallFlag.NE) +
                (e << WallFlag.E) + (se << WallFlag.SE) + (s << WallFlag.S) + (sw << WallFlag.SW);
    }

    TileRef findTileByFlags(long wallFlags) {
        for (Map.Entry<Long, TileRef> t : tilesByFlag.entrySet()) {
            // check that the tile flags match each neighbour (each 3 bits)
            long v = wallFlags & t.getKey().longValue();
            boolean found = true;
            for (int i = 0; i < 8; ++i) {
                if ((v & WallFlag.mask[i]) == 0) {
                    found = false;
                    break;
                }
            }
            if (found)
                return t.getValue();
        }
        return null;
    }

    public Tileset(String name, String path, Vector2i tileSize, Vector2i offset, Vector2i padding) throws FileNotFoundException {
        this.name = name;
        this.path = path;
        this.tileSize = tileSize;
        this.offset = offset;
        this.padding = padding;
        this.id = Editor.instance.addTileset(this);

        this.name = name;
        this.path = path;
        this.tileSize = tileSize;
        this.img = new Image(new FileInputStream(path));
        int w = (int)this.img.getWidth() - offset.x;
        int h = (int)this.img.getHeight() - offset.y;
        int tx = tileSize.x + padding.x;
        int ty = tileSize.y + padding.y;
        numTiles = new Vector2i(w / tx, h / ty);
        tiles = new Tile[numTiles.x][numTiles.y];

        for (int y = 0; y < numTiles.y; ++y) {
            for (int x = 0; x < numTiles.x; ++x) {
                Tile t = new Tile(new Vector2i(x, y), id);
                t.terrian = 0;
                tiles[x][y] = t;
            }
        }

        // hack hack!
        long i = WallFlag.Inner;
        long b = WallFlag.Border;
        long a = i | b;
        long o = WallFlag.Outer;
        long x = WallFlag.DontCare;

        setTerrain(0, 0, 1, makeFlags(o, x, o, x, b, x, b, x));
        setTerrain(1, 0, 1, makeFlags(b, x, o, x, b, x, a, x));
        setTerrain(2, 0, 1, makeFlags(b, x, o, x, o, x, b, x));

        setTerrain(0, 1, 1, makeFlags(o, x, b, x, a, x, b, x));
        setTerrain(1, 1, 1, makeFlags(a, a, a, a, a, a, a, a));
        setTerrain(2, 1, 1, makeFlags(a, x, b, x, o, x, b, x));

        setTerrain(0, 2, 1, makeFlags(o, x, b, x, b, x, o, x));
        setTerrain(1, 2, 1, makeFlags(b, x, a, x, b, x, o, x));
        setTerrain(2, 2, 1, makeFlags(b, x, b, x, o, x, o, x));

        setTerrain(3, 2, 1, makeFlags(a, a, a, a, b, o, b, a));
        setTerrain(4, 2, 1, makeFlags(b, a, a, a, a, a, b, o));
        setTerrain(3, 3, 1, makeFlags(a, a, b, o, b, a, a, a));
        setTerrain(4, 3, 1, makeFlags(b, o, b, a, a, a, a, a));
    }
}
