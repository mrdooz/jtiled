package JTiled;

import javafx.animation.AnimationTimer;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

public class TilesetTab {

    Editor editor;
    ScrollPane pane;
    Vector2i mousePos = new Vector2i(0,0);
    Vector2i dragStart = new Vector2i(0,0);
    Vector2i topLeft = new Vector2i(0, 0);
    Vector2i dragEnd = new Vector2i(0,0);
    Vector2i selectionSize = new Vector2i(1,1);

    double zoom = 2;

    Vector2i snappedPos(MouseEvent mouseEvent) {
        if (editor.selectedTileset == null)
            return new Vector2i(0,0);

        Tileset t = editor.selectedTileset;

        int paddingX = Math.max(t.padding.x, 1);
        int paddingY = Math.max(t.padding.y, 1);

        double gx = editor.selectedTileset.tileSize.x;
        double gy = editor.selectedTileset.tileSize.y;

        double x = mouseEvent.getX() - zoom * t.offset.x;
        double y = mouseEvent.getY() - zoom * t.offset.y;
        return new Vector2i(
                (int)(x / (zoom * (paddingX + gx))),
                (int)(y / (zoom * (paddingY + gy))));
    }

    void calcSelectedSize(Vector2i a, Vector2i b) {
        selectionSize = new Vector2i(b.x - a.x + 1, b.y - a.y + 1);
    }

    TilesetTab(Editor editor, ScrollPane pane) {
        this.editor = editor;
        this.pane = pane;
        Canvas canvas = new Canvas();
        pane.setContent(canvas);
        final GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.scale(zoom, zoom);

        canvas.addEventHandler(MouseEvent.MOUSE_CLICKED,
                mouseEvent -> {
                    if (mouseEvent.getClickCount() == 2) {
                        // double click, then edit the properties
                        if (editor.selectedTileset != null)
                            updateFromDialog(editor.selectedTileset);
                    }
                });

        canvas.addEventHandler(MouseEvent.MOUSE_MOVED,
                mouseEvent -> {
                    mousePos = snappedPos(mouseEvent);
                });

        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED,
                mouseEvent -> {
                    mousePos = snappedPos(mouseEvent);
                    int ax = Math.min(mousePos.x, dragStart.x);
                    int ay = Math.min(mousePos.y, dragStart.y);
                    int bx = Math.max(mousePos.x, dragEnd.x);
                    int by = Math.max(mousePos.y, dragEnd.y);
                    topLeft = new Vector2i(ax, ay);
                    calcSelectedSize(topLeft, new Vector2i(bx, by));
                });

        // mouse down, start marking a brush
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED,
                mouseEvent -> {
                    dragStart = snappedPos(mouseEvent);
                    topLeft = dragEnd = dragStart;
                    calcSelectedSize(dragStart, dragEnd);
                });

        // mouse up, end marking brush
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                mouseEvent -> {
                    editor.curBrush = new Brush(topLeft, selectionSize, editor.selectedTileset);
                });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                final GraphicsContext gc = canvas.getGraphicsContext2D();
                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                if (editor.selectedTileset != null) {
                    Tileset t = editor.selectedTileset;
                    Image img = t.img;
                    pane.setPrefSize(img.getWidth() * zoom, img.getHeight() * zoom);

                    if (editor.showTilesetGrid) {
                        double sizeX = t.tileSize.x;
                        double sizeY = t.tileSize.y;
                        int numTilesX = (int)(img.getWidth() / sizeX) + 1;
                        int numTilesY = (int)(img.getHeight() / sizeY) + 1;

                        canvas.setWidth((img.getWidth() + t.offset.x + numTilesX * t.padding.x) * zoom);
                        canvas.setHeight((img.getHeight() + t.offset.y + numTilesY * t.padding.y) * zoom);

                        gc.setFill(Color.rgb(200, 200, 50, 0.5));

                        // If the tileset itself doesn't contain any padding, use a default value
                        int paddingX = Math.max(t.padding.x, 1);
                        int paddingY = Math.max(t.padding.y, 1);

                        for (int i = 0; i < numTilesY; ++i) {
                            for (int j = 0; j < numTilesX; ++j) {
                                double x = t.offset.x + j * (t.padding.x + sizeX);
                                double y = t.offset.y + i * (t.padding.y + sizeY);
                                double dx = t.offset.x + j * (sizeX + paddingX);
                                double dy = t.offset.y + i * (sizeY + paddingY);
                                gc.drawImage(img, x, y, sizeX, sizeY, dx, dy, sizeX, sizeY);

                                // highlight the tile if it's inside the current brush
                                if (j >= topLeft.x && i >= topLeft.y && j < topLeft.x + selectionSize.x && i < topLeft.y + selectionSize.y) {
                                    gc.fillRect(dx, dy, sizeX, sizeY);
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
                String path = file.getPath();
                String filename = file.getName();
                Tileset t = null;
                try {
                    t = new Tileset(filename, path, new Vector2i(16, 16));
                    updateFromDialog(t);
                    editor.tilesets.add(t);
                    editor.selectedTileset = t;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            });
        });

    }

    void updateFromDialog(Tileset t) {
        try {
            URL url = getClass().getResource("/NewTileset.fxml");
            Parent root = FXMLLoader.load(url);

            BoundPropertySet props = new BoundPropertySet(root, "name",
                    "tileWidth", "tileHeight", "offsetX", "offsetY", "paddingX", "paddingY");

            props.setStringValue("name", t.name);
            props.setIntValue("tileWidth", t.tileSize.x);
            props.setIntValue("tileHeight", t.tileSize.y);
            props.setIntValue("offsetX", t.offset.x);
            props.setIntValue("offsetY", t.offset.y);
            props.setIntValue("paddingX", t.padding.x);
            props.setIntValue("paddingY", t.padding.y);

            Scene scene = new Scene(root, 400, 300);
            Stage stage = new Stage();

            Button btnOk = (Button)root.lookup("#ok");
            btnOk.setOnAction(event -> {
                stage.close();
                t.tileSize.x = props.getIntValue("tileWidth", 16);
                t.tileSize.y = props.getIntValue("tileHeight", 16);
                t.offset.x = props.getIntValue("offsetX", 0);
                t.offset.y = props.getIntValue("offsetY", 0);
                t.padding.x = props.getIntValue("paddingX", 0);
                t.padding.y = props.getIntValue("paddingY", 0);
            });

            Button btnCancel = (Button)root.lookup("#cancel");
            btnCancel.setOnAction(event -> {
                stage.close();
            });

            stage.setTitle("Edit tileset");
            stage.setScene(scene);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
