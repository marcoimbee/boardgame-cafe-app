package it.unipi.dii.lsmsdb.boardgamecafe;
//Internal Packages
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.PostModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;

import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.services.BoardgameService;
import it.unipi.dii.lsmsdb.boardgamecafe.services.CommentService;
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
import org.bson.types.ObjectId;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.expression.ParseException;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import java.util.Optional;
import java.util.Optional;
import java.util.List;



// ############################## MAIN FOR GUI- BoardgamecafeApplication_Config ##############################
/*
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
    private BoardgameService serviceBoardgame;
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
    @Autowired
    private BoardgameDBNeo4j boardgameDBNeo4j;
    @Autowired
    private CommentService serviceComment;
    @Autowired
    private CommentDBMongo commentDBMongo;

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

        System.out.println("\n\n-----------------------LOADING-DATA----------------------------");
        System.out.println("\n\n");


        // ************************** Queries Test-Code Section **************************
        /*

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


        // Test del metodo findCountriesWithMostUsers
        System.out.println("\n- Countries With Most Users -");
        try {
            Document countriesMostUsers = userDBMongo.
                    findCountriesWithMostUsers(10);  // valore per limitare i risultati
            System.out.println("\nResults from Aggregation:");
            System.out.println(countriesMostUsers.toJson());
        } catch (Exception ex) {
            System.out.println("Error while fetching countries: " + ex.getMessage());
        }


        // Test del metodo findTopRatedBoardgamesPerYear
        System.out.println("\n- Top Rated Boardgames per Year -");
        try {
            Document topRatedBoardgames = boardgameDBMongo.
                    findTopRatedBoardgamesPerYear(10, 10);  // Ad esempio, minimo 10 recensioni e top 5 risultati
            System.out.println("\nResults from Aggregation:");
            System.out.println(topRatedBoardgames.toJson());
        } catch (Exception ex) {
            System.out.println("Error while fetching top-rated boardgames: " + ex.getMessage());
        }


        // Test del metodo findTopRatedBoardgame (Based on highest score in its reviews)
        System.out.println("\n- Top Rated Boardgames by highest score in its reviews -");
        try {
            Document topRatedBoardgame = boardgameDBMongo.
                    findTopRatedBoardgames(15,10);
            System.out.println("\nResults from Aggregation:");
            System.out.println(topRatedBoardgame.toJson());
        } catch (Exception ex) {
            System.out.println("Error while fetching top-rated boardgame: " + ex.getMessage());
        }


        // Test del metodo findTopPostsByBoardgameName
        // Versione con raggruppamento per titolo - ToCheck
        System.out.println("\n- Top Posts by Boardgame Name ordered by comments number -");
        try {
            Document topPostsTagBased = postDBMongo.
                    findTopPostsByBoardgameName("Catan",20);
            System.out.println("\nResults from Aggregation:");
            System.out.println(topPostsTagBased.toJson());
        } catch (Exception ex) {
            System.out.println("Error while fetching top-rated boardgame: " + ex.getMessage());
        }


        // Test del metodo findActiveUsersByReviews (MostActiveUsers)
        System.out.println("************ MOST ACTIVE USERS (REVIEWS-BASED) ************");
        //MongoDB Related
        System.out.println("\n");
        // Formato per le date
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // Convertire le stringhe delle date in oggetti di tipo Date
            Date startDate = dateFormat.parse("1970-01-01");
            Date endDate = dateFormat.parse("2022-12-31");

            Document activeUsers  = userDBMongo.
                    findActiveUsersByReviews(startDate,endDate,10);

            System.out.println("- Results from Aggregation:");
            System.out.println(activeUsers.toJson());

        } catch (ParseException ex) {
            System.out.println("Error parsing dates: " + ex.getMessage());
        } catch (Exception ex) {
            System.out.println("Error while fetching top-rated boardgame: " + ex.getMessage());
        }
        System.out.println("\n\n");


        // Test del metodo suggestUsersByCommonBoardgamePosted (for New Users to follow)
        System.out.println("************ USERS WITH COMMON POSTS BASED ON BOARDGAMES  ************");
        //Neo4j Related
        System.out.println("\n");
        List<UserModelMongo> mySuggestedUsers = serviceUser.
                suggestUsersByCommonBoardgamePosted("goldengoose951", 5);

        if (mySuggestedUsers.isEmpty())
            System.out.println("mySuggestedUsers vuota");
        for(UserModelMongo suggestedUser : mySuggestedUsers)
        {
            System.out.println(suggestedUser.toString());
        }
        System.out.println("\n\n");


        System.out.println("************ USERS WHO LIKED SAME POSTS ORDERED BY MESURE OF LATTER ************");
        System.out.println("\n");
        List<UserModelMongo> mySuggestedUsers = serviceUser.
                suggestUsersByCommonLikedPosted("blackelephant814", 5);
        if (mySuggestedUsers.isEmpty())
            System.out.println("mySuggestedUsers vuota");
        for(UserModelMongo suggestedUser : mySuggestedUsers)
            System.out.println(suggestedUser.toString());
        System.out.println("\n\n");


        // Test del metodo suggestPostLikedByFollowedUsers (for Posts to suggest)
        System.out.println("************ LIKED POST RESULTS ************");
        //Neo4j Related
        System.out.println("\n");
        List<PostModelMongo> suggestedLikedPosts = servicePost.
                suggestPostLikedByFollowedUsers("happybutterfly415", 5);

        if (suggestedLikedPosts.isEmpty())
            System.out.println("SuggestedLikedPosts List vuota");
        for(PostModelMongo likedPost : suggestedLikedPosts)
        {
            List<CommentModelMongo> comments = likedPost.getComments();
            //System.out.println("\n");
            System.out.println("******* ToString *******: ");
            System.out.println(likedPost);
            System.out.println("************************");
            /*System.out.println("Title: " + suggestedPost.getTitle());
            System.out.println("Body: " + suggestedPost.getText());
            System.out.println("\nPost's Comments: ");
            if (comments.isEmpty()) {
                System.out.println("    " + " - Empty List: Not Any Comments Added");
            }
            for (CommentModelMongo comment: comments) {
                System.out.println("    " + " - " + " Comment's Author: " + comment.getUsername());
                System.out.println("    " + "              Text: " + comment.getText());
            }
        }
        System.out.println("\n\n");


        // Test del metodo suggestPostLikedByFollowedUsers (for Posts to suggest)
        System.out.println("************ COMMENTED POST RESULTS ************");
        //Neo4j Related
        System.out.println("\n");
        List<PostModelMongo> suggestedCommentedPosts = servicePost.
                suggestPostCommentedByFollowedUsers("redkoala794", 5);

        if (suggestedCommentedPosts.isEmpty())
            System.out.println("SuggestedPostsCommented List vuota");
        for(PostModelMongo commentedPost : suggestedCommentedPosts)
        {
            List<CommentModelMongo> comments = commentedPost.getComments();
            //System.out.println("\n");
            System.out.println("******* ToString *******: ");
            System.out.println(commentedPost);
            System.out.println("************************");
            /*System.out.println("Title: " + suggestedPost.getTitle());
            System.out.println("Body: " + suggestedPost.getText());
            System.out.println("\nPost's Comments: ");
            if (comments.isEmpty()) {
                System.out.println("    " + " - Empty List: Not Any Comments Added");
            }
            for (CommentModelMongo comment: comments) {
                System.out.println("    " + " - " + " Comment's Author: " + comment.getUsername());
                System.out.println("    " + "              Text: " + comment.getText());
            }
        }
        System.out.println("\n");


        // Test del metodo getBoardgamesWithPostsByFollowedUsers (NEO4J)
        System.out.println("\n- SUGGESTED BOARDGAMES ABOUT WHICH USERS YOU FOLLOW HAVE POSTED -");
        String testUsername = "redkoala794";
        try {
            System.out.println("\nResults: ");
            List<BoardgameModelMongo> suggestedBoardgames = serviceBoardgame.
                    suggestBoardgamesWithPostsByFollowedUsers(testUsername);
            if (!suggestedBoardgames.isEmpty()) {
                for (BoardgameModelMongo boardgame: suggestedBoardgames) {
                    System.out.println(boardgame.toString());
                }
            } else {
                System.out.println("Empty result set");
            }
        } catch (Exception ex) {
            System.out.println("Error while executing getBoardgamesWithPostsByFollowedUsers: " + ex.getMessage());
        }
        System.out.println("\n\n");

        // Test del metodo getBoardgamesWithPostsByFollowedUsers (NEO4J)
        System.out.println("\n- SUGGESTED INFLUENCER USERS -");
        try {
            System.out.println("\nResults: ");
            List<UserModelMongo> suggestedInfluencers = serviceUser.
                    suggestInfluencerUsers(10, 50, 10, 5);
            if (!suggestedInfluencers.isEmpty()) {
                for (UserModelMongo influencer: suggestedInfluencers) {
                    System.out.println(influencer.toString());
                }
            } else {
                System.out.println("Empty result set");
            }
        } catch (Exception ex) {
            System.out.println("Error while executing suggestInfluencerUsers(): " + ex.getMessage());
        }
        System.out.println("\n\n");   */



        // ########################## Services Test-Code Section ##########################


        // Test del metodo insertBoardgame()
