module com.example.skuska {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.example.skuska to javafx.fxml;
    exports com.example.skuska;
}