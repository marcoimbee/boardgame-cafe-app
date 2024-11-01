package it.unipi.dii.lsmsdb.boardgamecafe.services;

import it.unipi.dii.lsmsdb.boardgamecafe.BoardgamecafeApplication; //To Retrieve model bean instance

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.BoardgameModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.BoardgameDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.ReviewDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms.UserDBMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.utils.Symbols;

import org.neo4j.cypherdsl.core.Use;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Component
public class ReviewService {

    @Autowired
    ReviewDBMongo reviewMongoOp;
    @Autowired
    BoardgameDBMongo boardgameMongoOp;
    @Autowired
    UserDBMongo userMongoOp;

    private final static Logger logger = LoggerFactory.getLogger(ReviewService.class);


    public boolean insertReview(ReviewModelMongo review,
                                BoardgameModelMongo boardgame,
                                UserModelMongo user) {
        try {
            if (!reviewMongoOp.addReview(review)) {
                logger.error("Error in adding the review to the collection of reviews");
                return false;
            }
            review = reviewMongoOp.findByUsernameAndBoardgameName(user.getUsername(),
                    boardgame.getBoardgameName()).get();

            if (!addReviewToUser(user, review)) {
                logger.error("Error in adding the review to the collection of users");
                return false;
            }
            if (!addReviewToBoardgame(boardgame, review)) {
                logger.error("Error in adding the review to the collection of boardgames");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean addReviewToUser(UserModelMongo user, ReviewModelMongo review) {

        //checkLastReviewUser(user, false);
        user.addReview(review);

        if (!userMongoOp.updateUser(user.getId(), user, "user")) {
            logger.error("Error in adding the review to the collection of users");
            if (!reviewMongoOp.deleteReviewById(review.getId())) {
                logger.error("Error in deleting the review from the collection of reviews");
            }
            return false;
        }
        return true;
    }

    public boolean addReviewToBoardgame(BoardgameModelMongo boardgame, ReviewModelMongo review) {
        // checkLastReviewBoardgame(boardgame, false);
        boardgame.addReview(review);

        if (!boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame))  {
            logger.error("Error in adding the review to the collection of boardgames");
            if (!reviewMongoOp.deleteReviewById(review.getId())) {
                logger.error("Error in deleting the review from the collection of reviews");
            }
            return false;
        }
        return true;
    }

    public boolean deleteReview(ReviewModelMongo selectedReview,
                                BoardgameModelMongo boardgame,
                                UserModelMongo user) {
        try {

            String reviewId = selectedReview.getId();

            if (!deleteReviewInBoardgame(boardgame, selectedReview, reviewId)) {
                logger.error("Error in deleting the review from the collection of boardgames");
                return false;
            }
            if (!deleteReviewInUser(user, selectedReview, reviewId)) {
                logger.error("Error in deleting the review from the collection of users");
                return false;
            }
            if (!reviewMongoOp.deleteReview(selectedReview)) {
                logger.error("Error in deleting the review from the collection of reviews");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean deleteReviewInBoardgame(ReviewModelMongo selectedReview) // Cancella sia da mongo che in locale
    {
        Optional<BoardgameModelMongo> boardgameResult =
                boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

        if (boardgameResult.isEmpty())
            throw new RuntimeException("deleteReviewInBoardgame Exception: The referred boardgame not exists in DB. Name: " + selectedReview.getBoardgameName());

        BoardgameModelMongo referredBoardgame = boardgameResult.get();
        if (boardgameMongoOp.deleteReviewInBoardgameReviewsById(boardgameResult.get().getBoardgameName(), selectedReview.getId())) // Cancella da Mongo
            if(referredBoardgame.deleteReview(selectedReview)) // Cancella da locale
                return true;
            else
            {
                System.out.println("deleteReviewInBoardgame Exception: The reviews was on Mongo but not in local");
                return false;
            }
        else
            throw new RuntimeException("The referred boardgame ON MONGO doesn't have this review -> ******\n" + selectedReview + "\n******\n");
    }

    public boolean deleteUserReview(UserModelMongo user, String reviewId) {

        if (user.getReviewInUser(reviewId) != null) {
            //checkLastReviewUser(user, true);
            user.deleteReview(reviewId);
            return userMongoOp.updateUser(user.getId(), user, "user");
        }
        return true;
    }

    public boolean deleteBoardgameReview(BoardgameModelMongo boardgame, String reviewId) {

        if (boardgame.getReviewInBoardgame(reviewId) != null) {
            //checkLastReviewBoardgame(boardgame, true);
            boardgame.deleteReview(reviewId);
            return boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame);
        }
        return true;
    }
    /*
        private void checkLastReviewBoardgame(BoardgameModelMongo boardgame, boolean isDelete) {

            int numReviews = boardgame.getReviews().size();
            if (numReviews == 50) {
                if (isDelete) {
                    List<ReviewModelMongo> oldReviews = reviewMongoOp.
                                           findOldReviews(boardgame.getBoardgameName(), true);
                    boardgame.getReviews().add(numReviews, oldReviews.get(0));
                } else {
                    boardgame.getReviews().remove(numReviews - 1);
                }
            }
        }

        private void checkLastReviewUser(UserModelMongo newUser, boolean isDelete) {

            int numReviews = newUser.getReviews().size();
            if (numReviews == 50) {
                if (isDelete) {
                    List<ReviewModelMongo> oldReviews =
                            reviewMongoOp.findOldReviews(newUser.getUsername(), false);

                    newUser.getReviews().add(numReviews, oldReviews.get(0));
                } else {
                    newUser.getReviews().remove(numReviews - 1);
                }
            }
        }
    */
    public boolean deleteReviewInBoardgame(BoardgameModelMongo boardgame,
                                           ReviewModelMongo selectedReview,
                                           String reviewId){
        if (boardgame == null)
        {
            Optional<BoardgameModelMongo> boardgameResult =
                    boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

            if (boardgameResult.isEmpty()) {
                return false;
            }
            BoardgameModelMongo newBoardgame = boardgameResult.get();
            return deleteBoardgameReview(newBoardgame ,reviewId);

        } else
            return deleteBoardgameReview(boardgame ,reviewId);
    }

    private String getReviewId(ReviewModelMongo review) {

        Optional<ReviewModelMongo> reviewResult =
                reviewMongoOp.findByUsernameAndBoardgameName( review.getUsername(),
                        review.getBoardgameName());
        if (reviewResult.isPresent()) {
            return reviewResult.get().getId();
        } else {
            logger.error("Review not found");
        }
        return "";
    }
    /*
        public ReviewModelMongo getSelectedReview(int counterPages, int tableIndex,
                                                  UserModelMongo user, BoardgameModelMongo boardagme,
                                                  List<ReviewModelMongo> reviews) {
            boolean isEmbedded = true;
            int index;

            ReviewModelMongo review;
            if (counterPages > 4) {
                index = tableIndex + (counterPages * 10 - 50);
                review = reviews.get(index);
                isEmbedded = false;
            } else {
                index = tableIndex + (counterPages * 10);
                if (boardagme == null) {
                    review = user.getReviews().get(index);
                } else {
                    review = boardagme.getReviews().get(index);
                }
            }
            if (review == null) {
                logger.error("Review null, not found ");
                return null;
            }
            BoardgamecafeApplication.getInstance().getModelBean().putBean(Symbols.IS_EMBEDDED, isEmbedded);
            return review;
        }
    */
    private boolean updateReviewInBoardgame(ReviewModelMongo selectedReview) {

        Optional<BoardgameModelMongo> boardgameResult =
                boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

        if (boardgameResult.isPresent()) {
            BoardgameModelMongo boardgame = boardgameResult.get();

            if (boardgame.deleteReview(selectedReview.getId())) {
                boardgame.addReview(selectedReview);
            }
            return boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame);
        }
        return false;
    }

    public boolean updateReviewInUser(ReviewModelMongo selectedReview) {
        Optional<GenericUserModelMongo> userResult =
                userMongoOp.findByUsername(selectedReview.getUsername());

        if (userResult.isPresent()) {
            GenericUserModelMongo genericUser = userResult.get();
            if (genericUser.get_class() != "user") {
                logger.error("Error: selected user is not a common user");
                return false;
            }
            UserModelMongo user = (UserModelMongo) genericUser;

            if (user.deleteReview(selectedReview.getId())) {
                user.addReview(selectedReview);
            }
            return userMongoOp.updateUser(user.getId(), user, "user");
        }
        return false;
    }

    public boolean updateReview(ReviewModelMongo selectedReview) {
        try {
            if (!reviewMongoOp.updateReview(selectedReview.getId(), selectedReview)) {
                logger.error("Error in updating the review in the collection of reviews");
                return false;
            }
            if (!updateReviewInUser(selectedReview)) {
                logger.error("Error in updating the review in the collection of users");
                return false;
            }
            if (!updateReviewInBoardgame(selectedReview)) {
                logger.error("Error in updating the review in the collection of Board Games");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}
