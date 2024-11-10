package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.CommentModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.UserContentUpdateReason;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import org.bson.types.ObjectId;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import org.bson.Document;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class BoardgameDBMongo {


    private final static Logger logger = LoggerFactory.getLogger(BoardgameDBMongo.class);

    @Autowired
    private BoardgameRepoMongo boardgameRepoMongoOp;
    @Autowired
    private MongoOperations mongoOperations;

    public BoardgameDBMongo() {}

    public boolean addBoardgameOld(BoardgameModelMongo boardgame) {
        boolean result = true;
        try {
            boardgameRepoMongoOp.save(boardgame);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public BoardgameModelMongo addBoardgame(BoardgameModelMongo boardgame)
    {
        try { return boardgameRepoMongoOp.save(boardgame); }
        catch (Exception e) { e.printStackTrace(); }
        return null;
    }

    public boolean deleteBoardgame(BoardgameModelMongo boardgame) {
        try {
            boardgameRepoMongoOp.delete(boardgame);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteReviewInBoardgameReviewsById(String boardgameName, String reviewId)
    {
        Query query = new Query();
        query.addCriteria(Criteria.where("boardgameName").is(boardgameName));

        Update update = new Update();
        update.pull("reviews", new BasicDBObject("_id", new ObjectId(reviewId)));

        UpdateResult result = mongoOperations.updateFirst(query, update, BoardgameModelMongo.class);

        return (result.getModifiedCount() > 0);
    }

    public Optional<BoardgameModelMongo> findBoardgameByName(String boardgameName) {
        Optional<BoardgameModelMongo> boardgame = Optional.empty();
        try {
            boardgame = boardgameRepoMongoOp.findByBoardgameName(boardgameName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boardgame;
    }

    public boolean updateBoardgameReviewsAfterAdminAction(String username, UserContentUpdateReason updateReason, List<ReviewModelMongo> userReviews) {
        try {
            if (updateReason == UserContentUpdateReason.DELETED_USER || updateReason == UserContentUpdateReason.BANNED_USER) {
                Query query = Query.query(Criteria.where("reviews.username").is(username));

                Update update = new Update();
                if (updateReason == UserContentUpdateReason.DELETED_USER) {
                    update.set("reviews.$.username", "[Deleted user]");
                } else {
                    update.set("reviews.$.username", "[Banned user]")
                            .set("reviews.$.body", "[Banned user]");
                }

                mongoOperations.updateMulti(
                        query,
                        update,
                        BoardgameDBMongo.class,
                        "boardgames"
                );
            }

            if (updateReason == UserContentUpdateReason.UNBANNED_USER) {
                for (ReviewModelMongo review : userReviews) {
                    ObjectId reviewObjectId = new ObjectId(review.getId());
                    Query query = Query.query(Criteria.where("reviews._id").is(reviewObjectId));

                    Update update = new Update();
                    update.set("reviews.$.username", review.getUsername())
                            .set("reviews.$.body", review.getBody());

                    mongoOperations.updateFirst(
                            query,
                            update,
                            BoardgameDBMongo.class,
                            "boardgames"
                    );
                }
            }

            return true;
        } catch(Exception ex) {
            System.err.println("[ERROR] updateBoardgameReviewsAfterUserBanOrDeletion@BoardgameDBMongo.java raised an exception: " + ex.getMessage());
            return false;
        }
    }

    public boolean updateBoardgameMongo(String id, BoardgameModelMongo newBoardgame) {
        try {
            Optional<BoardgameModelMongo> boardgame = boardgameRepoMongoOp.findById(id);
            if (boardgame.isPresent()) {

                BoardgameModelMongo boardgameToBeUpdated = boardgame.get();

                boardgameToBeUpdated.setBoardgameName(newBoardgame.getBoardgameName());
                boardgameToBeUpdated.setThumbnail(newBoardgame.getThumbnail());
                boardgameToBeUpdated.setImage(newBoardgame.getImage());
                boardgameToBeUpdated.setDescription(newBoardgame.getDescription());
                boardgameToBeUpdated.setYearPublished(newBoardgame.getYearPublished());
                boardgameToBeUpdated.setMinPlayers(newBoardgame.getMinPlayers());
                boardgameToBeUpdated.setMaxPlayers(newBoardgame.getMaxPlayers());
                boardgameToBeUpdated.setPlayingTime(newBoardgame.getPlayingTime());
                boardgameToBeUpdated.setMinAge(newBoardgame.getMinAge());
                boardgameToBeUpdated.setBoardgameCategory(newBoardgame.getBoardgameCategory());
                boardgameToBeUpdated.setBoardgameDesigner(newBoardgame.getBoardgameDesigner());
                boardgameToBeUpdated.setBoardgamePublisher(newBoardgame.getBoardgamePublisher());
                boardgameToBeUpdated.setReviews(newBoardgame.getReviews());

                this.addBoardgame(boardgameToBeUpdated); //Uso di save per aggiornare tutto il document
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public List<BoardgameModelMongo> findRecentBoardgames(int limit, int skip) {
        List<BoardgameModelMongo> boardgames = null;
        try {
            Query query = new Query();
            query.with(Sort.by(Sort.Order.desc("yearPublished"), Sort.Order.asc("_id")));
            query.skip(skip).limit(limit);
            boardgames = mongoOperations.find(query, BoardgameModelMongo.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boardgames;
    }


    //ToCheck
    public List<BoardgameModelMongo> findBoardgames(String name, String param, int limit, int skip) {
        List<BoardgameModelMongo> boardgames = new ArrayList<>();
        try {
            if (name.isEmpty()) {
                boardgames.addAll(findRecentBoardgames(limit, skip));
            } else if (param.equals("Name")){
                boardgames.addAll(boardgameRepoMongoOp.findByNameRegexOrderByYearPublicationDesc(name,
                        Sort.by(Sort.Direction.DESC, "yearPublished"))); }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boardgames;
    }

    public Document findTopRatedBoardgamesPerYear(int minReviews, int results) {
        // Step 1: Scomponi il campo delle recensioni
        UnwindOperation unwindOperation = unwind("reviews");

        // (Stage_1) Step 2: Raggruppa per anno di rilascio e per ID del gioco
        GroupOperation groupOperation = group("$yearPublished")
                .avg("$reviews.rating").as("avgRating")
                .count().as("numReviews")
                .first("boardgameName").as("name");

        // (Stage_2) Step 3: Filtra solo i giochi con un numero minimo di recensioni
        MatchOperation matchOperation = match(new Criteria("numReviews").gte(minReviews));

        // (Stage_3) Step 4: Proietta i risultati per ottenere il nome del gioco, l'anno e il numero di recensioni
        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("year")
                .andExpression("name").as("name")
                .andExclude("_id").andExpression("numReviews").as("reviews")
                .and(ArithmeticOperators.Round.roundValueOf("avgRating").place(1)).as("rating");

        // (Stage_4) Step 5: Ordina per punteggio medio e numero di recensioni in ordine decrescente
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "year","rating", "reviews"));

        // Step 6: Limita i risultati a un certo numero (es. i top risultati)
        LimitOperation limitOperation = limit(results);

        // Step 7: Definisci l'aggregazione completa
        Aggregation aggregation = newAggregation(unwindOperation, groupOperation, matchOperation, projectionOperation, sortOperation, limitOperation);

        // Step 8: Esegui l'aggregazione sulla collezione di giochi da tavolo
        AggregationResults<BoardgameModelMongo> result = mongoOperations.aggregate(aggregation, "boardgames", BoardgameModelMongo.class);

        // Step 9: Restituisci i risultati grezzi
        return result.getRawResults();
    }

    public Document findTopRatedBoardgames(int minReviews, int results) {
        // Step 1: Scomponi il campo delle recensioni
        UnwindOperation unwindOperation = unwind("reviews");

        // Step 2: Raggruppa per il nome del gioco (non usando l'_id)
        GroupOperation groupOperation = group("boardgameName")  // Raggruppa per nome del gioco
                .avg("$reviews.rating").as("avgRating")        // Calcola la media dei punteggi
                .count().as("numReviews")                     // Conta il numero di recensioni
                .first("yearPublished").as("year");

        MatchOperation matchOperation = match(new Criteria("numReviews").gte(minReviews));

        // Step 3: Proietta i risultati per ottenere il nome del gioco, il numero di recensioni e il punteggio medio
        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("name")
                .andExpression("year").as("year") // Usa _id per rappresentare il nome del gioco
                .andExpression("numReviews").as("reviews")       // Assegna il numero di recensioni
                .and(ArithmeticOperators.Round.roundValueOf("avgRating").place(1)).as("rating");  // Arrotonda il punteggio medio

        // Step 4: Ordina per punteggio medio (rating) e in caso di parità, per numero di recensioni (reviews)
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "rating")
                .and(Sort.by(Sort.Direction.DESC, "reviews"))
                .and(Sort.by(Sort.Direction.DESC, "year")));

        // Step 5: Limita i risultati a 1 per ottenere il gioco con il punteggio più alto
        LimitOperation limitOperation = limit(results);

        // Step 6: Definisci l'aggregazione completa
        Aggregation aggregation = newAggregation(unwindOperation, groupOperation, matchOperation, projectionOperation, sortOperation, limitOperation);

        // Step 7: Esegui l'aggregazione sulla collezione di giochi da tavolo
        AggregationResults<BoardgameModelMongo> result = mongoOperations.aggregate(aggregation, "boardgames", BoardgameModelMongo.class);

        // Step 8: Restituisci i risultati grezzi
        return result.getRawResults();
    }


    public Optional<BoardgameModelMongo> findBoardgameById(String boardgameId) {
        Optional<BoardgameModelMongo> boardgame = Optional.empty();
        try {
            boardgame = boardgameRepoMongoOp.findById(boardgameId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return boardgame;
    }

    public boolean addReviewInBoardgameArray(BoardgameModelMongo boardgame, ReviewModelMongo newReview)
    {
        Query query = new Query(Criteria.where("_id").is(boardgame.getId()));
        Update update = new Update().push("reviews", newReview);
        UpdateResult result = mongoOperations.updateFirst(query, update, BoardgameModelMongo.class);

        // Se almeno un documento è stato modificato, l'update è riuscito
        return result.getModifiedCount() > 0;
    }

    public List<String> getBoardgameTags() {
        try {
            return boardgameRepoMongoOp.findAllBoardgameNames();
        } catch (Exception ex) {
            ex.printStackTrace();
            return new ArrayList<>();
        }
    }
}