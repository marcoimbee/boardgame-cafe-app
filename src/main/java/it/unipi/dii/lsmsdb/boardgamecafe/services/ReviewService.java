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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger logger = LoggerFactory.getLogger(ReviewService.class);

    @Transactional
    public boolean insertReview(ReviewModelMongo review,
                                BoardgameModelMongo boardgame,
                                UserModelMongo user) {
        try {
            if (!reviewMongoOp.addReview(review)) {
                throw new RuntimeException("Error while adding the review to the Reviews collection.");
            }
//            if (!addReviewToUser(user, review)) {
//                throw new RuntimeException("Error while adding the review to the Users collection. Rolling back...");
//            }
            if (!updateAvgRatingAndReviewCountAfterInsertion(boardgame, review)) {
                throw new RuntimeException("Error while adding the review to the Boardgames collection. Rolling back...");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] insertReview@ReviewService.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

//    private boolean addReviewToUser(UserModelMongo user, ReviewModelMongo review) {
//
//        //checkLastReviewUser(user, false);
//        user.addReview(review); //local object
//
//        if (!userMongoOp.addReviewInUserArray(user, review)) {
//            logger.error("Error in adding the review to the collection of users");
//            if (!reviewMongoOp.deleteReview(review)) {
//                logger.error("Error in deleting the review from the collection of reviews");
//            }
//            return false;
//        }
//        return true;
//    }

    private boolean updateAvgRatingAndReviewCountAfterInsertion(BoardgameModelMongo boardgame, ReviewModelMongo review)
    {
        boardgame.updateAvgRatingAndReviewCount(review.getRating());
        if (!boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame))  {
            logger.error("Error in updating the reviewCount and avgRating to the collection of boardgames");
            if (!reviewMongoOp.deleteReview(review)) {
                logger.error("Error in deleting the review from the collection of reviews");
            }
            return false;
        }
        return true;
    }

    @Transactional
    public boolean deleteReview(ReviewModelMongo selectedReview, UserModelMongo loggedUser) {
        // This method needs to delete a specific review from the following collections:
        //      - Reviews
        //      - Users
        //      - Boardgames
        try {
            if (!selectedReview.getUsername().equals(loggedUser.getUsername()))
                throw new RuntimeException("Permission denied.");

            if (!updateAvgRatingAndReviewCountAfterReviewDeletion(selectedReview))
                throw new RuntimeException("Failed to delete a review from the Boardgames collection");

//            if (!deleteReviewInUser(loggedUser, selectedReview)) {
//                throw new RuntimeException("Failed to delete a review from the Users collection");
//            }

            if (!reviewMongoOp.deleteReview(selectedReview)) {
                throw new RuntimeException("Failed to delete a review in the Reviews collection");
            }
        } catch (RuntimeException e) {
            System.out.println("[ERROR] deleteReview@ReviewService.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean updateAvgRatingAndReviewCountAfterReviewDeletion(ReviewModelMongo selectedReview) {
        Optional<BoardgameModelMongo> boardgameResult =
                boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

        if (boardgameResult.isEmpty())
            throw new RuntimeException("The referred boardgame does not exists in the database");

        BoardgameModelMongo referredBoardgame = boardgameResult.get();
        referredBoardgame.updateAvgRatingAfterReviewDeletion(selectedReview.getRating());


        if (!boardgameMongoOp.updateBoardgameMongo(referredBoardgame.getId(), referredBoardgame))//boardgameMongoOp.deleteReviewInBoardgameReviewsById(boardgameResult.get().getBoardgameName(), selectedReview.getId())) { // MongoDB deletion
           throw new RuntimeException("The referred boardgame doesn't have the selected review: " + selectedReview);

        return true;
    }

//    private boolean deleteReviewInUser(UserModelMongo user, ReviewModelMongo selectedReview) {      // MongoDB deletion and local deletion
//        if (userMongoOp.deleteReviewInUserReviewsById(user.getId(), selectedReview.getId())) {
//            if (user.deleteReview(selectedReview.getId()))
//                return true;
//            else {
//                System.out.println("[WARNING] The review was found in MongoDB but not locally");
//                return false;
//            }
//        }
//        throw new RuntimeException("The selected review was not present in MongoDB");
//    }

    @Transactional
    public boolean updateReview(ReviewModelMongo selectedReview, int oldRating) {
        try {
            if (!reviewMongoOp.updateReview(selectedReview.getId(), selectedReview)) {
                throw new RuntimeException("Error while updating a review in the Reviews collection");
            }
//            if (!updateReviewInUser(selectedReview)) {
//                throw new RuntimeException("Error while updating a review in Users collection");
//            }
            if (!updateAvgRatingAndReviewCountAfterReviewUpdate(selectedReview, oldRating)) {
                throw new RuntimeException("Error while updating a review in the Boardgames collection");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] updateReview@ReviewService.java raised an exception: " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean updateAvgRatingAndReviewCountAfterReviewUpdate(ReviewModelMongo selectedReview, int oldRating) {
        Optional<BoardgameModelMongo> boardgameResult =
                boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

        if (boardgameResult.isPresent()) {
            BoardgameModelMongo boardgame = boardgameResult.get();
            boardgame.updateAvgRatingAfterReviewUpdate(selectedReview.getRating(), oldRating);
            return boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame);
        }
        System.out.println("[WARNING] No boardgame named '" + selectedReview.getBoardgameName() + "' is present in the DB.");
        return false;
    }

//    public boolean updateReviewInUser(ReviewModelMongo selectedReview) {
//        Optional<GenericUserModelMongo> userResult =
//                userMongoOp.findByUsername(selectedReview.getUsername(), false);
//
//        if (userResult.isPresent()) {
//            GenericUserModelMongo genericUser = userResult.get();
//            String _classValue = genericUser.get_class();
//
//            if (!_classValue.equals("user")) {
//                System.out.println("[ERROR] The target user is not a common user.");
//                return false;
//            }
//            UserModelMongo user = (UserModelMongo) genericUser;
//
//            //Local object state management
//            if (user.deleteReview(selectedReview.getId())) {
//                user.addReview(selectedReview);
//            }
//            return userMongoOp.updateUser(user.getId(), user, "user");
//        }
//        System.out.println("[WARNING] No user named '" + selectedReview.getUsername() + "' is present in the DB.");
//        return false;
//    }

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
