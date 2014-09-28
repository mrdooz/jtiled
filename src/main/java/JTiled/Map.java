package JTiled;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.*;

@XStreamAlias("map")
public class Map {

    @XStreamAsAttribute
    String name;

    // size is in tiles, not pixels
    Vector2i size;
    Vector2i tileSize;

    @XStreamOmitField
    Layer curLayer;

    @XStreamImplicit(itemFieldName="layers")
    List<Layer> layers = new ArrayList<>();

    // Save the previous brush & pos to avoid redundant map updates
    @XStreamOmitField
    Brush prevBrush;
    @XStreamOmitField
    Vector2i prevPos;

    public Map(String name, Vector2i size, Vector2i tileSize) {
        this.name = name;
        this.size = size;
        this.tileSize = tileSize;
    }

    @FunctionalInterface
    interface MapDoneCallback {
        void onResult(Map map);
    }

    static void buildFromDialog(URL url, MapDoneCallback cb) {
        try {
            Parent root = FXMLLoader.load(url);
            BoundPropertySet props = new BoundPropertySet(root, "name", "mapWidth", "mapHeight", "tileWidth", "tileHeight");

            Scene scene = new Scene(root, 400, 300);
            Stage stage = new Stage();

            Button btnOk = (Button)root.lookup("#ok");
            btnOk.setOnAction(event -> {
                stage.close();
                cb.onResult(new Map(props.getValue("name"),
                        new Vector2i(props.getIntValue("mapWidth", 0), props.getIntValue("mapHeight", 0)),
                        new Vector2i(props.getIntValue("tileWidth", 0), props.getIntValue("tileHeight", 0))));
            });

            Button btnCancel = (Button)root.lookup("#cancel");
            btnCancel.setOnAction(event -> {
                stage.close();
                cb.onResult(null);
            });

            stage.setTitle("New map:)");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void Draw(GraphicsContext gc)
    {
        for (Layer layer : layers) {
            layer.Draw(gc);
        }

        // draw the grid
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);
        for (int i = 0; i < size.y; ++i) {
            for (int j = 0; j < size.x; ++j) {
                gc.strokeRect(j * tileSize.x, i * tileSize.y, tileSize.x, tileSize.y);
            }
        }
    }

    private void ApplyTileBrush(Brush brush, Vector2i pos) {

        for (int i = 0; i < brush.size.y; ++i) {
            for (int j = 0; j < brush.size.x; ++j) {

                int x = pos.x + j;
                int y = pos.y + i;
                if (x < 0 || x >= size.x || y < 0 || y >= size.y)
                    continue;

                curLayer.tiles[x][y] = brush.tiles[j][i];
            }
        }
    }

    private void ApplyTerrainBrush(Brush brush, Vector2i pos) {

        // Set the brushed tile to the current terrain
        curLayer.setTile(pos, brush.tiles[0][0]);

        // Recalc border tiles
        curLayer.calcBorder();

        // In terrain mode, collect all the adjacent tiles that share the same terrain,
        // and then apply edge rules to determine which tile should be used
        List<Vector2i> flood = new ArrayList<>();
        boolean visited[][] = new boolean[curLayer.map.size.x][curLayer.map.size.y];

        Queue<Vector2i> frontier = new LinkedList<>();
        frontier.add(pos);
        visited[pos.x][pos.y] = true;

        int terrain = Editor.instance.curTerrain;

        Vector2i[] ofs = new Vector2i[] {
                new Vector2i(-1, +0), new Vector2i(+1, +0), new Vector2i(+0, -1), new Vector2i(+0, +1) };

        while (!frontier.isEmpty()) {

            // pop the front, and add the unvisited neighbours that share the same terrain
            Vector2i f = frontier.poll();
            flood.add(f);

            for (Vector2i x : ofs) {
                Vector2i p = f.add(x);
                Tile t = Tile.findByRef(curLayer.getTile(p));
                if (t != null && !visited[p.x][p.y] && t.terrian == terrain ) {
                    frontier.add(p);
                    visited[p.x][p.y] = true;
                }
            }
        }

        for (Vector2i b : flood) {
            // look at the four neighbouring tiles, and determine which walls are needed
            long flags
                    = flagCombine(b, terrain, WallFlag.W)
                    + flagCombine(b, terrain, WallFlag.NW)
                    + flagCombine(b, terrain, WallFlag.N)
                    + flagCombine(b, terrain, WallFlag.NE)
                    + flagCombine(b, terrain, WallFlag.E)
                    + flagCombine(b, terrain, WallFlag.SE)
                    + flagCombine(b, terrain, WallFlag.S)
                    + flagCombine(b, terrain, WallFlag.SW);

            TileRef r = brush.tileset.findTileByFlags(flags);
            if (r == null) {
                int a = 10;
            }

            curLayer.tiles[b.x][b.y] = r != null ? r : brush.tiles[0][0]; //  brush.tileset.tilesByFlag.get(flags);
        }
    }

    long flagCombine(Vector2i pos, int terrain, long shift) {
        Vector2i ofs = WallFlag.dir[(int)(shift / 3)];
        int xOfs = ofs.x;
        int yOfs = ofs.y;

        if (curLayer.sameTerrain(pos, xOfs, yOfs, terrain))
            return (curLayer.isBorder(pos, xOfs, yOfs) ? WallFlag.Border : WallFlag.Inner) << shift;

        return WallFlag.Outer << shift;
    }

    void ApplyBrush(Brush brush, Vector2i pos) {

        if (brush == prevBrush && pos == prevPos)
            return;

        prevBrush = brush;
        prevPos = pos;

        if (Editor.instance.paintMode == PaintMode.Terrain) {
            ApplyTerrainBrush(brush, pos);
        } else {
            ApplyTileBrush(brush, pos);
        }
    }
}
