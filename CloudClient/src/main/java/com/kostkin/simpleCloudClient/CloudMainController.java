package com.kostkin.simpleCloudClient;

import com.kostkin.DaemonThreadFactory;
import com.kostkin.model.*;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudMainController implements Initializable {
    public ListView<String> clientView;
    public ListView<String> serverView;
    public TextField clientNewFileName;
    public TextField serverNewFileName;
    public AnchorPane signIn;
    public TextField login;
    public PasswordField password;

    private String currentDirectory;

    private Socket socket;

    private Network<ObjectDecoderInputStream, ObjectEncoderOutputStream> network;

    private boolean needReadMessages = true;

    private DaemonThreadFactory factory;

    public void downloadFile(ActionEvent actionEvent) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.outputStream().writeObject(new FileRequest(fileName));
    }

    public void selectFolder() {
        serverView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                String folder = serverView.getSelectionModel().getSelectedItem();
                try {
                    network.outputStream().writeObject(new ServerPathRequest(folder));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
            network = new Network<>(
                    new ObjectDecoderInputStream(socket.getInputStream()),
                    new ObjectEncoderOutputStream(socket.getOutputStream())
            );
            factory.getThread(this::readMessages, "cloud-client-read-thread")
                    .start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendToServer(ActionEvent actionEvent) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Path file = Path.of(currentDirectory).resolve(fileName);
        network.outputStream().writeObject(new FileMessage(file));
    }

    private void readMessages() {
        try {
            while (needReadMessages) {
                CloudMessage message = (CloudMessage) network.inputStream().readObject();
                if (message instanceof FileMessage fileMessage) {
                    Files.write(Path.of(currentDirectory).resolve(fileMessage.getFileName()), fileMessage.getBytes());
                    Platform.runLater(() -> fillView(clientView, getFiles(currentDirectory)));
                } else if (message instanceof ListMessage listMessage) {
                    Platform.runLater(() -> fillView(serverView, listMessage.getFiles()));
                } else if (message instanceof SignIn) {
                    waitAuth();
                }
                selectFolder();
            }
        } catch (Exception e) {
            System.err.println("Server off");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        needReadMessages = true;
        factory = new DaemonThreadFactory();
        initNetwork();
        setCurrentDirectory(System.getProperty("user.home"));
        fillView(clientView, getFiles(currentDirectory));
        clientView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                String selected = clientView.getSelectionModel().getSelectedItem();
                File selectedFile = new File(currentDirectory + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(currentDirectory + "/" + selected);
                }
            }
        });
    }

    private void setCurrentDirectory(String directory) {
        currentDirectory = directory;
        fillView(clientView, getFiles(currentDirectory));

    }

    private void fillView(ListView<String> view, List<String> data) {
        view.getItems().clear();
        view.getItems().addAll(data);
    }

    private List<String> getFiles(String directory) {
        File dir = new File(directory);
        if (dir.isDirectory()) {
            String[] list = dir.list();
            if (list != null) {
                List<String> files = new ArrayList<>(Arrays.asList(list));
                files.add(0, "..");
                return files;
            }
        }
        return List.of();
    }


    public void deleteFromClient(ActionEvent event) {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Path file = Path.of(currentDirectory, fileName);
        try {
            Files.delete(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Platform.runLater(() -> fillView(clientView, getFiles(currentDirectory)));
    }

    public void deleteFromServer(ActionEvent event) throws IOException {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        network.outputStream().writeObject(new DeleteMessage(fileName));
    }

    public void renameFileOnClient(ActionEvent event) throws IOException {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        Path source = Path.of(currentDirectory, fileName);
        String newFileNameText = clientNewFileName.getText().trim();
        if (!newFileNameText.isEmpty()) {
            Files.move(source, source.resolveSibling(newFileNameText));
            Platform.runLater(() -> fillView(clientView, getFiles(currentDirectory)));
        }
    }

    public void renameFileOnServer(ActionEvent event) throws IOException {
        String oldFileName = serverView.getSelectionModel().getSelectedItem();
        String newFileName = serverNewFileName.getText().trim();
        if (!newFileName.isEmpty()) {
            network.outputStream().writeObject(new RenameFile(oldFileName, newFileName));
        }
    }

    public void AuthButton(ActionEvent event) throws IOException {
        if (!login.getText().isEmpty() && !password.getText().isEmpty()) {
            network.outputStream().writeObject(new AuthMessage(login.getText(), password.getText()));
        }
    }

    public void waitAuth() {
        while (true) {
            try {
                CloudMessage message = (CloudMessage) network.inputStream().readObject();
                if (message instanceof ListMessage listMessage) {
                    signIn.setVisible(false);
                    Platform.runLater(() -> fillView(serverView, listMessage.getFiles()));
                    break;
                } else if (message instanceof AuthWrong) {
                    Platform.runLater(() -> showError("Неверные логин или пароль"));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }
    public static void showError(String error) {
        final Alert alert = new Alert(Alert.AlertType.ERROR, error, new ButtonType("OK", ButtonBar.ButtonData.OK_DONE));
        alert.setTitle("Ошибка!");
        alert.showAndWait();
    }
}
