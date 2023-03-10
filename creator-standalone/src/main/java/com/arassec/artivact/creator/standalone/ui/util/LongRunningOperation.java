package com.arassec.artivact.creator.standalone.ui.util;

import com.arassec.artivact.creator.standalone.core.model.CreatorArtivact;
import com.arassec.artivact.creator.standalone.core.model.ArtivactCreatorException;
import com.arassec.artivact.creator.standalone.core.util.ProgressMonitor;
import com.arassec.artivact.creator.standalone.ui.event.EditorEvent;
import com.arassec.artivact.creator.standalone.ui.event.EditorEventType;
import com.arassec.artivact.creator.standalone.ui.event.SceneEvent;
import com.arassec.artivact.creator.standalone.ui.event.SceneEventType;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.springframework.context.ApplicationEventPublisher;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class LongRunningOperation implements Runnable {

    private final Dialog<ButtonType> progressDialog;

    private final ProgressMonitor progressMonitor;

    private final Operation operation;

    private final Operation finishedCallback;

    private final Operation cancelledCallback;

    private final ButtonType cancelDialogButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

    private final ApplicationEventPublisher applicationEventPublisher;

    private final CreatorArtivact activeCreatorArtivact;

    private boolean cancel = false;

    private final Label progressLabel;

    private TextArea notesTextArea;

    private boolean endedRegularly = true;

    public LongRunningOperation(Window window, ProgressMonitor progressMonitor, Operation operation,
                                Operation finishedCallback, Operation cancelledCallback,
                                ApplicationEventPublisher applicationEventPublisher, CreatorArtivact activeCreatorArtivact) {
        this.progressMonitor = progressMonitor;
        this.operation = operation;
        this.finishedCallback = finishedCallback;
        this.cancelledCallback = cancelledCallback;
        this.applicationEventPublisher = applicationEventPublisher;
        this.activeCreatorArtivact = activeCreatorArtivact;

        progressDialog = new Dialog<>();
        progressDialog.initStyle(StageStyle.UNDECORATED);
        progressDialog.initModality(Modality.APPLICATION_MODAL);
        progressDialog.initOwner(window);
        progressDialog.getDialogPane().getScene().getWindow().setOnCloseRequest(Event::consume);
        progressDialog.getDialogPane().setStyle("-fx-border-color: black;");

        progressDialog.getDialogPane().getButtonTypes().add(cancelDialogButton);
        progressDialog.getDialogPane().lookupButton(cancelDialogButton).setDisable(true);

        var contentBox = new VBox();

        progressLabel = new Label();
        var progressLabelPane = new StackPane();
        progressLabelPane.setPadding(new Insets(5, 5, 5, 5));
        progressLabelPane.setPrefWidth(400);
        progressLabelPane.setAlignment(Pos.BASELINE_LEFT);
        progressLabelPane.getChildren().add(progressLabel);
        contentBox.getChildren().add(progressLabelPane);

        if (activeCreatorArtivact != null) {
            notesTextArea = new TextArea();
            notesTextArea.setText(activeCreatorArtivact.getNotes());
            var notesBox = new VBox();
            notesBox.getChildren().add(new Label("Notes:"));
            notesBox.getChildren().add(notesTextArea);
            var notesTextAreaPane = new StackPane();
            notesTextAreaPane.setPadding(new Insets(10, 5, 5, 5));
            notesTextAreaPane.getChildren().add(notesBox);
            contentBox.getChildren().add(notesTextAreaPane);
        }

        progressDialog.getDialogPane().setContent(contentBox);

        applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.MODAL_OPENED, null, null));
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();

        Runnable progressUpdater = () -> {
            String progress = progressMonitor.getProgress();
            if (progress != null && progress.length() > 100) {
                progressLabel.setText(progress.substring(0, 400) + "...");
            } else {
                long currentTime = System.currentTimeMillis();
                progressLabel.setText(formatTime(currentTime - startTime) + progress);
            }
        };

        Platform.runLater(() -> {
            Optional<ButtonType> result = progressDialog.showAndWait();
            if (result.isPresent() && result.get() == cancelDialogButton) {
                cancel = true;
                if (!endedRegularly) {
                    applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.MODAL_CLOSED, null, null));
                }
            }
        });

        CompletableFuture<String> future = CompletableFuture.supplyAsync(operation::execute);
        while (!future.isDone() && !cancel) {
            try {
                //noinspection BusyWait
                Thread.sleep(500);
                Platform.runLater(progressUpdater);
                if (cancelledCallback != null) {
                    progressDialog.getDialogPane().lookupButton(cancelDialogButton).setDisable(false);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new ArtivactCreatorException("Interrupted during long-running operation!", e);
            }
        }

        if (cancel) {
            applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.MODAL_CLOSED, null, null));
            future.cancel(true);
            if (cancelledCallback != null) {
                cancelledCallback.execute();
            }
            return;
        }

        try {
            progressMonitor.setProgress(future.get());
            Platform.runLater(progressUpdater);
        } catch (ExecutionException e) {
            progressMonitor.setProgress(formatStackTrace(e));
            Platform.runLater(progressUpdater);
            progressDialog.getDialogPane().lookupButton(cancelDialogButton).setDisable(false);
            endedRegularly = false;
        } catch (InterruptedException e) {
            progressMonitor.setProgress(formatStackTrace(e));
            Platform.runLater(progressUpdater);
            progressDialog.getDialogPane().lookupButton(cancelDialogButton).setDisable(false);
            endedRegularly = false;
            Thread.currentThread().interrupt();
        } finally {
            Platform.runLater(finishedCallback::execute);
            if (endedRegularly) {
                applicationEventPublisher.publishEvent(new SceneEvent(SceneEventType.MODAL_CLOSED, null, null));
                if (activeCreatorArtivact != null) {
                    activeCreatorArtivact.setNotes(notesTextArea.getText());
                    applicationEventPublisher.publishEvent(new EditorEvent(EditorEventType.UPDATE_NOTES, -1));
                }
                Platform.runLater(progressDialog::close);
            }
        }
    }

    private String formatStackTrace(Exception exception) {
        try (var writer = new StringWriter()) {
            var printWriter = new PrintWriter(writer);
            exception.printStackTrace(printWriter);
            printWriter.flush();
            return writer.toString();
        } catch (IOException e) {
            throw new ArtivactCreatorException("Could not format StackTrace: " + exception.getMessage(), e);
        }
    }

    private String formatTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long secondsTotal = TimeUnit.MILLISECONDS.toSeconds(millis);
        long seconds = secondsTotal - (minutes * 60);
        return "("
                + String.format("%02d", minutes)
                + ":"
                + String.format("%02d", seconds)
                + ") ";
    }

}
