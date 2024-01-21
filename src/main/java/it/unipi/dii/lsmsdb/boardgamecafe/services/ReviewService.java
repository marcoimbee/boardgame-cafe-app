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
            String reviewId = this.getReviewId(review);
            int rating = review.getRating();
            //String title = review.getTitle();
            String body = review.getBody();
            String boardgameName = review.getBoardgameName();
            String username = review.getUsername();

            if (!addReviewToUser(rating, username, body, boardgameName, reviewId, user, review)) {
                logger.error("Error in adding the review to the collection of users");
                return false;
            }
            if (!addReviewToBoardgame(rating, body, username, reviewId, boardgame, review)) {
                logger.error("Error in adding the review to the collection of boardgames");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private boolean addReviewToUser(int rating, String username, String body,
                                    String boardgameName, String reviewId,
                                    UserModelMongo user, ReviewModelMongo review) {

        ReviewModelMongo reviewUser =
                        new ReviewModelMongo.ReviewBuilder(rating, username, body,
                        new Date()).boardgameName(boardgameName).id(reviewId).build();

        checkLastReviewUser(user, false);
        user.addReview(reviewUser);

        if (!userMongoOp.updateUser(user.getId(), user, "user")) {
            logger.error("Error in adding the review to the collection of users");
            if (!reviewMongoOp.deleteReview(review)) {
                logger.error("Error in deleting the review from the collection of reviews");
            }
            return false;
        }
        return true;
    }

    public boolean addReviewToBoardgame(int rating, String body, String username,
                                    String reviewId, BoardgameModelMongo boardgame,
                                    ReviewModelMongo review) {

        ReviewModelMongo reviewBoardgame =
                        new ReviewModelMongo.ReviewBuilder(rating, username, body,
                        new Date()).username(username).id(reviewId).build();

        checkLastReviewBoardgame(boardgame, false);
        boardgame.addReview(reviewBoardgame);

        if (!boardgameMongoOp.updateBoardgameMongo(boardgame.getBoardgameId(), boardgame))  {
            logger.error("Error in adding the review to the collection of boardgames");
            if (!reviewMongoOp.deleteReview(review)) {
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
            boolean isEmbedded = (boolean) BoardgamecafeApplication.getInstance().
                                           getModelBean().getBean(Symbols.IS_EMBEDDED);
            String reviewId = selectedReview.getId();

            if (!deleteReviewInBoardgame(boardgame, selectedReview, reviewId, isEmbedded)) {
                logger.error("Error in deleting the review from the collection of boardgames");
                return false;
            }
            if (!deleteReviewInUser(user, selectedReview, reviewId, isEmbedded)) {
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

    private boolean deleteReviewInUser(UserModelMongo user,
                                       ReviewModelMongo selectedReview,
                                       String reviewId, boolean isEmbedded) {
        if (user == null) {

            String username = selectedReview.getUsername();
            if (username.equals("Deleted User")) {
                return true;
            }

            Optional<GenericUserModelMongo> userResult =
                                            userMongoOp.findByUsername(username);

            if (userResult.isEmpty()) {
                return false;
            }

            UserModelMongo newUser = (UserModelMongo) userResult.get();
            return deleteUserReview(newUser, reviewId);

        } else if (isEmbedded) {
            return deleteUserReview(user, reviewId);
        }
        return true;
    }

    public boolean deleteUserReview(UserModelMongo user, String reviewId) {

        if (user.getReviewInUser(reviewId) != null) {
            checkLastReviewUser(user, true);
            user.deleteReview(reviewId);
            return userMongoOp.updateUser(user.getId(), user, "user");
        }
        return true;
    }

    public boolean deleteBoardgameReview(BoardgameModelMongo boardgame, String reviewId) {

        if (boardgame.getReviewInBoardgame(reviewId) != null) {
            checkLastReviewBoardgame(boardgame, true);
            boardgame.deleteReview(reviewId);
            return boardgameMongoOp.updateBoardgameMongo(boardgame.getBoardgameId(), boardgame);
        }
        return true;
    }

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

    public boolean deleteReviewInBoardgame(BoardgameModelMongo boardgame,
                                       ReviewModelMongo selectedReview,
                                       String reviewId, boolean isEmbedded){
        if (boardgame == null)
        {
            Optional<BoardgameModelMongo> boardgameResult =
                    boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

            if (boardgameResult.isEmpty()) {
                return false;
            }
            BoardgameModelMongo newBoardgame = boardgameResult.get();
            return deleteBoardgameReview(newBoardgame ,reviewId);

        } else if (isEmbedded) {

            return deleteBoardgameReview(boardgame ,reviewId);
        }
        return true;
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

    private boolean updateReviewInBoardgame(int rating, String body,
                                            String username, String reviewId,
                                            String boardgameName) {

        Optional<BoardgameModelMongo> boardgameResult =
                                        boardgameMongoOp.findBoardgameByName(boardgameName);

        if (boardgameResult.isPresent()) {

            ReviewModelMongo newReview =
                    new ReviewModelMongo.ReviewBuilder(rating, username, body,
                    new Date()).username(username).id(reviewId).build();

            BoardgameModelMongo boardgame = boardgameResult.get();

            if (boardgame.deleteReview(reviewId)) {
                boardgame.addReview(newReview);
            }
            return boardgameMongoOp.updateBoardgameMongo(boardgame.getBoardgameId(), boardgame);
        }
        return false;
    }

    public boolean updateReviewInUser(int rating, String body,
                                      String boardgameName, String reviewId,
                                      UserModelMongo user) {

        String username = user.getUsername();

        if ((boolean) BoardgamecafeApplication.getInstance().
                getModelBean().getBean(Symbols.IS_EMBEDDED)) {

            ReviewModelMongo newReview =
                    new ReviewModelMongo.ReviewBuilder(rating, username, body,
                    new Date()).boardgameName(boardgameName).id(reviewId).build();

            if (user.deleteReview(reviewId)) {
                user.addReview(newReview);
            }
            return userMongoOp.updateUser(user.getId(), user, "user");
        }
        return false;
    }

    public boolean updateReview(ReviewModelMongo selectedReview,
                                UserModelMongo user, int rating,
                                Date dateOfReview, String body) {
        try {
            String reviewId = selectedReview.getId();
            String username = user.getUsername();
            String boardgameName = selectedReview.getBoardgameName();

            ReviewModelMongo newReview =
                    new ReviewModelMongo.ReviewBuilder(rating, username,
                    body, dateOfReview).username(username).boardgameName(boardgameName).build();

            if (!reviewMongoOp.updateReview(reviewId, newReview)) {
                logger.error("Error in updating the review in the collection of reviews");
                return false;
            }
            if (!updateReviewInUser(rating, body, boardgameName, reviewId, user)) {
                logger.error("Error in updating the review in the collection of users");
                return false;
            }
            if (!updateReviewInBoardgame(rating, body, username, reviewId, boardgameName)) {
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
