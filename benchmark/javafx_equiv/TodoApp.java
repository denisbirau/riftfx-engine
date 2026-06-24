// JavaFX imperative equivalent of a to-do list.
// Used only for lines-of-code comparison — not part of the main build.
package com.riftfx.benchmark.javafx_equiv;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class TodoApp extends Application {

    private final ObservableList<String> items = FXCollections.observableArrayList();
    private Label countLabel;

    @Override
    public void start(Stage stage) {
        countLabel = new Label("Tasks: 0");

        ListView<String> listView = new ListView<>(items);
        listView.setPrefHeight(200);

        TextField inputField = new TextField();
        inputField.setPromptText("Enter a task...");

        Button addButton = new Button("Add");
        addButton.setOnAction(e -> {
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                items.add(text);
                inputField.clear();
                countLabel.setText("Tasks: " + items.size());
            }
        });

        Button removeButton = new Button("Remove Selected");
        removeButton.setOnAction(e -> {
            int idx = listView.getSelectionModel().getSelectedIndex();
            if (idx >= 0) {
                items.remove(idx);
                countLabel.setText("Tasks: " + items.size());
            }
        });

        Button clearButton = new Button("Clear All");
        clearButton.setOnAction(e -> {
            items.clear();
            countLabel.setText("Tasks: 0");
        });

        HBox inputRow = new HBox(8, inputField, addButton);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(inputField, Priority.ALWAYS);

        HBox actionRow = new HBox(8, removeButton, clearButton);
        actionRow.setAlignment(Pos.CENTER_LEFT);

        VBox root = new VBox(10, countLabel, listView, inputRow, actionRow);
        root.setPadding(new Insets(15));
        VBox.setVgrow(listView, Priority.ALWAYS);

        stage.setScene(new Scene(root, 400, 350));
        stage.setTitle("To-Do List");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
