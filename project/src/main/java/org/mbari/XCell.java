package org.mbari;

import java.time.Duration;
import org.mbari.vcr4j.sharktopoda.client.localization.IO;
import org.mbari.vcr4j.sharktopoda.client.localization.Localization;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;

public class XCell extends ListCell<Localization> {
  HBox hbox = new HBox();
  Label label = new Label("(empty)");
  Pane pane = new Pane();
  Button button = new Button("Seek");
  private final AppController appController;


  public XCell(AppController appController) {
      this.appController = appController;
      hbox.getChildren().addAll(label, pane, button);
      HBox.setHgrow(pane, Priority.ALWAYS);

      // FIXME: ListCells are reused. So it's indeterminate which localization is being called
      // Don't put the button in the list cell
      button.setOnAction(event -> appController.seek());

      // --- Java 8 introduced lambdas/functions. You can use these for
      // event handlers with JavaFS to greatly simplify code. This one liner
      // does the same thing as the commented out block below.
      // button.setOnAction(event -> System.out.println(lastItem + " : " + event));

      // button.setOnAction(new EventHandler<ActionEvent>() {
      //     @Override
      //     public void handle(ActionEvent event) {
      //         System.out.println(lastItem + " : " + event);
      //     }
      // });

  }

  @Override
  protected void updateItem(Localization item , boolean empty) {
      super.updateItem(item, empty);
      setText(null);  // No text in label of super class
      if (empty) {
          setGraphic(null);
      } else {
          Platform.runLater(() -> {
              label.setText(item.getConcept());
              setGraphic(hbox);
          });
          
      }
  }

  protected void setLabel(String newlabel){
      label.setText(newlabel);
  }
}
