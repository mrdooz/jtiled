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

    interface MapDoneCallback {
        void onResult(Map map);
    }

    static void buildFromDialog(URL url, MapDoneCallback cb) {
        try {
            Parent root = FXMLLoader.load(url);
            TextField nameField = (TextField)root.lookup("#name");
            TextField mapWidthField = (TextField)root.lookup("#mapWidth");
            TextField mapHeightField = (TextField)root.lookup("#mapHeight");
            TextField tileWidthField = (TextField)root.lookup("#tileWidth");
            TextField tileHeightField = (TextField)root.lookup("#tileHeight");

            StringProperty name = new SimpleStringProperty();
            name.bind(nameField.textProperty());

            StringProperty mapWidth = new SimpleStringProperty();
            mapWidth.bind(mapWidthField.textProperty());

            StringProperty mapHeight = new SimpleStringProperty();
            mapHeight.bind(mapHeightField.textProperty());

            StringProperty tileWidth = new SimpleStringProperty();
            tileWidth.bind(tileWidthField.textProperty());

            StringProperty tileHeight = new SimpleStringProperty();
            tileHeight.bind(tileHeightField.textProperty());

            Scene scene = new Scene(root, 400, 300);
            Stage stage = new Stage();

            Button btnOk = (Button)root.lookup("#ok");
            btnOk.setOnAction(event -> {
                stage.close();
                cb.onResult(new Map(name.getValue(),
                        new Vector2i(Integer.parseInt(mapWidth.getValue()), Integer.parseInt(mapHeight.getValue())),
                        new Vector2i(Integer.parseInt(tileWidth.getValue()), Integer.parseInt(tileHeight.getValue()))));
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

    Layer curLayer;
    ObservableList<Layer> layers = FXCollections.observableArrayList();
}
