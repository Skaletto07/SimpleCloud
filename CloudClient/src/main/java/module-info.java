module com.kostkin.simpleCloudClient {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.kostkin.common;
    requires org.apache.logging.log4j;

    opens com.kostkin.simpleCloudClient to javafx.fxml;
    exports com.kostkin.simpleCloudClient;
}