//
//        System.out.println("************ BOARDGAME-SERVICE RESULTS ************");
//        String boardgameName = "Monopoly24";
//        String thumbnail = "https://c7.alamy.com/compit/bf9pym/gioco-di-monopoli-bf9pym.jpg";
//        String image = "https://c7.alamy.com/compit/bf9pym/gioco-di-monopoli-bf9pym.jpg";
//        String description = "This game takes its name from the economic concept of monopoly, or the domination of the market by a single seller";
//        int yearPublished = 2024;
//        int minPlayers = 2;
//        int maxPlayers = 6;
//        int playingTime = 5;
//        int minAge = 8;
//        List<String> boardgameCategoryList = new ArrayList<>();
//        boardgameCategoryList.add("Society");
//        List<String> boardgameDesignerList = new ArrayList<>();
//        boardgameDesignerList.add("Elizabeth Magie");
//        boardgameDesignerList.add("Charles Darrow");
//        List<String> boardgamePublisherList = new ArrayList<>();
//        boardgamePublisherList.add("Hasbro");
//
//        //Creazione Boardgame
//        BoardgameModelMongo monopolyBoardgame = new
//                BoardgameModelMongo(boardgameName, thumbnail, image, description, yearPublished,
//                minPlayers, maxPlayers, playingTime, minAge, boardgameCategoryList, boardgameDesignerList, boardgamePublisherList);
//
//        System.out.println("\nResult of the Boardgame insertion operation: ");
//        boolean boardgameToBeInsert = serviceBoardgame.insertBoardgame(monopolyBoardgame);
//
//        if (boardgameToBeInsert) {
//            System.out.println("\n\nThe Boardgame has been correctly inserted into MongoDB and Neo4j dbms.");
//        } else {
//            System.out.println("\n\nThe Boardgame has NOT been correctly inserted into the MongoDB and Neo4j dbms.");
//        }
//        System.out.println("\n\n");

//
//        PostModelMongo postTest = postDBMongo.findById("65a930a56448dd90156b31ff").get();
//        String postId = postTest.getId();
//        String username_test = "author_test_2";
//        String body_test = " commento verifica deleteComment method";
//        Date timestamp = new Date();
//        CommentModelMongo commentTest = new CommentModelMongo(
//                postId,
//                username_test,
//                body_test, timestamp);
//
//        //Operazione che genera correttamente l'id del commento che spunterà poi nell'array dei commenti del post
//        CommentModelMongo commentIntoCommentCollection = commentDBMongo.addComment(commentTest);
//
//        boolean serviceTest = serviceComment.addCommentToPost(
//                commentIntoCommentCollection, postTest);
//
//        boolean serviceTest2 = serviceComment.deleteComment(
//                commentTest, postTest);
//
//        if(serviceTest2)
//        {
//            System.out.println("\n- Status: OK! - Comment added and removed in post collection into mongo db");
//        } else {
//            System.out.println("Error during adding comment in post both for neo4j and mongo db");
//        }
    }

}
