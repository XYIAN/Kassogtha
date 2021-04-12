package org.mbari;

import org.mbari.vcr4j.commands.SeekElapsedTimeCmd;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

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
            log.debug("Seeing to {}", duration);
            videoIo.send(new SeekElapsedTimeCmd(duration));
        }
    }

    /**
     * Seeks to the row selected in
     */
    public void seek() {

        Optional<Localization> selectedOpt = app.getListview()
                .getSelectionModel()
                .getSelectedItems()
                .stream()
                .findFirst();

        selectedOpt.ifPresent(item -> seek(item.getDuration()));
    }
}
