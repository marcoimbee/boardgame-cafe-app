package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
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
    public void testAddUser() {
        assertNotNull(userDBNeo4j.findById(sampleUser.getId()));
    }

    @Test
    public void testUpdateUser() {
        sampleUser.setUsername("updated_test_user_username");
        assertTrue(userDBNeo4j.updateUser(sampleUser.getId(), sampleUser));
    }

    @Test
    public void testDeleteUserDetach() {
        assertTrue(userDBNeo4j.deleteUserDetach(sampleUser.getUsername()));
    }

    @Test
    public void testFindByUsername() {
        UserModelNeo4j retrievedUser = userDBNeo4j.findByUsername(sampleUser.getUsername()).get();
        assertEquals(sampleUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    public void testFollowUser() {
        userDBNeo4j.followUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    public void testUnfollowUser() {
        userDBNeo4j.unfollowUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(0, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    public void testGetFollowedUsersUsernames() {
        userDBNeo4j.followUser(sampleUser.getUsername(), sampleFollowedUser.getUsername());
        assertEquals(sampleFollowedUser.getUsername(), userDBNeo4j.getFollowedUsernames(sampleUser.getUsername()).get(0));
    }

    @Test
    public void testGetCountFollowing() {
        userDBNeo4j.followUser(sampleUser.getUsername(), sampleFollowedUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowing(sampleUser.getUsername()));
    }

    @Test
    public void testGetCountFollowers() {
        userDBNeo4j.followUser(sampleFollowerUser.getUsername(), sampleUser.getUsername());
        assertEquals(1, userDBNeo4j.getCountFollowers(sampleUser.getUsername()));
    }

    @Test
    public void testFindById() {
        UserModelNeo4j retrievedUser = userDBNeo4j.findById(sampleUser.getId()).get();
        assertEquals(sampleUser.getUsername(), retrievedUser.getUsername());
    }

    @Test
    public void testSetUserAsBanned() {
        assertTrue(userDBNeo4j.setUserAsBanned(sampleUser.getUsername()));
    }

    @Test
    public void testRestoreUserNodeAfterUnban() {
        userDBNeo4j.setUserAsBanned(sampleUser.getUsername());
        userDBNeo4j.restoreUserNodeAfterUnban(sampleUser.getId(), sampleUser.getUsername());
        UserModelNeo4j restoredUserNode = userDBNeo4j.findByUsername(sampleUser.getUsername()).get();
        assertEquals(sampleUser.getUsername(), restoredUserNode.getUsername());
    }
}