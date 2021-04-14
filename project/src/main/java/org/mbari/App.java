package org.mbari;

import javafx.application.Application;
import javafx.application.Platform;
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
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.time.Duration;
import java.util.UUID;

import org.mbari.vcr4j.VideoIO;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.mbari.vcr4j.sharktopoda.SharktopodaError;
import org.mbari.vcr4j.sharktopoda.SharktopodaState;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;

/**
 * JavaFX App
 */
public class App extends Application {

    // README: For UI apps a common practice is to do the layout in one class. Then create a second
    // class with the non-ui logic. This keeps the code base much cleaner
    private AppController appController;

    private TableView<Localization> table;

    private ListView<Localization> listview;

    private Localization currentLoc;

    /**
     * JavaFX calls this before start()
     */
    @Override
    public void init() {
        appController = new AppController(this);
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

        table = new TableView<Localization>();
        table.setEditable(false);

        var conceptCol = new TableColumn<Localization, String>("Concept");
        conceptCol.setCellValueFactory(new PropertyValueFactory<Localization, String>("concept"));
        
        conceptCol.prefWidthProperty().bind(table.widthProperty().multiply(0.333));

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
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.333));
        //adding name column 
        var nameCol = new TableColumn<Localization, String>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<Localization, String>("concept"));
        nameCol.setCellFactory(column -> {
                return new TableCell<Localization, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        if (item == null || empty) {
                            setText(null);
                        }
                        else {
                            //loc = getLocalizations();
                            //Localization loc = loc.getElapsedTime(); 
                            setText(item);
                        }
                    }
                };
            });
        nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.333));

        nameCol.setResizable(false); 
        conceptCol.setResizable(false);
        timeCol.setResizable(false);

        table.getColumns().addAll(nameCol, conceptCol, timeCol);
        table.setMinWidth(470);
        
        // ------------------------------- Table Eventhandler--------------------------------------------
        
        
        
        
        //pane.getChildren().add(table);
        //table.setOnMouseClicked(new EventHandler<MouseEvent>()){
            //edit name in here?
        //};
        // ------------------------------- Table --------------------------------------------

        // ------------------------------- ListView --------------------------------------------


        // VBox vBox = new VBox();
        // var sceneEmpty = new Scene(vBox, 480, 480);
        //TODO: Insead of Strings make these the items that are added, have the items be renameable and add a button to them 

        StackPane pane = new StackPane();


        // README: THe listview is redundant! It's showing the same info as the TableView. Move the
        // seek button to just a single button in the UI (or you can try a popup on the tableview)
        // The button can get the selected items from the table and seek to them

        // ObservableList<Localization> list = FXCollections.observableArrayList("Item 1", "Item 2", "Item 3", "Item 4");
        // listview = new ListView<>();

        // listview.setCellFactory((Callback<ListView<Localization>, ListCell<Localization>>) new Callback<ListView<Localization>, ListCell<Localization>>() {
        //     @Override
        //     public ListCell<Localization> call(ListView<Localization> param) {
        //         return new XCell(appController);
        //     };
        // });

        // listview.setOnMouseClicked(new EventHandler<MouseEvent>(){
        //     @Override
        //     public void handle(MouseEvent event){
        //         if(event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
        //             // event.getTarget().getChildren().setLabel("SOme new label");
        //         }
        //     }
        // });

        // listview.prefWidthProperty().bind(listview.widthProperty().multiply(1.5));
        // listview.setMinWidth(200);
        // pane.getChildren().add(listview);

        HBox hBox = new HBox(table);

        // HBox.setMargin(pane, new Insets(20,20,20,20));
        HBox.setMargin(table, new Insets(20,20,20,20));


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
        search.setMinWidth(220);
        search.setMinHeight(25);

        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10,1);

 
 
        // ----- Search Bar -----

        HBox topHbox = new HBox(logoView, spacer,  search);//logoView,
        HBox.setMargin(search, new Insets(30,20,20,20));
        HBox.setMargin(logoView, new Insets(20,20,20,20));

        

        // ------------------------------- Top Menu --------------------------------------------

        // ------------------------------- Bottom Buttons --------------------------------------------

        Button saveBtn = new Button("Save");
        Button downLoadBtn = new Button("Download");
        Button upLoadBtn = new Button("Upload");
        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> deleteRowFromTable());
        Button seekBtn = new Button("Seek");
        seekBtn.setOnAction(e -> seekButtonClicked());

        HBox hButtonBox = new HBox(saveBtn, downLoadBtn, upLoadBtn, deleteBtn, seekBtn);
        
        HBox.setMargin(saveBtn, new Insets(20,20,20,20));
        HBox.setMargin(downLoadBtn, new Insets(20,20,20,20));
        HBox.setMargin(upLoadBtn, new Insets(20,20,20,20));
        HBox.setMargin(deleteBtn, new Insets(20,20,20,20));
        HBox.setMargin(seekBtn, new Insets(20,20,20,20));
        
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

        initComms(table, listview);


        // ------------------------------- Stage --------------------------------------------

    }

    private static String formatDuration(Duration duration) {
        return String.format("%d:%02d:%02d:%03d", 
                                duration.toHours(), 
                                duration.toMinutesPart(), 
                                duration.toSecondsPart(),
                                duration.toMillisPart());
    }

    private void initComms(TableView<Localization> table,ListView<Localization> listview) {

        var incomingPort = 5561;   // ZeroMQ subscriber port
        var outgoingPort = 5562;   // ZeroMQ publisher port
        appController.initLocalizationComms(outgoingPort, incomingPort);
        IO io = appController.getIo();

        var items = io.getController().getLocalizations();
        items.addListener((ListChangeListener.Change<? extends Localization> c) -> {
            System.out.println("Received: " + c);
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(loc -> System.out.println(loc));
                }
            }
        });

        items.addListener((ListChangeListener.Change<? extends Localization> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    Localization head = c.getAddedSubList().get(0);
                    appController.initControlComms(head.getVideoReferenceUuid(), 8800);
                }
            }
        });


        table.setItems(items);
        // listview.setItems(items);
    }

    public TableView<Localization> getTable() {
        return table;
    }

    public ListView<Localization> getListview() {
        return listview;
    }

    public static void main(String[] args) {
        launch();
    }

    private void deleteRowFromTable(){
        // table.getItems().removeAll(
            // table.getSelectionModel().getSelectedItem()
        // );
// table.re
        // table.getSelectionModel().getSelectedItem().
    }

    // after this function call the current location will be accessable
    private void seekButtonClicked(){
        System.out.println(formatDuration(table.getSelectionModel().getSelectedItem().getElapsedTime()));
        currentLoc = table.getSelectionModel().getSelectedItem();
    }

}
