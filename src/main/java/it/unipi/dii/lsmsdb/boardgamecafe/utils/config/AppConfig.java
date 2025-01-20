package it.unipi.dii.lsmsdb.boardgamecafe.utils.config;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import java.io.IOException;
import java.util.ResourceBundle;

/*
    This works as a configuration class for the application,
    and it's responsible for the creation and management of the various beans, included the StageManager instance
 */
@Configuration
public class AppConfig {

    @Autowired
    SpringFXMLLoader springFXMLLoader;

    @Bean
    public ResourceBundle resourceBundle() { return ResourceBundle.getBundle("Bundle"); }

    @Bean
    @Lazy(value = true)
    public StageManager stageManager(Stage stage) throws IOException {
        return new StageManager(springFXMLLoader, stage);
    }
}
