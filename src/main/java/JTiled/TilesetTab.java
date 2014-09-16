package JTiled;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileNotFoundException;

public class TilesetTab {

    Editor editor;
    ScrollPane pane;
    Vector2i mousePos = new Vector2i(0,0);
    Vector2i dragStart = new Vector2i(0,0);
    Vector2i selectionSize = new Vector2i(1,1);

    double zoom = 2;
    double gridSpacing = 2;

    Vector2i snappedPos(MouseEvent mouseEvent) {
        if (editor.selectedTileset == null)
            return new Vector2i(0,0);

        Point2D p = pane.localToScreen(mouseEvent.getX(), mouseEvent.getY());

        double gx = editor.selectedTileset.tileSize.x;
        double gy = editor.selectedTileset.tileSize.y;
        return new Vector2i(
                (int)(mouseEvent.getX() / (zoom * (gridSpacing + gx))),
                (int)(mouseEvent.getY() / (zoom * (gridSpacing + gy))));
    }

    TilesetTab(Editor editor, ScrollPane pane) {
        this.editor = editor;
        this.pane = pane;
        Canvas canvas = new Canvas();
        pane.setContent(canvas);
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.scale(zoom, zoom);

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED,
                mouseEvent -> {
                    mousePos = snappedPos(mouseEvent);
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> {
                    mousePos = snappedPos(mouseEvent);
                    int ax = Math.min(mousePos.x, dragStart.x);
                    int ay = Math.min(mousePos.y, dragStart.y);
                    int bx = Math.max(mousePos.x, dragStart.x);
                    int by = Math.max(mousePos.y, dragStart.y);
                    selectionSize = new Vector2i(bx - ax + 1, bx - ax + 1);
                });

        // mouse down, start marking a brush
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                mouseEvent -> {
                    dragStart = snappedPos(mouseEvent);
                    selectionSize = new Vector2i(1,1);
                });

        // mouse up, end marking brush
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                mouseEvent -> {
                    int ax = Math.min(mousePos.x, dragStart.x);
                    int ay = Math.min(mousePos.y, dragStart.y);
                    editor.curBrush = new Brush(new Vector2i(ax, ay), selectionSize, editor.selectedTileset);
                });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                final GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                if (editor.selectedTileset != null) {
                    Image img = editor.selectedTileset.img;
                    pane.setPrefSize(img.getWidth() * zoom, img.getHeight() * zoom);

                    if (editor.showTilesetGrid) {
                        double gx = editor.selectedTileset.tileSize.x;
                        double gy = editor.selectedTileset.tileSize.y;
                        int gridX = (int)(img.getWidth() / gx) + 1;
                        int gridY = (int)(img.getHeight() / gy) + 1;

                        canvas.setWidth((img.getWidth() + gridX * gridSpacing) * zoom);
                        canvas.setHeight((img.getHeight() + gridY * gridSpacing) * zoom);

                        gc.setFill(Color.rgb(200, 200, 50, 0.5));

                        for (int i = 0; i < gridY; ++i) {
                            for (int j = 0; j < gridX; ++j) {
                                double x = j * gx;
                                double y = i * gy;
                                double dx = j * (gx + gridSpacing);
                                double dy = i * (gy + gridSpacing);
                                gc.drawImage(img, x, y, gx, gy, dx, dy, gx, gy);

                                // highlight the tile if it's inside the current brush
                                if (j >= dragStart.x && i >= dragStart.y && j < dragStart.x + selectionSize.x && i < dragStart.y + selectionSize.y) {
                                    gc.fillRect(dx, dy, gx, gy);
                                }
                            }
                        }

                    } else {
                        canvas.setWidth(img.getWidth() * zoom);
                        canvas.setHeight(img.getHeight() * zoom);
                        gc.drawImage(editor.selectedTileset.img, 0, 0);
                    }
                }
            }
        }.start();

        // Validate that only image files are being dropped on the tileset tab
        pane.setOnDragOver(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            if (!db.hasFiles()) {
                dragEvent.consume();
                return;
            }

            for (File file : db.getFiles()) {
                String filename = file.getName();
                if (!Utils.isValidImageFile(filename)) {
                    dragEvent.consume();
                    return;
                }
            }
            dragEvent.acceptTransferModes(TransferMode.COPY_OR_MOVE);
        });

        // Create a new tileset if an image is dropped on the tileset tab
        pane.setOnDragDropped(dragEvent -> {
            Dragboard db = dragEvent.getDragboard();
            if (!db.hasFiles())
                return;

            db.getFiles().stream().filter(file -> Utils.isValidImageFile(file.getName())).forEach(file -> {
                // TODO: Add new tileset dialog
                String path = file.getPath();
                String filename = file.getName();
                Tileset t = null;
                try {
                    t = new Tileset(filename, path, new Vector2i(16, 16));
                    editor.tilesets.add(t);
                    editor.selectedTileset = t;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        });

    }
}
