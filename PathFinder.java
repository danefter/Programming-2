/**
 * @author Dan Jensen
 *
 * **/

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

import static javafx.scene.control.Alert.AlertType.*;

public class PathFinder extends Application {

    private final GridPane options = new GridPane();

    private final BorderPane bp = new BorderPane();

    private final ArrayList<Place> selectList = new ArrayList<>();

    private final File map = new File("europa.graph");

    private String imageName = "file:europa.gif";

    private final Image image = new Image(imageName);

    private final ImageView imageView = new ImageView();

    private final TreeMap<String, Place> nodeStrings = new TreeMap<>();

    private final Pane p = new Pane();

    private final Set<Line> lineSet = new HashSet<>();

    private final Set<Edge<Place>> edgeSet = new HashSet<>();

    private Graph<Place> graph = new ListGraph<>();

    private Stage mainStage;

    private boolean changed;

    private final MyVBox root = new MyVBox(p);


    //sets Map to image
    public void open(Scanner input) {
        imageName = input.nextLine();
        imageView.setImage(new Image(imageName));
        String nodes = input.nextLine();
        String[] nodeArray = nodes.split("[;]");
        //places nodes as circles on map
        for (int i = 0; i < nodeArray.length; i = i + 3) {
            String stringNode = nodeArray[i];
            double x = Double.parseDouble(nodeArray[i + 1]);
            double y = Double.parseDouble(nodeArray[i + 2]);
            Place node = new Place(stringNode, x, y);
            writePlace(node);
        }
        //places and draws edges
        while (input.hasNext()) {
            String edges = input.nextLine();
            String[] edgeArray = edges.split("[;]");
            String src = edgeArray[0];
            String dest = edgeArray[1];
            String name = edgeArray[2];
            int weight = Integer.parseInt(edgeArray[3]);
            graph.connect(nodeStrings.get(src), nodeStrings.get(dest), name,  weight );
        }
        nodeStrings.values().forEach(n -> edgeSet.addAll(graph.getEdgesFrom(n)));
        edgeSet.forEach(e -> addEdgeLines(e.getName(), e.getSrc(), e.getDestination(), e.getWeight()));
        nodeStrings.values().forEach(Node::toFront);
    }

    //method to add new places, draws circle on map and adds to Pane
    private void writePlace(Place n) {
        graph.add(n);
        n.setId(n.getName());
        nodeStrings.put(n.getName(), n);
        n.setOnMouseClicked(new PlaceClickHandler());
        p.getChildren().addAll(n, n.getText());
        n.toFront();
    }

    //takes in nodes from text file europa.graph, adding edges
    private void writeNodes(PrintWriter p) {
        p.println(imageName);
        Set<Place> nodes = new HashSet<>(graph.getNodes());
        Set<Edge<Place>> edges = new HashSet<>();
        SortedSet<String> edgeStrings = new TreeSet<>(Comparator.naturalOrder());
        for (Place n : nodes) {
            p.print(n.getName() + ";" + n.getX() + ";" + n.getY() + ";");
            edges.addAll(graph.getEdgesFrom(n));
        }
        p.println();
        for (Edge<Place> e : edges) {
            edgeStrings.add(e.getSrc().getName() + ";" + e.getDestination().getName()
                    + ";" + e.getName() + ";" + e.getWeight());
        }
        edgeStrings.forEach(p::println);
    }

    //adds edge to edge map
    public void addEdgeLines(String name,Place src, Place dest, int weight) {
        graph.connect(src, dest, name, weight);
        Line line = new Line();
        line.setStartX(src.getX());
        line.setStartY(src.getY());
        line.setEndX(dest.getX());
        line.setEndY(dest.getY());
        line.setId(name);
        line.setUserData(weight);
        lineSet.add(line);
        p.getChildren().add(line);
        src.toFront();
        dest.toFront();
    }

    //removes all nodes and edges and whatever has been selected
    public void reset() {
        graph = new ListGraph<>();
        p.getChildren().removeAll(lineSet);
        p.getChildren().removeAll(nodeStrings.values());
        lineSet.clear();
        nodeStrings.clear();
        selectList.clear();
        changed = false;
    }

