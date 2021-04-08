package org.mbari;

import org.mbari.vcr4j.commands.SeekElapsedTimeCmd;
import org.mbari.vcr4j.sharktopoda.SharktopodaVideoIO;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;

import java.net.SocketException;
import java.net.UnknownHostException;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

public class AppController {

    private final EventBus eventBus = new EventBus();

    private IO io;

    private SharktopodaVideoIO videoIo;

    private final App app;

    public AppController(App app) {
        this.app = app;
    }

    public void initLocalizationComms(int inport, int outport) {
        if (io != null && (io.getIncomingPort() != inport || io.getOutgoingPort() != outport)) {
           io.close();
           io = null;
        }

        if (io == null) {
            io = new IO(inport, outport, "localization", "localization");
        }
    }


    public void initControlComms(UUID videoReferenceUuid, int port) {
        if (videoIo != null && !videoIo.getUUID().equals(videoReferenceUuid)) {
            videoIo.close();
            videoIo = null;
        }

        if (videoIo == null) {
            try {
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
