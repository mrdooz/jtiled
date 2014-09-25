package JTiled;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

@XStreamAlias("layer")
public class Layer {
    @XStreamAsAttribute
    String name;
    @XStreamAsAttribute
    boolean visible;

    @XStreamOmitField
    Map map;

    @XStreamImplicit(itemFieldName="tiles")
    Tile.Ref[][] tiles;

    Layer(String name, Map map) {
        this.name = name;
        this.map = map;
        tiles = new Tile.Ref[map.size.x][map.size.y];
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

    boolean sameTerrain(Vector2i v, int ofsX, int ofsY, int terrain) {
        int ox = v.x + ofsX;
        int oy = v.y + ofsY;
        if (ox < 0 || ox > map.size.x || oy < 0 || oy > map.size.y || tiles[ox][oy] == null)
            return false;

        return Tile.findByRef(tiles[ox][oy]).terrian == terrain;
    }


    @Override
    public String toString() {
        return name;
    }
}