    //Prodduces window for messages
    public void alert(String s, ActionEvent event) {
        Alert alert = new Alert(ERROR);
        alert.setHeaderText(s);
        alert.showAndWait();
        event.consume();
    }

    //long JavaFX setup for Stage
    @Override
    public void start(Stage mainStage) {
        p.setId("outputArea");
        this.mainStage = mainStage;

        imageView.setId("imageView");
        p.getChildren().add(imageView);

        mainStage.setTitle("PathFinder");
        mainStage.setWidth(628);
        mainStage.setHeight(128);

        Button findPath = new Button("Find Path");
        findPath.setId("btnFindPath");
        findPath.setOnAction(new FindPathHandler());
        Button showCon = new Button("Show Connection");
        showCon.setId("btnShowConnection");
        showCon.setOnAction(new ShowConHandler());
        Button newPlace = new Button("New Place");
        newPlace.setId("btnNewPlace");
        newPlace.setOnAction(new NewPlaceHandler());
        Button newCon = new Button("New Connection");
        newCon.setId("btnNewConnection");
        newCon.setOnAction(new NewConHandler());
        Button changeCon = new Button("Change Connection");
        changeCon.setId("btnChangeConnection");
        changeCon.setOnAction(new ChangeConHandler());

        options.add(findPath, 1, 1);
        options.add(showCon, 2, 1);
        options.add(newPlace, 3, 1);
        options.add(newCon, 4, 1);
        options.add(changeCon, 5, 1);
        options.setPadding(new Insets(10));
        options.setHgap(20);
        options.setVgap(1);

        bp.setTop(options);

        Menu file = new Menu("File");
        file.setId("menuFile");
        MenuItem newMap = new MenuItem("New Map");
        newMap.setId("menuNewMap");
        newMap.setOnAction(new NewMapHandler());
        MenuItem openFile = new MenuItem("Open");
        openFile.setId("menuOpenFile");
        openFile.setOnAction(new OpenHandler());
        MenuItem saveFile = new MenuItem("Save");
        saveFile.setId("menuSaveFile");
        saveFile.setOnAction(new SaveMapHandler());
        MenuItem saveImage = new MenuItem("Save Image");
        saveImage.setId("menuSaveImage");
        saveImage.setOnAction(new SaveImageHandler());
        MenuItem exit = new MenuItem("Exit");
        exit.setId("menuExit");
        exit.setOnAction(new ExitItemHandler());

        file.getItems().add(newMap);
        file.getItems().add(openFile);
        file.getItems().add(saveFile);
        file.getItems().add(saveImage);
        file.getItems().add(exit);

        MenuBar mb = new MenuBar();
        mb.setId("menu");
        mb.getMenus().add(file);

        root.getChildren().addAll(mb, bp);
        root.setAlignment(Pos.TOP_CENTER);
        Scene sc = new Scene(root);
        mainStage.setScene(sc);
        mainStage.setOnCloseRequest(new ExitHandler());
        mainStage.show();
    }

    class MyVBox extends VBox {
        MyVBox(Node... items) {
            super(items);
            setBorder(bp.getBorder());
        }
    }

    //saves snapshot of map
    class SaveImageHandler implements EventHandler<ActionEvent> {
        @Override public void handle(ActionEvent event) {
            try {
                WritableImage image = p.snapshot(null, null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
                ImageIO.write(bufferedImage, "png", new File("capture.png"));
            } catch (IOException e) {
                String s = "Something went wrong...";
                alert(s, event);
            }
        }
    }

