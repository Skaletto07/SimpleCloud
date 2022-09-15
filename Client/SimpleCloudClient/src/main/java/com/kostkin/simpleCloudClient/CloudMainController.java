package com.kostkin.simpleCloudClient;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class CloudMainController implements Initializable {
    public ListView<String> clientView  ;
    public ListView<String> serverView;

    private String currentDirectory;

    private String currentDirectoryServer = "C:\\SpringLearning\\SimpleCloud\\server_files";

    private DataInputStream dis;

    private DataOutputStream dos;

    private Socket socket;

    private static final String SEND_FILE_COMMAND = "file";

    private static final String RECEIVE_FILE_COMMAND = "file_request";

    private static final String SERVER = "server";

    private static final String CLIENT = "client";

    private void initNetwork() {
        try {
            socket = new Socket("localhost", 8189);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendToServer(ActionEvent actionEvent) {
        String fileName = clientView.getSelectionModel().getSelectedItem();
        String filePath = currentDirectory + "/" + fileName;
        File file = new File(filePath);
        if (file.isFile()) {
            try {
                dos.writeUTF(SEND_FILE_COMMAND);
                dos.writeUTF(fileName);
                dos.writeLong(file.length());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = fis.readAllBytes();
                    dos.write(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                System.err.println("e=" + e.getMessage());
            }
        }
        refresh(SERVER);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        initNetwork();
        setCurrentDirectory(System.getProperty("user.home"));
        setCurrentDirectoryServer(currentDirectoryServer);
        fillView(clientView, getFiles(currentDirectory));
        fillView(serverView, getFiles(currentDirectoryServer));
        clientView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                String selected = clientView.getSelectionModel().getSelectedItem();
                File selectedFile = new File(currentDirectory + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectory(selectedFile.getAbsolutePath());
                }
            }
        });
        serverView.setOnMouseClicked(mouseEvent -> {
            if (mouseEvent.getClickCount() == 2) {
                String selected = serverView.getSelectionModel().getSelectedItem();
                File selectedFile = new File(currentDirectoryServer + "/" + selected);
                if (selectedFile.isDirectory()) {
                    setCurrentDirectoryServer(selectedFile.getAbsolutePath());
                }
            }
        });
    }

    private void setCurrentDirectory(String directory) {
        currentDirectory = directory;
        fillView(clientView, getFiles(currentDirectory));

    }

    private void setCurrentDirectoryServer(String directory) {
        currentDirectoryServer = directory;
        fillView(serverView, getFiles(currentDirectoryServer));

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

    public void sendToClient(ActionEvent actionEvent) {
        String fileName = serverView.getSelectionModel().getSelectedItem();
        String filePath = currentDirectoryServer + "/" + fileName;
        File file = new File(filePath);
        if (file.isFile()) {
            try {
                dos.writeUTF(RECEIVE_FILE_COMMAND);
                dos.writeUTF(fileName);
                dos.writeLong(file.length());
                try (FileInputStream fis = new FileInputStream(file)) {
                    byte[] bytes = fis.readAllBytes();
                    dos.write(bytes);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } catch (Exception e) {
                System.err.println("e=" + e.getMessage());
            }
        }
        refresh(CLIENT);
    }

    public void refresh(String forRefresh) {
        if (forRefresh.equals(SERVER)) {
        setCurrentDirectoryServer(currentDirectoryServer);
        } else if (forRefresh.equals(CLIENT))
            setCurrentDirectory(System.getProperty("user.home"));
    }
}
