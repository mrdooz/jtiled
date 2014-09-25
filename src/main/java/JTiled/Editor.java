// JTiled. A simple clone of Tiled. magnus.osterlind@gmail.com

package JTiled;

import com.sun.org.apache.xml.internal.serialize.OutputFormat;
import com.sun.org.apache.xml.internal.serialize.XMLSerializer;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.beans.XMLEncoder;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.SynchronousQueue;

public class Editor extends Application {

    public static Editor instance;

//    ObservableList<Tileset> tilesets = FXCollections.observableArrayList();
    List<Map> maps = new ArrayList<>();

    Map selectedMap;
    Tileset selectedTileset;
    Brush curBrush;
    boolean showTilesetGrid = true;

    List<TilesetTab> tilesetTabs = new ArrayList<>();
    List<MapTab> mapTabs = new ArrayList<>();
    IdentityHashMap<Tab, Map> tabToMap = new IdentityHashMap<>();

    TabPane mapPane = new TabPane();

    HashMap<Integer, Tileset> tilesets = new HashMap<>();
    int nextTilesetId = 1;

    public Editor() {
        instance = this;
    }

    int addTileset(Tileset tileset) {
        int id = nextTilesetId++;
        tilesets.put(id, tileset);
        return id;
    }

    Tab createLayersTab() {
        Tab tab = new Tab("Layers");

        ListView<Layer> layersListView = new ListView<>();
        for (Layer x : selectedMap.layers)
            layersListView.getItems().add(x);

        HBox menuBox = new HBox(8);
        menuBox.getChildren().addAll(new Button("Cut"), new Button("Copy"), new Button("Paste"));
        VBox vbox = new VBox(layersListView, menuBox);
        VBox.setMargin(menuBox, new Insets(8, 8, 8, 8));
        VBox.setVgrow(layersListView, Priority.ALWAYS);

        tab.setContent(vbox);
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

    Tab createMapTab(Map map) {
        Tab tab = new Tab(map.name);
        ScrollPane pane = new ScrollPane();
        tab.setContent(pane);
        mapTabs.add(new MapTab(map, this, pane));
        tabToMap.put(tab, map);
        return tab;
    }

    void saveMap() {

        if (selectedMap == null)
            return;

        XStream xstream = new XStream(new DomDriver());
        xstream.processAnnotations(Map.class);
        String xml = xstream.toXML(selectedMap);
        System.out.println(xml);

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

            // add the map root
            map.setAttribute("orientation", "orthogonal");
            map.setAttribute("width", String.valueOf(w));
            map.setAttribute("height", String.valueOf(h));
            map.setAttribute("tilewidth", String.valueOf(tw));
            map.setAttribute("tileheight", String.valueOf(th));

            // add the tilesets
            int gid = 1;
            for (Tileset tileset : tilesets.values()) {
                org.w3c.dom.Element t = doc.createElement("tileset");
                t.setAttribute("firstgid", String.valueOf(gid));
                t.setAttribute("name", tileset.name);
                t.setAttribute("tilewidth", String.valueOf(tw));
                t.setAttribute("tileheight", String.valueOf(th));
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
                elem.setAttribute("width", String.valueOf(w));
                elem.setAttribute("height", String.valueOf(h));

                // add the actual tiles
                org.w3c.dom.Element data = doc.createElement("data");

                for (int y = 0; y < selectedMap.size.y; ++y) {
                    for (int x = 0; x < selectedMap.size.x; ++x) {
                        org.w3c.dom.Element t = doc.createElement("tile");
                        Tile tile = Tile.findByRef(layer.tiles[x][y]);
                        Tileset tileset = tilesets.get(tile.tilesetId);
                        int id = tile != null ? 1 + tile.pos.y * tileset.numTiles.x + tile.pos.x : 0;
                        t.setAttribute("gid", String.valueOf(id));
                        data.appendChild(t);
                    }
                }
                elem.appendChild(data);
                map.appendChild(elem);
            }

            doc.appendChild(map);

            try {
                OutputFormat format = new OutputFormat(doc);
                format.setIndenting(true);
                String filename = "/Users/dooz/projects/gdx/1.tmx";
                XMLSerializer serializer = new XMLSerializer(new FileOutputStream(new File(filename)), format);
                serializer.serialize(doc);
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    void applyBrush(Vector2i pos) {
        if (curBrush == null || selectedMap == null)
            return;

        selectedMap.ApplyBrush(curBrush, pos);
    }

    @Override
    public void start(Stage primaryStage) throws Exception{

        Group root = new Group();
        Scene scene = new Scene(root, 1024, 768);
        BorderPane border = new BorderPane();
        HBox hbox = new HBox();
        Button btnSave = new Button("save");
        btnSave.setOnAction(event -> saveMap());
        Button btnNewMap = new Button("new map");
        btnNewMap.setOnAction(event -> {
            Map.buildFromDialog(getClass().getResource("/NewMap.fxml"), m -> {
                if (m != null) {
                    mapPane.getTabs().add(createMapTab(m));
                }
            });
        });
        hbox.getChildren().addAll(btnSave, btnNewMap);
        border.setTop(hbox);

        SplitPane split = new SplitPane();
        split.prefWidthProperty().bind(scene.widthProperty());
        split.prefHeightProperty().bind(scene.heightProperty());

        split.setOrientation(Orientation.HORIZONTAL);
        mapPane.getTabs().add(createMapTab(new Map("map1", new Vector2i(100, 100), new Vector2i(32, 32))));
//        mapPane.getTabs().add(createMapTab(new Map("map1", new Vector2i(100, 100), new Vector2i(16, 16))));

        mapPane.getSelectionModel().selectedItemProperty().addListener(
                (observableValue, oldTab, newTab) -> {
                    // find the map associated with the new tab
                    selectedMap = tabToMap.get(newTab);
                }
        );

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
            Tileset t = new Tileset("test", "/Users/dooz/tmp/tmw_desert_spacing.png", new Vector2i(32, 32));
//            Tileset t = new Tileset("test", "/Users/dooz/tmp/dungeon_sheet_0.png", new Vector2i(16, 16));
//            tilesets.add(t);
            selectedTileset = t;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
