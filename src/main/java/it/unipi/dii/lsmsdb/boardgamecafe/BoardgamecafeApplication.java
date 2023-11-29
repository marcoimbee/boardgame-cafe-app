package it.unipi.dii.lsmsdb.boardgamecafe;

//import it.unipi.dii.lsmsdb.phoneworld.model.ModelBean;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.GraphNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.PhoneNeo4j;
//import it.unipi.dii.lsmsdb.phoneworld.repository.neo4j.UserNeo4j;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameRepositoryNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserNeo4jDB;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.UserRepositoryNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserRepositoryMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.services.ServiceUser;

//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
//import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
//import javafx.application.Application;
//import javafx.stage.Stage;

import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

@SpringBootApplication  //extends Application
public class BoardgamecafeApplication {

    @Autowired
    Driver driver; //Used in version without Interface Repository

    @Autowired
    private UserNeo4jDB userNeo4jDB;

    @Autowired
    private BoardgameRepositoryNeo4j boardgameRepositoryNeo4j;
    @Autowired
    private UserRepositoryNeo4j userRepositoryNeo4j;
    @Autowired
    private UserRepositoryMongo mongoRepository;
    @Autowired
    private ServiceUser serviceUser;


    public static void main(String[] args)
    {
        SpringApplication.
                run(BoardgamecafeApplication.class, args);
    }

    //Annotation @EventListner: indicates to Spring that the
    //afterTheStart() method should be executed after the application has started.
    @EventListener(ApplicationReadyEvent.class)
    public void afterTheStart()
    {
        /* ------ Test Operations on BoardGameCafeDB ------*/

        String username = "whitekoala768";
        String username2 = "johnny.test30";
        String bordgameName ="Monopoly";
        String idUser = "655f83770b0a94c33a977526";
        String idUser2 = "865l9633f0l96v33a2569885";


        UserMongo userMongo = serviceUser.createUser(idUser2,
                username2, "giovanni_testemail@example.com",
                "24681012","Giovanni","Test","male",
                "IT","NotBanned",1974,06,11);

        UserNeo4j userNeo4j = new UserNeo4j(userMongo.getId(), userMongo.getUsername());

        System.out.println(" \n- New user added within MongoDB -\n");
        serviceUser.insertUser(userMongo, userNeo4j);

        //serviceUser.deleteUser(userMongo);
        //System.out.println(" \n- The user" + username2 + " DELETED from both MongoDB and Neo4j dbms -\n");


        // *************** MongoDB Operations Management ***************

        //System.out.println(" \n- Shown below are all users within MongoDB -\n");
        //mongoRepository.findAll().forEach(System.out::println);

        System.out.println("\n- Shown below is a specifc user into MongoDB filtered out by username -");
        System.out.println("- Reference USERNAME: " + username2 + "\n");
        System.out.println(mongoRepository.findByUsername(username2));


        // *************** Neo4jDB Operations Management ***************

        /*
        System.out.println(" \n- Shown below is one user and its followers list within Neo4jDB (By ID) -\n");
        try (var session = driver.session()){
            session.run("MATCH (u1:User {id: '655f83770b0a94c33a977526'})<-[:FOLLOWS]-(u2:User) RETURN DISTINCT u2.username as username").list().forEach(r ->{
                System.out.println((r.get("username")));
            });
        }
        */

        /*
        for(UserNeo4j users: userRepositoryNeo4j.findAll())
        {
            System.out.println("\n***** The User @" + users.getUsername() + " has these infos: *****\n");

            System.out.println(" " + "# List of ' BOARDGAMES ' added: \n");

            if (users.getBoardgames().isEmpty()) {
                System.out.println("    " + " - Empty List: Not Any Boardgames Added");
            }
            for (BoardgameNeo4j boardgames: users.getBoardgames()) {
                System.out.println("    " + " - Boardgame: " + boardgames.getName());
            }

            System.out.println("\n " + "# List of ' FOLLOWERS ' users: \n");

            if (users.getFollowers().isEmpty()) {
                System.out.println("    " + " - Empty List: Not Followed By Any Users");
            }
            for (UserNeo4j follower: users.getFollowers()) {
                System.out.println("    " + " - Follower: " + follower.getUsername());
            }

            System.out.println("\n " + "# List of ' FOLLOWING ' users: \n");

            if (users.getFollowedUsers().isEmpty()) {
                System.out.println("    " + " - Empty List: Not Following Any Users");
            }
            for (UserNeo4j follower: users.getFollowedUsers()) {
                System.out.println("    " + " - Follower: " + follower.getUsername());
            }
        }
        */

        //UserNeo4j user = userRepositoryNeo4j.findByUsername(username);
        //System.out.println(user);

        /*
        //Direct Test of the method Inside Repository Interface
        System.out.println(userRepositoryNeo4j.findFollowersByUsername(username));
        System.out.println(userRepositoryNeo4j.findUsersByBoardgameName(bordgameName));
        System.out.println(boardgameRepositoryNeo4j.findBoardgamesByUsername(username));

        //Indirect Test of the method Inside Repository Interface
        System.out.println(userNeo4jDB.getFollowing(username));
        */

        System.out.println("\n- Shown below is a specifc user into Neo4jDB filtered out by username -");
        System.out.println("- Reference USERNAME: " + username2 + "\n");
        System.out.println(userRepositoryNeo4j.findByUsername(username2));

    }
}
