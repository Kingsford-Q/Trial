module com.example.trial {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics; 


    opens com.example.trial to javafx.fxml;
    exports com.example.trial;
}