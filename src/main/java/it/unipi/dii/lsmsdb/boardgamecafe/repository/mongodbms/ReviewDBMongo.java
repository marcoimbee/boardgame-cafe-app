package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

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

        boolean result = true;
        try {
            Optional<ReviewModelMongo> review = reviewMongo.findById(id);
            if (review.isPresent()) {
                review.get().setUsername(newReview.getUsername());
                review.get().setBoardgameName(newReview.getBoardgameName());
                review.get().setRating(newReview.getRating());
                review.get().setBody(newReview.getBody());
                review.get().setDateOfReview(newReview.getDateOfReview());

                reviewMongo.save(review.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteReviewById(String id) {
        boolean result = true;
        try {
            reviewMongo.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteReview(ReviewModelMongo review) {
        boolean result = true;
        try {
            reviewMongo.delete(review);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteReviewByUsername(String id) {
        boolean result = true;
        try {
            reviewMongo.deleteReviewByUsername(id);
        } catch (Exception e) {
            e.printStackTrace();
            result = false;
        }
        return result;
    }

    public boolean deleteReviewByBoardgameName(String id) {
        boolean result = true;
        try {
            reviewMongo.deleteReviewByBoardgameName(id);
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

    // --- ToBeChanged ---
    /*
    public Document findTopPhonesByRating(int minReviews, int results) {
        GroupOperation groupOperation = group("$phoneName").avg("$rating")
                .as("avgRating").count().as("numReviews");
        MatchOperation matchOperation = match(new Criteria("numReviews").gte(minReviews));
        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("phoneName").andExclude("_id")
                .andExpression("numReviews").as("reviews")
                .and(ArithmeticOperators.Round.roundValueOf("avgRating").place(1)).as("rating");
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "rating", "reviews"));
        LimitOperation limitOperation = limit(results);
        Aggregation aggregation = newAggregation(groupOperation, matchOperation, projectionOperation,
                sortOperation, limitOperation);
        AggregationResults<ReviewModelMongo> result = mongoOperations
                .aggregate(aggregation, "reviews", ReviewModelMongo.class);
        return result.getRawResults();
    }

    public Document findMostActiveUsers(int results) {
        GroupOperation groupOperation = group("$username").count().as("numReviews").
                avg("$rating").as("ratingUser");
        SortOperation sortOperation = sort(Sort.by(Sort.Direction.DESC, "numReviews"));
        LimitOperation limitOperation = limit(results);
        ProjectionOperation projectionOperation = project()
                .andExpression("_id").as("username")
                .andExpression("numReviews").as("reviews").andExclude("_id")
                .and(ArithmeticOperators.Round.roundValueOf("ratingUser").place(1)).as("rating");
        Aggregation aggregation = newAggregation(groupOperation, sortOperation, limitOperation,
                projectionOperation);
        AggregationResults<ReviewModelMongo> result = mongoOperations
                .aggregate(aggregation, "reviews", ReviewModelMongo.class);
        return result.getRawResults();
    }
    */
}