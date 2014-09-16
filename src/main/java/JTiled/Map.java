package JTiled;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class Map {
    // size is in tiles, not pixels
    Vector2i size;
    Vector2i tileSize;

    Map(Vector2i size, Vector2i tileSize) {
        this.size = size;
        this.tileSize = tileSize;
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
