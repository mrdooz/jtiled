package JTiled;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Tileset {
    String name;
    String path;
    Image img;
    Vector2i tileSize;
    Vector2i numTiles;
    int gidStart;   // used for serialization

    Tileset(String name, String path, Vector2i tileSize) throws FileNotFoundException {
        this.name = name;
        this.path = path;
        this.tileSize = tileSize;
        this.img = new Image(new FileInputStream(path));
        this.numTiles = new Vector2i((int)this.img.getWidth() / this.tileSize.x, (int)this.img.getHeight() / this.tileSize.y);
    }

    void drawTile(GraphicsContext gc, int x, int y, float destX, float destY) {
        // draw the specified tile at the given position
        gc.drawImage(
                img,
                x * tileSize.x,
                y * tileSize.y,
                tileSize.x,
                tileSize.y,
                destX,
                destY,
                tileSize.x,
                tileSize.y);
    }
}
