package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
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
            System.err.println("[ERROR] addBoardgame()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteBoardgame(BoardgameModelMongo boardgame) {
        try {
            boardgameRepoMongoOp.delete(boardgame);
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] deleteBoardgame()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
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

                this.addBoardgame(boardgameToBeUpdated);
            }
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] updateBoardgameMongo()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    public List<BoardgameModelMongo> findRecentBoardgames(int limit, int skip) {
        List<BoardgameModelMongo> boardgames = null;
        try {
            Query query = new Query();
            query.with(Sort.by(Sort.Order.desc("yearPublished"), Sort.Order.asc("_id")));
            query.skip(skip).limit(limit);
            boardgames = mongoOperations.find(query, BoardgameModelMongo.class);
        } catch (Exception e) {
            System.err.println("[ERROR] findRecentBoardgames()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
        }
        return boardgames;
    }

    public List<BoardgameModelMongo> findBoardgamesStartingWith(String namePrefix, int limit, int skip) {
        List<BoardgameModelMongo> boardgames = null;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("boardgameName").regex("^" + namePrefix, "i")); // 'i' ignores lowercase and uppercase characters in the search
            query.with(Sort.by(Sort.Order.asc("boardgameName"), Sort.Order.asc("_id")));
            query.skip(skip).limit(limit);
            boardgames = mongoOperations.find(query, BoardgameModelMongo.class);
        } catch (Exception e) {
            System.err.println("[ERROR] findBoardgamesStartingWith()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
        }
        return boardgames;
    }

    public Optional<BoardgameModelMongo> findBoardgameById(String boardgameId) {
        Optional<BoardgameModelMongo> boardgame = Optional.empty();
        try {
            boardgame = boardgameRepoMongoOp.findById(boardgameId);
        } catch (Exception e) {
            System.err.println("[ERROR] findBoardgameById()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
        }
        return boardgame;
    }

    public List<String> getBoardgameTags() {
        try {
            return boardgameRepoMongoOp.findAllBoardgameNames();
        } catch (Exception e) {
            System.err.println("[ERROR] getBoardgameTags()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public List<String> getBoardgamesCategories() {
        List<String> categories = new ArrayList<>();
        try {
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.unwind("boardgameCategory"),        // Unwrapping categories
                    Aggregation.group("boardgameCategory").first("boardgameCategory").as("category"),
                    Aggregation.project("category")     // Return just the category values
            );

            AggregationResults<Document> results = mongoOperations.aggregate(       // Aggregation pipeline execution
                    aggregation,
                    "boardgames",
                    Document.class
            );

            results.getMappedResults().forEach(         // Getting the categories from the results
                    doc -> categories.add(doc.getString("category"))
            );

            categories.removeIf(String::isEmpty);       // Removing potential empty values
        } catch (Exception e) {
            System.err.println("[ERROR] getBoardgameCategories()@BoardgameDBMongo.java raised an exception: " + e.getMessage());
        }
        return categories;
    }

    public List<BoardgameModelMongo> findBoardgamesByCategory(String category, int limit, int skip) {
        List<BoardgameModelMongo> boardgameOfThisCategory = new ArrayList<>();
        try {
            Aggregation aggregation = Aggregation.newAggregation(
                    Aggregation.match(Criteria.where("boardgameCategory").is(category)),        // Filtering boardgames based on their category
                    Aggregation.skip(skip),             // Skipping first 'skip' results
                    Aggregation.limit(limit)        // Limiting to 'limit' results
            );

            boardgameOfThisCategory = mongoOperations.aggregate(        // Aggregation pipeline execution
                    aggregation,
                    "boardgames",
                    BoardgameModelMongo.class
            ).getMappedResults();
        } catch (Exception e) {
            System.err.println("[ERROR findBoardgamesByCategory()@BoardgameDBMongo.java raised an exception:" + e.getMessage());
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
