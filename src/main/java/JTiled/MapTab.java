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


    MapTab(Map map, Editor editor, ScrollPane pane) {
        this.editor = editor;
        editor.maps.add(map);

        // if this is the first map, mark is as selected
        if (editor.selectedMap == null)
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
                    mousePos = snappedPos(mouseEvent.getX(), mouseEvent.getY());
                });

        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                mouseEvent -> {
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> {
                    mousePos = snappedPos(mouseEvent.getX(), mouseEvent.getY());
                    editor.applyBrush(mousePos);
                });

        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                mouseEvent -> {
                    editor.applyBrush(mousePos);
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
}
