package it.unipi.dii.lsmsdb.boardgamecafe.repository.mongodbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.mongo.UserModelMongo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

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
    @Order(10)
    public void GIVEN_new_user_WHEN_add_THEN_gets_added() {
        assertEquals("test_username", userDBMongo.getUserMongo().findByUsername("test_username").get().getUsername());
    }

    @Test
    @Order(20)
    public void GIVEN_user_WHEN_delete_THEN_it_gets_deleted() {
        assertTrue(userDBMongo.deleteUser(sampleUser));
    }

    @Test
    @Order(30)
    public void GIVEN_username_WHEN_find_by_username_THEN_matching_user_returned_admins_included() {
        assertNotNull(userDBMongo.findByUsername(sampleUser.getUsername(), true).get());
    }

    @Test
    @Order(40)
    public void GIVEN_username_WHEN_find_by_username_THEN_matching_user_returned_admins_excluded() {
        assertNotNull(userDBMongo.findByUsername(sampleUser.getUsername(), false).get());
    }

    @Test
    @Order(50)
    public void GIVEN_email_WHEN_find_by_email_THEN_matching_user_returned() {
        assertNotNull(userDBMongo.findByEmail(sampleUser.getEmail()));
    }

    @Test
    @Order(60)
    public void GIVEN_number_of_users_WHEN_find_all_THEN_users_returned_with_skip() {
        assertNotNull(userDBMongo.findAllUsersWithLimit(10, 10));
    }

    @Test
    @Order(70)
    public void GIVEN_number_of_users_WHEN_find_all_THEN_users_returned_skip_zero() {
        assertNotNull(userDBMongo.findAllUsersWithLimit(10, 0));
    }

    @Test
    @Order(80)
    public void testGetUserUsernames_THEN_usernames_returned() {
        assertNotNull(userDBMongo.getUserUsernames());
    }

    @Test
    @Order(90)
    public void testGetBannedUsers_THEN_banned_users_returned() {
        sampleUser.setBanned(true);
        userDBMongo.updateUser(sampleUser.getId(), sampleUser, sampleUser.get_class());
        assertNotNull(userDBMongo.getBannedUsers());
    }
}
