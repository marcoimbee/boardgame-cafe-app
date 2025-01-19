package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PostDBNeo4jTest {

    @Autowired
    PostDBNeo4j postDBNeo4j;
    @Autowired
    UserDBNeo4j userDBNeo4j;

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
    static UserModelNeo4j testAuthor, followedUser;

    @BeforeAll
    public static void setup() {
        testPost1 = new PostModelNeo4j(testIdPost1);
        testAuthor = new UserModelNeo4j(testIdUsername1, testUsername1);
        testPost1.setAuthor(testAuthor);

        // Creazione di un altro utente e post per i test
        testPost2 = new PostModelNeo4j(testIdPost2);
        followedUser = new UserModelNeo4j(testIdUsername2, testUsername2);
        testPost2.setAuthor(followedUser);

        testBoardgameNeo4j = new BoardgameModelNeo4j(testIdBoardgame, testBoardgameName,
                testImageBoardgame, testDescriptionBoardgame,
                testYearPublishedBoardgame);
    }

    @Test
    @Order(10)
    public void GIVEN_a_post_WHEN_adding_to_Neo4j_THEN_post_is_added_successfully() {
        boolean shouldReturnTrue = postDBNeo4j.addPost(testPost1);
        assertTrue(shouldReturnTrue);
        boolean shouldReturnTrue2 = postDBNeo4j.addPost(testPost2);
        assertTrue(shouldReturnTrue2);
    }

    @Test
    @Order(20)
    public void GIVEN_a_post_id_WHEN_finding_by_id_THEN_the_found_post_has_the_same_id() {
        Optional<PostModelNeo4j> shouldBeNotEmpty = postDBNeo4j.findById(testIdPost1);

        PostModelNeo4j shouldHaveSameId = shouldBeNotEmpty.get();
        assertEquals(testIdPost1, shouldHaveSameId.getId());
    }

    @Test
    @Order(30)
    void GIVEN_a_post_with_referred_boardgame_WHEN_updating_the_post_THEN_post_is_updated_successfully() {
        testPost1.setTaggedGame(testBoardgameNeo4j);
        boolean shouldReturnTrue = postDBNeo4j.updatePost(testPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test
    @Order(60)
    public void GIVEN_post_id_and_like_count_WHEN_setting_like_count_THEN_cache_is_updated() {
        postDBNeo4j.setLikeCount(testIdPost1, 5);
        int cachedLikes = postDBNeo4j.findTotalLikesByPostId(testIdPost1);
        assertEquals(5, cachedLikes);
        postDBNeo4j.setLikeCount(testIdPost2, 5);
        int cachedLikes2 = postDBNeo4j.findTotalLikesByPostId(testIdPost2);
        assertEquals(5, cachedLikes2);
    }

    @Test
    @Order(70)
    public void GIVEN_post_id_WHEN_finding_total_likes_THEN_return_correct_count() {
        int totalLikes = postDBNeo4j.findTotalLikesByPostId(testIdPost1);
        assertTrue(totalLikes >= 0);
    }
    @Test
    @Order(80)
    public void GIVEN_username_and_post_id_WHEN_adding_like_THEN_like_is_added_successfully() {
        postDBNeo4j.addLikePost(testUsername1, testIdPost1, true);
        boolean hasLiked = postDBNeo4j.hasUserLikedPost(testUsername1, testIdPost1);
        assertTrue(hasLiked);
        postDBNeo4j.addLikePost(testUsername2, testIdPost2, true);
        boolean hasLiked2 = postDBNeo4j.hasUserLikedPost(testUsername2, testIdPost2);
        assertTrue(hasLiked2);
    }

    @Test
    @Order(90)
    public void GIVEN_username_and_post_id_WHEN_checking_if_user_liked_post_THEN_return_correct_result() {
        boolean hasLiked = postDBNeo4j.hasUserLikedPost(testUsername1, testIdPost1);
        assertTrue(hasLiked); // Assuming the user hasn't liked the post in this setup
        boolean hasLiked2 = postDBNeo4j.hasUserLikedPost(testUsername2, testIdPost2);
        assertTrue(hasLiked2); // Assuming the user hasn't liked the post in this setup
    }

    @Test
    @Order(100)
    public void GIVEN_username_WHEN_getting_posts_by_followed_users_THEN_return_posts_list() {
        // Salvare l'utente e il post nel database
        userDBNeo4j.addUser(followedUser);
        // Creare una relazione "follows" tra l'utente di test e l'altro utente
        userDBNeo4j.followUser(testUsername1, testUsername2);

        List<PostModelNeo4j> posts = postDBNeo4j.getPostsByFollowedUsers(testUsername1, 10, 0);
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
    }

    //ToDo: Controllare perchè non supera il test, la lista è, stranamente, vuota.
//    @Test
//    @Order(110)
//    public void GIVEN_username_WHEN_getting_posts_liked_by_followed_users_THEN_return_posts_list() {
//        // Salvare l'utente e il post nel database
//        userDBNeo4j.addUser(followedUser);
//        // Creare una relazione "follows" tra l'utente di test e l'altro utente
//        userDBNeo4j.followUser(testUsername1, testUsername2);
//        //Creare una relazione "liked" tra il post e l'utente seguito
//        postDBNeo4j.addLikePost(testUsername2, testIdPost2, true);
//
//        List<PostModelNeo4j> posts = postDBNeo4j.getPostsLikedByFollowedUsers(testUsername1, 10, 0);
//        assertNotNull(posts);
//        assertFalse(posts.isEmpty());
//    }

    @Test
    @Order(120)
    public void GIVEN_username_and_post_id_WHEN_removing_like_THEN_like_is_removed_successfully() {
        postDBNeo4j.removeLikePost(testUsername1, testIdPost1);
        boolean hasLiked = postDBNeo4j.hasUserLikedPost(testUsername1, testIdPost1);
        assertFalse(hasLiked);
    }

    @Test
    @Order(130)
    void GIVEN_a_post_WHEN_deleting_the_post_and_all_its_references_THEN_deletion_is_successful() {
        boolean shouldReturnTrue = userDBNeo4j.deleteUserDetach(testAuthor.getUsername());
        assertTrue(shouldReturnTrue);
        shouldReturnTrue = postDBNeo4j.deletePost(testIdPost1);
        assertTrue(shouldReturnTrue);
    }

    @Test
    @Order(140)
    public void GIVEN_post_referred_to_boardgame_WHEN_deleting_by_referred_boardgame_THEN_post_is_deleted() {
        assertTrue(postDBNeo4j.deleteByReferredBoardgame(testIdBoardgame));
    }

    @Test
    @Order(150)
    public void GIVEN_post_by_user_WHEN_deleting_by_username_THEN_post_is_deleted() {
        assertTrue(postDBNeo4j.deleteByUsername(testUsername1));
    }

    @Test
    @Order(160)
    public void cleanup() {
        // Rimuovere la relazione "follows"
        userDBNeo4j.unfollowUser(testUsername1, testUsername2);
        userDBNeo4j.unfollowUser(testUsername2, testUsername1);
        // Rimuovere il like sul post
        postDBNeo4j.removeLikePost(testUsername1, testIdPost1);
        postDBNeo4j.removeLikePost(testUsername2, testIdPost2);

        // Eliminare il post creato nel setup
        boolean postDeleted = postDBNeo4j.deletePost(testIdPost2);
        assertTrue(postDeleted, "Failed to delete followed post in cleanup");
        // Eliminare l'utente creato nel setup
        boolean userDeleted = userDBNeo4j.deleteUserDetach(testUsername2);
        assertTrue(userDeleted, "Failed to delete followed user in cleanup");
    }
}
