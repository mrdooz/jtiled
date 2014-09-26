package JTiled;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;

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

    HashMap<Integer, TileRef> tilesByFlag = new HashMap<>();

    static Tileset findByRef(int id) {
        return Editor.instance.tilesets.get(id);
    }

    void setTerrain(int x, int y, int terrain, int flags) {
        tiles[x][y].terrian = terrain;
        tiles[x][y].wallFlags = flags;
        tilesByFlag.put(flags, TileRef.valueOf(tiles[x][y]));
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
        setTerrain(0, 0, 1, WallFlag.Right | WallFlag.Bottom);
        setTerrain(1, 0, 1, WallFlag.Left | WallFlag.Bottom | WallFlag.Right);
        setTerrain(2, 0, 1, WallFlag.Left | WallFlag.Bottom);

        setTerrain(0, 1, 1, WallFlag.Top | WallFlag.Right | WallFlag.Bottom);
        setTerrain(1, 1, 1, WallFlag.Top | WallFlag.Right | WallFlag.Left | WallFlag.Bottom);
        tilesByFlag.put(0, TileRef.valueOf(tiles[1][1]));

        setTerrain(2, 1, 1, WallFlag.Left | WallFlag.Top | WallFlag.Bottom);

        setTerrain(0, 2, 1, WallFlag.Right | WallFlag.Top);
        setTerrain(1, 2, 1, WallFlag.Left | WallFlag.Top | WallFlag.Right);
        setTerrain(2, 2, 1, WallFlag.Left | WallFlag.Top);

//        setTerrain(3, 3, 1, WallFlag.Left | WallFlag.Top);
//        setTerrain(4, 3, 1, WallFlag.Right | WallFlag.Top);
//        setTerrain(3, 4, 1, WallFlag.Left | WallFlag.Bottom);
//        setTerrain(4, 4, 1, WallFlag.Right | WallFlag.Bottom);
    }
}
