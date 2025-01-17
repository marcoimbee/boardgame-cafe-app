package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.BasicDBObject;
import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.bson.Document;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class BoardgameDBMongo {
    private final static Logger logger = LoggerFactory.getLogger(BoardgameDBMongo.class);

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

//    public boolean updateBoardgameReviewsAfterAdminAction(String username, UserContentUpdateReason updateReason, List<ReviewModelMongo> userReviews) {
//        try {
//            if (updateReason == UserContentUpdateReason.DELETED_USER || updateReason == UserContentUpdateReason.BANNED_USER) {
//                Query query = Query.query(Criteria.where("reviews.username").is(username));
//
//                Update update = new Update();
//                if (updateReason == UserContentUpdateReason.DELETED_USER) {
//                    update.set("reviews.$.username", "[Deleted user]");
//                } else {
//                    update.set("reviews.$.username", "[Banned user]")
//                            .set("reviews.$.body", "[Banned user]");
//                }
//
//                mongoOperations.updateMulti(
//                        query,
//                        update,
//                        BoardgameDBMongo.class,
//                        "boardgames"
//                );
//            }
//
//            if (updateReason == UserContentUpdateReason.UNBANNED_USER) {
//                for (ReviewModelMongo review : userReviews) {
//                    ObjectId reviewObjectId = new ObjectId(review.getId());
//                    Query query = Query.query(Criteria.where("reviews._id").is(reviewObjectId));
//
//                    Update update = new Update();
//                    update.set("reviews.$.username", review.getUsername())
//                            .set("reviews.$.body", review.getBody());
//
//                    mongoOperations.updateFirst(
//                            query,
//                            update,
//                            BoardgameDBMongo.class,
//                            "boardgames"
//                    );
//                }
//            }
//
//            return true;
//        } catch(Exception ex) {
//            System.err.println("[ERROR] updateBoardgameReviewsAfterUserBanOrDeletion@BoardgameDBMongo.java raised an exception: " + ex.getMessage());
//            return false;
//        }
//    }

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

    public void updateReviewCount(String boardgameName) {
        // Aggregation pipeline per contare le reviews
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("boardgameName").is(boardgameName)),
                Aggregation.group("boardgameName").count().as("reviewCount")
        );

        // Esegui l'aggregazione sulla collection 'reviews'
        AggregationResults<Document> result = mongoOperations.aggregate(aggregation,
                "reviews", Document.class);
        Document aggregationResult = result.getUniqueMappedResult();

        // Recupera il conteggio delle reviews
        if (aggregationResult != null) {
            Integer reviewCount = aggregationResult.getInteger("reviewCount");

            // Aggiorna il campo reviewCount nella collection 'boardgames'
            Query query = new Query(Criteria.where("boardgameName").is(boardgameName));
            Update update = new Update();
            update.set("reviewCount", reviewCount);

            mongoOperations.updateFirst(query, update, "boardgames");
        }
    }

    public boolean setAvgRating(String boardgameId, Double avgRating)
    {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(boardgameId)),
                Aggregation.project().and("avgRating").as("newAvgRating")
        );

        Update update = new Update();
        update.set("avgRating", avgRating);

        UpdateResult result = mongoOperations.updateFirst(
                Query.query(Criteria.where("_id").is(boardgameId)),
                update,
                "boardgames"
        );

        return (result.getModifiedCount() > 0);
    }
}
