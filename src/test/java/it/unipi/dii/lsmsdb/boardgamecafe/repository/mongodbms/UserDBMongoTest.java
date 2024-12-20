package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.GenericUserModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.ReviewModelMongo;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
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
class UserDBMongoTest {

    @Autowired
    private UserDBMongo userDBMongo;
    private UserModelMongo sampleUser;

    @BeforeEach
    public void start() {
        init();
    }

    @AfterEach
    public void clean() {
        userDBMongo.deleteUser(sampleUser);
    }

    private void init() {
        String sampleUsername = "test_username";
        String sampleEmail = "test@test.com";
        String sampleSalt = "ttesstt";
        String samplePasswordHash = "tttttttttttttttteeeeeeeeeeeeeeeesssssssssssssssstttttttttttttttt";
        String sampleClass = "user";
        String sampleName = "testName";
        String sampleSurname = "testSurname";
        String sampleGender = "M";
        Date sampleDateOfBirth = new Date();
        String sampleNationality = "England";
        boolean sampleBanned = false;

        UserModelMongo user1 = new UserModelMongo(
                sampleUsername,
                sampleEmail,
                sampleName,
                sampleSurname,
                sampleGender,
                sampleDateOfBirth,
                sampleNationality,
                sampleBanned,
                sampleSalt,
                samplePasswordHash,
                sampleClass
        );

        userDBMongo.addUser(user1);
        sampleUser = (UserModelMongo) userDBMongo.getUserMongo().findByUsername(sampleUsername).get();
    }

    @Test
    public void testAddUser() {
        assertEquals("test_username", userDBMongo.getUserMongo().findByUsername("test_username").get().getUsername());
    }

    @Test
    public void testDeleteUser() {
        assertTrue(userDBMongo.deleteUser(sampleUser));
    }

    @Test
    public void testFindByUsername_includeAdmins() {
        assertNotNull(userDBMongo.findByUsername(sampleUser.getUsername(), true).get());
    }

    @Test
    public void testFindByUsername_excludeAdmins() {
        assertNotNull(userDBMongo.findByUsername(sampleUser.getUsername(), false).get());
    }

    @Test
    public void testFindByEmail() {
        assertNotNull(userDBMongo.findByEmail(sampleUser.getEmail()));
    }

    @Test
    public void testDeleteReviewInUserReviewsById_existingReview() {
        ReviewModelMongo sampleReview = new ReviewModelMongo(
                "sample_review_id",
                "test_boardgame",
                "test_username",
                1,
                "test review body",
                new Date()
        );
        userDBMongo.addReviewInUserArray(sampleUser, sampleReview);
        assertTrue(userDBMongo.deleteReviewInUserReviewsById(sampleUser.getId(), sampleReview.getId()));
    }

    @Test
    public void testDeleteReviewInUserReviewsById_nonExistingReview() {
        assertFalse(userDBMongo.deleteReviewInUserReviewsById(sampleUser.getId(), "non-existent_review_id"));
    }

    @Test
    public void testFindAllUsersWithLimit_includeSkipFactor() {
        assertNotNull(userDBMongo.findAllUsersWithLimit(10, 10));
    }

    @Test
    public void testFindAllUsersWithLimit_skipZero() {
        assertNotNull(userDBMongo.findAllUsersWithLimit(10, 0));
    }

    @Test
    public void testAddReviewInUserArray() {
        ReviewModelMongo sampleReview = new ReviewModelMongo(
                "sample_review_id",
                "test_boardgame",
                "test_username",
                1,
                "test review body",
                new Date()
        );
        assertTrue(userDBMongo.addReviewInUserArray(sampleUser, sampleReview));
    }

    @Test
    public void testGetUserUsernames() {
        assertNotNull(userDBMongo.getUserUsernames());
    }

    @Test
    public void testGetBannedUsers() {
        sampleUser.setBanned(true);
        userDBMongo.updateUser(sampleUser.getId(), sampleUser, sampleUser.get_class());
        assertNotNull(userDBMongo.getBannedUsers());
    }
}
