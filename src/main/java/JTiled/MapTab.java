package JTiled;

import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;

public class MapTab {

    Editor editor;
    Vector2i mousePos = new Vector2i(0,0);
    double zoom = 2;

    Vector2i snappedPos(double x, double y) {
        if (editor.selectedMap == null)
            return new Vector2i(0,0);

        double gx = editor.selectedMap.tileSize.x;
        double gy = editor.selectedMap.tileSize.y;
        return new Vector2i(
                (int)(x / (zoom * gx)),
                (int)(y / (zoom * gy)));
    }


    MapTab(Editor editor, ScrollPane pane) {
        this.editor = editor;
        Map map = new Map(new Vector2i(100, 100), new Vector2i(16, 16));
        editor.maps.add(map);
        editor.selectedMap = map;
        // add a new default
        Layer layer = new Layer("default", map);
        map.curLayer = layer;
        map.layers.add(layer);

        Canvas canvas = new Canvas();
        pane.setContent(canvas);
        final GraphicsContext gc = canvas.getGraphicsContext2D();
        double zoom = 2;
        gc.scale(zoom, zoom);

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED,
                mouseEvent -> {
                    // TODO: handle scrolling offset
                    mousePos = snappedPos(mouseEvent.getX(), mouseEvent.getY());
                });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                mouseEvent -> {
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> {
                    // TODO: handle scrolling offset
                    mousePos = snappedPos(mouseEvent.getX(), mouseEvent.getY());
                    if (editor.curBrush != null) {
                        ApplyBrush(editor.curBrush, mousePos);
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                mouseEvent -> {
                    if (editor.curBrush != null) {
                        ApplyBrush(editor.curBrush, mousePos);
                    }
                });


        new AnimationTimer() {
            @Override
            public void handle(long now) {
                final GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                if (editor.selectedMap != null) {
                    int w = (int)(zoom * editor.selectedMap.size.x * editor.selectedMap.tileSize.x);
                    int h = (int)(zoom * editor.selectedMap.size.y * editor.selectedMap.tileSize.y);
                    canvas.setWidth(w);
                    canvas.setHeight(h);
                    pane.setPrefSize(w, h);

                    editor.selectedMap.Draw(gc);

                    if (editor.curBrush != null) {
                        double gx = map.tileSize.x;
                        double gy = map.tileSize.y;
                        editor.curBrush.Draw(gc, mousePos.x * gx, mousePos.y * gy);
                    }
                }
            }
        }.start();
    }

    void ApplyBrush(Brush brush, Vector2i pos) {
        for (int i = 0; i < brush.size.y; ++i) {
            for (int j = 0; j < brush.size.x; ++j) {

                int x = pos.x + j;
                int y = pos.y + i;

                if (x >= editor.selectedMap.size.x || y >= editor.selectedMap.size.y)
                    continue;

                editor.selectedMap.curLayer.tiles[x][y] = brush.tiles[j][i];
            }
        }
    }
}
