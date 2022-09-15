module com.kostkin.simplecloudclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens kostkin.kostkin.simpleCloudClient to javafx.fxml;
    exports kostkin.kostkin.simpleCloudClient;
}