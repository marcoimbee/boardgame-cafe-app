package it.unipi.dii.lsmsdb.boardgamecafe.utils.config;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.util.ResourceBundle;

/*
    SprigFXMLLoader is a personalized FXMLLoader that integrates with the injection of the dependencies of Spring.
    Allows the uploading of FXML files and to automatically inject the controller objects in the FXML components.
    This makes the dependencies management easier.
 */
@Component
public class SpringFXMLLoader {

    private final ResourceBundle resourceBundle;
    private final ApplicationContext context;

    @Autowired @Lazy
    public SpringFXMLLoader(ApplicationContext context, ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
        this.context = context;
    }

    public Parent load(String fxmlPath) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(context::getBean);
        loader.setResources(resourceBundle);
        loader.setLocation(getClass().getResource(fxmlPath));
        return loader.load();
    }
}
