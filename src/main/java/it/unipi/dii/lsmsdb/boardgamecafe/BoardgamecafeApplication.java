package it.unipi.dii.lsmsdb.boardgamecafe;
//Internal Packages
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
//JavaFX Components
import javafx.application.Application;
import javafx.stage.Stage;
//Spring Components
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
//Others Components


@SpringBootApplication
public class BoardgamecafeApplication extends Application{

    protected ConfigurableApplicationContext springContext;
    protected StageManager stageManager;
    private ModelBean modelBean = new ModelBean();
    private static final BoardgamecafeApplication singleton = new BoardgamecafeApplication();
    public static BoardgamecafeApplication getInstance() {
        return singleton;
    }
    public ModelBean getModelBean() {
        return modelBean;
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void init() throws Exception {
        springContext = bootStrapSpringAppContext();
    }
    @Override
    public void start(Stage stage) throws Exception {
        stageManager = springContext.getBean(StageManager.class, stage);
        displayInitStage();
    }
    @Override
    public void stop() throws Exception {
        springContext.close();
    }

    //Useful Methods
    protected void displayInitStage() {
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }
    private ConfigurableApplicationContext bootStrapSpringAppContext() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BoardgamecafeApplication.class);
        String[] args = getParameters().getRaw().toArray(String[]::new);
        return builder.run(args);
    }
}
