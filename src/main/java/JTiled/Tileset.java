package JTiled;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Tileset {

    String name;
    String path;
    Vector2i tileSize;
    Vector2i offset;
    Vector2i padding;
    Image img;
    Vector2i numTiles;
    Tile tiles[][];

    int gidStart;   // used for serialization

    Tileset(String name, String path, Vector2i tileSize) throws FileNotFoundException {
        this(name, path, tileSize, new Vector2i(0,0), new Vector2i(0, 0));
    }

    public Tileset(String name, String path, Vector2i tileSize, Vector2i offset, Vector2i padding) throws FileNotFoundException {
        this.name = name;
        this.path = path;
        this.tileSize = tileSize;
        this.offset = offset;
        this.padding = padding;

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
                Tile t = new Tile(new Vector2i(x, y), this);
                t.terrian = 1;
                t.size = tileSize;
                tiles[x][y] = t;
            }
        }

        // hack hack!
        tiles[0][0].flag = TileFlag.TopLeft;
        tiles[0][0].wallFlags = WallFlag.Left | WallFlag.Top;
        tiles[1][0].flag = TileFlag.TopMiddle;
        tiles[1][0].wallFlags = WallFlag.Top;
        tiles[2][0].flag = TileFlag.TopRight;
        tiles[2][0].wallFlags = WallFlag.Right | WallFlag.Top;

        tiles[0][1].flag = TileFlag.MiddleLeft;
        tiles[0][1].wallFlags = WallFlag.Left;
        tiles[1][1].flag = TileFlag.Middle;
        tiles[1][1].wallFlags = 0;
        tiles[2][1].flag = TileFlag.MiddleRight;
        tiles[2][1].wallFlags = WallFlag.Right;

        tiles[0][2].flag = TileFlag.BottomLeft;
        tiles[0][2].wallFlags = WallFlag.Left | WallFlag.Bottom;
        tiles[1][2].flag = TileFlag.BottomMiddle;
        tiles[1][2].wallFlags = WallFlag.Bottom;
        tiles[2][2].flag = TileFlag.BottomRight;
        tiles[2][2].wallFlags = WallFlag.Right | WallFlag.Bottom;
    }
}
