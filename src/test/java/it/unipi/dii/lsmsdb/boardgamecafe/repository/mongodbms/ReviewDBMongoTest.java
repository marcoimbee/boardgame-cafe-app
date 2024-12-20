package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;


@RunWith(SpringRunner.class)
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
        sampleReview = reviewDBMongo.getReviewMongo().findByUsername(sampleUsername).getLast();
    }

    @Test
    public void testAddReview() {
        ReviewModelMongo insertedReview = reviewDBMongo
                .getReviewMongo()
                .findById(sampleReview.getId())
                .get();
        assertEquals("test_user", insertedReview.getUsername());
    }

    @Test
    public void testFindReviewByUsername() {
        assertNotNull(reviewDBMongo.findReviewByUsername(sampleReview.getUsername()));
    }

    @Test
    public void testFindRecentReviewsByUsername_includeSkipFactor() {
        List<ReviewModelMongo> retrievedReviews = reviewDBMongo.findRecentReviewsByUsername(sampleReview.getUsername(), 100, 10);
        assertEquals(0, retrievedReviews.size());
    }

    @Test
    public void testFindRecentReviewsByUsername_skipZero() {
        List<ReviewModelMongo> retrievedReviews = reviewDBMongo.findRecentReviewsByUsername(sampleReview.getUsername(), 100, 0);
        assertEquals(1, retrievedReviews.size());
    }

    @Test
    public void testFindRecentReviewsByBoardgame_includeSkipFactor() {
        List<ReviewModelMongo> retrievedReviews = reviewDBMongo.findRecentReviewsByBoardgame(sampleReview.getBoardgameName(), 100, 10);
        assertEquals(0, retrievedReviews.size());
    }

    @Test
    public void testFindRecentReviewsByBoardgame_skipZero() {
        List<ReviewModelMongo> retrievedReviews = reviewDBMongo.findRecentReviewsByBoardgame(sampleReview.getBoardgameName(), 100, 0);
        assertEquals(1, retrievedReviews.size());
    }

    @Test
    public void testUpdateReview() {
        String updatedReviewBody = "updated test review body";
        ReviewModelMongo updatedReview = sampleReview;
        updatedReview.setBody(updatedReviewBody);
        assertTrue(reviewDBMongo.updateReview(updatedReview.getId(), updatedReview));
    }

    @Test
    public void testDeleteReview() {
        assertTrue(reviewDBMongo.deleteReview(sampleReview));
    }

    @Test
    public void testDeleteReviewByUsername() {
        assertTrue(reviewDBMongo.deleteReviewByUsername(sampleReview.getUsername()));
    }

    @Test
    public void testDeleteReviewByBoardgameName() {
        assertTrue(reviewDBMongo.deleteReviewByUsername(sampleReview.getBoardgameName()));
    }
}
