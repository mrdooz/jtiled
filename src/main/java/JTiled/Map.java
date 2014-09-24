package JTiled;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Map {

    String name;
    // size is in tiles, not pixels
    Vector2i size;
    Vector2i tileSize;

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
                        new Vector2i(Integer.parseInt(props.getValue("mapWidth")), Integer.parseInt(props.getValue("mapHeight"))),
                        new Vector2i(Integer.parseInt(props.getValue("tileWidth")), Integer.parseInt(props.getValue("tileHeight")))));
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

    void ApplyBrush(Brush brush, Vector2i pos) {

        // save any new tiles that we need to recalculate the type for
        List<Vector2i> boundary = new ArrayList<>();
        HashMap<Integer, Tile> brushTiles = new HashMap<>();

        for (int i = 0; i < brush.size.y; ++i) {
            for (int j = 0; j < brush.size.x; ++j) {

                int x = pos.x + j;
                int y = pos.y + i;

                Tile t = curLayer.tiles[x][y];
                Tile b = brush.tiles[j][i];
                brushTiles.put(b.wallFlags, b);

                if (x < 0 || x >= size.x || y < 0 || y >= size.y)
                    continue;


                if (t == null || t.terrian != b.terrian) {
                    curLayer.tiles[x][y] = b;
                } else {
                    // temporarily overwrite with the brush tile. this is done just so the new tile will have
                    // the correct terrain, because we use the terrain to determine which walls are needed
                    curLayer.tiles[x][y] = b;
                    boundary.add(new Vector2i(x, y));
                }
            }
        }

        int terrain = brush.tiles[0][0].terrian;
        for (Vector2i b : boundary) {
            // look at the four neighbouring tiles, and determine which walls are needed
            int flags = 0;
            if (!curLayer.sameTerrain(b, -1, +0, terrain))
                flags |= WallFlag.Left;
            if (!curLayer.sameTerrain(b, +0, -1, terrain))
                flags |= WallFlag.Top;
            if (!curLayer.sameTerrain(b, +1, +0, terrain))
                flags |= WallFlag.Right;
            if (!curLayer.sameTerrain(b, +0, +1, terrain))
                flags |= WallFlag.Bottom;

            curLayer.tiles[b.x][b.y] = brushTiles.get(flags);
        }
    }

    Layer curLayer;
    ObservableList<Layer> layers = FXCollections.observableArrayList();
}
