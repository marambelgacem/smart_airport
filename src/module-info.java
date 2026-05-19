module airport {
    requires javafx.controls;
    requires org.junit.jupiter.api;

    exports airport;
    exports airport.collection;
    exports airport.exception;
    exports airport.model.avion;
    exports airport.model.personnel;
    exports airport.model.service;
    exports airport.model.vol;

    opens airport.ui to javafx.graphics;
}
