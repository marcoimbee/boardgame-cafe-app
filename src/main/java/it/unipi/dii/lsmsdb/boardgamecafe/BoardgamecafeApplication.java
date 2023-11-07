package it.unipi.dii.lsmsdb.boardgamecafe;

//import it.unipi.dii.lsmsdb.phoneworld.model.ModelBean;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.GraphNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.PhoneNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.UserNeo4j;

//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.UserTest;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.INFUserMongoDB;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ServiceUser;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.UserTest;
//import javafx.application.Application;
//import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

@SpringBootApplication  //extends Application
public class BoardgamecafeApplication {

    @Autowired
    private INFUserMongoDB mongoRepository;
    @Autowired
    private UserMongo userMongo;
    @Autowired
    private ServiceUser serviceUser;

    public static void main(String[] args)
    {
        SpringApplication.run(BoardgamecafeApplication.class, args);
    }

    //Annotation @EventListner: indicates to Spring that the
    // afterTheStart() method should be executed after the
    // application has started. If you remove the annotation,
    // the method will not be executed.
    // - Allows you to register a method as an "event handler",
    // in this case the method afterTheStart() handle the
    // "ApplicationReadyEvent" event. The latter will be generated
    // after the application has been started and all beans have been created.
    @EventListener(ApplicationReadyEvent.class)
    public void afterTheStart()
    {
        /* ------ Test Mongo Operations on BoardGameCafeDB ------*/

        String username1 = "whitekoala768";
        String username2 = "heavyladybug904";

        /*
        UserTest user = serviceUser.createUser("e076a482ec7643c0a9f01db0",
                "heavyladybug904", "dyon.zonnenberg@example.com",
                "123456","Dyon","Zonnenberg","male",
                "NL","NotBanned",1974,06,11);

        System.out.println(" \n- New user added within MongoDB -\n");
        serviceUser.insertUser(user);
        */
        System.out.println(" \n- Shown below are all users within MongoDB -\n");
        mongoRepository.findAll().forEach(System.out::println);

        System.out.println("\n- Shown below is a specifc user into MongoDB filtered out by username -");
        System.out.println("- Reference USERNAME: " + username1 + "\n");
        System.out.println(mongoRepository.findByUsername(username1));

        /*
        System.out.println("\n- Shown below is a specifc user into MongoDB filtered out by username -");
        System.out.println("- Reference USERNAME: " + username2 + "\n");
        System.out.println(mongoRepository.findByUsername(username2));
        */
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
