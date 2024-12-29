package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserDBNeo4jTest {

    @Autowired
    private UserDBNeo4j userDBNeo4j;
    private UserModelNeo4j sampleUser;
    private UserModelNeo4j sampleFollowedUser;
    private UserModelNeo4j sampleFollowerUser;

    @BeforeEach
    public void start() {
        init();
    }

    @AfterEach
    public void clean() {
        userDBNeo4j.deleteUserDetach(sampleUser.getUsername());
        userDBNeo4j.deleteUserDetach(sampleFollowedUser.getUsername());
        userDBNeo4j.deleteUserDetach(sampleFollowerUser.getUsername());
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
    }

    @Test
    public void GIVEN_new_user_WHEN_add_THEN_gets_added() {
        assertNotNull(userDBNeo4j.findById(sampleUser.getId()));
    }

    @Test
    public void GIVEN_updated_user_WHEN_update_THEN_gets_updated() {
        sampleUser.setUsername("updated_test_user_username");
        assertTrue(userDBNeo4j.updateUser(sampleUser.getId(), sampleUser));
    }

    @Test
    public void GIVEN_user_username_WHEN_delete_and_detach_THEN_gets_deleted_and_detached() {
        assertTrue(userDBNeo4j.deleteUserDetach(sampleUser.getUsername()));
    }

    @Test
    public void GIVEN_user_username_WHEN_find_by_username_THEN_returned_matching_user() {
        UserModelNeo4j retrievedUser = userDBNeo4j.findByUsername(sampleUser.getUsername()).get();
        assertEquals(sampleUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    public void GIVEN_follower_user_and_followed_user_WHEN_follow_THEN_follower_follows_followed() {
        userDBNeo4j.followUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    public void GIVEN_follower_user_and_followed_user_WHEN_unfollow_THEN_follower_unfollows_followed() {
        userDBNeo4j.unfollowUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(0, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    public void GIVEN_user_username_WHEN_get_follower_usernames_THEN_followed_users_usernames_returned() {
        userDBNeo4j.followUser(sampleUser.getUsername(), sampleFollowedUser.getUsername());
        assertEquals(sampleFollowedUser.getUsername(), userDBNeo4j.getFollowedUsernames(sampleUser.getUsername()).get(0));
    }

    @Test
    public void GIVEN_user_username_WHEN_get_following_count_THEN_number_followed_users_returned() {
        userDBNeo4j.followUser(sampleUser.getUsername(), sampleFollowedUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowing(sampleUser.getUsername()));
    }

    @Test
    public void GIVEN_user_username_WHEN_get_followers_count_THEN_number_follower_users_returned() {
        userDBNeo4j.followUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    public void GIVEN_user_id_WHEN_find_by_id_THEN_user_matching_id_returned() {
        UserModelNeo4j retrievedUser = userDBNeo4j.findById(sampleUser.getId()).get();
        assertEquals(sampleUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    public void GIVEN_user_username_WHEN_set_banned_THEN_gets_banned() {
        assertTrue(userDBNeo4j.setUserAsBanned(sampleUser.getUsername()));
    }

    @Test
    public void GIVEN_user_username_WHEN_restore_node_after_unban_THEN_gets_unbanned_and_node_restored() {
        userDBNeo4j.setUserAsBanned(sampleUser.getUsername());
        userDBNeo4j.restoreUserNodeAfterUnban(sampleUser.getId(), sampleUser.getUsername());
        UserModelNeo4j restoredUserNode = userDBNeo4j.findByUsername(sampleUser.getUsername()).get();
        assertEquals(sampleUser.getUsername(), restoredUserNode.getUsername());
    }
}
