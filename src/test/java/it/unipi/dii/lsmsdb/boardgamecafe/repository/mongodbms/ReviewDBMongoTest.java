package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReviewDBMongoTest {

    @Autowired
    private ReviewDBMongo reviewDBMongo;
    private ReviewModelMongo sampleReview;

    @BeforeEach
    public void start() {
        init();
    }

    @AfterEach
    public void clean() {
        reviewDBMongo.deleteReview(sampleReview);
    }

    private void init() {
        String sampleBoardgameName = "sample boardgame name";
        String sampleUsername = "test_user";
        int sampleRating = 1;
        String sampleBody = "sample review body";
        Date sampleDateOfReview = new Date();

        ReviewModelMongo review1 = new ReviewModelMongo(
                sampleBoardgameName,
                sampleUsername,
                sampleRating,
                sampleBody,
                sampleDateOfReview
        );

        reviewDBMongo.addReview(review1);

        List<ReviewModelMongo> reviews = reviewDBMongo.getReviewMongo().findByUsername(sampleUsername);

        if (reviews != null && !reviews.isEmpty()) {
            sampleReview = reviews.get(reviews.size() - 1);
        } else {
            sampleReview = null;
        }
    }

    @Test
    public void GIVEN_review_WHEN_added_THEN_it_gets_added() {
        ReviewModelMongo insertedReview = reviewDBMongo
                .getReviewMongo()
                .findById(sampleReview.getId())
                .get();
        assertEquals("test_user", insertedReview.getUsername());
    }

    @Test
    public void GIVEN_user_username_WHEN_search_by_author_THEN_reviews_he_wrote_returned() {
        assertNotNull(reviewDBMongo.findReviewByUsername(sampleReview.getUsername()));
    }

    @Test
    public void GIVEN_user_username_WHEN_search_by_author_THEN_recent_reviews_he_wrote_returned_WITH_limit_factor_included() {
        List<ReviewModelMongo> retrievedReviews = reviewDBMongo.findRecentReviewsByUsername(sampleReview.getUsername(), 100, 10);
        assertEquals(0, retrievedReviews.size());
    }

    @Test
    public void GIVEN_user_username_WHEN_search_by_author_THEN_recent_reviews_he_wrote_returned_WITH_limit_factor_zero() {
        List<ReviewModelMongo> retrievedReviews = reviewDBMongo.findRecentReviewsByUsername(sampleReview.getUsername(), 100, 0);
        assertEquals(1, retrievedReviews.size());
    }

    @Test
    public void GIVEN_boardgame_name_WHEN_search_by_boardgame_name_THEN_recent_reviews_he_wrote_returned_WITH_limit_factor_included() {
        List<ReviewModelMongo> retrievedReviews = reviewDBMongo.findRecentReviewsByBoardgame(sampleReview.getBoardgameName(), 100, 10);
        assertEquals(0, retrievedReviews.size());
    }

    @Test
    public void GIVEN_boardgame_name_WHEN_search_by_boardgame_name_THEN_recent_reviews_he_wrote_returned_WITH_limit_factor_zero() {
        List<ReviewModelMongo> retrievedReviews = reviewDBMongo.findRecentReviewsByBoardgame(sampleReview.getBoardgameName(), 100, 0);
        assertEquals(1, retrievedReviews.size());
    }

    @Test
    public void GIVEN_updated_review_WHEN_update_THEN_it_gets_updated() {
        String updatedReviewBody = "updated test review body";
        ReviewModelMongo updatedReview = sampleReview;
        updatedReview.setBody(updatedReviewBody);
        assertTrue(reviewDBMongo.updateReview(updatedReview.getId(), updatedReview));
    }

    @Test
    public void GIVEN_review_WHEN_delete_THEN_it_gets_deleted() {
        assertTrue(reviewDBMongo.deleteReview(sampleReview));
    }

    @Test
    public void GIVEN_user_username_WHEN_delete_by_author_THEN_reviews_he_wrote_deleted() {
        assertTrue(reviewDBMongo.deleteReviewByUsername(sampleReview.getUsername()));
    }

    @Test
    public void GIVEN_boardgame_name_WHEN_delete_by_boardgame_name_THEN_reviews_to_boardgame_deleted() {
        assertTrue(reviewDBMongo.deleteReviewByUsername(sampleReview.getBoardgameName()));
    }
}
