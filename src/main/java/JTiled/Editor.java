// JTiled. A simple clone of Tiled. magnus.osterlind@gmail.com

package JTiled;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class Editor extends Application {

    ObservableList<Tileset> tilesets = FXCollections.observableArrayList();
    List<Map> maps = new ArrayList<>();

    Map selectedMap;
    Tileset selectedTileset;
    Brush curBrush;
    boolean showTilesetGrid = true;

    List<TilesetTab> tilesetTabs = new ArrayList<>();
    List<MapTab> mapTabs = new ArrayList<>();

    Tab createLayersTab() {
        Tab tab = new Tab("Layers");
        // TODO: this is pretty horrible :)
        ListView<Layer> layersListView = new ListView<>(selectedMap.layers);
        tab.setContent(layersListView);
        return tab;
    }

    Tab createTilesetsTab()
    {
        Tab tab = new Tab("Tilesets");
        SplitPane splitPane = new SplitPane();
        splitPane.setOrientation(Orientation.VERTICAL);

        ScrollPane pane = new ScrollPane();
        pane.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        pane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        tilesetTabs.add(new TilesetTab(this, pane));

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

        mapTabs.add(new MapTab(this, pane));

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

            try {
                OutputFormat format = new OutputFormat(doc);
                format.setIndenting(true);
                String filename = "/Users/dooz/tmp/1.xml";
                XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(filename)), format);
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
