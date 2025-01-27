package it.unipi.dii.lsmsdb.boardgamecafe.repository.neo4jdbms;

import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.BoardgameModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.PostModelNeo4j;
import it.unipi.dii.lsmsdb.boardgamecafe.mvc.model.neo4j.UserModelNeo4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestInfo;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
// To avoid static methods in @BeforeAll uncomment the line below
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BoardgamesDBNeo4jTest {

    @Autowired
    BoardgameDBNeo4j boardgameDBNeo4j;
    @Autowired
    PostDBNeo4j postDBNeo4j;
    @Autowired
    UserDBNeo4j userDBNeo4j;

    static BoardgameModelNeo4j testBoardgame;
    static PostModelNeo4j testPost;
    static UserModelNeo4j genericUser, testAuthorPost;
    static final String testIdBoardgame = "testIdBoardgame";
    static final String testBoardgameName = "testBoardgameName";
    static final String testBoardgameImage = "urlImageTest";
    static final String testBoardgameDescription = "descriptionTest";
    static final int testBoardgameYearPublished = 2024;
    static final String testIdUser1 = "user1_id";
    static final String testUserName1 = "usernameTest1";
    static final String testIdUser2 = "user2_id";
    static final String testUserName2 = "usernameTest2";
    static final String testIdPost = "post_id";


    @BeforeAll
    public static void setup() {
        testBoardgame = new BoardgameModelNeo4j(testIdBoardgame, testBoardgameName,
                                                testBoardgameImage, testBoardgameDescription, testBoardgameYearPublished);

        genericUser = new UserModelNeo4j(testIdUser1, testUserName1);
        testAuthorPost = new UserModelNeo4j(testIdUser2, testUserName2);
        testPost = new PostModelNeo4j(testIdPost);
        testPost.setAuthor(testAuthorPost);
        testPost.setTaggedGame(testBoardgame);
    }

    @AfterEach
    public void conditionalCleanup(TestInfo testInfo) {
        if (testInfo.getTags().contains("specialCleanup")) {
            postDBNeo4j.deletePost(testIdPost);
            userDBNeo4j.deleteUserDetach(testUserName1);
            userDBNeo4j.deleteUserDetach(testUserName2);
        }
    }

    @Test
    @Order(10)
    public void GIVEN_a_boardgame_WHEN_adding_to_Neo4j_THEN_boardgame_is_added_successfully() {
        var shouldNotBeNull = boardgameDBNeo4j.addBoardgame(testBoardgame);
        assertNotNull(shouldNotBeNull);
    }

    @Test
    @Order(20)
    public void GIVEN_a_boardgame_id_WHEN_finding_by_id_THEN_boardgame_is_found() {
        var shouldBeNotEmpty = boardgameDBNeo4j.findById(testIdBoardgame);
        assertTrue(shouldBeNotEmpty.isPresent());
    }

    @Test
    @Order(30)
    public void GIVEN_a_boardgame_name_WHEN_finding_by_name_THEN_boardgame_is_found() {
        var shouldBeNotEmpty = boardgameDBNeo4j.findByBoardgameName(testBoardgameName);
        assertTrue(shouldBeNotEmpty.isPresent());
    }

    @Test
    @Order(40)
    public void GIVEN_a_boardgame_WHEN_updating_name_THEN_boardgame_is_updated_successfully() {
        String newName = "NewName";
        String oldBoardgameName = testBoardgame.getBoardgameName();
        testBoardgame.setBoardgameName(newName);
        boardgameDBNeo4j.updateBoardgameNeo4j(oldBoardgameName, testBoardgame);

        var shouldHaveUpdatedBoardgameName = boardgameDBNeo4j.findByBoardgameName("NewName").get();
        assertEquals(newName, shouldHaveUpdatedBoardgameName.boardgameName);
    }

    @Test
    @Order(60)
    public void GIVEN_boardgames_WHEN_finding_recent_THEN_recent_boardgames_returned_correctly() {
        List<BoardgameModelNeo4j> recentBoardgames = boardgameDBNeo4j.findRecentBoardgames(10, 0);

        assertNotNull(recentBoardgames, "The list of recent boardgames should not be null.");
        assertFalse(recentBoardgames.isEmpty());
        assertTrue(recentBoardgames.size() <= 10, "The size of the returned list should not exceed the limit.");
    }

    @Test
    @Tag("specialCleanup")
    @Order(80)
    public void GIVEN_username_WHEN_finding_boardgames_posted_by_followed_users_THEN_boardgames_are_returned() {

        userDBNeo4j.addUser(genericUser);
        userDBNeo4j.addUser(testAuthorPost);
        userDBNeo4j.followUser(testUserName1, testUserName2);
        postDBNeo4j.addPost(testPost);
        List<BoardgameModelNeo4j> boardgames = boardgameDBNeo4j.
                getBoardgamesWithPostsByFollowedUsers(testUserName1, 10, 0);

        assertNotNull(boardgames, "La lista restituita non deve essere null");
        assertFalse(boardgames.isEmpty());
        assertTrue(boardgames.size() <= 10, "Il numero di boardgame restituiti non deve superare il limite");
    }

    @Test
    @Order(90)
    public void GIVEN_a_boardgame_name_WHEN_deleting_THEN_boardgame_is_deleted_successfully() {
        var shouldBeTrue = boardgameDBNeo4j.deleteBoardgameDetach(testBoardgameName);
        assertTrue(shouldBeTrue);
    }
}
