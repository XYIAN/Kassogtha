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
//import javafx.scene.control.Tooltip; 
//import javafx.scene.control.ComboBox;
import javafx.geometry.Insets;
import javafx.event.*;

// import javafx.scene.layout.VBox;
// import javafx.scene.layout.HBox;
// import javafx.scene.layout.BorderPane;
import javafx.scene.layout.*;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent; 
import javafx.scene.image.Image;
import javafx.stage.Window;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import java.util.stream.Stream; 


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

    private List<String> conceptList;

    /**
     * JavaFX calls this before start()
     */
    @Override
    public void init() {
        appController = new AppController(this);
    }


    @Override
    public void start(Stage stage) {

        // initialize the conceptList with MBARI's doc
        conceptList = appController.getAutoFillStrings("concepts.json");
        for (String concept: conceptList){
            System.out.println(concept);
        }

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


        // // ----- Rename Combobox -----

        ComboBox<String> rename = new ComboBox<>();
        new FilteredComboBoxDecorator<>(rename, FilteredComboBoxDecorator.STARTSWITH_IGNORE_SPACES);
        rename.setItems(FXCollections.observableArrayList(conceptList));

        rename.setPromptText("Rename");
        rename.setMinWidth(220);
        rename.setMinHeight(25);
        table.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                var text = newValue == null ? null : newValue.getConcept();
                rename.setAccessibleText(text);;
            });

        rename.setOnAction(evt -> {
            var newConcept = rename.getValue();
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

        // ----- Rename ComboBox -----

        Button upLoaconceptUpdBtn = new Button("Upload Concepts");
        upLoaconceptUpdBtn.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Json Concept File");
            fileChooser.setInitialFileName("new_concepts.json");

            // limit the type of files that are allowed to be uploaded
            fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("JSON file", "*.json")
            );
            File selectedFile = fileChooser.showOpenDialog(stage);
            if(selectedFile != null){
                appController.uploadConcepts(selectedFile);
            }
           
        });

        VBox renameBox = new VBox(rename, upLoaconceptUpdBtn);
        VBox.setMargin(rename, new Insets(10,20,20,10));
        VBox.setMargin(upLoaconceptUpdBtn, new Insets(10,10,10,10));


        HBox topHbox = new HBox(logoView, spacer,  renameBox);//logoView,
        HBox.setMargin(logoView, new Insets(20,20,20,20));


        // ------------------------------- Top Menu --------------------------------------------

        // ------------------------------- Bottom Buttons --------------------------------------------

        Button saveBtn = new Button("Save");
        saveBtn.setOnMouseClicked(new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event){
                appController.save();
            }
        });

        Button downLoadBtn = new Button("Download");

        // this button should encompass both upload functionalites, both for the autocomplete and the Localizations
        Button playBtn = new Button("Play");
        playBtn.setOnAction(e -> appController.play());
       

        Button clearBtn = new Button("Delete");
        clearBtn.setOnAction(e -> deleteRowFromTable());

        Button seekBtn = new Button("Seek");
        seekBtn.setOnAction(e -> seekButtonClicked());

        HBox hButtonBox = new HBox(saveBtn, downLoadBtn, clearBtn, seekBtn, playBtn);
        
        HBox.setMargin(saveBtn, new Insets(20,20,20,20));
        HBox.setMargin(downLoadBtn, new Insets(20,20,20,20));
        HBox.setMargin(playBtn, new Insets(20,20,20,20));
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
        currentLoc = table.getSelectionModel().getSelectedItem();
        appController.delete(currentLoc);

    }
/*
    public void autoComplete(){
        var gson = new Gson();
        var concepts = gson.fromJson(stringOfJson, String[].class);
        //handle listener 
        items.addListener 
    }
*/
    // after this function call the current location will be accessable
    private void seekButtonClicked(){
        System.out.println(formatDuration(table.getSelectionModel().getSelectedItem().getElapsedTime()));
        currentLoc = table.getSelectionModel().getSelectedItem();
        appController.seek(currentLoc.getElapsedTime());
    }

}
