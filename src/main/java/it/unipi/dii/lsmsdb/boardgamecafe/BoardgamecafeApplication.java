package it.unipi.dii.lsmsdb.boardgamecafe;

//import it.unipi.dii.lsmsdb.phoneworld.model.ModelBean;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.GraphNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.PhoneNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.UserNeo4j;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.UserTest;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.GraphNeo4jDB;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserNeo4jDB;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.INFUserMongoDB;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ServiceUser;

//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;

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
    private ServiceUser serviceUser;

    private static final BoardgamecafeApplication singleton = new BoardgamecafeApplication();
    private ModelBean modelBean = new ModelBean();
    private GraphNeo4jDB graphNeo4j = new GraphNeo4jDB("bolt://localhost:7687",
                                                        "neo4j", "PhoneWorld");
    private UserNeo4jDB userNeo4j = new UserNeo4jDB(graphNeo4j);

    public static BoardgamecafeApplication getInstance() {
        return singleton;
    }
    public ModelBean getModelBean() {
        return modelBean;
    }
    public UserNeo4jDB getUserNeo4j() {
        return userNeo4j;
    }


    public static void main(String[] args)
    {
        SpringApplication.run(BoardgamecafeApplication.class, args);
    }

    //Annotation @EventListner: indicates to Spring that the
    //afterTheStart() method should be executed after the application has started.
    @EventListener(ApplicationReadyEvent.class)
    public void afterTheStart()
    {
        /* ------ Test Mongo Operations on BoardGameCafeDB ------*/

        String username1 = "whitekoala768";
        String username2 = "heavyladybug904";
        String idUser = "e076a482ec7643c0a9f01db0";

        UserTest user = serviceUser.createUser(idUser,
                "heavyladybug904", "dyon.zonnenberg@example.com",
                "123456","Dyon","Zonnenberg","male",
                "NL","NotBanned",1974,06,11);

        System.out.println(" \n- New user added within MongoDB -\n");
        serviceUser.insertUser(user);

        // - MongoDB Operations Management -
        System.out.println(" \n- Shown below are all users within MongoDB -\n");
        mongoRepository.findAll().forEach(System.out::println);

        System.out.println("\n- Shown below is a specifc user into MongoDB filtered out by username -");
        System.out.println("- Reference USERNAME: " + username2 + "\n");
        System.out.println(mongoRepository.findByUsername(username1));

        /*
        System.out.println("\n- Shown below is a specifc user into MongoDB filtered out by username -");
        System.out.println("- Reference USERNAME: " + username1 + "\n");
        System.out.println(mongoRepository.findByUsername(username1));
        */

        // - Neo4jDB Operations Management -
        System.out.println("\n- Shown below is a specifc user into Neo4jDB filtered out by ID -");
        System.out.println("- Reference ID: " + idUser + "\n");
        System.out.println(userNeo4j.findUserById(idUser));
    }
}
