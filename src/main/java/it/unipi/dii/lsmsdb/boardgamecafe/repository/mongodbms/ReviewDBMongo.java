package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import com.mongodb.BasicDBObject;
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

import java.util.List;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@Component
public class ReviewDBMongo {

    @Autowired
    private ReviewRepoMongo reviewMongo;
    @Autowired
    private MongoOperations mongoOperations;

    public ReviewDBMongo() {}

    public ReviewRepoMongo getReviewMongo() {
        return reviewMongo;
    }

    public boolean addReview(ReviewModelMongo review) {
        boolean result = true;
        try {
            reviewMongo.save(review);
        } catch (Exception e) {
            System.err.println("[ERROR] addReview()@ReviewDBMongo.java raised an exception: " + e.getMessage());
            result = false;
        }
        return result;
    }

    public List<ReviewModelMongo> findReviewByUsername(String username) {
        List<ReviewModelMongo> reviews = null;
        try {
            reviews = reviewMongo.findByUsername(username);
        } catch (Exception e) {
            System.err.println("[ERROR] findReviewByUsername()@ReviewDBMongo.java raised an exception: " + e.getMessage());
        }
        return reviews;
    }

    public List<ReviewModelMongo> findRecentReviewsByUsername(String username, int limit, int skip) {
        List<ReviewModelMongo> reviews = null;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("username").is(username));
            query.with(Sort.by(Sort.Direction.DESC, "dateOfReview"));
            query.skip(skip).limit(limit);
            reviews = mongoOperations.find(query, ReviewModelMongo.class);
        } catch (Exception e) {
            System.err.println("[ERROR] findRecentReviewsByUsername()@ReviewDBMongo.java raised an exception: " + e.getMessage());
        }
        return reviews;
    }

    public List<ReviewModelMongo> findRecentReviewsByBoardgame(String boardgameName, int limit, int skip) {
        List<ReviewModelMongo> reviews = null;
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("boardgameName").is(boardgameName));
            query.with(Sort.by(Sort.Direction.DESC, "dateOfReview"));
            query.skip(skip).limit(limit);
            reviews = mongoOperations.find(query, ReviewModelMongo.class);
        } catch (Exception e) {
            System.err.println("[ERROR] findRecentReviewsByBoardgame()@ReviewDBMongo.java raised an exception: " + e.getMessage());
        }
        return reviews;
    }

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

                this.addReview(reviewToBeUpdated);
            }
        } catch (Exception e) {
            System.err.println("[ERROR] updateReview()@ReviewDBMongo.java raised an exception: " + e.getMessage());
            return false;
        }

        return true;
    }

    public boolean deleteReview(ReviewModelMongo review) {
        boolean result = true;
        try {
            reviewMongo.delete(review);
        } catch (Exception e) {
            System.err.println("[ERROR] deleteReview()@ReviewDBMongo.java raised an exception: " + e.getMessage());
            result = false;
        }
        return result;
    }

    public boolean deleteReviewByUsername(String username) {
        boolean result = true;
        try {
            reviewMongo.deleteReviewByUsername(username);
        } catch (Exception e) {
            System.err.println("[ERROR] deleteReviewByUsername()@ReviewDBMongo.java raised an exception: " + e.getMessage());
            result = false;
        }
        return result;
    }

    public boolean deleteReviewByBoardgameName(String boardgameName) {
        boolean result = true;
        try {
            reviewMongo.deleteReviewByBoardgameName(boardgameName);
        } catch (Exception e) {
            System.err.println("[ERROR] deleteReviewByBoardgameName()@ReviewDBMongo.java raised an exception: " + e.getMessage());
            result = false;
        }
        return result;
    }

    public Document getTopRatedBoardgamePerYear(int minReviews, int limit, int year) {
        ProjectionOperation projectYear = project()
                .andExpression("boardgameName").as("name")
                .andExpression("rating").as("rating")
                .andExpression("year(dateOfReview)").as("year");

        Criteria minReviewsAndYear = new Criteria().andOperator(
                Criteria.where("numReviews").gte(minReviews),
                Criteria.where("_id.year").is(year) // Accessing year via _id, since the year became a key because of the grouping
        );

        GroupOperation groupByYearAndGame = group("name" ,"year")
                .avg("rating").as("avgRating")
                .count().as("numReviews");

        MatchOperation matchMinReviews = match(minReviewsAndYear);

        GroupOperation groupByYear = group("_id.year")
                .push(new BasicDBObject("name", "$_id.name")
                        .append("avgRating", "$avgRating")
                        .append("numReviews", "$numReviews"))
                .as("topGames");

        AddFieldsOperation addFieldsSortTopGames = addFields()
                .addField("topGames")
                .withValue(new BasicDBObject("$let", new BasicDBObject("vars", new BasicDBObject("topGames", "$topGames"))
                        .append("in", new BasicDBObject("$sortArray", new BasicDBObject("input", "$$topGames")
                                .append("sortBy", new BasicDBObject("avgRating", -1)))))).build();

        ProjectionOperation limitTopGamesPerYear = project()
                .and("topGames").slice(limit)
                .as("topGames");

        SortOperation sortByYear = sort(Sort.by(Sort.Order.asc("_id"), Sort.Order.desc("topGames.avgRating")));

        Aggregation aggregation = newAggregation(
                projectYear,
                groupByYearAndGame,
                matchMinReviews,
                groupByYear,
                addFieldsSortTopGames,
                limitTopGamesPerYear,
                sortByYear
        );

        AggregationResults<Document> results = mongoOperations.aggregate(
                aggregation,
                "reviews",
                Document.class
        );

        return results.getRawResults();
    }

    public Double getAvgRatingByBoardgameName(String boardgameName) {
        MatchOperation matchByName = match(Criteria.where("boardgameName").is(boardgameName));

        GroupOperation groupByBoardgameName = group("boardgameName")
                .avg("rating").as("avgRating");

        ProjectionOperation projectFinalResult = project()
                .and("_id").as("boardgameName")
                .and("avgRating").as("avgRating");

        Aggregation aggregation = newAggregation(
                matchByName,
                groupByBoardgameName,
                projectFinalResult
        );

        AggregationResults<Document> results = mongoOperations.aggregate(
                aggregation,
                "reviews",
                Document.class
        );

        Document docResult = results.getUniqueMappedResult();
        if (docResult != null && docResult.containsKey("avgRating")) {
            return docResult.getDouble("avgRating");
        }

        return null;
    }

    public boolean updateReviewsAfterBoardgameUpdate(String oldBoardgameName, String updatedBoardgameName) {
        try {
            Query query = new Query();
            query.addCriteria(Criteria.where("boardgameName").is(oldBoardgameName));

            Update update = new Update();
            update.set("boardgameName", updatedBoardgameName);

            mongoOperations.updateMulti(query, update, "reviews");

            return true;
        } catch (Exception ex) {
            System.err.println("[ERROR] updateReviewsAfterBoardgameUpdate()@ReviewDBMongo.java raised an exception: " + ex.getMessage());
            return false;
        }
    }
}
