// JavaFX imperative equivalent of bench_small counter.
// Used only for lines-of-code comparison — not part of the main build.
package com.riftfx.benchmark.javafx_equiv;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class CounterApp extends Application {

    private int count = 0;
    private Label countLabel;

    @Override
    public void start(Stage stage) {
        countLabel = new Label("Count: 0");
        countLabel.setStyle("-fx-font-size: 18px;");

        Button incButton = new Button("+");
        incButton.setOnAction(e -> {
            count++;
            countLabel.setText("Count: " + count);
        });

        Button decButton = new Button("−");
        decButton.setOnAction(e -> {
            count--;
            countLabel.setText("Count: " + count);
        });

        Button resetButton = new Button("Reset");
        resetButton.setOnAction(e -> {
            count = 0;
            countLabel.setText("Count: 0");
        });

        HBox controls = new HBox(10, decButton, incButton);
        controls.setAlignment(Pos.CENTER);

        VBox root = new VBox(15, countLabel, controls, resetButton);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(20));

        stage.setScene(new Scene(root, 300, 200));
        stage.setTitle("Counter");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
