package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms.BoardgameDBNeo4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Component
public class ReviewService {

    @Autowired
    ReviewDBMongo reviewMongoOp;
    @Autowired
    BoardgameDBMongo boardgameMongoOp;
    @Autowired
    BoardgameDBNeo4j boardgameDBNeo4j;
    @Autowired
    UserDBMongo userMongoOp;

    @Transactional
    public boolean insertReview(ReviewModelMongo review,
                                BoardgameModelMongo boardgame,
                                UserModelMongo user)
    {
        try {
            if (!reviewMongoOp.addReview(review)) {
                throw new RuntimeException("Error while adding the review to the Reviews collection.");
            }
            if (!updateAvgRatingAndReviewCountAfterInsertion(boardgame, review)) {
                if (!reviewMongoOp.deleteReview(review)) {
                    throw new RuntimeException("Error while removing review after updating avgRating in Boardgames collection. Rolling back...");
                }
                throw new RuntimeException("Error while adding the review to the Boardgames collection. Rolling back...");
            }
            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] insertReview()@ReviewService.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    private boolean updateAvgRatingAndReviewCountAfterInsertion(BoardgameModelMongo boardgame, ReviewModelMongo review) {
        boardgame.updateAvgRatingAndReviewCount(review.getRating());
        if (!boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame))  {
            System.err.println("[ERROR] Error while updating 'reviewCount' and 'avgRating' fields in MongoDB 'Boardgames' collection");
            if (!reviewMongoOp.deleteReview(review)) {
                System.err.println("[ERROR] Error while deleting the review from MongoDB's 'Reviews' collection");
            }
            return false;
        }
        return true;
    }

    @Transactional
    public boolean deleteReview(ReviewModelMongo selectedReview, UserModelMongo loggedUser) {
        try {
            if (!selectedReview.getUsername().equals(loggedUser.getUsername()))
                throw new RuntimeException("Permission denied.");

            if (!updateAvgRatingAndReviewCountAfterReviewDeletion(selectedReview))
                throw new RuntimeException("Failed to update a boardgame's fields after a review deletion");

            if (!reviewMongoOp.deleteReview(selectedReview)) {
                throw new RuntimeException("Failed to delete a review in the Reviews collection");
            }

            return true;
        } catch (RuntimeException e) {
            System.out.println("[ERROR] deleteReview()@ReviewService.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    private boolean updateAvgRatingAndReviewCountAfterReviewDeletion(ReviewModelMongo selectedReview) {
        Optional<BoardgameModelMongo> boardgameResult = boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

        if (boardgameResult.isEmpty()) {
            throw new RuntimeException("The boardgame does not exists in the database");
        }

        BoardgameModelMongo referredBoardgame = boardgameResult.get();
        referredBoardgame.updateAvgRatingAfterReviewDeletion(selectedReview.getRating());

        if (!boardgameMongoOp.updateBoardgameMongo(referredBoardgame.getId(), referredBoardgame)) {
            throw new RuntimeException("The boardgame doesn't have the selected review: " + selectedReview);
        }

        return true;
    }

    @Transactional
    public boolean updateReview(ReviewModelMongo selectedReview, int oldRating) {
        try {
            if (!reviewMongoOp.updateReview(selectedReview.getId(), selectedReview)) {
                throw new RuntimeException("Error while updating a review in MongoDB's 'Reviews' collection");
            }
            if (!updateAvgRatingAndReviewCountAfterReviewUpdate(selectedReview, oldRating)) {
                throw new RuntimeException("Error while updating a review in the MongoDB's 'Boardgames' collection");
            }

            return true;
        } catch (Exception e) {
            System.err.println("[ERROR] updateReview()@ReviewService.java raised an exception: " + e.getMessage());
            return false;
        }
    }

    private boolean updateAvgRatingAndReviewCountAfterReviewUpdate(ReviewModelMongo selectedReview, int oldRating) {
        Optional<BoardgameModelMongo> boardgameResult = boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

        if (boardgameResult.isPresent()) {
            BoardgameModelMongo boardgame = boardgameResult.get();
            boardgame.updateAvgRatingAfterReviewUpdate(selectedReview.getRating(), oldRating);
            return boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame);
        }

        System.err.println("[WARNING] No boardgame named '" + selectedReview.getBoardgameName() + "' is present in the DB.");
        return false;
    }

    public LinkedHashMap<BoardgameModelNeo4j, Double> getTopRatedBoardgamePerYear(int minReviews, int limit, int year) {
        List<Document> docList = (List<Document>) reviewMongoOp.getTopRatedBoardgamePerYear(minReviews, limit, year).get("results");
        LinkedHashMap<BoardgameModelNeo4j, Double> boardgamePairListForParamYear = new LinkedHashMap<>();

        if (!docList.isEmpty()) {
            List<Document> docTopRated = (List<Document>) docList.get(0).get("topGames");

            for (Document doc_boardgame : docTopRated) {
                String boardgameName = (String) doc_boardgame.get("name");
                Optional<BoardgameModelNeo4j> boardgameOptional = this.boardgameDBNeo4j.findByBoardgameName(boardgameName);
                Double rating = (Double)doc_boardgame.get("avgRating");
                boardgameOptional.ifPresent(boardgame -> boardgamePairListForParamYear.put(boardgame, rating));
            }
        }
        return boardgamePairListForParamYear;
    }
}
