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
import javafx.scene.control.cell.TextFieldTableCell;
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
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
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

    private Localization currentLoc;

    private String nameVal = "Name";

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

        var conceptCol = new TableColumn<Localization, String>("Name");
        conceptCol.setCellValueFactory(new PropertyValueFactory<Localization, String>("concept"));
        conceptCol.setCellFactory(column -> {
            return new TableCell<Localization, String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    if (item == null || empty) {
                        setText(null);
                    }
                    else {
                        //loc = getLocalizations();
                        setText(item);
                        nameVal = item;
                    }
                }
            };
        });
        
        conceptCol.prefWidthProperty().bind(table.widthProperty().multiply(0.6));

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
        timeCol.prefWidthProperty().bind(table.widthProperty().multiply(0.4));
       
       
        //adding name column 
        // let's make the Name column our Textview element

        //TODO: This is what I need help with

        /*
            The intention is to remove one of the columns "name" or " concept"
            For now "name" is supposed to hold the Textfield, but it hasn't been working
            Below we can see a few different attempts, but nothing has worked so far.
        */
        // var nameCol = new TableColumn<Localization, String>("Name");
        // nameCol.setCellValueFactory(new PropertyValueFactory<>("concept"));
        // nameCol.setCellFactory(TextFieldTableCell.<Localization>forTableColumn());


        // nameCol.prefWidthProperty().bind(table.widthProperty().multiply(0.333));

        // nameCol.setResizable(false); 
        conceptCol.setResizable(false);
        timeCol.setResizable(false);

        // table.getColumns().addAll(nameCol, conceptCol, timeCol);
        table.getColumns().addAll(conceptCol, timeCol);
        table.setMinWidth(470);
        
        // ------------------------------- Table Eventhandler--------------------------------------------
        
        
        // ------------------------------- Table --------------------------------------------

        //TODO: Insead of Strings make these the items that are added, have the items be renameable and add a button to them 


        HBox hBox = new HBox(table);

        // HBox.setMargin(pane, new Insets(20,20,20,20));
        HBox.setMargin(table, new Insets(20,20,20,20));


        // ------------------------------- Top Menu --------------------------------------------

        
        // // ----- Image -----
        Image logo = new Image("icons/kassogthaLogoSide.jpg");
        ImageView logoView = new ImageView();
        logoView.setFitHeight(70);
        logoView.setFitWidth(200);
        logoView.setImage(logo);
        // // ----- Image -----


        // // ----- Search Bar -----
 
        TextField rename = new TextField();
        rename.setPromptText("Rename");
        rename.setMinWidth(220);
        rename.setMinHeight(25);
        table.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                var text = newValue == null ? null : newValue.getConcept();
                rename.setText(text);
            });
        rename.setOnAction(evt -> {
            var newConcept = rename.getText();
            if (newConcept != null) {
              var localization = table.getSelectionModel()
                .getSelectedItem();
              if (localization != null) {
                  localization.setConcept(newConcept);
                  appController.update(localization);
                  table.getColumns().get(0).setVisible(false);
                  table.getColumns().get(0).setVisible(true);
              }
            }
        });


        final Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        spacer.setMinSize(10,1);

        // ----- Search Bar -----

        HBox topHbox = new HBox(logoView, spacer,  rename);//logoView,
        HBox.setMargin(rename, new Insets(30,20,20,20));
        HBox.setMargin(logoView, new Insets(20,20,20,20));


        // ------------------------------- Top Menu --------------------------------------------

        // ------------------------------- Bottom Buttons --------------------------------------------

        Button saveBtn = new Button("Save");

        saveBtn.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event){
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save");
                fileChooser.getExtensionFilters().addAll(new ExtensionFilter("All Files", "*.*"));
                File fileToSave = fileChooser.showSaveDialog(null);
                appController.save(fileToSave);
            }
        });

        Button downLoadBtn = new Button("Download");

        Button upLoadBtn = new Button("Upload");

        Button clearBtn = new Button("Delete");
        clearBtn.setOnAction(e -> deleteRowFromTable());

        Button seekBtn = new Button("Seek");
        seekBtn.setOnAction(e -> seekButtonClicked());

        HBox hButtonBox = new HBox(saveBtn, downLoadBtn, upLoadBtn, clearBtn, seekBtn);
        
        HBox.setMargin(saveBtn, new Insets(20,20,20,20));
        HBox.setMargin(downLoadBtn, new Insets(20,20,20,20));
        HBox.setMargin(upLoadBtn, new Insets(20,20,20,20));
        HBox.setMargin(clearBtn, new Insets(20,20,20,20));
        HBox.setMargin(seekBtn, new Insets(20,20,20,20));
        
        // ------------------------------- Bottom Buttons --------------------------------------------

        root.setTop(topHbox);
        root.setCenter(hBox);
        root.setBottom(hButtonBox);

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

    private static String formatDuration(Duration duration) {
        return String.format("%d:%02d:%02d:%03d", 
                                duration.toHours(), 
                                duration.toMinutesPart(), 
                                duration.toSecondsPart(),
                                duration.toMillisPart());
    }

    private void initComms(TableView<Localization> table) {

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
    }

    public TableView<Localization> getTable() {
        return table;
    }

    public static void main(String[] args) {
        launch();
    }

    private void deleteRowFromTable(){
    }

    // after this function call the current location will be accessable
    private void seekButtonClicked(){
        System.out.println(formatDuration(table.getSelectionModel().getSelectedItem().getElapsedTime()));
        currentLoc = table.getSelectionModel().getSelectedItem();
        appController.seek(currentLoc.getElapsedTime());
    }

}
