package org.mbari;

import org.mbari.vcr4j.commands.SeekElapsedTimeCmd;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.client.gson.DurationConverter;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.stage.FileChooser;

import java.io.*;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.*;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AppController {

    private static final Logger log = LoggerFactory.getLogger(AppController.class);

    private final EventBus eventBus = new EventBus();

    private IO io;

    private SharktopodaVideoIO videoIo;

    private final App app;

    public AppController(App app) {
        this.app = app;
    }

    /**
     * Initializes localization communications.
     * @param inport
     * @param outport
     */
    public void initLocalizationComms(int inport, int outport) {
        if (io != null && (io.getIncomingPort() != inport || io.getOutgoingPort() != outport)) {
           io.close();
           io = null;
        }

        if (io == null) {
            log.info("Intializing localization ZeroMQ comms");
            io = new IO(inport, outport, "localization", "localization");
        }
    }


    /**
     * Initializes control communications
     * @param videoReferenceUuid
     * @param port
     */
    public void initControlComms(UUID videoReferenceUuid, int port) {
        if (videoIo != null && !videoIo.getUUID().equals(videoReferenceUuid)) {
            videoIo.close();
            videoIo = null;
        }

        if (videoIo == null) {
            try {
                log.info("Intializing video control UDP comms");
                videoIo = new SharktopodaVideoIO(videoReferenceUuid, "localhost", port);
            } catch (UnknownHostException e) {
                e.printStackTrace();
                // When calling localhost this exception should never be thrown
            } catch (SocketException e) {
                e.printStackTrace();
                // TODO Add error handling
            }
        }
    }

    public EventBus getEventBus() {
        return eventBus;
    }

    public IO getIo() {
        return io;
    }

    public SharktopodaVideoIO getVideoIo() {
        return videoIo;
    }

    public void seek(Duration duration) {
        if (videoIo != null) {
            log.debug("Seeking to {}", duration);
            videoIo.send(new SeekElapsedTimeCmd(duration));
        }
    }

    /**
     * Seeks to the row selected in
     */
    public void seek() {

        Optional<Localization> selectedOpt = app.getTable()
                .getSelectionModel()
                .getSelectedItems()
                .stream()
                .findFirst();

        selectedOpt.ifPresent(item -> seek(item.getDuration()));
    }

    public void update(Localization localization) {
        io.getController().removeLocalization(localization.getLocalizationUuid());
        io.getController().addLocalization(localization);
    }


    public void delete(Localization localization) {
        io.getController().removeLocalization(localization.getLocalizationUuid());
    }


    public void save() {
        var xs = new ArrayList<Localization>(io.getController().getLocalizations());
        Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
            .registerTypeAdapter(Duration.class, new DurationConverter())
            .create();
        String json = gson.toJson(xs);
        try {
            Date date = new Date() ;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss") ;
            var writer = new java.io.FileWriter(simpleDateFormat.format(date) + ".json");
            writer.write(json);
            writer.close();
        }
        catch (IOException e){
            System.out.println("[ERROR] AppController.save() - IOException: " + e.toString());
        }
    }



    public void auto(Localization loc){
        //Path conceptList = Paths.get("/home/xyian/dev/Kassogtha/project/src/main/resources/concepts.json");
        //var conceptJson = "/home/xyian/dev/Kassogtha/project/src/main/resources/concepts.json";
        //var conceptJson = io.getController().getConcept();
        //log.debug("concept to convert::::-->", conceptJson);
        //File conceptList = new File 
        List<String> conceptList = new ArrayList<>();
        var url = getClass().getResource("/concepts.json");
        //BufferedReader reader = null; 
        try(var reader = new BufferedReader(new InputStreamReader(url.openStream()))){
            String line; 
            while((line = reader.readLine()) != null){
                if(line.length() > 1){
                    line = line.replaceAll("\"", ""); 
                }
                log.debug(line);
                conceptList.add(line); 
            }
            for (String concept: conceptList){
                System.out.println(concept);
            }
            log.debug("concept to convert::::-->", conceptList.toString());
        } catch(IOException e){
            e.printStackTrace(); 
        } 

        //convert JSON concept list to string array 
        //Gson gson = new GsonBuilder();
        //var concepts = gson.fromJson(stringOfJson, String[].class);
        //var concepts = gson.fromJson(conceptList, String[].class);
    }







    // the idea here is to simply set up the origin of the concept autocompletes
    // We will feed getAutoFillStrings the name of the jasonObject to fill in the list of strings
    public List<String> getAutoFillStrings(String conceptJasonDoc){
        List<String> conceptList = new ArrayList<>();
        var url = getClass().getResource("/" + conceptJasonDoc);
        try(var reader = new BufferedReader(new InputStreamReader(url.openStream()))){
            String line; 
            while((line = reader.readLine()) != null){
                if(line.length() > 1){
                    line = line.replaceAll("\"", ""); 
                }
                conceptList.add(line); 
            }
            return conceptList;

        } catch(IOException e){
            e.printStackTrace(); 
        } 
        return new ArrayList<>();
    }
    


    // @param <T>
    // public class conceptAuto<T>{
    //     private ComboBox<T> cmb;
    //     String filter = "";
    //     private ObservableList<T> originalItems;
    
    //     public ComboBoxAutoComplete(ComboBox<T> cmb) {
    //         this.cmb = cmb;
    //         originalItems = FXCollections.observableArrayList(cmb.getItems());
    //         cmb.setTooltip(new Tooltip());
    //         cmb.setOnKeyPressed(this::handleOnKeyPressed);
    //         cmb.setOnHidden(this::handleOnHiding);
    //         return; 
    //     }
    
    //     public void handleOnKeyPressed(KeyEvent e) {
    //         ObservableList<T> filteredList = FXCollections.observableArrayList();
    //         KeyCode code = e.getCode();
    
    //         if (code.isLetterKey()) {
    //             filter += e.getText();
    //         }
    //         if (code == KeyCode.BACK_SPACE && filter.length() > 0) {
    //             filter = filter.substring(0, filter.length() - 1);
    //             cmb.getItems().setAll(originalItems);
    //         }
    //         if (code == KeyCode.ESCAPE) {
    //             filter = "";
    //         }
    //         if (filter.length() == 0) {
    //             filteredList = originalItems;
    //             cmb.getTooltip().hide();
    //         } else {
    //             Stream<T> itens = cmb.getItems().stream();
    //             String txtUsr = filter.toString().toLowerCase();
    //             itens.filter(el -> el.toString().toLowerCase().contains(txtUsr)).forEach(filteredList::add);
    //             cmb.getTooltip().setText(txtUsr);
    //             Window stage = cmb.getScene().getWindow();
    //             double posX = stage.getX() + cmb.getBoundsInParent().getMinX();
    //             double posY = stage.getY() + cmb.getBoundsInParent().getMinY();
    //             cmb.getTooltip().show(stage, posX, posY);
    //             cmb.show();
    //         }
    //         cmb.getItems().setAll(filteredList);
    //     }
    
    //     public void handleOnHiding(Event e) {
    //         filter = "";
    //         cmb.getTooltip().hide();
    //         T s = cmb.getSelectionModel().getSelectedItem();
    //         cmb.getItems().setAll(originalItems);
    //         cmb.getSelectionModel().select(s);
    //     }
    
    // }//end autoComplet
}
