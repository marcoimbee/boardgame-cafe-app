package it.unipi.dii.lsmsdb.boardgamecafe;
//Internal Packages
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.services.PostService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.UserService;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.ModelBean;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;

//JavaFX Components
import javafx.application.Application;
import javafx.stage.Stage;

//Spring Components
import org.bson.Document;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.List;
import java.util.Optional;


/*
// ############################## MAIN FOR GUI- BoardgamecafeApplication_Config ##############################
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
*/

// ****************************** MAIN NOT FOR GUI - SpringOnly_Config ******************************

@SpringBootApplication  //extends Application removed
public class BoardgamecafeApplication {

    @Autowired
    Driver driver; //Used in version without Interface Repository

    @Autowired
    private UserRepoMongo userRepoMongo;
    @Autowired
    private UserDBMongo userDBMongo;
    @Autowired
    private UserRepoNeo4j userRepositoryNeo4j;
    @Autowired
    private UserDBNeo4j userNeo4jDB;
    @Autowired
    private UserService serviceUser;
    @Autowired
    private PostService servicePost;
    @Autowired
    private PostRepoNeo4j postRepoNeo4j;
    @Autowired
    private PostDBNeo4j postDBNeo4j;
    @Autowired
    private PostRepoMongo postRepoMongo;
    @Autowired
    private PostDBMongo postDBMongo;
    @Autowired
    private BoardgameRepoNeo4j boardgameRepositoryNeo4j;
    @Autowired
    private BoardgameDBMongo boardgameDBMongo;

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
            // ------ Test Operations on BoardGameCafeDB ------

        //General variable used for hard-coded version (Into old test-code of this main)
        String username = "whitekoala768";
        String username2 = "johnny.test30";
        String bordgameName ="Monopoly";
        String idUser = "655f83770b0a94c33a977526";
        String idUser2 = "865l9633f0l96v33a2569885";


        // ************************** (Begin) New Test-Code Section **************************

        System.out.println("\n-----------------------LOADING-DATA----------------------------");

        System.out.println("\n- Shown below is a specific post that has higher likes into BoardGameCafe App -");
        System.out.println("- Data obtained from MongoDB filtered thanks to Neo4jDB relationship info.-");
        Optional<PostModelMongo> optionalPost = servicePost.showMostLikedPost();
        if (optionalPost.isPresent()) {
            // Estrai l'oggetto PostModelMongo dall'Optional container
            PostModelMongo post = optionalPost.get();

            // Così facendo posso accedere agli attributi dell'oggetto PostModelMongo
            String bestPostId = post.getId();
            String bestPostTitle = post.getTitle();
            String bestPostAuthorName = post.getUsername();
            List<CommentModelMongo> comments = post.getComments();
            int numComment = 0;
            int totalPostLikes = postDBNeo4j.findTotalLikesByPostID(bestPostId);

            System.out.println("\n-- Main Infos Best-Post extracted from Optional:");
            System.out.println("Post ID: " + bestPostId);
            System.out.println("Post Title: " + bestPostTitle);
            System.out.println("Post's Author Username: " + bestPostAuthorName);
            System.out.println("Total Likes: " + totalPostLikes);
            System.out.println("\nPost's Comments: ");
            if (comments.isEmpty()) {
                System.out.println("    " + " - Empty List: Not Any Comments Added");
            }
            for (CommentModelMongo comment: comments) {
                System.out.println("    " + " - "+ ++numComment +")"+" Comment's Author: " + comment.getUsername());
                System.out.println("    " + "              Body: " + comment.getText());
            }
            System.out.println("\nFull 'Optional' Raw Data-Structure Object:");
            System.out.println(optionalPost);
        } else {
            System.out.println("Nessun post con più like trovato.");
        }

        // Test del metodo findTopRatedBoardgamesPerYear
        System.out.println("\n- Top Rated Boardgames per Year -");
        try {
            Document topRatedBoardgames = boardgameDBMongo.
                    findTopRatedBoardgamesPerYear(10, 5);  // Ad esempio, minimo 10 recensioni e top 5 risultati
            System.out.println("\nResults from Aggregation:");
            System.out.println(topRatedBoardgames.toJson());
        } catch (Exception ex) {
            System.out.println("Error while fetching top-rated boardgames: " + ex.getMessage());
        }


        // ************************** (EndOf) New Test-Code Section **************************


        //UserModelMongo userMongo =
        //              serviceUser.createUser(username2, "giovanni_testemail@example.com",
        //                                      "24681012","Giovanni","Test","male",
        //                                      "IT","NotBanned",1974,06,11);

        //UserModelNeo4j userNeo4j = new UserModelNeo4j(userMongo.getId(), userMongo.getUsername());

        // System.out.println(" \n- New user added within MongoDB and Neo4j -\n");
        //serviceUser.insertUser(userMongo, userNeo4j);

        //serviceUser.deleteUser(userMongo);
        //System.out.println(" \n- The user" + username2 + " DELETED from both MongoDB and Neo4j dbms -\n");


        // *************** MongoDB Operations Management ***************

        //System.out.println(" \n- Shown below are all users within MongoDB -\n");
        //mongoRepository.findAll().forEach(System.out::println);

        //System.out.println("\n- Shown below is a specifc user into MongoDB filtered out by username -");
        //System.out.println("- Reference USERNAME: " + username2 + "\n");
        //System.out.println(mongoRepository.findByUsername(username2));


        // *************** Neo4jDB Operations Management ***************


            //for(UserNeo4j users: userRepositoryNeo4j.findAll())
            //{
            //System.out.println("\n***** The User @" + users.getUsername() + " has these infos: *****\n");

            //System.out.println(" " + "# List of ' BOARDGAMES ' added: \n");

            //if (users.getBoardgames().isEmpty()) {
                //System.out.println("    " + " - Empty List: Not Any Boardgames Added");
            //}
            //for (BoardgameNeo4j boardgames: users.getBoardgames()) {
                //System.out.println("    " + " - Boardgame: " + boardgames.getName());
            //}

            //System.out.println("\n " + "# List of ' FOLLOWERS ' users: \n");

            //if (users.getFollowers().isEmpty()) {
                //System.out.println("    " + " - Empty List: Not Followed By Any Users");
            //}
            //for (UserNeo4j follower: users.getFollowers()) {
                //System.out.println("    " + " - Follower: " + follower.getUsername());
            //}

            //System.out.println("\n " + "# List of ' FOLLOWING ' users: \n");

            //if (users.getFollowedUsers().isEmpty()) {
                //System.out.println("    " + " - Empty List: Not Following Any Users");
            //}
            //for (UserNeo4j follower: users.getFollowedUsers()) {
                //System.out.println("    " + " - Follower: " + follower.getUsername());
            //}
        //}
    }

}   //EOF Main SpringOnly Configuration
