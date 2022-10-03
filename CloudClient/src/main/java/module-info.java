module com.kostkin.simpleCloudClient {
    requires javafx.controls;
    requires javafx.fxml;
    requires com.kostkin.common;
    requires io.netty.codec;

    opens com.kostkin.simpleCloudClient to javafx.fxml;
    exports com.kostkin.simpleCloudClient;
}