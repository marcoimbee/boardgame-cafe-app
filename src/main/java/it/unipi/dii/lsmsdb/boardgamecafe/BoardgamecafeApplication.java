package it.unipi.dii.lsmsdb.boardgamecafe;
//Internal Packages
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.FxmlView;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.view.StageManager;
//Spring Components
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameRepoMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameRepoNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.services.BoardgameService;
import javafx.application.Application;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.event.EventListener;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;


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
        stageManager.switchScene(FxmlView.WELCOMEPAGE);
    }
    private ConfigurableApplicationContext bootStrapSpringAppContext() {
        SpringApplicationBuilder builder = new SpringApplicationBuilder(BoardgamecafeApplication.class);
        String[] args = getParameters().getRaw().toArray(String[]::new);
        return builder.run(args);
    }
}

//@SpringBootApplication
//public class BoardgamecafeApplication {
//
//    @Autowired
//    private BoardgameDBMongo boardgameDBMongo;
//    @Autowired
//    private BoardgameDBNeo4j boardgameDBNeo4j;
//
//    public static void main(String[] args){ SpringApplication.run(BoardgamecafeApplication.class, args); }
//    @EventListener(ApplicationReadyEvent.class)
//    public void afterTheStart() {
//        List<BoardgameModelMongo> boardgamesFromMongo = boardgameDBMongo.
//                                                        findRecentBoardgames(20889, 0);
//        int updatedCount = 0; // Contatore per i giochi aggiornati
//        if (!boardgamesFromMongo.isEmpty()) {
//            for (BoardgameModelMongo boardgameMongo : boardgamesFromMongo) {
//                String mongoName = boardgameMongo.getBoardgameName();
//                String mongoImage = boardgameMongo.getImage();
//                String mongoDescription = boardgameMongo.getDescription();
//
//                // Truncate the description to a maximum of 50 words
//                String truncatedDescription = truncateDescription(mongoDescription, 50);
//
//                Optional<BoardgameModelNeo4j> optionalBoardgameNeo4j = boardgameDBNeo4j.
//                                                                       findByBoardgameName(mongoName);
//                if (optionalBoardgameNeo4j.isPresent()) {
//                    BoardgameModelNeo4j boardgameNeo4jGet = optionalBoardgameNeo4j.get();
//                    boardgameNeo4jGet.setImage(mongoImage);
//                    boardgameNeo4jGet.setDescription(truncatedDescription); // Aggiorna la descrizione con quella di Mongo
//
//                    boolean updateOpsResult = boardgameDBNeo4j.
//                                              updateBoardgameNeo4j(boardgameNeo4jGet.getId(),
//                                                                   boardgameNeo4jGet);
//                    if (updateOpsResult) {
//                        System.out.println("\nThe boardgame with name " + mongoName +
//                                " has been updated successfully in Neo4j.");
//                        updatedCount++;
//                    } else {
//                        System.out.println("\nFailed to update the boardgame with name " + mongoName +
//                                " in Neo4j. Please check the data or Neo4j connection.");
//                    }
//                }
//            }
//        } else {
//            System.out.println("\nNo boardgames found in MongoDB!");
//        }
//        // Stampa il numero totale di giochi aggiornati
//        System.out.println("\nTotal boardgames updated in Neo4j: " + updatedCount);
//        System.out.println("\n\n");
//    }
//
//    // Metodo per troncare la descrizione
//    private String truncateDescription(String description, int maxWords) {
//        String[] words = description.split("\\s+"); // Divide la descrizione in parole
//        if (words.length <= maxWords) {
//            return description; // Restituisce la descrizione originale se ha meno di maxWords
//        }
//        return String.join(" ", Arrays.copyOf(words, maxWords)) + "..."; // Restituisce le prime maxWords parole seguite da "..."
//    }
//}   //EOF Main SpringOnly Configuration