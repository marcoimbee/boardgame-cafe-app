package it.unipi.dii.lsmsdb.boardgamecafe;
//Internal Packages

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.*;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.CommentModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.*;
import it.unipi.dii.lsmsdb.boardgamecafe.services.*;

//JavaFX Components

//Spring Components
import javafx.application.Application;
import javafx.stage.Stage;
import org.neo4j.driver.Driver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.EventListener;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Date;
import java.util.List;

import java.util.Optional;


// ############################## MAIN FOR GUI- BoardgamecafeApplication_Config ##############################

@SpringBootApplication
public class BoardgamecafeApplication extends Application {

    protected ConfigurableApplicationContext springContext;
    protected StageManager stageManager;

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
        stageManager.switchScene(FxmlView.GUESTPOSTS);
    }
    private ConfigurableApplicationContext bootStrapSpringAppContext() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BoardgamecafeApplication.class);
        String[] args = getParameters().getRaw().toArray(String[]::new);
        return builder.run(args);
    }
}



//// ****************************** MAIN NOT FOR GUI - SpringOnly_Config ******************************
//@SpringBootApplication  //extends Application removed
//public class BoardgamecafeApplication {
//
//    @Autowired
//    Driver driver; //Used in version without Interface Repository
//
//    @Autowired
//    private UserRepoMongo userRepoMongo;
//    @Autowired
//    private UserDBMongo userDBMongo;
//    @Autowired
//    private UserRepoNeo4j userRepositoryNeo4j;
//    @Autowired
//    private UserDBNeo4j userNeo4jDB;
//    @Autowired
//    private UserService serviceUser;
//    @Autowired
//    private PostService servicePost;
//    @Autowired
//    private BoardgameService serviceBoardgame;
//    @Autowired
//    private PostRepoNeo4j postRepoNeo4j;
//    @Autowired
//    private PostDBNeo4j postDBNeo4j;
//    @Autowired
//    private PostRepoMongo postRepoMongo;
//    @Autowired
//    private PostDBMongo postDBMongo;
//    @Autowired
//    private BoardgameRepoNeo4j boardgameRepositoryNeo4j;
//    @Autowired
//    private BoardgameDBMongo boardgameDBMongo;
//    @Autowired
//    private BoardgameDBNeo4j boardgameDBNeo4j;
//    @Autowired
//    private CommentService serviceComment;
//    @Autowired
//    private CommentDBMongo commentDBMongo;
//    @Autowired
//    private ReviewService serviceReview;
//    @Autowired
//    private ReviewDBMongo reviewDBMongoOps;
//    @Autowired
//    private ReviewRepoMongo reviewRepoMongoOps;
//
//    public static void main(String[] args)
//    {
//        SpringApplication.
//                run(BoardgamecafeApplication.class, args);
//    }
//
//    //Annotation @EventListner: indicates to Spring that the
//    //afterTheStart() method should be executed after the application has started.
//    @EventListener(ApplicationReadyEvent.class)
//    public void afterTheStart() {
////        // ------ Test Operations on BoardGameCafeDB ------
////
//////        //General variable used for hard-coded version (Into old test-code of this main)
//////        String username = "whitekoala768";
//////        String username2 = "johnny.test30";
//////        String bordgameName ="Monopoly";
//////        String idUser = "655f83770b0a94c33a977526";
//////        String idUser2 = "865l9633f0l96v33a2569885";
////          String email = "noah.lavoie@example.com"
////
////
////        // ************************** (Begin) New Test-Code Section **************************
////
////        System.out.println("\n\n-----------------------LOADING-DATA----------------------------");
////        System.out.println("\n\n");
//
//
//        // ************************** Queries Test-Code Section **************************
//        /*
//
//        System.out.println("\n- Shown below is a specific post that has higher likes into BoardGameCafe App -");
//        System.out.println("- Data obtained from MongoDB filtered thanks to Neo4jDB relationship info.-");
//        Optional<PostModelMongo> optionalPost = servicePost.showMostLikedPost();
//        if (optionalPost.isPresent()) {
//            // Estrai l'oggetto PostModelMongo dall'Optional container
//            PostModelMongo post = optionalPost.get();
//
//            // Così facendo posso accedere agli attributi dell'oggetto PostModelMongo
//            String bestPostId = post.getId();
//            String bestPostTitle = post.getTitle();
//            String bestPostAuthorName = post.getUsername();
//            List<CommentModelMongo> comments = post.getComments();
//            int numComment = 0;
//            int totalPostLikes = postDBNeo4j.findTotalLikesByPostID(bestPostId);
//
//            System.out.println("\n-- Main Infos Best-Post extracted from Optional:");
//            System.out.println("Post ID: " + bestPostId);
//            System.out.println("Post Title: " + bestPostTitle);
//            System.out.println("Post's Author Username: " + bestPostAuthorName);
//            System.out.println("Total Likes: " + totalPostLikes);
//            System.out.println("\nPost's Comments: ");
//            if (comments.isEmpty()) {
//                System.out.println("    " + " - Empty List: Not Any Comments Added");
//            }
//            for (CommentModelMongo comment: comments) {
//                System.out.println("    " + " - "+ ++numComment +")"+" Comment's Author: " + comment.getUsername());
//                System.out.println("    " + "              Body: " + comment.getText());
//            }
//            System.out.println("\nFull 'Optional' Raw Data-Structure Object:");
//            System.out.println(optionalPost);
//        } else {
//            System.out.println("Nessun post con più like trovato.");
//        }
//
//
//        // Test del metodo findCountriesWithMostUsers
//        System.out.println("\n- Countries With Most Users -");
//        try {
//            Document countriesMostUsers = userDBMongo.
//                    findCountriesWithMostUsers(10);  // valore per limitare i risultati
//            System.out.println("\nResults from Aggregation:");
//            System.out.println(countriesMostUsers.toJson());
//        } catch (Exception ex) {
//            System.out.println("Error while fetching countries: " + ex.getMessage());
//        }
//
//
//        // Test del metodo findTopRatedBoardgamesPerYear
//        System.out.println("\n- Top Rated Boardgames per Year -");
//        try {
//            Document topRatedBoardgames = boardgameDBMongo.
//                    findTopRatedBoardgamesPerYear(10, 10);  // Ad esempio, minimo 10 recensioni e top 5 risultati
//            System.out.println("\nResults from Aggregation:");
//            System.out.println(topRatedBoardgames.toJson());
//        } catch (Exception ex) {
//            System.out.println("Error while fetching top-rated boardgames: " + ex.getMessage());
//        }
//
//
//        // Test del metodo findTopRatedBoardgame (Based on highest score in its reviews)
//        System.out.println("\n- Top Rated Boardgames by highest score in its reviews -");
//        try {
//            Document topRatedBoardgame = boardgameDBMongo.
//                    findTopRatedBoardgames(15,10);
//            System.out.println("\nResults from Aggregation:");
//            System.out.println(topRatedBoardgame.toJson());
//        } catch (Exception ex) {
//            System.out.println("Error while fetching top-rated boardgame: " + ex.getMessage());
//        }
//
//
//        // Test del metodo findTopPostsByBoardgameName
//        // Versione con raggruppamento per titolo - ToCheck
//        System.out.println("\n- Top Posts by Boardgame Name ordered by comments number -");
//        try {
//            Document topPostsTagBased = postDBMongo.
//                    findTopPostsByBoardgameName("Catan",20);
//            System.out.println("\nResults from Aggregation:");
//            System.out.println(topPostsTagBased.toJson());
//        } catch (Exception ex) {
//            System.out.println("Error while fetching top-rated boardgame: " + ex.getMessage());
//        }
//
//
//        // Test del metodo findActiveUsersByReviews (MostActiveUsers)
//        System.out.println("************ MOST ACTIVE USERS (REVIEWS-BASED) ************");
//        //MongoDB Related
//        System.out.println("\n");
//        // Formato per le date
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
//        try {
//            // Convertire le stringhe delle date in oggetti di tipo Date
//            Date startDate = dateFormat.parse("1970-01-01");
//            Date endDate = dateFormat.parse("2022-12-31");
//
//            Document activeUsers  = userDBMongo.
//                    findActiveUsersByReviews(startDate,endDate,10);
//
//            System.out.println("- Results from Aggregation:");
//            System.out.println(activeUsers.toJson());
//
//        } catch (ParseException ex) {
//            System.out.println("Error parsing dates: " + ex.getMessage());
//        } catch (Exception ex) {
//            System.out.println("Error while fetching top-rated boardgame: " + ex.getMessage());
//        }
//        System.out.println("\n\n");
//
//
//        // Test del metodo suggestUsersByCommonBoardgamePosted (for New Users to follow)
//        System.out.println("************ USERS WITH COMMON POSTS BASED ON BOARDGAMES  ************");
//        //Neo4j Related
//        System.out.println("\n");
//        List<UserModelMongo> mySuggestedUsers = serviceUser.
//                suggestUsersByCommonBoardgamePosted("goldengoose951", 5);
//
//        if (mySuggestedUsers.isEmpty())
//            System.out.println("mySuggestedUsers vuota");
//        for(UserModelMongo suggestedUser : mySuggestedUsers)
//        {
//            System.out.println(suggestedUser.toString());
//        }
//        System.out.println("\n\n");
//
//
//        System.out.println("************ USERS WHO LIKED SAME POSTS ORDERED BY MESURE OF LATTER ************");
//        System.out.println("\n");
//        List<UserModelMongo> mySuggestedUsers = serviceUser.
//                suggestUsersByCommonLikedPosted("blackelephant814", 5);
//        if (mySuggestedUsers.isEmpty())
//            System.out.println("mySuggestedUsers vuota");
//        for(UserModelMongo suggestedUser : mySuggestedUsers)
//            System.out.println(suggestedUser.toString());
//        System.out.println("\n\n");
//
//
//        // Test del metodo suggestPostLikedByFollowedUsers (for Posts to suggest)
//        System.out.println("************ LIKED POST RESULTS ************");
//        //Neo4j Related
//        System.out.println("\n");
//        List<PostModelMongo> suggestedLikedPosts = servicePost.
//                suggestPostLikedByFollowedUsers("happybutterfly415", 5);
//
//        if (suggestedLikedPosts.isEmpty())
//            System.out.println("SuggestedLikedPosts List vuota");
//        for(PostModelMongo likedPost : suggestedLikedPosts)
//        {
//            List<CommentModelMongo> comments = likedPost.getComments();
//            //System.out.println("\n");
//            System.out.println("******* ToString *******: ");
//            System.out.println(likedPost);
//            System.out.println("************************");
//            /*System.out.println("Title: " + suggestedPost.getTitle());
//            System.out.println("Body: " + suggestedPost.getText());
//            System.out.println("\nPost's Comments: ");
//            if (comments.isEmpty()) {
//                System.out.println("    " + " - Empty List: Not Any Comments Added");
//            }
//            for (CommentModelMongo comment: comments) {
//                System.out.println("    " + " - " + " Comment's Author: " + comment.getUsername());
//                System.out.println("    " + "              Text: " + comment.getText());
//            }
//        }
//        System.out.println("\n\n");
//
//
//        // Test del metodo suggestPostLikedByFollowedUsers (for Posts to suggest)
//        System.out.println("************ COMMENTED POST RESULTS ************");
//        //Neo4j Related
//        System.out.println("\n");
//        List<PostModelMongo> suggestedCommentedPosts = servicePost.
//                suggestPostCommentedByFollowedUsers("redkoala794", 5);
//
//        if (suggestedCommentedPosts.isEmpty())
//            System.out.println("SuggestedPostsCommented List vuota");
//        for(PostModelMongo commentedPost : suggestedCommentedPosts)
//        {
//            List<CommentModelMongo> comments = commentedPost.getComments();
//            //System.out.println("\n");
//            System.out.println("******* ToString *******: ");
//            System.out.println(commentedPost);
//            System.out.println("************************");
//            /*System.out.println("Title: " + suggestedPost.getTitle());
//            System.out.println("Body: " + suggestedPost.getText());
//            System.out.println("\nPost's Comments: ");
//            if (comments.isEmpty()) {
//                System.out.println("    " + " - Empty List: Not Any Comments Added");
//            }
//            for (CommentModelMongo comment: comments) {
//                System.out.println("    " + " - " + " Comment's Author: " + comment.getUsername());
//                System.out.println("    " + "              Text: " + comment.getText());
//            }
//        }
//        System.out.println("\n");
//
//
//        // Test del metodo getBoardgamesWithPostsByFollowedUsers (NEO4J)
//        System.out.println("\n- SUGGESTED BOARDGAMES ABOUT WHICH USERS YOU FOLLOW HAVE POSTED -");
//        String testUsername = "redkoala794";
//        try {
//            System.out.println("\nResults: ");
//            List<BoardgameModelMongo> suggestedBoardgames = serviceBoardgame.
//                    suggestBoardgamesWithPostsByFollowedUsers(testUsername);
//            if (!suggestedBoardgames.isEmpty()) {
//                for (BoardgameModelMongo boardgame: suggestedBoardgames) {
//                    System.out.println(boardgame.toString());
//                }
//            } else {
//                System.out.println("Empty result set");
//            }
//        } catch (Exception ex) {
//            System.out.println("Error while executing getBoardgamesWithPostsByFollowedUsers: " + ex.getMessage());
//        }
//        System.out.println("\n\n");
//
//        // Test del metodo getBoardgamesWithPostsByFollowedUsers (NEO4J)
//        System.out.println("\n- SUGGESTED INFLUENCER USERS -");
//        try {
//            System.out.println("\nResults: ");
//            List<UserModelMongo> suggestedInfluencers = serviceUser.
//                    suggestInfluencerUsers(10, 50, 10, 5);
//            if (!suggestedInfluencers.isEmpty()) {
//                for (UserModelMongo influencer: suggestedInfluencers) {
//                    System.out.println(influencer.toString());
//                }
//            } else {
//                System.out.println("Empty result set");
//            }
//        } catch (Exception ex) {
//            System.out.println("Error while executing suggestInfluencerUsers(): " + ex.getMessage());
//        }
//        System.out.println("\n\n");   */
//
//
//        // ########################## Services Test-Code Section ##########################
//
//
////        System.out.println("\n************ BOARDGAME-SERVICE RESULTS ************");
////
////        // --- Test del metodo insertBoardgame() ---
////        String boardgameName = "Monopoly24";
////        String thumbnail = "https://c7.alamy.com/compit/bf9pym/gioco-di-monopoli-bf9pym.jpg";
////        String image = "https://c7.alamy.com/compit/bf9pym/gioco-di-monopoli-bf9pym.jpg";
////        String description = "This game takes its name from the economic concept of monopoly, or the domination of the market by a single seller";
////        int yearPublished = 2024; //da 2024 a 2025
////        int minPlayers = 2;
////        int maxPlayers = 6; //Da 6 a 8
////        int playingTime = 5;
////        int minAge = 8;
////        List<String> boardgameCategoryList = new ArrayList<>();
////        boardgameCategoryList.add("Society");
////        //boardgameCategoryList.add("Strategy"); //Nuova categoria
////        List<String> boardgameDesignerList = new ArrayList<>();
////        boardgameDesignerList.add("Elizabeth Magie");
////        boardgameDesignerList.add("Charles Darrow");
////        List<String> boardgamePublisherList = new ArrayList<>();
////        boardgamePublisherList.add("Hasbro");
//
//        //Creazione Boardgame
////        BoardgameModelMongo monopolyBoardgame = new
////                BoardgameModelMongo(boardgameName, thumbnail, image, description, yearPublished,
////                minPlayers, maxPlayers, playingTime, minAge, boardgameCategoryList, boardgameDesignerList, boardgamePublisherList);
//
////
////        System.out.println("\nResult of the Boardgame insertion operation: ");
////        boolean boardgameToBeInsert = serviceBoardgame.insertBoardgame(monopolyBoardgame);
////
////        if (boardgameToBeInsert) {
////            System.out.println("\n\nThe Operation has been correctly " +
////                    "performed both for MongoDB and Neo4j dbms.");
////        } else {
////            System.out.println("\n\nThe Operation has NOT been correctly " +
////                    "performed both for MongoDB and Neo4j dbms.");
////        }
////        System.out.println("\n\n");
//
//
////        // --- Test del metodo deleteBoardgame() ---
////        String monopolyBoardgameName = "Monopoly24";
////        String monopolyBoardgameId = "6722642d74e0a46fa1a7307b";
////        Optional<BoardgameModelMongo> boardgameFromMongo = boardgameDBMongo.
////                                                           findBoardgameById(monopolyBoardgameId);
////        if(boardgameFromMongo.isPresent()){
////            System.out.println("\nResult of the Boardgame Deletion operation: ");
////            BoardgameModelMongo boardgameToBeDeleted = boardgameFromMongo.get();
////
////            boolean deleteOpsResult = serviceBoardgame.deleteBoardgame(boardgameToBeDeleted);
////            if (deleteOpsResult) {
////                System.out.println("\n\nThe Operation has been correctly " +
////                                        "performed both for MongoDB and Neo4j dbms.");
////            } else {
////                System.out.println("\n\nThe Operation has NOT been correctly " +
////                                        "performed both for MongoDB and Neo4j dbms.");
////            }
////        } else {
////            System.out.println("\nBoardgame not found!");
////        }
////        System.out.println("\n\n");
//
////
////        // --- Test del metodo updateBoardgame() ---
////        if(boardgameFromMongo.isPresent()){
////            System.out.println("\nResult of the Boardgame Update operation: ");
////            BoardgameModelMongo boardgameToBeUpdated = boardgameFromMongo.get();
////            String boardgameToBeUpdatedId = boardgameToBeUpdated.getId();
////
////            BoardgameModelMongo monopolyBoardgameUpdate = new
////                    BoardgameModelMongo(boardgameToBeUpdatedId, boardgameName, thumbnail,
////                    image, description, yearPublished, minPlayers, maxPlayers, playingTime,
////                    minAge, boardgameCategoryList, boardgameDesignerList, boardgamePublisherList);
////
////            boolean updateOpsResult = serviceBoardgame.updateBoardgame(monopolyBoardgameUpdate);
////            if (updateOpsResult) {
////                System.out.println("\n\nThe Operation has been correctly " +
////                        "performed both for MongoDB and Neo4j dbms.");
////            } else {
////                System.out.println("\n\nThe Operation has NOT been correctly " +
////                        "performed both for MongoDB and Neo4j dbms.");
////            }
////        } else {
////            System.out.println("\nBoardgame not found!");
////        }
////        System.out.println("\n\n");
//
//
////        System.out.println("\n************ REVIEW-SERVICE RESULTS ************");
////
////
////        //Useful Variables for Review Creation
////        String existingBoardgameName = "7 Wonders";
////        String existingUsername = "tinymeercat901";
////        int rating = 4;
////        String body = "Gioco molto interessante (Modifica) -, con i miei amici ci siamo divertiti molto. Consigliato!";
////        Date dateOfReview = new Date();
//
////        //Step_1) Ottenere un UTENTE a cui far creare la recensione così da aggiungerla
////        //        alla sua lista di recensioni come "nuova recensione".
////        Optional<GenericUserModelMongo> userFromMongo = userDBMongo.findByUsername(existingUsername);
////
////        //Step_2) Ottenere il BOARDGAME a cui associare la recensione cosi da aggiungerla
////        //        alla sua lista di recensioni come "nuova recensione".
////        Optional<BoardgameModelMongo> boardgameFromMongo_2 = boardgameDBMongo.
////                                                            findBoardgameByName(existingBoardgameName);
////
////        // - Test del metodo insertReview()
////        if(userFromMongo.isPresent() && boardgameFromMongo_2.isPresent()){
////            System.out.println("\nResult of the Review Insert operation: ");
////
////            UserModelMongo userCreatorReview = (UserModelMongo) userFromMongo.get();
////            BoardgameModelMongo boardgameToBeReviewed = boardgameFromMongo_2.get();
////
////            //Step_3) Creare La Recensione
////            ReviewModelMongo newReview = new ReviewModelMongo(
////                                              existingBoardgameName,
////                                              existingUsername,
////                                              rating, body,
////                                              dateOfReview);
////
////            //Step_4) Inserire la recensione in MongoDB
////            boolean insertOpsResult = serviceReview.insertReview(newReview,
////                                                                 boardgameToBeReviewed,
////                                                                 userCreatorReview);
////            if (insertOpsResult) {
////                System.out.println("\n\nThe Operation has been correctly " +
////                        "performed both for MongoDB and Neo4j dbms.");
////            } else {
////                System.out.println("\n\nThe Operation has NOT been correctly " +
////                        "performed both for MongoDB and Neo4j dbms.");
////            }
////        } else {
////            System.out.println("\n User Creator or Boardgame to be reviewd not found!");
////        }
////        System.out.println("\n\n");
//
//
////        // --- Test del metodo updateReview() ---
////        String reviewToBeModifiedID = "6728f31c52e70a2f2b00a093";
////
////        System.out.println("\nResult of the Review Update operation: ");
////
////        ReviewModelMongo reviewUpdate = new ReviewModelMongo(
////                                            reviewToBeModifiedID,
////                                            existingBoardgameName,
////                                            existingUsername,
////                                            rating, body,
////                                            dateOfReview);
////
////        boolean updateOpsResult = serviceReview.updateReview(reviewUpdate);
////        if (updateOpsResult) {
////            System.out.println("\n\nThe Operation has been correctly " +
////                    "performed both for MongoDB and Neo4j dbms.");
////        } else {
////            System.out.println("\n\nThe Operation has NOT been correctly " +
////                    "performed both for MongoDB and Neo4j dbms.");
////        }
//
//
////        //- Test del metodo getWrittenPosts che dovrebbe ritornare un risultato basato sulle relazioni in neo4j -
////        //TO-BE-FIXED
////        Optional<UserModelNeo4j> userFromNeo4j = userNeo4jDB.findByUsername("whitelion758");
////        if (userFromNeo4j.isPresent()){
////            UserModelNeo4j userTest = userFromNeo4j.get();
////
////            List<PostModelNeo4j> writtenPosts = userTest.getWrittenPosts();
////
////            if (writtenPosts.isEmpty()){
////                System.out.println("\nLista Posts Vuota");
////            }
////            for (PostModelNeo4j post: writtenPosts) {
////
////                System.out.println(post);
////            }
////
////            //System.out.println(userTest);
////
////        } else {
////            System.out.println("\nError: User Not found into neo4j db");
////        }
//
//
//        // ************************** Inizio Test Delete comment **************************
//        /*
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
////        boolean serviceTest = serviceComment.addCommentToPost(
////                commentIntoCommentCollection, postTest);
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
//        */
//        // ************************** Fine Test Delete comment **************************
//
//
//        // ************************** Inizio Test Insert Post **************************
//        // 67226b0f590d5d341be06c53
////        PostModelMongo newPost = new PostModelMongo( "67226b0f590d5d341be06c53", "whitelion758", "Stu pe ndo!", "Gaetano mi ha detto che viviamo nel ghetto...","Carcassonne", new Date() , 0);
////        if (servicePost.deletePost(newPost))
////            System.out.println("DEL OK");
////        else
////            System.out.println("DEL NO");
//
////        newPost = servicePost.insertPost(newPost);
////        if (newPost != null)
////            System.out.println("Post inserito");
////        else
////            System.out.println("Post non inserito");
//
//        // ************************** Fine Test Insert Post **************************
//
//        // ************************** Inizio Test Like Post **************************
//
////        servicePost.likeOrDislikePost("whitelion758", "67226b0f590d5d341be06c53");
////        servicePost.likeOrDislikePost("whitelion758", "67226b0f590d5d341be06c53");
//        //servicePost.likeOrDislikePost("whitelion758", "67226b0f590d5d341be06c53");
//        //servicePost.likeOrDislikePost("whitelion758", "67226b0f590d5d341be06c53");
//
//        // ************************** Fine Test Like Post **************************
//
////        Optional<UserModelNeo4j> me = userNeo4jDB.findById("65a92ef66448dd901569533d"); //userNeo4jDB.findByUsername("whitelion758").get();
////        if (me.isEmpty())
////        {
////            System.out.println("User vuoto");
////            return;
////        }
////        System.out.println("Nome utente -> " + me.get().getUsername());
////        //PostModelNeo4j x = me.getPostWrittenByUser(me.getUsername());
////        //System.out.println("ID: " + x.getId());
////
////        List<PostModelNeo4j> writtenPost = me.get().getWrittenPosts();
////        if (writtenPost.isEmpty())
////            System.out.println("writtenPost vuoto");
////
////        List <UserModelNeo4j> followed = me.get().getFollowedUsers();
////        if (followed.isEmpty())
////            System.out.println("followed vuoto");
////
////        List <CommentModelNeo4j> comments_ = me.get().getWrittenComments();
////        if (comments_.isEmpty())
////            System.out.println("comments vuoto");
////
////
////        for (PostModelNeo4j post : writtenPost)
////        {
////            System.out.println("Post: " + post.getId());
////        }
//
////        // TO DO: Esegui il test dell'aggiunta ed eliminazione di una review
////        Optional<BoardgameModelMongo> bOptional = boardgameDBMongo.findBoardgameByName("Monopoly");
////        Optional<GenericUserModelMongo> uOptional = userDBMongo.findByUsername("smallsnake417");
////        ReviewModelMongo r = new ReviewModelMongo("Monopoly", "smallsnake417", 2, "Bello si, ma i soldi non sono veri!", new Date());
////        //Optional<ReviewModelMongo> rOptional = serviceReview.find
//
////        if (uOptional.isEmpty() || bOptional.isEmpty())
////        {
////            System.out.println("fallito");
////            return;
////        }
////        BoardgameModelMongo b = bOptional.get();
////        UserModelMongo u = (UserModelMongo)uOptional.get();
////
////        if (!serviceReview.insertReview(r, b, u))
////        {
////            System.out.println("Inserimento fallito");
////            return;
////        }
//
////        if (!serviceReview.deleteReview(r, u))
////        {
////            System.out.println("Cancellazione fallita");
////            return;
////        }
////        System.out.println("Eliminazione OK");
//
//        // ************************** (EndOf) New Test-Code Section **************************
//        //}
//
//
////// ************************** Inizio Test Insert comment **************************
////
////        PostModelMongo postTest = postDBMongo.findById("65a930a56448dd90156b31ff").get();
////        String postId = postTest.getId();
////        String username_test = "g.sferr";
////        String body_test = " commento verifica Refresh Grafico method";
////        Date timestamp = new Date();
////        CommentModelMongo commentTest = new CommentModelMongo(
////                postId,
////                username_test,
////                body_test, timestamp);
////
////        //Operazione che genera correttamente l'id del commento che spunterà poi nell'array dei commenti del post
////        CommentModelMongo commentIntoCommentCollection = commentDBMongo.addComment(commentTest);
////
////        Optional<UserModelNeo4j> userFromNeo = userNeo4jDB.findByUsername(commentIntoCommentCollection.getUsername());
////        if (userFromNeo.isPresent()){
////            UserModelNeo4j userNeo4j = userFromNeo.get();
////
////            boolean serviceTest = serviceComment.insertComment(commentIntoCommentCollection, postTest, userNeo4j);
////
////            if (serviceTest) {
////                System.out.println("\n- Status: OK! - Comment added and removed in post collection into mongo db");
////            } else {
////                System.out.println("Error during adding comment in post both for neo4j and mongo db");
////            }
////        }
////
////// ************************** Fine Test Delete comment **************************
//    }
//}   //EOF Main SpringOnly Configuration
