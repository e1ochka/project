package com.example.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.util.Optional;

public class KanbanBoard extends Application {

    private HBox draggedHBox;

    @Override
    public void start(Stage primaryStage) {
        VBox todoColumn = createColumn("To Do");
        VBox inProgressColumn = createColumn("In Progress");
        VBox doneColumn = createColumn("Done");

        TextField newTaskField = new TextField();
        Button addTaskButton = new Button("Add Task");
        addTaskButton.setOnAction(event -> {
            String newTaskText = newTaskField.getText();
            if (!newTaskText.isEmpty()) {
                HBox taskCard = createTaskCard(newTaskText);
                todoColumn.getChildren().add(taskCard);
                newTaskField.clear();
            }
        });

        HBox root = new HBox(todoColumn, inProgressColumn, doneColumn, new VBox(newTaskField, addTaskButton));
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("Kanban Board");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private VBox createColumn(String title) {
        VBox column = new VBox();
        column.setMinWidth(200);
        column.setStyle("-fx-background-color: #e0e0e0;");
        Label columnLabel = new Label(title);
        column.getChildren().add(columnLabel);

        column.setOnDragOver(event -> {
            if (event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        column.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString() && draggedHBox != null) {
                VBox parentColumn = (VBox) draggedHBox.getParent();
                parentColumn.getChildren().remove(draggedHBox);
                column.getChildren().add(draggedHBox);
                success = true;
            }
            event.setDropCompleted(success);
            event.consume();
        });

        return column;
    }

    private HBox createTaskCard(String taskText) {
        Label taskLabel = new Label(taskText);
        taskLabel.setStyle("-fx-background-color: #ffffff; -fx-border-color: #000000; -fx-padding: 5px;");
        taskLabel.setOnDragDetected(event -> {
            Dragboard db = taskLabel.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(taskText);
            db.setContent(content);
            draggedHBox = (HBox) taskLabel.getParent();
            event.consume();
        });

        taskLabel.setOnMouseClicked(event -> {
            Dialog<Void> dialog = new Dialog<>();
            dialog.setTitle("Task Actions");

            ButtonType editButtonType = new ButtonType("Edit", ButtonBar.ButtonData.OK_DONE);
            ButtonType deleteButtonType = new ButtonType("Delete", ButtonBar.ButtonData.APPLY);
            ButtonType colorButtonType = new ButtonType("Change Color", ButtonBar.ButtonData.FINISH);
            dialog.getDialogPane().getButtonTypes().addAll(editButtonType, deleteButtonType, colorButtonType, ButtonType.CANCEL);

            dialog.setContentText("Select the action to perform on the task.");

            dialog.setResultConverter(buttonType -> {
                if (buttonType == editButtonType) {
                    TextInputDialog editDialog = new TextInputDialog(taskLabel.getText());
                    editDialog.setTitle("Edit Task");
                    editDialog.setHeaderText("Edit the Task");
                    editDialog.setContentText("Enter the new task name:");
                    Optional<String> result = editDialog.showAndWait();
                    result.ifPresent(newTaskText -> taskLabel.setText(newTaskText));
                } else if (buttonType == deleteButtonType) {
                    HBox taskCard = (HBox) taskLabel.getParent();
                    VBox column = (VBox) taskCard.getParent();
                    if (column != null) { // Добавлено условие для проверки на null
                        column.getChildren().remove(taskCard);
                    }
                } else if (buttonType == colorButtonType) {
                    ColorPicker colorPicker = new ColorPicker(Color.WHITE);
                    colorPicker.setOnAction(e -> {
                        taskLabel.setStyle("-fx-background-color: " + toRGBCode(colorPicker.getValue()) + "; -fx-border-color: #000000; -fx-padding: 5px;");
                    });

                    VBox colorDialogRoot = new VBox(new Label("Choose task color:"), colorPicker);

                    Alert colorDialog = new Alert(Alert.AlertType.CONFIRMATION);
                    colorDialog.setTitle("Change Color");
                    colorDialog.getDialogPane().setContent(colorDialogRoot);
                    colorDialog.showAndWait();
                }

                return null; // Возвращаем null, так как результат обработан внутри диалоговых окон
            });

            dialog.showAndWait();
        });

        return new HBox(taskLabel);
    }

    private String toRGBCode(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }

    public static void main(String[] args) {
        launch(args);
    }
}