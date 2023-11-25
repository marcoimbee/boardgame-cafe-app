package it.unipi.dii.lsmsdb.boardgamecafe;

//import it.unipi.dii.lsmsdb.phoneworld.model.ModelBean;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.GraphNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.PhoneNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.UserNeo4j;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserTest;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.INFUserNeo4jDB;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.INFUserMongoDB;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ServiceUser;

//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
//import javafx.application.Application;
//import javafx.stage.Stage;

import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.Optional;

@SpringBootApplication  //extends Application
public class BoardgamecafeApplication {

    @Autowired
    Driver driver;
    @Autowired
    private INFUserMongoDB mongoRepository;
    @Autowired
    private INFUserNeo4jDB neo4jRepository;
    @Autowired
    private ServiceUser serviceUser;

    public static void main(String[] args)
    {
        SpringApplication.run(BoardgamecafeApplication.class, args);
    }

    //Annotation @EventListner: indicates to Spring that the
    //afterTheStart() method should be executed after the application has started.
    @EventListener(ApplicationReadyEvent.class)
    public void afterTheStart()
    {
        /* ------ Test Operations on BoardGameCafeDB ------*/

        String username1 = "whitekoala768";
        String username2 = "heavyladybug904";
        String idUser = "e076a482ec7643c0a9f01db0";

        //UserTest userMongo = new UserTest();

        /*
        UserTest userMongo = serviceUser.createUser(idUser,
                "heavyladybug904", "dyon.zonnenberg@example.com",
                "123456","Dyon","Zonnenberg","male",
                "NL","NotBanned",1974,06,11);

        UserNeo4j userNeo4j = new UserNeo4j(userMongo.getId(), userMongo.getUsername());

        System.out.println(" \n- New user added within MongoDB -\n");
        serviceUser.insertUser(userMongo, userNeo4j);
        */

        // - MongoDB Operations Management -

        //System.out.println(" \n- Shown below are users within MongoDB -\n");
        //mongoRepository.findAll().forEach(System.out::println);
        //System.out.println(mongoRepository.findByUsername(username1));

        //System.out.println("\n- Shown below is a specifc user into MongoDB filtered out by username -");
        //System.out.println("- Reference USERNAME: " + username2 + "\n");
        //System.out.println(mongoRepository.findByUsername(username1));


        // - Neo4jDB Operations Management -

        //System.out.println(" \n- Shown below is one user within Neo4jDB -\n");
        //System.out.println(neo4jRepository.findByUsername(username1).stream().map(u->u.getUsername()).toList());

        System.out.println(" \n- Shown below are users within Neo4jDB (half of those) -\n");
        try (var session = driver.session()){
            session.run("MATCH (n:User) RETURN n.username as username LIMIT 25").list().forEach(r ->{
                System.out.println((r.get("username")));
            });
        }

    }
}
