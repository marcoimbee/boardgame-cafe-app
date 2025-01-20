package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDBNeo4jTest {

    @Autowired
    private UserDBNeo4j userDBNeo4j;
    @Autowired
    private BoardgameDBNeo4j boardgameDBNeo4j;
    @Autowired
    private PostDBNeo4j postDBNeo4j;

    private UserModelNeo4j sampleUser;
    private UserModelNeo4j sampleFollowedUser;
    private UserModelNeo4j sampleFollowerUser;

    static PostModelNeo4j testPost1, testPost2;
    static final String testIdPost1 = "testIdPost1";
    static final String testIdPost2 = "testIdPost2";
    static final String testUsername1 = "testUsername1";
    static final String testIdUsername1 = "testIdUsername1";
    static final String testUsername2 = "testUsername2";
    static final String testIdUsername2 = "testIdUsername2";
    static final String testIdBoardgame = "testIdBoardgame";
    static final String testBoardgameName = "testBoardgameName";
    static final String testImageBoardgame = "testImageLink";
    static final String testDescriptionBoardgame = "testDescription";
    static final int testYearPublishedBoardgame = 2024;
    static BoardgameModelNeo4j testBoardgameNeo4j;
    static UserModelNeo4j testAuthor, notFriendUser;

    // Username of one of the users on neo4j who has the required characteristics to test the method:
    // "getMostFollowedUsersUsernames"
    String oneOfTheMostFollowedUser = "blackpeacock168";

    @BeforeEach
    public void start() {
        init();
    }

    @AfterEach
    public void clean() {
        userDBNeo4j.deleteUserDetach(sampleUser.getUsername());
        userDBNeo4j.deleteUserDetach(sampleFollowedUser.getUsername());
        userDBNeo4j.deleteUserDetach(sampleFollowerUser.getUsername());
        userDBNeo4j.deleteUserDetach(notFriendUser.getUsername());
        userDBNeo4j.deleteUserDetach(testAuthor.getUsername());
        postDBNeo4j.deletePost(testIdPost1);
        postDBNeo4j.deletePost(testIdPost2);
        boardgameDBNeo4j.deleteBoardgameDetach(testBoardgameName);
    }

    private void init() {
        String sampleUserId = "test_user_id";
        String sampleUserUsername = "test_user_username";
        String sampleFollowedUserId = "test_followed_user_id";
        String sampleFollowedUserUsername = "test_followed_user_username";
        String sampleFollowerUserId = "test_follower_user_id";
        String sampleFollowerUserUsername = "test_follower_user_username";

        sampleUser = new UserModelNeo4j(sampleUserId, sampleUserUsername);
        sampleFollowedUser = new UserModelNeo4j(sampleFollowedUserId, sampleFollowedUserUsername);
        sampleFollowerUser = new UserModelNeo4j(sampleFollowerUserId, sampleFollowerUserUsername);
        userDBNeo4j.addUser(sampleUser);
        userDBNeo4j.addUser(sampleFollowedUser);
        userDBNeo4j.addUser(sampleFollowerUser);

        testBoardgameNeo4j = new BoardgameModelNeo4j(testIdBoardgame, testBoardgameName,
                testImageBoardgame, testDescriptionBoardgame,
                testYearPublishedBoardgame);

        testPost1 = new PostModelNeo4j(testIdPost1);
        testAuthor = new UserModelNeo4j(testIdUsername1, testUsername1);
        testPost1.setAuthor(testAuthor);
        testPost1.setTaggedGame(testBoardgameNeo4j);
        userDBNeo4j.addUser(testAuthor);
        postDBNeo4j.addPost(testPost1);

        // Creazione di un altro utente e post per i test
        testPost2 = new PostModelNeo4j(testIdPost2);
        notFriendUser = new UserModelNeo4j(testIdUsername2, testUsername2);
        testPost2.setAuthor(notFriendUser);
        testPost2.setTaggedGame(testBoardgameNeo4j);
        userDBNeo4j.addUser(notFriendUser);
        postDBNeo4j.addPost(testPost2);
    }

    @Test
    @Order(10)
    public void GIVEN_new_user_WHEN_add_THEN_gets_added() {
        assertNotNull(userDBNeo4j.findById(sampleUser.getId()));
    }

    @Test
    @Order(20)
    public void GIVEN_updated_user_WHEN_update_THEN_gets_updated() {
        sampleUser.setUsername("updated_test_user_username");
        assertTrue(userDBNeo4j.updateUser(sampleUser.getId(), sampleUser));
    }

    @Test
    @Order(30)
    public void GIVEN_user_username_WHEN_delete_and_detach_THEN_gets_deleted_and_detached() {
        assertTrue(userDBNeo4j.deleteUserDetach(sampleUser.getUsername()));
    }

    @Test
    @Order(40)
    public void GIVEN_user_username_WHEN_find_by_username_THEN_returned_matching_user() {
        UserModelNeo4j retrievedUser = userDBNeo4j.findByUsername(sampleUser.getUsername()).get();
        assertEquals(sampleUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    @Order(50)
    public void GIVEN_follower_user_and_followed_user_WHEN_follow_THEN_follower_follows_followed() {
        userDBNeo4j.followUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    @Order(60)
    public void GIVEN_follower_user_and_followed_user_WHEN_unfollow_THEN_follower_unfollows_followed() {
        userDBNeo4j.unfollowUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(0, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    @Order(70)
    public void GIVEN_user_username_WHEN_get_follower_usernames_THEN_followed_users_usernames_returned() {
        userDBNeo4j.followUser(sampleUser.getUsername(), sampleFollowedUser.getUsername());
        assertEquals(sampleFollowedUser.getUsername(), userDBNeo4j.getFollowedUsernames(sampleUser.getUsername()).get(0));
    }

    @Test
    @Order(80)
    public void GIVEN_user_username_WHEN_get_following_count_THEN_number_followed_users_returned() {
        userDBNeo4j.followUser(sampleUser.getUsername(), sampleFollowedUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowing(sampleUser.getUsername()));
    }

    @Test
    @Order(90)
    public void GIVEN_user_username_WHEN_get_followers_count_THEN_number_follower_users_returned() {
        userDBNeo4j.followUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    @Order(100)
    public void GIVEN_user_id_WHEN_find_by_id_THEN_user_matching_id_returned() {
        UserModelNeo4j retrievedUser = userDBNeo4j.findById(sampleUser.getId()).get();
        assertEquals(sampleUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    @Order(110)
    public void GIVEN_username_WHEN_getUsersByCommonBoardgamePosted_THEN_users_with_common_boardgames_returned() {
        // ottenere utenti con boardgame in comune nei posts creati nel campo tag
        List<String> suggestedUsers = userDBNeo4j.
                getUsersByCommonBoardgamePosted(testUsername1, 10, 0);
        // verifica che il suggerimento includa l'utente corretto
        assertTrue(suggestedUsers.contains(notFriendUser.getUsername()));
    }

    @Test
    @Order(120)
    public void GIVEN_username_WHEN_getUsersBySameLikedPosts_THEN_users_with_common_liked_posts_returned() {
        // aggiunta like a un post da entrambi gli utenti
        postDBNeo4j.addLikePost(sampleUser.getUsername(), testIdPost1, true);
        postDBNeo4j.addLikePost(notFriendUser.getUsername(),testIdPost1, true);

        // Act: ottenere utenti con post comuni piaciuti
        List<String> suggestedUsers = userDBNeo4j.getUsersBySameLikedPosts(sampleUser.getUsername(), 10, 0);
        // Assert: verifica che l'utente seguito venga suggerito
        assertTrue(suggestedUsers.contains(notFriendUser.getUsername()));
    }

    @Test
    @Order(130)
    public void GIVEN_min_followers_count_WHEN_getMostFollowedUsersUsernames_THEN_users_with_high_followers_returned() {
        // ottenere utenti con almeno 1 follower
        List<String> mostFollowedUsers = userDBNeo4j.getMostFollowedUsersUsernames(5, 10);
        // verifica che l'utente con più follower del limit venga incluso
        assertTrue(mostFollowedUsers.contains(oneOfTheMostFollowedUser));
    }

}
