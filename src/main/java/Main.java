// JTiled. A simple clone of Tiled. magnus.osterlind@gmail.com

package JTiled;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import sun.plugin.dom.core.Document;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main extends Application {

    class Vector2i {
        int x, y;

        Vector2i(int x, int y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString() {
            return String.format("%d, %d", x, y);
        }
    }

    class Tileset {
        String name;
        String path;
        Image img;
        Vector2i tileSize;
        Vector2i numTiles;
        int gidStart;   // used for serialization

        Tileset(String name, String path, Vector2i tileSize) throws FileNotFoundException {
            this.name = name;
            this.path = path;
            this.tileSize = tileSize;
            this.img = new Image(new FileInputStream(path));
            this.numTiles = new Vector2i((int)this.img.getWidth() / this.tileSize.x, (int)this.img.getHeight() / this.tileSize.y);
        }

        void drawTile(GraphicsContext gc, int x, int y, float destX, float destY) {
            // draw the specified tile at the given position
            gc.drawImage(
                    img,
                    x * tileSize.x,
                    y * tileSize.y,
                    tileSize.x,
                    tileSize.y,
                    destX,
                    destY,
                    tileSize.x,
                    tileSize.y);
        }
    }

    class Layer {
        String name;
        Map map;
        boolean visible;

        Layer(String name, Map map) {
            this.name = name;
            this.map = map;
            tiles = new Tile[map.size.x][map.size.y];
            this.visible = true;
        }

        void Draw(GraphicsContext gc) {
            gc.setStroke(Color.GRAY);
            gc.setLineWidth(0.5);

            Vector2i size = map.size;
            Vector2i tileSize = map.tileSize;

            for (int i = 0; i < size.y; ++i) {
                for (int j = 0; j < size.x; ++j) {
                    Tile tile = tiles[i][j];
                    if (tile != null) {
                        tile.Draw(gc, j * tileSize.x, i * tileSize.y);
                    }
                }
            }
        }

        Tile[][] tiles;

        @Override
        public String toString() {
            return name;
        }
    }

    enum TileFlag {
        None,

        Middle,

        OuterTopLeft,
        OuterTopMiddle,
        OuterTopRight,
        OuterRight,
        OuterBottomRight,
        OuterBottomMiddle,
        OuterBottomLeft,
        OuterLeft,

        InnerTopLeft,
        InnerTopMiddle,
        InnerTopRight,
        InnerRight,
        InnerBottomRight,
        InnerBottomMiddle,
        InnerBottomLeft,
        InnerLeft,
    }

    class Tile {
        Vector2i pos;
        Vector2i size;
        Tileset tileset;

        TileFlag flag = TileFlag.None;

        Tile(Vector2i pos, Tileset tileset) {
            this.pos = pos;
            this.tileset = tileset;
            this.size = tileset.tileSize;
        }

        void Draw(GraphicsContext gc, double destX, double destY) {
            if (tileset == null)
                return;

            gc.drawImage(tileset.img,
                    pos.x * size.x, pos.y * size.y, size.x, size.y,
                    destX, destY, size.x, size.y);
        }
    }

    class Map {
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

    class Brush
    {
        Vector2i size;
        Tileset tileset;
        Tile tiles[][];
        Vector2i tileSize;

        Brush(Vector2i pos, Vector2i size, Tileset tileset) {
            // create a brush from a quad in the given tileset

            this.size = size;
            this.tileset = tileset;
            this.tileSize = tileset.tileSize;
            tiles = new Tile[size.x][size.y];

            for (int i = 0; i < size.y; ++i) {
                for (int j = 0; j < size.x; ++j) {
                    tiles[j][i] = new Tile(new Vector2i(pos.x + j, pos.y + i), tileset);
                }
            }
        }

        void Draw(GraphicsContext gc, double destX, double destY) {
            if (tileset == null)
                return;

            double ofsY = 0;
            for (int i = 0; i < size.y; ++i) {
                double ofsX = 0;
                for (int j = 0; j < size.x; ++j) {
                    Tile tile = tiles[j][i];
                    tile.Draw(gc, destX + ofsX, destY + ofsY);
                    ofsX += tileSize.x;
                }
                ofsY += tileSize.y;
            }
        }
    }

    ObservableList<Tileset> tilesets = FXCollections.observableArrayList();
    List<Map> maps = new ArrayList<>();

    Map selectedMap;
    Tileset selectedTileset;
    Brush curBrush;
    boolean showTilesetGrid = true;

    List<TilesetTab> tilesetTabs = new ArrayList<>();
    List<MapTab> mapTabs = new ArrayList<>();

    private boolean isValidImageFile(String url) {
        List<String> imgTypes = Arrays.asList(".jpg", ".jpeg", ".png", ".gif", ".bmp");
        return imgTypes.stream().anyMatch(t -> url.endsWith(t));
    }

    Tab createLayersTab() {
        Tab tab = new Tab("Layers");
        // TODO: this is pretty horrible :)
        ListView<Layer> layersListView = new ListView<>(selectedMap.layers);
        tab.setContent(layersListView);
        return tab;
    }

    class MapTab {

        Vector2i mousePos = new Vector2i(0,0);

        double zoom = 2;

        Vector2i snappedPos(double x, double y) {
            if (selectedMap == null)
                return new Vector2i(0,0);

            double gx = selectedMap.tileSize.x;
            double gy = selectedMap.tileSize.y;
            return new Vector2i(
                    (int)(x / (zoom * gx)),
                    (int)(y / (zoom * gy)));
        }


        MapTab(ScrollPane pane) {
            Map map = new Map(new Vector2i(100, 100), new Vector2i(16, 16));
            maps.add(map);
            selectedMap = map;
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
                        if (curBrush != null) {
                            ApplyBrush(curBrush, mousePos);
                        }
                    });

            canvas.addEventHandler(MouseEvent.MOUSE_RELEASED,
                    mouseEvent -> {
                        if (curBrush != null) {
                            ApplyBrush(curBrush, mousePos);
                        }
                    });


            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    final GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                    if (selectedMap != null) {
                        int w = (int)(zoom * selectedMap.size.x * selectedMap.tileSize.x);
                        int h = (int)(zoom * selectedMap.size.y * selectedMap.tileSize.y);
                        canvas.setWidth(w);
                        canvas.setHeight(h);
                        pane.setPrefSize(w, h);

                        selectedMap.Draw(gc);

                        if (curBrush != null) {
                            double gx = map.tileSize.x;
                            double gy = map.tileSize.y;
                            curBrush.Draw(gc, mousePos.x * gx, mousePos.y * gy);
                        }
                    }
                }
            }.start();
        }

        void ApplyBrush(Brush brush, Vector2i pos) {
            Vector2i s = new Vector2i(1, 1);
            for (int i = 0; i < brush.size.y; ++i) {
                for (int j = 0; j < brush.size.x; ++j) {

                    int x = pos.x + j;
                    int y = pos.y + i;

                    if (x >= selectedMap.size.x || y >= selectedMap.size.y)
                        continue;

                    selectedMap.curLayer.tiles[x][y] = brush.tiles[j][i];
                }
            }
        }
    }

    class TilesetTab {

        ScrollPane pane;
        Vector2i mousePos = new Vector2i(0,0);
        Vector2i dragStart = new Vector2i(0,0);
        Vector2i selectionSize = new Vector2i(1,1);

        double zoom = 2;
        double gridSpacing = 2;

        Vector2i snappedPos(MouseEvent mouseEvent) {
            if (selectedTileset == null)
                return new Vector2i(0,0);

            Point2D p = pane.localToScene(mouseEvent.getX(), mouseEvent.getY());

            double gx = selectedTileset.tileSize.x;
            double gy = selectedTileset.tileSize.y;
            return new Vector2i(
                    (int)(p.getX() / (zoom * (gridSpacing + gx))),
                    (int)(p.getY() / (zoom * (gridSpacing + gy))));
        }

        TilesetTab(ScrollPane pane) {
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
                        selectionSize = new Vector2i(mousePos.x - dragStart.x + 1, mousePos.y - dragStart.y + 1);
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
                        curBrush = new Brush(dragStart, selectionSize, selectedTileset);
                    });

            new AnimationTimer() {
                @Override
                public void handle(long now) {
                    final GraphicsContext gc = canvas.getGraphicsContext2D();
                    gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

                    if (selectedTileset != null) {
                        Image img = selectedTileset.img;
                        pane.setPrefSize(img.getWidth() * zoom, img.getHeight() * zoom);

                        if (showTilesetGrid) {
                            double gx = selectedTileset.tileSize.x;
                            double gy = selectedTileset.tileSize.y;
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
                            gc.drawImage(selectedTileset.img, 0, 0);
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
                    if (!isValidImageFile(filename)) {
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

                db.getFiles().stream().filter(file -> isValidImageFile(file.getName())).forEach(file -> {
                    // TODO: Add new tileset dialog
                    String path = file.getPath();
                    String filename = file.getName();
                    Tileset t = null;
                    try {
                        t = new Tileset(filename, path, new Vector2i(16, 16));
                        tilesets.add(t);
                        selectedTileset = t;
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                });
            });

        }
    }

    Tab createTilesetsTab()
    {
        Tab tab = new Tab("Tilesets");
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);

        ScrollPane pane = new ScrollPane();
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tilesetTabs.add(new TilesetTab(pane));

        splitPane.getItems().addAll(pane, new Button("tjong"));
        tab.setContent(splitPane);

        return tab;
    }

    Tab createMapTab() {
        Tab tab = new Tab("map1");

        ScrollPane pane = new ScrollPane();
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        tab.setContent(pane);

        mapTabs.add(new MapTab(pane));

        return tab;
    }

    void saveMap() {

        if (selectedMap == null)
            return;

        DocumentBuilderFactory icFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder icBuilder;
        try {
            icBuilder = icFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = icBuilder.newDocument();
            org.w3c.dom.Element map = doc.createElement("map");

            int w = selectedMap.size.x;
            int h = selectedMap.size.y;
            int tw = selectedMap.tileSize.x;
            int th = selectedMap.tileSize.y;

            int cx = w / tw;
            int cy = h / th;

            // add the map root
            map.setAttribute("width", String.valueOf(w));
            map.setAttribute("height", String.valueOf(h));
            map.setAttribute("tilewidth", String.valueOf(tw));
            map.setAttribute("tileheight", String.valueOf(th));

            // add the tilesets
            int gid = 0;
            for (Tileset tileset : tilesets) {
                org.w3c.dom.Element t = doc.createElement("tileset");
                tileset.gidStart = gid;
                t.setAttribute("gid", String.valueOf(gid));
                t.setAttribute("name", tileset.name);
                t.setAttribute("tilewidth", String.valueOf(tileset.numTiles.x));
                t.setAttribute("tileheight", String.valueOf(tileset.numTiles.y));
                t.setAttribute("spacing", String.valueOf(0));
                t.setAttribute("margin", String.valueOf(0));

                // add the image
                org.w3c.dom.Element img = doc.createElement("image");
                img.setAttribute("source", tileset.path);
                img.setAttribute("width", String.valueOf((int)tileset.img.getWidth()));
                img.setAttribute("height", String.valueOf((int)tileset.img.getHeight()));
                t.appendChild(img);

                map.appendChild(t);
                gid += tileset.numTiles.x * tileset.numTiles.y;
            }

            // add the layers
            for (Layer layer : selectedMap.layers) {
                org.w3c.dom.Element elem = doc.createElement("layer");
                elem.setAttribute("name", layer.name);
                elem.setAttribute("visible", String.valueOf(layer.visible));

                // add the actual tiles
                org.w3c.dom.Element data = doc.createElement("data");

                for (int y = 0; y < selectedMap.size.y; ++y) {
                    for (int x = 0; x < selectedMap.size.x; ++x) {
                        org.w3c.dom.Element t = doc.createElement("tile");
                        Tile tile = layer.tiles[x][y];
                        if (tile != null) {
                            int id = tile.pos.y * cx + tile.pos.x;
                            t.setAttribute("id", String.valueOf(id));
                            data.appendChild(t);
                        }
                    }
                }
                elem.appendChild(data);
                map.appendChild(elem);
            }

            doc.appendChild(map);

            OutputFormat format = new OutputFormat(doc);
            format.setIndenting(true);
            String filename = "/Users/dooz/tmp/1.xml";
            XMLSerializer serializer = null;
            try {
                serializer = new XMLSerializer(new FileOutputStream(new File(filename)), format);
                serializer.serialize(doc);
                System.out.println(doc);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        Group root = new Group();
        Scene scene = new Scene(root, 1024, 768);
        BorderPane border = new BorderPane();
        HBox hbox = new HBox();
        hbox.getChildren().add(new Button("test"));
        border.setTop(hbox);

        SplitPane split = new SplitPane();
        split.prefWidthProperty().bind(scene.widthProperty());
        split.prefHeightProperty().bind(scene.heightProperty());

        split.setOrientation(Orientation.HORIZONTAL);
        TabPane mapPane = new TabPane();
        mapPane.getTabs().addAll(createMapTab());

        StackPane right = new StackPane();
        SplitPane rightSplit = new SplitPane();
        rightSplit.setOrientation(Orientation.VERTICAL);
        TabPane layerPane = new TabPane();
        Tab minimapTab = new Tab("Mini-map");
        Tab objectsTab = new Tab("Objects");
        layerPane.getTabs().addAll(minimapTab, objectsTab, createLayersTab());

        TabPane tilesetPane = new TabPane();
        Tab terrainsTab = new Tab("Terrains");
        tilesetPane.getTabs().addAll(terrainsTab, createTilesetsTab());

        rightSplit.getItems().addAll(layerPane, tilesetPane);
        right.getChildren().add(rightSplit);
        split.getItems().addAll(mapPane, right);
        border.setCenter(split);

        mapPane.setPrefWidth(200);
        mapPane.setPrefHeight(400);
        right.setPrefWidth(200);

        root.getChildren().add(border);

        primaryStage.setTitle("JTiled");
        primaryStage.setScene(scene);
        primaryStage.show();

        if (true) {
            Tileset t = new Tileset("test", "/Users/dooz/tmp/dungeon_sheet_0.png", new Vector2i(16, 16));
            tilesets.add(t);
            selectedTileset = t;
            saveMap();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
