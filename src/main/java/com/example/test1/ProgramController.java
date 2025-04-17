package com.example.test1;


import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 *
 * @author sowme
 */
public class ProgramController {

    public static boolean directed = false, undirected = false, weighted = false, unweighted = false;

    @FXML
    private AnchorPane panel1;

    static HelloController cref;


    @FXML
    protected void onpanel1NextButtonClick() {
        FadeTransition ft = new FadeTransition();
        ft.setDuration(Duration.millis(1000));
        ft.setNode(panel1);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            loadNextScene();
        });
        ft.play();
        System.out.println("Here");
    }

    void loadNextScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("hello-view.fxml"));
            Parent root = loader.load();
            Scene newScene = new Scene(root);
            cref = loader.getController();

            System.out.println("Controller ref: " + cref);
            newScene.getStylesheets().add(getClass().getResource("Styling.css").toExternalForm());
            HelloApplication.primaryStage.setScene(newScene);
        } catch (IOException ex) {

            System.out.println("No");
        }
    }

}
