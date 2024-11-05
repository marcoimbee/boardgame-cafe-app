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
import org.springframework.transaction.annotation.Transactional;

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


    @Transactional
    public boolean insertReview(ReviewModelMongo review,
                                BoardgameModelMongo boardgame,
                                UserModelMongo user) {
        try {
            String usernameCreatorReview = user.getUsername();
            String boardgameToBeReviewdName = boardgame.getBoardgameName();

            if (!reviewMongoOp.addReview(review)) {
                throw new RuntimeException("\nError in adding the review to the collection of Reviews.");
            }

            //Recupero la review appena aggiunta alla collection Reviews considerando
            // il gioco a cui fa riferimento l'utente che l'ha creata
            Optional<ReviewModelMongo> reviewFromMongo = reviewMongoOp.
                                       findByUsernameAndBoardgameName(
                                               usernameCreatorReview,
                                               boardgameToBeReviewdName);

            if(reviewFromMongo.isPresent()){

                ReviewModelMongo newReview = reviewFromMongo.get();

                if (!addReviewToUser(user, newReview)) {
                    throw new RuntimeException("\nError in adding the review to the collection of Users. " +
                            "Rollabck performed for review in Reviews Collection into MongoDB");
                }
                if (!addReviewToBoardgame(boardgame, newReview)) {
                    throw new RuntimeException("\nError in adding the review to the collection of Boardgames. " +
                            "Rollabck performed for review in Reviews Collection into MongoDB");
                }
                System.out.println("\nNew Review ID: " + newReview.getId());
                System.out.println("\nReview inserted into MongoDB dbms for Users, Reviews and Boardgames collections.");

            } else {
                throw new RuntimeException("\nReview created by " + usernameCreatorReview +" " +
                        "for te Boardgame -> " + boardgameToBeReviewdName + " not found!");
            }

        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean addReviewToUser(UserModelMongo user, ReviewModelMongo review) {

        //checkLastReviewUser(user, false);
        user.addReview(review); //local object

        if (!userMongoOp.addReviewInUserArray(user, review)) {
            logger.error("Error in adding the review to the collection of users");
            if (!reviewMongoOp.deleteReview(review)) {
                logger.error("Error in deleting the review from the collection of reviews");
            }
            return false;
        }
        return true;
    }

    public boolean addReviewToBoardgame(BoardgameModelMongo boardgame, ReviewModelMongo review) {

        // checkLastReviewBoardgame(boardgame, false);
        boardgame.addReview(review);

        if (!boardgameMongoOp.addReviewInBoardgameArray(boardgame, review))  {
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

    private boolean deleteReviewInUser(UserModelMongo user,
                                       ReviewModelMongo selectedReview,
                                       String reviewId) {
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

        } else
            return deleteUserReview(user, reviewId);
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

    @Transactional
    public boolean updateReview(ReviewModelMongo selectedReview) {
        try {
            if (!reviewMongoOp.updateReview(selectedReview.getId(), selectedReview)) {
                throw new RuntimeException("\nError in updating the review in the collection of reviews");
            }
            if (!updateReviewInUser(selectedReview)) {
                throw new RuntimeException("\nError in updating the review in the collection of users");
            }
            if (!updateReviewInBoardgame(selectedReview)) {
                throw new RuntimeException("\nError in updating the review in the collection of Board Games");
            }
        } catch (Exception e) {
            System.err.println("[ERROR] " + e.getMessage());
            return false;
        }
        return true;
    }

    private boolean updateReviewInBoardgame(ReviewModelMongo selectedReview) {

        Optional<BoardgameModelMongo> boardgameResult =
                boardgameMongoOp.findBoardgameByName(selectedReview.getBoardgameName());

        if (boardgameResult.isPresent()) {
            BoardgameModelMongo boardgame = boardgameResult.get();

            //Local object state management
            if (boardgame.deleteReview(selectedReview.getId())) {
                boardgame.addReview(selectedReview);
            }

            return boardgameMongoOp.updateBoardgameMongo(boardgame.getId(), boardgame);

            //******************************************************************************************
            // Valutare l'uso di questa variante, potrebbe non essere necessario fare delete/add nell'array
            // dell'oggetto locale dato che si andrebbe ad agire direttamente su MongoDB

//            //delete implementata da fra - pullare
//            if (!boardgameMongoOp.deleteReviewInBoardgameArray(boardgame, selectedReview)) {
//
//                return boardgameMongoOp.addReviewInBoardgameArray(boardgame, selectedReview);
//            }
            //******************************************************************************************
        }
        System.out.println("\nError: There is not present a bordgame with the name: " +
                              boardgameResult.get().getBoardgameName());
        return false;
    }

    public boolean updateReviewInUser(ReviewModelMongo selectedReview) {
        Optional<GenericUserModelMongo> userResult =
                userMongoOp.findByUsername(selectedReview.getUsername());

        if (userResult.isPresent()) {
            GenericUserModelMongo genericUser = userResult.get();
            String _classValue = genericUser.get_class();

            if (!_classValue.equals("user")) {
                logger.error("Error: selected user is not a common user");
                return false;
            }
            UserModelMongo user = (UserModelMongo) genericUser;

            //Local object state management
            if (user.deleteReview(selectedReview.getId())) {
                user.addReview(selectedReview);
            }
            return userMongoOp.updateUser(user.getId(), user, "user");

            //******************************************************************************************
            // Valutare l'uso di questa variante, potrebbe non essere necessario fare delete/add nell'array
            // dell'oggetto locale dato che si andrebbe ad agire direttamente su MongoDB

//            //delete implementata da fra - pullare
//            if (!userMongoOp.deleteReviewInUserArray(user, selectedReview)) {
//
//                return userMongoOp.addReviewInUserArray(user, selectedReview);
//            }
            //******************************************************************************************
        }
        System.out.println("\nError: There is not present a User with the username: " +
                              userResult.get().getUsername());
        return false;
    }


    // BEGIN - ************************* To_Check *************************

    // Valutare l'uso di questi 2 metodi: utili per la gestione automatica della numerosità delle
    // reviews all'interno dell'array di user e boardgame - potrebbe tornare utile anche per la grafica
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
    // END - ************************* To_Check *************************



//    //TO_CHECK -> GUI
//    public ReviewModelMongo getSelectedReview(int counterPages, int tableIndex,
//                                              UserModelMongo user, BoardgameModelMongo boardagme,
//                                              List<ReviewModelMongo> reviews) {
//        boolean isEmbedded = true;
//        int index;
//
//        ReviewModelMongo review;
//        if (counterPages > 4) {
//            index = tableIndex + (counterPages * 10 - 50);
//            review = reviews.get(index);
//            isEmbedded = false;
//        } else {
//            index = tableIndex + (counterPages * 10);
//            if (boardagme == null) {
//                review = user.getReviews().get(index);
//            } else {
//                review = boardagme.getReviews().get(index);
//            }
//        }
//        if (review == null) {
//            logger.error("Review null, not found ");
//            return null;
//        }
//        BoardgamecafeApplication.getInstance().getModelBean().putBean(Symbols.IS_EMBEDDED, isEmbedded);
//        return review;
//    }

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
}
