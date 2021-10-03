module me.p3074098.gotcha {
    requires javafx.controls;
    requires javafx.fxml;


    opens me.p3074098.gotcha to javafx.fxml;
    exports me.p3074098.gotcha.main;
    opens me.p3074098.gotcha.main to javafx.fxml;
    exports me.p3074098.gotcha.controller;
    opens me.p3074098.gotcha.controller to javafx.fxml;
}