package JTiled;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Map {
    // width and height are in tiles, not pixels
    Vector2i size;
    Vector2i tileSize;

    Map(Vector2i size, Vector2i tileSize) {
        this.size = size;
        this.tileSize = tileSize;
    }

    void Draw(GraphicsContext gc)
    {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(0.5);

        for (Layer layer : layers) {
            layer.Draw(gc);
        }

        for (int i = 0; i < size.y; ++i) {
            for (int j = 0; j < size.x; ++j) {
                gc.strokeRect(j * tileSize.x, i * tileSize.y, tileSize.x, tileSize.y);
            }
        }
    }

    Layer curLayer;
    ObservableList<Layer> layers = FXCollections.observableArrayList();
}
