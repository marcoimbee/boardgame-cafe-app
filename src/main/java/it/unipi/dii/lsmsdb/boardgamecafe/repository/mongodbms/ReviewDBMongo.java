package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.client.result.UpdateResult;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.bson.Document;
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

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class ReviewDBMongo {

    public ReviewDBMongo() {}

    @Autowired
    private ReviewRepoMongo reviewMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public ReviewRepoMongo getReviewMongo() {
        return reviewMongo;
    }

    public boolean addReview(ReviewModelMongo review) {
        boolean result = true;
        try {
            reviewMongo.save(review);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public Optional<ReviewModelMongo> findReviewById(String id) {
        Optional<ReviewModelMongo> review = Optional.empty();
        try {
            review = reviewMongo.findById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return review;
    }

    public Optional<ReviewModelMongo> findByUsernameAndBoardgameName(String username,
                                                                     String boardgameName) {

        Optional<ReviewModelMongo> review = Optional.empty();
        try {
            review = reviewMongo.findByUsernameAndBoardgameName(username, boardgameName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return review;
    }

    public List<ReviewModelMongo> findReviewByUsername(String username) {
        List<ReviewModelMongo> reviews = null;
        try {
            reviews = reviewMongo.findByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public List<ReviewModelMongo> findRecentReviewsByUsername(String username, int limit, int skip) {
        List<ReviewModelMongo> reviews = null;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("username").is(username));
            query.with(Sort.by(Sort.Direction.DESC, "dateOfReview")); // Ordinamento per data
            query.skip(skip).limit(limit); // Paginazione
            reviews = mongoOperations.find(query, ReviewModelMongo.class); // Ricerca nella collection delle recensioni
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }


    public List<ReviewModelMongo> findReviewByBoardgameName(String boardgameName) {
        List<ReviewModelMongo> reviews = null;
        try {
            reviews = reviewMongo.findByBoardgameName(boardgameName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }


    /*
    public boolean updateReview(String id, ReviewModelMongo newReview) {

        boolean result = true;
        try {
            Optional<ReviewModelMongo> review = reviewMongo.findById(id);
            if (review.isPresent()) {
                ReviewModelMongo resultReview = review.get();

                ReviewModelMongo.ReviewBuilder builder =
                        new ReviewModelMongo.ReviewBuilder(newReview);

                builder.id(id).boardgameName(resultReview.getBoardgameName()).
                               username(resultReview.getUsername());

                this.addReview(builder.build());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }     */

    public boolean updateReview(String id, ReviewModelMongo newReview) {

        try {
            Optional<ReviewModelMongo> review = reviewMongo.findById(id);
            if (review.isPresent()) {
                ReviewModelMongo reviewToBeUpdated = review.get();

                reviewToBeUpdated.setUsername(newReview.getUsername());
                reviewToBeUpdated.setBoardgameName(newReview.getBoardgameName());
                reviewToBeUpdated.setRating(newReview.getRating());
                reviewToBeUpdated.setBody(newReview.getBody());
                reviewToBeUpdated.setDateOfReview(newReview.getDateOfReview());

                this.addReview(reviewToBeUpdated); //Uso di save per aggiornare tutto il documento
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean deleteReviewById(String reviewId)
    {
        Query query = new Query();
        query.addCriteria(Criteria.where("_id").is(reviewId));

        Update update = new Update();
        update.pull("reviews", Query.query(Criteria.where("_id").is(reviewId)));

        UpdateResult result = mongoOperations.updateFirst(query, update, ReviewModelMongo.class);
        return (result.getModifiedCount() > 0);
    }

    public boolean deleteReview(ReviewModelMongo review)
    // Elimina la review dalla collection reviews
    {
        boolean result = true;
        try {
            reviewMongo.delete(review);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteReviewByUsername(String username)
    // Elimina tutte le reviews di un utente. Invocata in fase di eliminazione di un utente
    {
        boolean result = true;
        try {
            reviewMongo.deleteReviewByUsername(username);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteReviewByBoardgameName(String boardgameName)
    // Elimina tutte le reviews di un boardgame. Invocata in fase di eliminazione del gioco
    {
        boolean result = true;
        try {
            reviewMongo.deleteReviewByBoardgameName(boardgameName);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public List<ReviewModelMongo> findOldReviews(String parameter, boolean isBoardgame) {
        List<ReviewModelMongo> reviews = new ArrayList<>();
        try {
            Query query = new Query();
            if (isBoardgame) {
                query.addCriteria(new Criteria("boardgameName").is(parameter));
            } else {
                query.addCriteria(new Criteria("username").is(parameter));
            }
            query.with(Sort.by(Sort.Direction.DESC, "dateOfReview"));
            query.skip(50);
            reviews = mongoOperations.find(query, ReviewModelMongo.class);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return reviews;
    }

    public boolean updateReviewsOldUser(String username) {
        try {
            Query query = Query.query(
                    Criteria.where("username").is(username));
            Update update = new Update().set("username", "Deleted User");
            mongoOperations.updateMulti(query,
                                        update,
                                        ReviewModelMongo.class,
                                        "reviews");
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

}