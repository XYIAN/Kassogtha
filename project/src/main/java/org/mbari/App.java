package org.mbari;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Scene;

// import javafx.scene.control.TableCell;
// import javafx.scene.control.TableColumn;
// import javafx.scene.control.TableView;
// import javafx.scene.control.TextField;
// import javafx.scene.control.Button;
// import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.geometry.Insets;
import javafx.event.*;

// import javafx.scene.layout.VBox;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.BorderPane;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.Duration;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

/**
 * JavaFX App
 */
public class App extends Application {



    private IO io;


    static class XCell extends ListCell<String> {
        HBox hbox = new HBox();
        Label label = new Label("(empty)");
        Pane pane = new Pane();
        Button button = new Button("Seek");
        String lastItem;

        public XCell() {
            super();
            hbox.getChildren().addAll(label, pane, button);
            HBox.setHgrow(pane, Priority.ALWAYS);
            button.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    System.out.println(lastItem + " : " + event);
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(null);  // No text in label of super class
            if (empty) {
                lastItem = null;
                setGraphic(null);
            } else {
                lastItem = item;
                label.setText(item!=null ? item : "<null>");
                setGraphic(hbox);
            }
        }
    }






    @Override
    public void start(Stage stage) {


        // ------------------------------- Root --------------------------------------------

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10,10,10,10));


        // ------------------------------- Root --------------------------------------------


        // ------------------------------- ICON --------------------------------------------

        Image icon = new Image("/icons/octopus.png");

        stage.getIcons().add(icon);

        // ------------------------------- ICON --------------------------------------------



        // ------------------------------- Table --------------------------------------------

        var table = new TableView<Localization>();
        table.setEditable(false);

        var conceptCol = new TableColumn<Localization, String>("Concept");
        conceptCol.setCellValueFactory(new PropertyValueFactory<Localization, String>("concept"));
        
        conceptCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5));

        var timeCol = new TableColumn<Localization, Duration>("ElapsedTime");
        timeCol.setCellValueFactory(new PropertyValueFactory<Localization, Duration>("elapsedTime"));
        timeCol.setCellFactory(column -> {
                return new TableCell<Localization, Duration>() {
                    @Override
                    protected void updateItem(Duration item, boolean empty) {
                        if (item == null || empty) {
                            setText(null);
                        }
                        else {
                            setText(formatDuration(item));
                        }
                    }
                };
            });
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.5));

        conceptCol.setResizable(false);
        timeCol.setResizable(false);

        table.getColumns().addAll(timeCol, conceptCol);

        // ------------------------------- Table --------------------------------------------

        // ------------------------------- ListView --------------------------------------------


        // VBox vBox = new VBox();
        // var sceneEmpty = new Scene(vBox, 480, 480);
        //TODO: Insead of Strings make these the items that are added, have the items be renameable and add a button to them 

        StackPane pane = new StackPane();
        ObservableList<String> list = FXCollections.observableArrayList(
                "Item 1", "Item 2", "Item 3", "Item 4");
        ListView<String> lv = new ListView<>(list);
        lv.setCellFactory((Callback<ListView<String>, ListCell<String>>) new Callback<ListView<String>, ListCell<String>>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                return new XCell();
            }
        });
        pane.getChildren().add(lv);

        HBox hBox = new HBox(pane, table);

        hBox.setMargin(pane, new Insets(20,20,20,20));
        hBox.setMargin(table, new Insets(20,20,20,20));


        // ------------------------------- ListView --------------------------------------------


        // ------------------------------- Top Menu --------------------------------------------

        
        // // ----- Image -----
        Image logo = new Image("icons/kassogthaLogoSide.jpg");
        ImageView logoView = new ImageView();
        logoView.setFitHeight(70);
        logoView.setFitWidth(200);
        logoView.setImage(logo);
        // logoView.setPreserveRatio(true);
        // // ----- Image -----

        // // ----- Search Bar -----
 
        TextField search = new TextField();
        search.setPromptText("Search");
        search.setMinWidth(200);
        search.setMinHeight(25);

        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10,1);

 
 
        // ----- Search Bar -----

        HBox topHbox = new HBox(logoView, spacer,  search);//logoView,
        topHbox.setMargin(search, new Insets(30,20,20,20));
        topHbox.setMargin(logoView, new Insets(20,20,20,20));

        

        // ------------------------------- Top Menu --------------------------------------------

        // ------------------------------- Bottom Buttons --------------------------------------------

        Button saveBtn = new Button("Save");
        Button downLoadBtn = new Button("Download");
        Button upLoadBtn = new Button("Upload");
        HBox hButtonBox = new HBox(saveBtn, downLoadBtn, upLoadBtn);
        
        hButtonBox.setMargin(saveBtn, new Insets(20,20,20,20));
        hButtonBox.setMargin(downLoadBtn, new Insets(20,20,20,20));
        hButtonBox.setMargin(upLoadBtn, new Insets(20,20,20,20));
        
        // ------------------------------- Bottom Buttons --------------------------------------------


        root.setTop(topHbox);
        root.setCenter(hBox);
        root.setBottom(hButtonBox);
        // VBox containerBox = new VBox(topHbox, hBox, hButtonBox);


        // ------------------------------- Stage --------------------------------------------

        var scene = new Scene(root);
        // scene.setResizable(false);
        stage.setTitle("Kassogtha");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        initComms(table);

        // ------------------------------- Stage --------------------------------------------

    }

    private String formatDuration(Duration duration) {
        return String.format("%d:%02d:%02d:%03d", 
                                duration.toHours(), 
                                duration.toMinutesPart(), 
                                duration.toSecondsPart(),
                                duration.toMillisPart());
    }

    private void initComms(TableView<Localization> table) {
        var incomingPort = 5561;   // ZeroMQ subscriber port
        var outgoingPort = 5562;   // ZeroMQ publisher port
        var incomingTopic = "localization";
        var outgoingTopic = "localization";
        io = new IO(outgoingPort, incomingPort, outgoingTopic, incomingTopic);

        var items = io.getController().getLocalizations();
        items.addListener((ListChangeListener.Change<? extends Localization> c) -> {
            System.out.println("Received: " + c);
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(loc -> System.out.println(loc));
                }
            }
        });
        table.setItems(items);
    }

    public static void main(String[] args) {
        launch();
    }

}