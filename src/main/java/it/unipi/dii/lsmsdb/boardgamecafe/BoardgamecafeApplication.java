package it.unipi.dii.lsmsdb.boardgamecafe;

//import it.unipi.dii.lsmsdb.phoneworld.model.ModelBean;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.GraphNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.PhoneNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.UserNeo4j;

//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;

import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication  //extends Application
public class BoardgamecafeApplication {

    public static void main(String[] args)
    {
        SpringApplication.run(BoardgamecafeApplication.class, args);
    }

    /*
    private GraphNeo4jDB graphNeo4j = new GraphNeo4jDB("bolt://localhost:7687",
            "neo4j", "BoardGameCafe");
    private UserNeo4jDB userNeo4j = new UserNeo4jDB(graphNeo4j);
    private BoardgameNeo4jDB phoneNeo4j = new BoardgameNeo4jDB(graphNeo4j);
    private ModelBean modelBean = new ModelBean();
    protected ConfigurableApplicationContext springContext;
    protected StageManager stageManager;

    private static final BoardgamecafeApplication singleton = new BoardgamecafeApplication();

    public static BoardgamecafeApplication getInstance() {
        return singleton;
    }

    public ModelBean getModelBean() {
        return modelBean;
    }

    public UserNeo4jDB getUserNeo4j() {
        return userNeo4j;
    }

    public BoardgameNeo4jDB getPhoneNeo4j() {
        return phoneNeo4j;
    }

    @Override
    public void init() throws Exception {
        springContext = bootStrapSpringApp();
    }

    @Override
    public void start(Stage stage) throws Exception {
        stageManager = springContext.getBean(StageManager.class, stage);
        displayInitStage();

    }

    protected void displayInitStage() {
        stageManager.switchScene(FxmlView.UNUSER);
    }

    @Override
    public void stop() throws Exception {
        springContext.close();
    }

    private ConfigurableApplicationContext bootStrapSpringApp() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BoardgamecafeApplication.class);
        String[] args = getParameters().getRaw().toArray(String[]::new);
        return builder.run(args);
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
    */

}
