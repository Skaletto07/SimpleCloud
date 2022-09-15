module com.kostkin.simplecloudclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.kostkin.simpleCloudClient to javafx.fxml;
    exports com.kostkin.simpleCloudClient;
}