    //Reopens map and prompts save if its changed, otherwise reset
    class OpenHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            try {
                Scanner input = new Scanner(map);
                if (!changed) {
                    reset();
                    open(input);
                }
                if (changed) {
                    Alert alert = new Alert(CONFIRMATION);
                    alert.setTitle("Warning!");
                    alert.setHeaderText("Unsaved changes, continue anyways?");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.CANCEL)
                        event.consume();
                    if (result.isPresent() && result.get() == ButtonType.OK)
                        reset();
                        open(input);
                }
            } catch (Exception e) {
                String s = "Something went wrong...";
                alert(s, event);
            }
        }
    }

    //creates new map
    class NewMapHandler implements EventHandler<ActionEvent> {
        @Override public void handle(ActionEvent event) {
            if (!changed) {
                reset();
                imageView.setImage(image);
                imageView.preserveRatioProperty();
                bp.setCenter(p);
                mainStage.sizeToScene();
            }
            if (changed) {
                Alert alert = new Alert(CONFIRMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText("Unsaved changes, continue anyways?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL) event.consume();
                if (result.isPresent() && result.get() == ButtonType.OK) {
                    reset();
                    imageView.setImage(image);
                    imageView.preserveRatioProperty();
                    bp.setCenter(p);
                    mainStage.sizeToScene();
                    changed = true;
                }
            }
        }
    }

    //writes nodes and edges to text file
    class SaveMapHandler implements EventHandler<ActionEvent> {
        @Override public void handle(ActionEvent event) {
            try (PrintWriter p = new PrintWriter(map)) {
                writeNodes(p);
            } catch (IOException e) {
                String s = "Something went wrong...";
                alert(s, event);
            }
            changed = false;
        }
    }


    //window produced when edge is clicked on, showing connection
    class ShowConHandler implements EventHandler<ActionEvent>{
        @Override public void handle(ActionEvent event) {
            if (selectList.size() < 2) {
                String s = "Two places must be selected!";
                alert(s, event);
            }
            if (selectList.size() == 2) {
                Place node1 = nodeStrings.get(selectList.get(0).getId());
                Place node2 = nodeStrings.get(selectList.get(1).getId());
                if (graph.getEdgeBetween(node1, node2) != null) {
                    String n = graph.getEdgeBetween(node1, node2).getName();
                    int t = graph.getEdgeBetween(node1, node2).getWeight();
                    TextInputDialog td = new TextInputDialog();
                    td.setTitle("Connection");
                    td.setHeaderText("Connection from " + selectList.get(0).getId() + " to " + selectList.get(1).getId());
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20, 150, 10, 10));
                    TextField tf1 = new TextField(n);
                    TextField tf2 = new TextField(String.valueOf(t));
                    grid.add(new Label("Name: "), 0, 0);
                    grid.add(tf1, 1, 0);
                    grid.add(new Label("Time:"), 0, 1);
                    grid.add(tf2, 1, 1);
                    td.getDialogPane().setContent(grid);
                    tf1.setDisable(true);
                    tf2.setDisable(true);
                    td.getDialogPane().getButtonTypes().removeAll(ButtonType.CANCEL);
                    td.showAndWait();
                }
                if (graph.getEdgeBetween(node1, node2) == null) {
                    String s = "No direct connection!";
                    alert(s, event);
                }
            }
        }
    }

    //allows change of selected connection (for example weight)
    class ChangeConHandler implements EventHandler<ActionEvent>{
        @Override public void handle(ActionEvent event){
            if (selectList.size() < 2) {
                String s = "Two places must be selected!";
                alert(s, event);
            }
            if (selectList.size() == 2) {
                Place node1 = nodeStrings.get(selectList.get(0).getId());
                Place node2 = nodeStrings.get(selectList.get(1).getId());
                if (graph.getEdgeBetween(node1, node2) == null) {
                    String s = "No direct connection!";
                    alert(s, event);
                }
                if (graph.getEdgeBetween(node1, node2) != null) {
                    String n = graph.getEdgeBetween(node1, node2).getName();
                    int t = graph.getEdgeBetween(node1, node2).getWeight();
                    lineSet.removeIf(l -> l.getStartX() == node1.getX() && l.getStartY() == node1.getY() && l.getEndX() == node2.getX() && l.getEndY() == node2.getY());
                    TextInputDialog td = new TextInputDialog();
                    td.setTitle("Connection");
                    td.setHeaderText("Connection from " + selectList.get(0).getId() + " to " + selectList.get(1).getId());
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20, 150, 10, 10));
                    TextField tf1 = new TextField(n);
                    TextField tf2 = new TextField(String.valueOf(t));
                    grid.add(new Label("Name: "), 0, 0);
                    grid.add(tf1, 1, 0);
                    grid.add(new Label("Time:"), 0, 1);
                    grid.add(tf2, 1, 1);
                    td.getDialogPane().setContent(grid);
                    tf1.setDisable(true);
                    td.showAndWait();
                    if (!tf2.getText().isBlank() && td.getResult() != null) {
                        try {
                            Line line = new Line();
                            line.setId(tf1.getText());
                            line.setUserData(Integer.parseInt(tf2.getText()));
                            lineSet.add(line);
                            graph.getEdgeBetween(node1, node2).setWeight(Integer.parseInt(tf2.getText()));
                            graph.getEdgeBetween(node2, node1).setWeight(Integer.parseInt(tf2.getText()));
                            changed = true;
                        } catch (NumberFormatException e) {
                            String s = "Time input must be a number!";
                            alert(s, event);
                        }
                    }
                }
            }
        }
    }

    //creates new connection between two selected nodes
    class NewConHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (selectList.size() < 2) {
                String s = "Two places must be selected!";
                alert(s, event);
            }
            if (selectList.size() == 2) {
                Place node1 = nodeStrings.get(selectList.get(0).getId());
                Place node2 = nodeStrings.get(selectList.get(1).getId());
                if (graph.getEdgeBetween(node1, node2) != null || graph.getEdgeBetween(node2, node1) != null) {
                    String s = "Connection already exists!";
                    alert(s, event);
                }
                if (graph.getEdgeBetween(node1, node2) == null && graph.getEdgeBetween(node2, node1) == null) {
                    TextInputDialog td = new TextInputDialog();
                    td.setTitle("Connection");
                    td.setHeaderText("Connection from " + selectList.get(0).getId() + " to " + selectList.get(1).getId());
                    GridPane grid = new GridPane();
                    grid.setHgap(10);
                    grid.setVgap(10);
                    grid.setPadding(new Insets(20, 150, 10, 10));
                    TextField tf1 = new TextField();
                    TextField tf2 = new TextField();
                    grid.add(new Label("Name: "), 0, 0);
                    grid.add(tf1, 1, 0);
                    grid.add(new Label("Time:"), 0, 1);
                    grid.add(tf2, 1, 1);
                    td.getDialogPane().setContent(grid);
                    td.showAndWait();
                    if (tf1.getText().isBlank() || tf2.getText().isBlank() && td.getResult() == null) {
                        String s = "Both inputs must not be empty!";
                        alert(s, event);
                    }
                    if (!tf1.getText().isBlank() && !tf2.getText().isBlank() && td.getResult() != null) {
                        try {
                            graph.connect(node1, node2, tf1.getText(), Integer.parseInt(tf2.getText()));
                            graph.connect(node2, node1, tf1.getText(), Integer.parseInt(tf2.getText()));
                            addEdgeLines(tf1.getText(), node1, node2, Integer.parseInt(tf2.getText()));
                            changed = true;
                        } catch (NumberFormatException e) {
                            String s = "Time input must be a number!";
                            alert(s, event);
                        }
                    }
                    if (td.getResult() == null) event.consume();
                }
            }
        }
    }

    //new node on click
    class NewPlaceHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (bp.getCenter() == p) {
                mainStage.getScene().setCursor(Cursor.CROSSHAIR);
                options.getChildren().get(2).setDisable(true);
                imageView.setOnMouseClicked(new MapClickHandler());
            } else
                event.consume();
        }
    }

    //sets node to red on click, selecting node
    class PlaceClickHandler implements EventHandler<javafx.scene.input.MouseEvent> {
        @Override
        public void handle(javafx.scene.input.MouseEvent event) {
            Place circle = (Place) event.getSource();
            if (selectList.size() < 2) {
                circle.setFill(Color.RED);
                selectList.add(circle);
                circle.setOnMouseClicked(new SelectedPlaceClickHandler());
            }
        }
    }

    //sets selected node to blue on click
    class SelectedPlaceClickHandler implements EventHandler<javafx.scene.input.MouseEvent> {
        @Override
        public void handle(javafx.scene.input.MouseEvent event) {
            Place circle = (Place) event.getSource();
            selectList.remove(circle);
            selectList.trimToSize();
            circle.setFill(Color.BLUE);
            circle.setOnMouseClicked(new PlaceClickHandler());
        }
    }

    //actual method for creation of new place
    class MapClickHandler implements EventHandler<javafx.scene.input.MouseEvent>{
        @Override public void handle(javafx.scene.input.MouseEvent event){
            double x = event.getX();
            double y = event.getY();
            imageView.setOnMouseClicked(null);
            TextInputDialog td = new TextInputDialog();
            td.setTitle("Name");
            td.setHeaderText("");
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));
            TextField tf1 = new TextField();
            grid.add(new Label("Name of place: "), 0, 0);
            grid.add(tf1, 1, 0);
            td.getDialogPane().setContent(grid);
            td.showAndWait();
            if (td.getResult() != null) {
                Place node = new Place(tf1.getText(), x, y);
                graph.add(node);
                node.setId(node.getName());
                node.setOnMouseClicked(new PlaceClickHandler());
                selectList.forEach(n -> {
                    if (n.getName().equals(node.getName())) node.setOnMouseClicked(new SelectedPlaceClickHandler());
                });
                nodeStrings.put(node.getName(), node);
                p.getChildren().addAll(node, node.getText());
                node.toFront();
                mainStage.getScene().setCursor(Cursor.DEFAULT);
                options.getChildren().get(2).setDisable(false);
                changed = true;
            }
            if (td.getResult() == null) {
                mainStage.getScene().setCursor(Cursor.DEFAULT);
                options.getChildren().get(2).setDisable(false);
            }
        }
    }

    //uses algorithm in getPath to produce a direct path between two nodes and presents them to user
    class FindPathHandler implements EventHandler<ActionEvent> {
        @Override
        public void handle(ActionEvent event) {
            if (selectList.size() < 2) {
                String s = "Two places must be selected!";
                alert(s, event);
            }
            if (selectList.size() == 2) {
                Place node1 = nodeStrings.get(selectList.get(1).getId());
                Place node2 = nodeStrings.get(selectList.get(0).getId());
                if (graph.pathExists(node1, node2)) {
                    List<Edge<Place>> edges = graph.getPath(node1, node2);
                    Alert alert = new Alert(INFORMATION);
                    alert.setResizable(true);
                    alert.setHeaderText("The path from " + selectList.get(0).getId() + " to " + selectList.get(1).getId() + ":");
                    TextArea ta = new TextArea();
                    ta.setWrapText(true);
                    ta.appendText(edges.get(0).toString());
                    int total = edges.get(0).getWeight();
                    for (int i = 1; i < edges.size(); i++) {
                        ta.appendText("\n" + edges.get(i).toString());
                        total = total + edges.get(i).getWeight();
                    }
                    ta.appendText("\n" + "Total " + total);
                    alert.getDialogPane().setContent(ta);
                    alert.showAndWait();
                }
                if (!graph.pathExists(node1, node2)) {
                    Alert alert = new Alert(INFORMATION);
                    alert.setHeaderText("Path does not exist.");
                    alert.showAndWait();
                }
            }
        }
    }

    class ExitItemHandler implements EventHandler<ActionEvent>{
        @Override public void handle(ActionEvent event){
            if (changed) {
                mainStage.fireEvent(new WindowEvent(mainStage, WindowEvent.WINDOW_CLOSE_REQUEST));
            }
            else mainStage.close();
        }
    }

    class ExitHandler implements EventHandler<WindowEvent>{
        @Override public void handle(WindowEvent event){
            if (changed) {
                Alert alert = new Alert(CONFIRMATION);
                alert.setTitle("Warning!");
                alert.setHeaderText("Unsaved changes, exit anyways?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.isPresent() && result.get() == ButtonType.CANCEL)
                    event.consume();
            }
            else mainStage.close();
        }
    }
}
