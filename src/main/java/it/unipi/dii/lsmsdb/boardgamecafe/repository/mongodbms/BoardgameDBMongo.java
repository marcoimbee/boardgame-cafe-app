package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BoardgameDBMongo {

    @Autowired
    private BoardgameRepoMongo boardgameRepoMongoOp;
    @Autowired
    private MongoOperations mongoOperations;

    public BoardgameDBMongo() {}

    public BoardgameModelMongo addBoardgame(BoardgameModelMongo boardgame) {
        try {
            return boardgameRepoMongoOp.save(boardgame);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    public boolean deleteReviewInBoardgameReviewsById(String boardgameName, String reviewId) {
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

    public boolean updateBoardgameMongo(String id, BoardgameModelMongo newBoardgame) {
        try {
            Optional<BoardgameModelMongo> boardgame = boardgameRepoMongoOp.findById(id);
            if (boardgame.isPresent()) {

                BoardgameModelMongo boardgameToBeUpdated = boardgame.get();

                boardgameToBeUpdated.setBoardgameName(newBoardgame.getBoardgameName());
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
                boardgameToBeUpdated.setAvgRating(newBoardgame.getAvgRating());
                boardgameToBeUpdated.setReviewCount(newBoardgame.getReviewCount());

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

    public List<BoardgameModelMongo> findBoardgamesStartingWith(String namePrefix, int limit, int skip) {
        List<BoardgameModelMongo> boardgames = null;
        try {
            Query query = new Query(); // ricordati che la 'i', serve per ignorare minuscole e maiuscole nella ricerca
            query.addCriteria(Criteria.where("boardgameName").regex("^" + namePrefix, "i"));
            query.with(Sort.by(Sort.Order.desc("yearPublished"), Sort.Order.asc("_id")));
            query.skip(skip).limit(limit);
            boardgames = mongoOperations.find(query, BoardgameModelMongo.class);
        }
        catch (Exception e) { e.printStackTrace(); }
        return boardgames;
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

    public boolean addReviewInBoardgameArray(BoardgameModelMongo boardgame, ReviewModelMongo newReview) {
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

    public List<String> getBoardgamesCategories() {
        List<String> categories = new ArrayList<>();
        try {
            // Costruzione della pipeline di aggregazione
            Aggregation aggregation = Aggregation.newAggregation(
                    // Decomposizione dell'array boardgameCategory
                    Aggregation.unwind("boardgameCategory"),
                    // Raggruppamento per ottenere valori univoci
                    Aggregation.group("boardgameCategory").first("boardgameCategory").as("category"),
                    // Proiezione per restituire solo i valori delle categorie
                    Aggregation.project("category")
            );

            // Esecuzione dell'aggregazione
            AggregationResults<Document> results = mongoOperations.aggregate(
                    aggregation,
                    "boardgames",
                    Document.class
            );

            // Estrazione delle categorie dai risultati
            results.getMappedResults().forEach(doc -> categories.add(doc.getString("category")));

            // Rimozione di eventuali valori vuoti
            categories.removeIf(String::isEmpty);
        } catch (Exception e) {
            System.out.println("Exception getBoardgamesCategories() -> " + e.getMessage());
        }
        return categories;
    }

    public List<BoardgameModelMongo> findBoardgamesByCategory(String category, int limit, int skip) {
        List<BoardgameModelMongo> boardgameOfThisCategory = new ArrayList<>();
        try {
            // Costruzione della pipeline di aggregazione
            Aggregation aggregation = Aggregation.newAggregation(
                    // Match per filtrare i boardgame in base alla categoria
                    Aggregation.match(Criteria.where("boardgameCategory").is(category)),
                    // Salto dei risultati iniziali
                    Aggregation.skip((long) skip),
                    // Limitazione del numero di risultati
                    Aggregation.limit(limit)
            );

            // Esecuzione della pipeline di aggregazione
            boardgameOfThisCategory = mongoOperations.aggregate(
                    aggregation,
                    "boardgames",
                    BoardgameModelMongo.class
            ).getMappedResults();
        } catch (Exception e) {
            System.out.println("Exception findBoardgamesByCategory() -> " + e.getMessage());
        }
        return boardgameOfThisCategory;
    }

    public boolean updateRatingAfterUserDeletion(String reviewedBoardgame, List<Integer> ratings) {
        try {
            BoardgameModelMongo boardgame = boardgameRepoMongoOp.findByBoardgameName(reviewedBoardgame).get();
            boardgame.updateAvgRatingAfterUserDeletion(ratings);
            this.updateBoardgameMongo(boardgame.getId(), boardgame);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] updateRatingAfterUserDeletion()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }
}
